package com.ats.blogapp.controller;

import com.ats.blogapp.access.entity.Role;
import com.ats.blogapp.access.entity.User;
import com.ats.blogapp.service.interfaces.RoleService;
import com.ats.blogapp.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

// Controller Layer
@Controller
public class AuthController {

    private final UserService userService;

    private final RoleService roleService;

    // Need's to encrypt the password to store it in db.
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserService userService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    // Show registration form .. to view the register page
    // In .. register page.
    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle registration form submission .. to create a new user with 'ROLE_USER' role.
    // In .. register page.
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") @Validated User user,
                               BindingResult result,
                               Model model){
        // Check for validation errors
        if(result.hasErrors()){
            // If error's occur redirect us to the same page.
            return "register";
        }

        // We must check the username & email to be unique.
        // Check if username already exists .. check if the username exist in the db before save it.
        Optional<User> existingUser = userService.findByUsername(user.getUsername());
        if(existingUser.isPresent()){
            model.addAttribute("usernameError", "Username is already taken.");
            return "register";
        }

        // Check if email already exists .. check if the email exist in the db before save it.
        existingUser = userService.findByEmail(user.getEmail());
        if(existingUser.isPresent()){
            model.addAttribute("emailError", "Email is already registered.");
            return "register";
        }

        // Encode the password .. encrypt the password to store it in db.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign ROLE_USER to the user .. Assign a ROLE_USER which is the default.
        // Further work: the admin created in the admin dashboard .... In admin/**.
        Role userRole = roleService.findRoleByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found."));
        user.setRole(userRole);

        // Save the user .. finally create a new user.
        userService.saveUser(user);

        // Redirect to login page with success message
        model.addAttribute("registerSuccess", "Registration successful! Please login.");
        return "login";
    }

    // Show login form .. to handle the login after registration.
    // In .. register page.
    @GetMapping("/login")
    public String showLoginForm(){
        return "login";
    }

    // Show access denied page .. if it's try to access admin pages ... /admin/**
    // In .. access-denied page.
    @GetMapping("/access-denied")
    public String showAccessDenied(){
        return "access-denied";
    }
}

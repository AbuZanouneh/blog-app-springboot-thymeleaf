package com.ats.blogapp.config;

import com.ats.blogapp.access.entity.Role;
import com.ats.blogapp.access.entity.User;
import com.ats.blogapp.service.interfaces.RoleService;
import com.ats.blogapp.service.interfaces.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// DataInitializer
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RoleService roleService, UserService userService, PasswordEncoder passwordEncoder) {
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if not present
        if(roleService.findRoleByName("ROLE_ADMIN").isEmpty()){
            Role adminRole = new Role("ROLE_ADMIN");
            roleService.saveRole(adminRole);
            System.out.println("ROLE_ADMIN created.");
        }

        if(roleService.findRoleByName("ROLE_USER").isEmpty()){
            Role userRole = new Role("ROLE_USER");
            roleService.saveRole(userRole);
            System.out.println("ROLE_USER created.");
        }

        // Create admin user if not present
        Optional<User> adminUserOpt = userService.findByUsername("admin");
        if(adminUserOpt.isEmpty()){
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPassword(passwordEncoder.encode("adminpassword")); //
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEnabled(true);

            // Assign ROLE_ADMIN
            Role adminRole = roleService.findRoleByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found."));
            adminUser.setRole(adminRole);

            userService.saveUser(adminUser);
            System.out.println("Admin user created with username 'admin' and password 'adminpassword'.");
        }
    }
}

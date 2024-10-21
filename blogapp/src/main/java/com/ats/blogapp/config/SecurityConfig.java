package com.ats.blogapp.config;

import com.ats.blogapp.service.impl.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Set;

// Configuration Layer
// it's ordinary class you can imported then modify it.
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(CustomUserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService;
    }

    // Bean for password encoding .. it's ordinary method you can imported then modify it.
    // You must annotated it as a '@Bean' and the same thing for all other methods.
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Bean for AuthenticationManager
    // AuthenticationManager is needed ... it's ordinary method you can imported then modify it.
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, CustomUserDetailsService userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    // SecurityFilterChain bean to define security configurations .. it's ordinary method you can imported then modify it.
    // You must annotated it as a '@Bean' and the same thing for all other methods.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for simplicity (Not recommended for production) ... also used in thymeleaf you must import thymeleaf security dependency.
                .csrf(csrf -> csrf.disable())

                // Configure authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Permit access to static resources and public URLs .. Add here the URL's that both user's () can access - not required authentication.
                        .requestMatchers("/",
                                         "/home",
                                         "/register",
                                         "/css/**",
                                         "/js/**",
                                         "/images/**",
                                         "categories/**",
                                         "tags/**",
                                         "/uploads/**",
                                          "posts/**",
                                          "/about",
                                          "/contact").permitAll()

                        // Restrict access to admin URLs .. This '/admin/**' URL's for admin.
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Restrict access to user URLs .. Add here User pages .. Further work dont allow the admin to access them from here - only manage them from admin panel.
                        .requestMatchers("/user/**", "/post/**", "/comment/**").hasRole("USER")

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )

                // Configure form login ... it's ordinary class you can imported then modify it.
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page URL
                        //.defaultSuccessUrl("/", true) // Remove or comment out this line
                        .successHandler((request, response, authentication) -> {
                            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

                            if (roles.contains("ROLE_ADMIN")) {
                                response.sendRedirect("/admin/dashboard");
                            } else if (roles.contains("ROLE_USER")) {
                                response.sendRedirect("/user/dashboard");
                            } else {
                                response.sendRedirect("/");
                            }
                        })
                        .permitAll()
                )

                // Configure logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout") // Redirect after logout
                        .permitAll()
                )

                // Handle access denied exceptions
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }
}

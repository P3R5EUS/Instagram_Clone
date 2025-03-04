package org.example.controller;

import org.example.models.User;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Password encoding for security

    // ✅ Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        logger.info("Login attempt for user: " + username);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (BadCredentialsException e) {
            logger.warning("Invalid login attempt for user: " + username);
            return ResponseEntity.badRequest().body("Invalid username or password!");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails.getUsername());

        logger.info("Login successful for user: " + username);
        return ResponseEntity.ok(token);
    }

    // ✅ Register Endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {

        // Step 1: Initialize all user details as null
        Long id = null;
        String username = null;
        String password = null;
        String email = null;
        Set<String> roles = null;

        // Step 2: Import details from the received User object
        if (user != null) {
            id = user.getId();
            username = user.getUsername();
            password = user.getPassword();
            email = user.getEmail();
            roles = user.getRoles();
        }

        logger.info("Received registration request for username: " + username);

        if (username == null || password == null || email == null) {
            logger.warning("Missing fields in registration request.");
            return ResponseEntity.badRequest().body("Missing required fields: username, email, or password.");
        }

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            logger.warning("Registration failed: Username already exists -> " + username);
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        // Step 3: Assign default values if necessary
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add("USER"); // Default role
        }

        // Step 4: Hash the password before saving
        String encodedPassword = passwordEncoder.encode(password);

        // Step 5: Create a new user instance with imported values
        User newUser = new User();
        newUser.setId(id); // Can be null, as @GeneratedValue will auto-assign
        newUser.setUsername(username);
        newUser.setPassword(encodedPassword);
        newUser.setEmail(email);
        newUser.setRoles(roles);

        // Save the user
        userService.saveUser(newUser);
        logger.info("User registered successfully: " + username);

        return ResponseEntity.ok("User registered successfully!");
    }
}

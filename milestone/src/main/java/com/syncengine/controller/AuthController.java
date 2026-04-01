package com.syncengine.controller;

import com.syncengine.model.User;
import com.syncengine.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email    = body.get("email");

        if (username == null || password == null || email == null)
            return ResponseEntity.badRequest().body(Map.of("error", "All fields are required"));

        if (userRepository.existsByUsername(username))
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));

        if (userRepository.existsByEmail(email))
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // plain text — no security dep needed
        user.setEmail(email);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Account created successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password))
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));

        session.setAttribute("user", username);
        return ResponseEntity.ok(Map.of("message", "Login successful", "username", username));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(HttpSession session) {
        String user = (String) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        return ResponseEntity.ok(Map.of("username", user));
    }
}

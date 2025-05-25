package com.securemsg.controller;

import com.securemsg.model.User;
import com.securemsg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Base64;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal User currentUser, Model model) {
        // Reload current user from DB to ensure fresh data
        User user = userRepository.findById(currentUser.getIin()).orElse(currentUser);
        model.addAttribute("currentUser", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String gender,
                                @RequestParam(required = false) String birthDate,
                                @RequestParam(required = false) String newPassword,
                                @RequestParam(required = false) String confirmPassword,
                                @RequestParam(required = false) String theme,
                                @RequestParam(required = false) String language,
                                @RequestParam("photoFile") MultipartFile photoFile,
                                Model model) {
        // Fetch latest user from DB
        User user = userRepository.findById(currentUser.getIin()).orElseThrow();
        String originalEmail = user.getEmail();
        // Email change: if changed, ensure no conflict
        if (!email.equalsIgnoreCase(originalEmail)) {
            if (userRepository.findByEmail(email).isPresent()) {
                model.addAttribute("error", "emailTaken");
                model.addAttribute("currentUser", user);
                return "profile";
            }
            user.setEmail(email);
        }
        // Update basic fields
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setGender(gender);
        if (birthDate != null && !birthDate.isEmpty()) {
            try {
                user.setBirthDate(LocalDate.parse(birthDate));
            } catch (Exception e) {
                // If parsing fails, ignore or handle accordingly (could add error message)
            }
        }
        if (theme != null && !theme.isEmpty()) {
            user.setTheme(theme);
        }
        if (language != null && !language.isEmpty()) {
            user.setLanguage(language);
        }
        // Handle password change if provided
        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "passwordsNoMatch");
                model.addAttribute("currentUser", user);
                return "profile";
            }
            if (newPassword.length() < 6) {
                model.addAttribute("error", "passwordTooShort");
                model.addAttribute("currentUser", user);
                return "profile";
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        // Handle profile photo upload if provided
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                String contentType = photoFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    model.addAttribute("error", "photoInvalid");
                    model.addAttribute("currentUser", user);
                    return "profile";
                }
                // Convert image file to Base64 and store as data URI
                byte[] bytes = photoFile.getBytes();
                String base64 = Base64.getEncoder().encodeToString(bytes);
                String dataUri = "data:" + contentType + ";base64," + base64;
                user.setPhoto(dataUri);
            } catch (Exception e) {
                model.addAttribute("error", "photoFailed");
                model.addAttribute("currentUser", user);
                return "profile";
            }
        }
        // Save updates
        userRepository.save(user);
        return "redirect:/profile?success";
    }
}

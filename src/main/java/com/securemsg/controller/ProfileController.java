package com.securemsg.controller;

import com.securemsg.model.User;
import com.securemsg.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

// Контроллер профиля пользователя
@Controller
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // Страница профиля
    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal User currentUser, org.springframework.ui.Model model) {
        model.addAttribute("user", currentUser);
        return "profile";
    }

    // Обновление данных профиля (имя, email, язык, тема, фото)
    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal User currentUser,
                                @RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String language,
                                @RequestParam String theme,
                                @RequestParam String photoUrl) {
        boolean ok = userService.updateProfile(currentUser, name, email, language, theme, photoUrl);
        if (!ok) {
            return "redirect:/profile?error=exists";
        }
        return "redirect:/profile?success=info";
    }

    // Смена пароля
    @PostMapping("/profile/password")
    public String changePassword(@AuthenticationPrincipal User currentUser,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmNewPassword) {
        if (!newPassword.equals(confirmNewPassword)) {
            return "redirect:/profile?error=nomatch";
        }
        if (!userService.changePassword(currentUser, currentPassword, newPassword)) {
            return "redirect:/profile?error=old";
        }
        return "redirect:/profile?success=pass";
    }
}

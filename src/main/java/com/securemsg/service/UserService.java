package com.securemsg.service;

import com.securemsg.model.User;
import com.securemsg.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Регистрация нового пользователя с ролью USER
    public Optional<User> register(String name, String email, String rawPassword) {
        // Проверяем, не занят ли email
        if (userRepository.findByEmail(email).isPresent()) {
            return Optional.empty(); // email уже используется
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        // Шифруем пароль перед сохранением
        user.setPassword(passwordEncoder.encode(rawPassword));
        // Роль по умолчанию - USER
        user.setRoles(List.of("USER"));
        // Настройки по умолчанию
        user.setLanguage("ru");
        user.setTheme("light");
        user.setPhotoUrl("");
        // Сохраняем пользователя
        userRepository.save(user);
        return Optional.of(user);
    }

    // Обновление профиля (имя, email, язык, тема, фото)
    public boolean updateProfile(User user, String newName, String newEmail, String newLanguage, String newTheme, String newPhotoUrl) {
        // Если email изменился, проверяем уникальность
        if (!user.getEmail().equals(newEmail) && userRepository.findByEmail(newEmail).isPresent()) {
            return false; // не удалось - email занят
        }
        user.setName(newName);
        user.setEmail(newEmail);
        user.setLanguage(newLanguage);
        user.setTheme(newTheme);
        user.setPhotoUrl(newPhotoUrl);
        userRepository.save(user);
        return true;
    }

    // Смена пароля
    public boolean changePassword(User user, String currentRawPassword, String newRawPassword) {
        // Проверяем текущий пароль
        if (!passwordEncoder.matches(currentRawPassword, user.getPassword())) {
            return false; // текущий пароль неверный
        }
        // Сохраняем новый пароль
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
        return true;
    }

    // Получить список всех пользователей (отсортированных по имени)
    public List<User> findAllUsers() {
        List<User> users = userRepository.findAll();
        users.sort(Comparator.comparing(User::getName, String.CASE_INSENSITIVE_ORDER));
        return users;
    }

    // Получить имя пользователя по ID
    public String getNameById(String userId) {
        return userRepository.findById(userId)
                .map(User::getName)
                .orElse("Unknown");
    }

    // Найти пользователя по email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

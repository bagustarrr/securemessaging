package com.securemsg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import java.util.List;
import com.securemsg.repository.UserRepository;
import com.securemsg.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;

// Главный класс приложения Secure Messaging
@SpringBootApplication
public class SecureMessagingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureMessagingApplication.class, args);
    }

    // Создание учетной записи администратора по умолчанию (если отсутствует)
    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User admin = new User();
                admin.setName("Administrator");
                admin.setEmail("admin@example.com");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setRoles(List.of("ADMIN", "TEACHER"));
                admin.setLanguage("en");
                admin.setTheme("light");
                admin.setPhotoUrl("");
                userRepository.save(admin);
                System.out.println("Default admin account created: admin@example.com / admin");
            }
        };
    }
}

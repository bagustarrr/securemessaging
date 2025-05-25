package com.securemsg.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        System.out.println("❌ Ошибка входа: " + exception.getMessage());

        // Можно логировать тип исключения:
        if (exception instanceof org.springframework.security.authentication.BadCredentialsException) {
            System.out.println("⛔ Неверный пароль");
        } else if (exception instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
            System.out.println("⚠️ Пользователь не найден");
        }

        response.sendRedirect("/login?error=" + exception.getClass().getSimpleName());
    }
}

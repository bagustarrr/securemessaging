package com.securemsg.config;

import com.securemsg.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Use our UserRepository to fetch user data for authentication
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> userRepo.findById(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for WebSocket endpoint (since SockJS handshake is not CSRF-protected)
                .csrf(csrf -> csrf.ignoringRequestMatchers("/ws/**"))
                .authorizeHttpRequests(auth -> auth
                        // Allow static resources and public pages without authentication
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/login", "/register", "/error").permitAll()
                        // Admin functions security
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/admin/delete-group/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasAnyRole("ADMIN", "TEACHER")
                        // All other requests need to be logged in
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")              // custom login page
                        .usernameParameter("iin")         // form field for username (IIN)
                        .passwordParameter("password")    // form field for password
                        .defaultSuccessUrl("/chat", true) // go to main chat page on success
                        .permitAll()                      // allow everyone to see login
                )
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/login?logout") // redirect on logout
                );
        return http.build();
    }
}

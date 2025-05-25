package com.securemsg.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Data
@Document("users")
@ToString(exclude = {"password", "photo"})
public class User implements UserDetails {

    @Id
    @NotBlank
    @Size(min = 12, max = 12, message = "{validation.iin.size}")  // IIN must be exactly 12 digits
    private String iin;

    @Indexed(unique = true)
    @NotBlank
    @Email(message = "{validation.email.format}")
    private String email;

    @NotBlank
    @Size(min = 6, message = "{validation.password.size}")
    private String password;

    @NotBlank
    private String fullName;

    // Optional fields
    private String phone;
    private String gender;
    private LocalDate birthDate;

    @NotBlank
    private String role;   // e.g., "STUDENT", "TEACHER", or "ADMIN"
    private String photo;  // stored as Base64 Data URI (data:image/png;base64,....)
    private String theme;  // "light" or "dark"
    private String language; // "en", "ru", or "kz"

    // UserDetails interface methods:
    @Override
    public String getUsername() {
        return this.iin;  // use IIN as the username
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Map role to Security authority (e.g., "TEACHER" -> "ROLE_TEACHER")
        String roleName = (this.role != null ? this.role.toUpperCase() : "STUDENT");
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    // The following flags can all be true (no special account lock/expiration logic)
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

package com.securemsg.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        // Use a cookie to store locale (so it persists between sessions)
        CookieLocaleResolver cookieLocaleResolver = new CookieLocaleResolver();
        cookieLocaleResolver.setDefaultLocale(new Locale("en"));  // default to English
        cookieLocaleResolver.setCookieName("lang");               // cookie name for locale
        return cookieLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        // Interceptor that allows changing the locale by the 'lang' parameter
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");  // e.g., ?lang=en or ?lang=ru or ?lang=kz
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the locale change interceptor
        registry.addInterceptor(localeChangeInterceptor());
    }
}

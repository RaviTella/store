package com.ratella.store.Session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
//@EnableSpringWebSession
public class CookieConfig {
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("JSESSIONID"); // <1>
        resolver.addCookieInitializer((builder) -> builder.path("/")); // <2>
        resolver.addCookieInitializer((builder) -> builder.sameSite("Strict")); // <3>
        return resolver;
    }
}

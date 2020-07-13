package com.ratella.store.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;

import java.util.concurrent.ConcurrentHashMap;


@EnableWebFluxSecurity
public class WebSecurityConfig {


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http

                .csrf()
                .disable()
                .authorizeExchange()
                .pathMatchers("/ebooks/login")
                .permitAll()
                .pathMatchers("/css/**", "/fonts/**", "/js/**", "/plugins/**")
                .permitAll()
                .anyExchange()
                .authenticated()
                .and()
                .formLogin()
                .loginPage("/ebooks/login");
        return http.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user1 = User
                .withUsername("customer1")
                .password(passwordEncoder().encode("customer1"))
                .roles("customer")
                .build();

        UserDetails user2= User
                .withUsername("customer2")
                .password(passwordEncoder().encode("customer2"))
                .roles("customer")
                .build();
        return new MapReactiveUserDetailsService(user1,user2);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

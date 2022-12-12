// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.example.graphwebhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static com.azure.spring.cloud.autoconfigure.aad.AadWebApplicationHttpSecurityConfigurer.aadWebApplication;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.protect.authenticated}")
    private String[] protectedRoutes;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.apply(aadWebApplication())
                .and()
            .csrf()
                .ignoringRequestMatchers("/listen")
                .and()
            .authorizeHttpRequests()
                //.requestMatchers("/", "/login", "/listen").permitAll()
                //.anyRequest().authenticated();
                .requestMatchers(protectedRoutes).authenticated()
                .requestMatchers("/**").permitAll();

        return http.build();
    }
    /*


    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Configure security from AADWebSecurityConfigurerAdapter
        super.configure(http);
        // Add protected routes
        http.csrf().ignoringAntMatchers("/listen").and().authorizeRequests()
                // These routes require an authenticated user
                .antMatchers(protectedRoutes).authenticated().antMatchers("/**").permitAll();
    }
    */
}

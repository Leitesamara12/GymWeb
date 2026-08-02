package br.com.gymweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * A autorização de negócio é feita pelos controladores e pela sessão HTTP.
 * Nesta versão de desenvolvimento todas as rotas são acessíveis para que o
 * front servido pelo próprio Spring Boot possa consumir a API sem bloqueios.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http ) throws Exception {
        http
                .csrf(csrf -> csrf.disable( ))
                .httpBasic(httpBasic -> httpBasic.disable( ))
                .formLogin(formLogin -> formLogin.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build( );
    }
}

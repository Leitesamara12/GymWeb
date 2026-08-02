package br.com.gymweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class GymwebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymwebApplication.class, args);
    }
}

package org.shub.authservice;

import org.shub.authservice.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class AuthServiceApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(AuthServiceApplication.class, args);
        System.out.println("SecurityConfig bean present: " +
                ctx.containsBean("securityConfig"));
    }

}

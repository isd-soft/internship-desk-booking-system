package com.project.internship_desk_booking_system.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class RsaProperties {

    @Value("${app.security.rsa.private-key}")
    private String privateKey;
}

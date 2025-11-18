package com.project.internship_desk_booking_system.config.ldap;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class LdapProperties {
    @Value("${app.ldap.enabled:false}")
    private boolean ldapEnabled;

    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.base}")
    private String ldapBaseDn;

    @Value("${spring.ldap.username}")
    private String ldapUsername;

    @Value("${spring.ldap.password}")
    private String ldapPassword;

    @Value("${spring.ldap.user.search-base}")
    private String userSearchBase;

    @Value("${spring.ldap.user.search-filter}")
    private String userSearchFilter;


}

package com.project.internship_desk_booking_system.config.ldap;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for LDAP integration.
 * <p>
 * Values are loaded from application properties.
 * </p>
 */
@Configuration
@Getter
public class LdapProperties {
    /**
     * Whether LDAP authentication is enabled.
     */
    @Value("${app.ldap.enabled:false}")
    private boolean ldapEnabled;
    /**
     * LDAP server URL.
     */
    @Value("${spring.ldap.urls}")
    private String ldapUrl;
    /**
     * LDAP base DN.
     */
    @Value("${spring.ldap.base}")
    private String ldapBaseDn;
    /**
     * LDAP bind username.
     */
    @Value("${spring.ldap.username}")
    private String ldapUsername;
    /**
     * LDAP bind password.
     */
    @Value("${spring.ldap.password}")
    private String ldapPassword;
    /**
     * LDAP user search base.
     */
    @Value("${spring.ldap.user.search-base}")
    private String userSearchBase;
    /**
     * LDAP user search filter.
     */
    @Value("${spring.ldap.user.search-filter}")
    private String userSearchFilter;
}

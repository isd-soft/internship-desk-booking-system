package com.project.internship_desk_booking_system.config.ldap;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.core.Authentication;

@Slf4j
@RequiredArgsConstructor
public class SafeLdapAuthenticationProvider implements AuthenticationProvider {

    private final LdapContextSource ldapContextSource;
    private final LdapProperties ldapProperties;
    private final LdapHealthChecker ldapHealthChecker;


    @Override
    public Authentication authenticate(Authentication authentication) {

        if (!ldapProperties.isLdapEnabled()) {
            return null;
        }

        if (!ldapHealthChecker.isAvailable(ldapProperties.getLdapUrl())) {
            log.warn("LDAP unavailable → fallback to DB");
            return null;
        }

        try {
            var factory = new LdapBindAuthenticationManagerFactory(ldapContextSource);
            factory.setUserSearchBase(ldapProperties.getUserSearchBase());
            factory.setUserSearchFilter(ldapProperties.getUserSearchFilter());

            AuthenticationManager ldapManager = factory.createAuthenticationManager();
            Authentication result = ldapManager.authenticate(authentication);

            log.info("LDAP authenticated successfully for {}", authentication.getName());
            return result;

        } catch (Exception ex) {
            log.warn("LDAP error → fallback to DB");
            return null;
        }
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}


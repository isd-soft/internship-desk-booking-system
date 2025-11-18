    package com.project.internship_desk_booking_system.config.ldap;

    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Component;

    import javax.naming.Context;
    import javax.naming.ldap.InitialLdapContext;
    import java.util.Hashtable;

    @Component
    public class LdapHealthChecker {

        public boolean isAvailable(String url) {
            try {
                var env = new Hashtable<String, String>();
                env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                env.put(Context.PROVIDER_URL, url);

                new InitialLdapContext(env, null).close();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }


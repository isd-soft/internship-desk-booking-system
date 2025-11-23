package com.project.internship_desk_booking_system.service;

import com.project.internship_desk_booking_system.entity.CustomUserPrincipal;
import com.project.internship_desk_booking_system.entity.User;
import com.project.internship_desk_booking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Loads a user by username or email for Spring Security authentication.
     *
     * @param usernameOrEmail the username or email to search for
     * @return UserDetails for authentication
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserPrincipal(
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole()
        );
    }

}
package net.strbasic.coursework.service;

import lombok.RequiredArgsConstructor;
import net.strbasic.coursework.model.User;
import net.strbasic.coursework.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities("ROLE_" + userEntity.getRole())
                .build();
    }
}

package net.strbasic.coursework.service;

import lombok.RequiredArgsConstructor;
import net.strbasic.coursework.model.User;
import net.strbasic.coursework.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(String username, String password) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role("USER")
                .build();
        userRepository.save(user);
    }
}

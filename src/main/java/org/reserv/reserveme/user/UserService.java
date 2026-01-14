package org.reserv.reserveme.user;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String email, String displayName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("Email already in use");
        }

        // For backward compatibility create a user with a generated password placeholder
        String hashed = BCrypt.hashpw("changeme", BCrypt.gensalt());
        return userRepository.save(new User(email, hashed, "ROLE_USER", displayName));
    }

    public User registerUser(String email, String rawPassword, String displayName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateEmailException("Email already in use");
        }
        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        return userRepository.save(new User(email, hashed, "ROLE_USER", displayName));
    }

    public User authenticate(String email, String rawPassword) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!BCrypt.checkpw(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return user;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public java.util.Optional<User> findById(java.util.UUID id) {
        return userRepository.findById(id);
    }
}

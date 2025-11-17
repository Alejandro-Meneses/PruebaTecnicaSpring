package com.alejandro.pruebatecnica;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    "(?=.*\\d)" +
                    "(?=.*[@$!%*?&])" +
                    "(?=.{8,})" +
                    "[A-Za-z\\d@$!%*?&]+$";

    private static final Pattern PASSWORD_VALIDATOR = Pattern.compile(PASSWORD_PATTERN);

    public Users createUser(String username, String email, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (usersRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (usersRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email format is invalid");
        }

        if (!isSecurePassword(password)) {
            throw new IllegalArgumentException(
                    "Password must have: " +
                            "- Minimum 8 characters " +
                            "- At least 1 uppercase letter " +
                            "- At least 1 lowercase letter " +
                            "- At least 1 number " +
                            "- At least 1 special character (@$!%*?&)"
            );
        }

        Users newUser = new Users(username, email, encryptPassword(password));
        return usersRepository.save(newUser);
    }

    public List<Users> getAllUsers() {
        return usersRepository.findAll();
    }

    public Users getUserById(Integer id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    public Optional<Users> getUserByUsername(String username) {
        return usersRepository.findByUsername(username);
    }

    public void deleteUser(Integer id) {
        if (!usersRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }
        usersRepository.deleteById(id);
    }

    public boolean authenticateUser(String username, String password) {
        Optional<Users> user = usersRepository.findByUsername(username);
        if (user.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(password, user.get().getPassword());
    }

    private boolean isSecurePassword(String password) {
        return PASSWORD_VALIDATOR.matcher(password).matches();
    }

    private String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailPattern).matcher(email).matches();
    }
}
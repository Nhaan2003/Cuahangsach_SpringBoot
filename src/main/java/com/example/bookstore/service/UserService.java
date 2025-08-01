package com.example.bookstore.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.UserDTO;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(UserDTO userDTO) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Tên người dùng đã tồn tại");
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());
        user.setFullName(userDTO.getFullName());
        user.setPhone(userDTO.getPhone());
        user.setAddress(userDTO.getAddress());
        user.setRole("KH"); // Vai trò mặc định là khách hàng
        user.setStatus("Active");

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Integer userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));

        if (userDTO.getUsername() != null) {
            if (userRepository.findByUsername(userDTO.getUsername()).isPresent() &&
                !userDTO.getUsername().equals(user.getUsername())) {
                throw new IllegalArgumentException("Tên người dùng đã tồn tại");
            }
            user.setUsername(userDTO.getUsername());
        }

        if (userDTO.getEmail() != null) {
            if (userRepository.findByEmail(userDTO.getEmail()).isPresent() &&
                !userDTO.getEmail().equals(user.getEmail())) {
                throw new IllegalArgumentException("Email đã tồn tại");
            }
            user.setEmail(userDTO.getEmail());
        }

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getFullName() != null) {
            user.setFullName(userDTO.getFullName());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getAddress() != null) {
            user.setAddress(userDTO.getAddress());
        }

        if (userDTO.getRole() != null) {
            if (!List.of("KH", "Nvien", "Qly").contains(userDTO.getRole())) {
                throw new IllegalArgumentException("Vai trò không hợp lệ");
            }
            user.setRole(userDTO.getRole());
        }

        if (userDTO.getStatus() != null) {
            if (!List.of("Active", "Lock").contains(userDTO.getStatus())) {
                throw new IllegalArgumentException("Trạng thái không hợp lệ");
            }
            user.setStatus(userDTO.getStatus());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Integer userId, String status) {
        if (!List.of("Active", "Lock").contains(status)) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        user.setStatus(status);
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
    }

    public Long getTotalUserCount() {
        return userRepository.count();
    }
}
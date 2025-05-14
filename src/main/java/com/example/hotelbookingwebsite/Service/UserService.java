package com.example.hotelbookingwebsite.Service;

import com.example.hotelbookingwebsite.DTO.UserDTO;
import com.example.hotelbookingwebsite.Model.Customer;
import com.example.hotelbookingwebsite.Model.Manager;
import com.example.hotelbookingwebsite.Model.User;
import com.example.hotelbookingwebsite.Repository.CustomerRepository;
import com.example.hotelbookingwebsite.Repository.ManagerRepository;
import com.example.hotelbookingwebsite.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber).isPresent();
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public Manager saveManager(Manager manager) {
        return managerRepository.save(manager);
    }

    public User authenticate(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty() ||
                !passwordEncoder.matches(password, userOptional.get().getPassword())) {
            return null;
        }

        return userOptional.get();
    }

    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Page<UserDTO> getAllUsersPaginated(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(this::convertUserToUserDTO);
    }

    public Page<UserDTO> getUsersByRole(String role, Pageable pageable) {
        Page<User> userPage = userRepository.findByRole(role, pageable);
        return userPage.map(this::convertUserToUserDTO);
    }

	 public List<UserDTO> updateUsers(List<UserDTO> userDTOList) {
        return userDTOList.stream()
                .map(userDTO -> {
                    Optional<User> userOptional = userRepository.findById(userDTO.getId());

                    if (userOptional.isPresent()) {
                        User user = userOptional.get();

                        if (userDTO.getEmail() != null) {
                            user.setEmail(userDTO.getEmail());
                        }
                        if (userDTO.getFullname() != null) {
                            user.setFullname(userDTO.getFullname());
                        }
                        if (userDTO.getPhoneNumber() != null) {
                            user.setPhoneNumber(userDTO.getPhoneNumber());
                        }
                        if (userDTO.getRole() != null) {
                            user.setRole(userDTO.getRole().toUpperCase());
                        }

                        userRepository.save(user);

                        return convertUserToUserDTO(user);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

	public boolean deleteUser(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            customerRepository.deleteByUid(id);
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public User updateUser(User user) {
        Optional<User> existingUserOpt = userRepository.findById(user.getUid());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getFullname() != null) {
                existingUser.setFullname(user.getFullname());
            }
            if (user.getPhoneNumber() != null) {
                existingUser.setPhoneNumber(user.getPhoneNumber());
            }
            return userRepository.save(existingUser);
        }

        throw new UsernameNotFoundException("User not found with id: " + user.getUid());
    }


    private UserDTO convertUserToUserDTO(User user) {
        if (user == null) {
            return null;
        }

        UserDTO userListDTO = new UserDTO();
        userListDTO.setId(user.getUid());
        userListDTO.setEmail(user.getEmail());
        userListDTO.setFullname(user.getFullname());
        userListDTO.setPhoneNumber(user.getPhoneNumber());
        userListDTO.setRole(user.getRole());

        return userListDTO;
    }
}

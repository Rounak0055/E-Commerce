package com.paintandpetals.config;

import com.paintandpetals.entity.Category;
import com.paintandpetals.entity.User;
import com.paintandpetals.entity.enums.Role;
import com.paintandpetals.entity.enums.VendorStatus;
import com.paintandpetals.repository.CategoryRepository;
import com.paintandpetals.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AdminProperties adminProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategories();
    }

    private void seedAdmin() {
        if (userRepository.findByEmail(adminProperties.getEmail()).isEmpty()) {
            User admin = User.builder()
                    .email(adminProperties.getEmail())
                    .password(passwordEncoder.encode(adminProperties.getPassword()))
                    .firstName("Platform")
                    .lastName("Admin")
                    .role(Role.ADMIN)
                    .vendorStatus(VendorStatus.NOT_APPLICABLE)
                    .build();
            userRepository.save(admin);
            log.info("Seeded admin account: {}", adminProperties.getEmail());
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(Category.builder().name("Paints").slug("paints").build());
            categoryRepository.save(Category.builder().name("Petals").slug("petals").build());
            categoryRepository.save(Category.builder().name("Art Supplies").slug("art-supplies").build());
            log.info("Seeded product categories");
        }
    }
}

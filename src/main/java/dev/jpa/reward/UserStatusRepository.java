package dev.jpa.reward;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatusRepository
        extends JpaRepository<UserStatus, Long> {
}

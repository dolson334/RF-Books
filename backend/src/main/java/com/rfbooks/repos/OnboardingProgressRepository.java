package com.rfbooks.repos;

import com.rfbooks.entities.OnboardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgress, Long> {
    Optional<OnboardingProgress> findByUserId(String userId);
}

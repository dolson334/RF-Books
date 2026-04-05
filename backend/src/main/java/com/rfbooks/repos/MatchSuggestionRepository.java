package com.rfbooks.repos;

import com.rfbooks.entities.MatchSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchSuggestionRepository extends JpaRepository<MatchSuggestion, Long> {

    List<MatchSuggestion> findByUserIdAndStatus(String userId, String status);

    List<MatchSuggestion> findByUserIdAndStatusOrderByConfidenceScoreDesc(String userId, String status);

    void deleteByUserId(String userId);
}

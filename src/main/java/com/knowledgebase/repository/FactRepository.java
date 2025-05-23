package com.knowledgebase.repository;

import com.knowledgebase.model.Fact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactRepository extends JpaRepository<Fact, Long> {
    
    List<Fact> findBySubjectContainingIgnoreCase(String subject);
    
    List<Fact> findByPredicateContainingIgnoreCase(String predicate);
    
    @Query("SELECT f FROM Fact f WHERE " +
           "LOWER(f.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.predicate) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(f.object) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Fact> searchByKeyword(@Param("keyword") String keyword);
    
    Optional<Fact> findByContentIgnoreCase(String content);
}
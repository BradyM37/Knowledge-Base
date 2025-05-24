package com.knowledgebase.repository;

import com.knowledgebase.model.Fact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactRepository extends JpaRepository<Fact, Long> {
    
    List<Fact> findByContentContainingIgnoreCase(String query);
    
    Optional<Fact> findByContentIgnoreCase(String content);
    
    List<Fact> findBySubjectContainingIgnoreCase(String subject);
}
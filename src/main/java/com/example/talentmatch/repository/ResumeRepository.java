
package com.example.talentmatch.repository;

import com.example.talentmatch.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findBySkillsContaining(String skill);
    List<Resume> findByUserUsername(String username);
}

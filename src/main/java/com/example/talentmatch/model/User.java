package com.example.talentmatch.model;
import com.fasterxml.jackson.annotation.JsonIgnore;


import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Resume> resumes;

    @Column(unique = true)
    private String username;

    private String password;

    private String role; // ROLE_USER, ROLE_ADMIN
    
}
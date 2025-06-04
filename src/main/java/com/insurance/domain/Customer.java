package com.insurance.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Customer extends BaseEntity {
    @NotBlank
    private String name;
    
    @NotBlank
    private String cpf;
    
    @Email
    private String email;
    
    private LocalDate birthDate;
    
    private String address;
    
    private String phone;
    
    @OneToMany(mappedBy = "customer")
    private Set<InsurancePolicy> policies = new HashSet<>();
} 
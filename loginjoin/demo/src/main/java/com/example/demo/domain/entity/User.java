package com.example.demo.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="user")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;
    @Email(message = "example@example.com 형식으로 작성해주세요.")
    @Column(unique = true ,nullable = false)
    private String email;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = false)
    private String passWord;
    @Column(unique = true,nullable = false)
    private String phoneNumber;
    private String nickName;
    private String profileImageUrl;
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private String role;
}


// @CreationTimestamp : 엔티티 저장 시 한 번만 자동 설정
// @UpdateTimestamp : 수정될 때 마다 자동 업데이트
// lastLoginAt =  비즈니스 로직에서 구현해야함

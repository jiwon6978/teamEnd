package com.example.demo.domain.repository;

import com.example.demo.domain.entity.JwtToken;
import com.example.demo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken,Long> {
    Optional<JwtToken> deleteByAccessToken(String AccessToken);
}

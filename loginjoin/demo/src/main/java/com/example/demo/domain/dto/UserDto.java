package com.example.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private String userName;
    private String passWord;
    private String role;
    private String phoneNumber;
    private String email;


    public UserDto(String username,String password,String role,String phoneNumber,String email){
        this.userName = username;
        this.passWord = password;
        this.role = role;
        this.phoneNumber=phoneNumber;
        this.email=email;
    }

    //OAuth2 Client Info
    private String provider;
    private String providerId;
}

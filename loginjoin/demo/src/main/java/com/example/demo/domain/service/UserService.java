package com.example.demo.domain.service;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.dto.JoinDto;
import com.example.demo.domain.dto.ProfileUpdateDto;
import com.example.demo.domain.dto.QuitDto;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RedisUtil redisUtil;


    //회원가입
    @Transactional
    public Long joinRegistration(JoinDto dto) throws Exception {
        String encodedPassword = encoder.encode(dto.getPassWord());
        User user = User.builder()
                .id(null)
                .email(dto.getEmail())
                .userName(dto.getUserName())
                .passWord(encodedPassword)
                .phoneNumber(dto.getPhoneNumber())
                .role("ROLE_USER")
                .build();
        userRepository.save(user);
        return user.getId();
    }
    //프로필사진 업데이트
    @Transactional
    public void updateProfile(String email, ProfileUpdateDto dto) throws IOException {
        var UpdateImg = userRepository.findByEmail(email).orElseThrow();
    }



    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }



    //회원탈퇴
    @Transactional
    public void quitAccount(String email,QuitDto quitDto){
        System.out.println(">>> quitAccount() 진입 성공");
        String encodedPassword = encoder.encode(quitDto.getPassWord());

        //form에서 받은 정보
        String password = quitDto.getPassWord();
        boolean isAgreed = quitDto.isAgreed();
        System.out.println("탈퇴 확인 패스워드 : "+password);
        System.out.println("탈퇴 확인 동의 : "+isAgreed);


        //엔티티에서 사용자 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        System.out.println("사용자 email : "+ user.getEmail());
        log.info("DB 비밀번호: {}", user.getPassWord());
        log.info("matches 결과: {}", encoder.matches(password, user.getPassWord()));

        //탈퇴 확인
        if(!quitDto.isAgreed()) throw new IllegalArgumentException("항목에 동의해야 회원탈퇴가 됩니다.");

        if(!encoder.matches(password,user.getPassWord()))
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        //토큰삭제
        redisUtil.delete("RT : "+user.getEmail());
        System.out.println("DEBUG: User Delete Attempt for 레디스 토큰삭제 확인 : " + user.getEmail());

        //DB삭제
        userRepository.deleteByEmail(email);
        System.out.println("DB삭제 완료");






    }

}

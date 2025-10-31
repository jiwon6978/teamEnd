package com.example.demo.controller;

import com.example.demo.config.auth.PrincipalDetails;
import com.example.demo.domain.dto.QuitDto;
import com.example.demo.domain.dto.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import com.example.demo.domain.service.UserService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
@Slf4j
public class UserController {

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public void login(@AuthenticationPrincipal PrincipalDetails principalDetails) throws IOException {
        log.info("GET /login..." + principalDetails);

        if(principalDetails!=null)
            response.sendRedirect("/user");

    }

    //확인방법 - 1 Authentication Bean 주입
//    @GetMapping("/user")
//    public void user(Authentication authentication, Model model){
//        log.info("GET /user.." + authentication);
//        log.info("name..." + authentication.getName());
//		log.info("principal..." + authentication.getPrincipal());
//		log.info("authorities..." + authentication.getAuthorities());
//		log.info("details..." + authentication.getDetails());
//		log.info("credential..." + authentication.getCredentials());
//
//        model.addAttribute("auth_1",authentication);
//    }
    //확인방법 - 2
    @GetMapping("/user")
    public void user(Model model){

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        log.info("GET /user.." + authentication);
        log.info("name..." + authentication.getName());
        log.info("principal..." + authentication.getPrincipal());
        log.info("authorities..." + authentication.getAuthorities());
        log.info("details..." + authentication.getDetails());
        log.info("credential..." + authentication.getCredentials());

        model.addAttribute("auth_1",authentication);
    }

    //확인방법 - 3 Authentication's Principal 만 꺼내와 연결
    @GetMapping("/manager")
    public void manager(@AuthenticationPrincipal PrincipalDetails principalDetails){
        log.info("GET /manager.."+principalDetails);
    }
    @GetMapping("/admin")
    public void admin(){
        log.info("GET /admin..");
    }

    @GetMapping("/join")
    public void join(){
        log.info("GET /join..");
    }

    @PostMapping("/join")
    public String join_post(UserDto dto){
        log.info("POST /join.."+dto);
        String pwd =  passwordEncoder.encode(dto.getPassWord());//암호화
        //dto -> entity
        User user = new User();
        user.setUserName(dto.getUserName());
        user.setPassWord(pwd);
        user.setRole("ROLE_USER");
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());
        userRepository.save(user);
        boolean isJoin = true;
        if(isJoin){
            return "redirect:/login";
        }
        return "join";
    }

    @GetMapping("/quit")
    public String myAccount(){
        log.info("GET /quit page ");
        return "quit";
    }
    @PostMapping("/quit")
    public String quitMember(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @ModelAttribute QuitDto quitDto,
            RedirectAttributes redirectAttributes
           ){


        System.out.println("### QuitDto isAgreed 값: " + quitDto.isAgreed());
        //로그인 여부 확인
        System.out.println("### 탈퇴 서비스 로직 호출 직전");
        if (principalDetails == null)
            return "redirect:/login";

        try{
            //현재 사용자 추출
            String email = principalDetails.getUsername();
            System.out.println("현재사용자 : "+email);

            //Service계층으로 넘기기
            userService.quitAccount(email,quitDto);

            //성공 응답
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.LOCATION, "/");

            return "redirect:/main";

        }catch (IllegalArgumentException e){
            //비밀번호 불일치,동의 미체크 예외 처리
            redirectAttributes.addFlashAttribute("serverError", "비밀번호,동의항목을 체크해주세요.");
            return "redirect:/quit";

        }catch (Exception e){
            //기타 서버 오류 처리
            redirectAttributes.addFlashAttribute("serverError", "오류가 발생했습니다. 다시 진행해주세요.");
            return "redirect:/quit";
        }
    }





}
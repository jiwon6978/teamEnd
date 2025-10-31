package com.example.demo.config.auth.jwt;

import com.example.demo.config.auth.redis.RedisUtil;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.JwtTokenRepository;
import com.example.demo.domain.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    JWTTokenProvider jwtTokenProvider;
    @Autowired
    JwtTokenRepository jwtTokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RedisUtil redisUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getMethod().equals("DELETE") && request.getRequestURI().equals("/my-account")) {
            filterChain.doFilter(request, response);
            return;
        }



//--------------------------------------------------------------------------------
        //전

        System.out.println("[JWTAuthorizationFilter] doFilterInternal ...");
        //access-token 쿠키 받기
        String token = null;      //access-token 쿠키 받아 token=null;
        String email=null;
        Cookie[] cookies = request.getCookies();
        if(cookies!=null)
        {
            token = Arrays.stream(cookies)
                    .filter((cookie)->{return cookie.getName().equals(JWTProperties.ACCESS_TOKEN_COOKIE_NAME);})
                    .findFirst()
                    .map((cookie)->{return cookie.getValue();})
                    .orElse(null);

            email = Arrays.stream(cookies)
                    .filter((cookie)->{return cookie.getName().equals("email");})
                    .findFirst()
                    .map((cookie)->{return cookie.getValue();})
                    .orElse(null);
        }


        System.out.println("TOKEN : " + token );
        if(token!=null){
            //access-token -> Authentication 생성이후 SecurityContextHolder에 저장
            //1)access-token 만료되었는지 확인
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    //1-1) access-token==만료 x ? Authentication 생성
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    if(authentication!=null)
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }catch(Exception e){
//                --------------------------------------------
//                2 REFRESH_TOKEN이 REDIS에 저장되어있을 때
//                --------------------------------------------
                //1-2) access-token==만료 o ? refresh-token 만료 여부확인
                System.out.println("ExpiredJwtException ...AccessToken Expired.."  + e.getMessage());
                //2) RefreshToken의 만료유무
                String refreshToken = redisUtil.getRefreshToken("RT:"+email);
                try {
                    if (jwtTokenProvider.validateToken(refreshToken)) {
                        //2-1) RefreshToken!=만료 ? -> AccessToken 재발급 -> 쿠키전달 + DB Token Info 갱신
                        //AccessToken 재발급
                        long now = (new Date()).getTime();  //현재시간
                        User user = userRepository.findByEmail(email).get();
                        String accessToken = Jwts.builder()
                                .setSubject(email) //본문 TITLE
                                .setExpiration(new Date(now + JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME )) //만료날짜(밀리초단위)
                                .signWith(jwtTokenProvider.getKey(), SignatureAlgorithm.HS256) // 서명값
                                .claim("email",email) // 본문 내용
                                .claim("auth",user.getRole()) // 본문 내용 "ROLE_USER,USER_ADMIN"
                                .compact();
                        //쿠키로 전달
                        Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME,accessToken);
                        cookie.setMaxAge(JWTProperties.ACCESS_TOKEN_EXPIRATION_TIME);    //accesstoken 유지시간
                        cookie.setPath("/");    //쿠키 적용경로(/ : 모든경로)
                        response.addCookie(cookie); //응답정보에 쿠키 포함
                    }
                }catch(ExpiredJwtException e2) {
                    //2-2) RefreshToken==만료 ? -> DB's Token Info 삭제
                    System.out.println("ExpiredJwtException ...RefreshToken Expired.." + e2.getMessage());
                    // access-token 제거(자동제거는 됨)
                    Cookie cookie = new Cookie(JWTProperties.ACCESS_TOKEN_COOKIE_NAME, null);
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    //;
                    redisUtil.delete("RT:" + email);

                }catch (Exception e3){

                }
            }

        }else {

            //accesstoken 만료, refreshtoken유효
            if(email!=null)
            {


            }

        }

        filterChain.doFilter(request,response);

        //후

    }
}

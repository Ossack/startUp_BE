package com.sparta.startup_be.login.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.startup_be.login.dto.SocialUserInfoDto;
import com.sparta.startup_be.login.model.User;
import com.sparta.startup_be.login.repository.UserRepository;
import com.sparta.startup_be.security.UserDetailsImpl;
import com.sparta.startup_be.security.jwt.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoUserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public SocialUserInfoDto kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        // 1. "인가 코드"로 "액세스 토큰" 요청
        String accessToken = getAccessToken(code);

        // 2. 토큰으로 카카오 API 호출
        SocialUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

        // 3. 카카오ID로 회원가입 처리
        User kakaoUser = registerKakaoUserIfNeed(kakaoUserInfo);

        // 4. 강제 로그인 처리
        Authentication authentication = forceLogin(kakaoUser);

        // 5. response Header에 JWT 토큰 추가
        kakaoUsersAuthorizationInput(authentication, response);
        return kakaoUserInfo;
    }

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getAccessToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", "64a0648a5d36ca7749a5d4211f64da7e");
        body.add("redirect_uri", "http://localhost:3000/user/kakao/callback");
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출
    private SocialUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        // responseBody에 있는 정보를 꺼냄냄
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
//        object.convert json to object in java objectMapper.readValue
        // readValue("String", TradePrice.class);

        String provider = "kakao";
        Long id = jsonNode.get("id").asLong();
        String email = "";
        try {
            email = jsonNode.get("kakao_account").get("email").asText();
        } catch (NullPointerException e) {
            // 이메일 선택 동의 거부 할 경우 랜덤 이메일 생성
            String email_front = UUID.randomUUID().toString();
            email = email_front + "@kakao.com";
        }
        String nickname = jsonNode.get("properties")
                .get("nickname").asText()  + "_" + provider;

        return new SocialUserInfoDto(id, nickname, email);
    }

    // 3. 카카오ID로 회원가입 처리
    private User registerKakaoUserIfNeed (SocialUserInfoDto kakaoUserInfo) {
        // DB 에 중복된 email이 있는지 확인
        String kakaoEmail = kakaoUserInfo.getEmail();
        String nickname = kakaoUserInfo.getNickname();
        User kakaoUser = userRepository.findByUserEmailAndNickname(kakaoEmail, nickname)
                .orElse(null);

        if (kakaoUser == null) {
            // 회원가입
            Optional<User> nickNameCheck = userRepository.findByNickname(nickname);

            // 닉네임 중복 검사
            if (nickNameCheck.isPresent()) {
                String tempNickName = nickname;
                int i = 1;
                while (true){
                    nickname = tempNickName + "_" + i;
                    Optional<User> nickNameCheck2 = userRepository.findByNickname(nickname);
                    if (!nickNameCheck2.isPresent()) {
                        break;
                    }
                    i++;
                }
            }

            // password: random UUID
            String password = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(password);

            String profile = "https://ossack.s3.ap-northeast-2.amazonaws.com/basicprofile.png";

            kakaoUser = new User(kakaoEmail, nickname, profile, encodedPassword);
            userRepository.save(kakaoUser);

        }
        return kakaoUser;
    }

    // 4. 강제 로그인 처리
    private Authentication forceLogin(User kakaoUser) {
        UserDetails userDetails = new UserDetailsImpl(kakaoUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    // 5. response Header에 JWT 토큰 추가
    private void kakaoUsersAuthorizationInput(Authentication authentication, HttpServletResponse response) {
        // response header에 token 추가
        UserDetailsImpl userDetailsImpl = ((UserDetailsImpl) authentication.getPrincipal());
        String token = JwtTokenUtils.generateJwtToken(userDetailsImpl);
        response.addHeader("Authorization", "BEARER" + " " + token);
    }
}
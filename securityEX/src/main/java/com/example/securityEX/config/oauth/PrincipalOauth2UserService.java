package com.example.securityEX.config.oauth;

import com.example.securityEX.config.auth.PrincipalDetails;
import com.example.securityEX.config.oauth.provider.GoogleUserInfo;
import com.example.securityEX.config.oauth.provider.KakaoUserInfo;
import com.example.securityEX.config.oauth.provider.NaverUserInfo;
import com.example.securityEX.config.oauth.provider.OAuth2UserInfo;
import com.example.securityEX.model.User;
import com.example.securityEX.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    // 후처리되는 함수 => 구글로 부터 받은 userRequest 데이터에 대한 후처리되는 함수
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{

        // 클라이언트의 등록 정보 -> 어떤 OAuth로 로그인 했는지 확인 가능
        System.out.println("Client Registration : " + userRequest.getClientRegistration().getRegistrationId());

        // 엑세스토큰
        System.out.println("Access Token : " + userRequest.getAccessToken().getTokenValue());

        // 사용자 정보
        // 소셜로그인 버튼 클릭 -> 소셜로그인창 -> 로그인 완료 -> code를 리턴(OAuth-Client라이브러리) -> AccessToken요청
        // userRequest 정보 -> loadUser함수 호출 -> 소셜로부터 회원프로필 받아준다.(loadUser함수가 회원 프로필을 받아준다.)
        OAuth2User oauth2User = super.loadUser(userRequest); // 구글로부터 받은 회원정보
        System.out.println(oauth2User.getAttributes());

        // 소셜에서 받은 회원정보를 바탕으로 강제 회원가입 진행 -> User 오브젝트 만들기 -> PrincipalDetails에 넣어줄 것임.
        OAuth2UserInfo oAuth2UserInfo = null;

        if (userRequest.getClientRegistration().getRegistrationId().equals("google")){
            oAuth2UserInfo = new GoogleUserInfo(oauth2User.getAttributes());
        }
        else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            oAuth2UserInfo = new NaverUserInfo((Map)oauth2User.getAttributes().get("response")); // 넘길때 하나 까서 안에있는 response를 넘겨줘야함.
        }
        else if(userRequest.getClientRegistration().getRegistrationId().equals("kakao")){
            oAuth2UserInfo = new KakaoUserInfo((Map)oauth2User.getAttributes().get("properties"));
        }
        else{
            System.out.println("no");
        }

        // userInfo에 존재
        String provider = oAuth2UserInfo.getProvider(); // google
        String providerId = oAuth2UserInfo.getProviderId(); // sub는 구글에서 제공하는 고유 ID
        String email = oAuth2UserInfo.getEmail();

        // userInfo에 안존재
        String username = provider + "_" + providerId; // google_123123123
        String password = new BCryptPasswordEncoder().encode("UOUR"); // OAuth로 로그인하는 경우는 비밀번호가 없기 때문에 임의로 넣어줌.
        String role = "ROLE_USER";

        // 회원가입 진행
        // 이미 회원가입이 되어있는지 확인
        User userEntity = userRepository.findByUsername(username);

        //여기서 가지고온 사용자 정보로 회원가입을 시킨다.
        // username => 구글의 sub(google_{sub}) => PK
        // password => 암호화
        // email => 구글의 email
        // role => ROLE_USER
        // provider => "google"
        // providerId => {sub}

        if (userEntity == null){
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        }
        else{
            System.out.println("이미 회원가입이 되어있습니다.");
        }


        // 모든 타입이 PrincipalDetails 타입이 모인다. => Authentication 객체에 일반, 구글, 네이버, 카카오 다 들어갈 수 있다.
        return new PrincipalDetails(userEntity, oauth2User.getAttributes()); // 이게 Authentication 에 들어간다.

    }
}

package com.example.securityEX.config.oauth.provider;
import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo{

    private Map<String,Object> attributes; // oauth2User.getAttributes()를 받아올것.
                                            // 근데 네이버는 getAttribute 안에 response 안에 id, email, name 이렇게 들어있음.

    public KakaoUserInfo(Map<String,Object> attributes){
        this.attributes = attributes;
    }

    // 이제 여기에서 각각에 맞게 찾아서 넣어주기
    @Override
    public String getProviderId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getEmail() {
        return (String)attributes.get("account_email");
    }

    @Override
    public String getName() {
        return (String)attributes.get("nickname");
    }
}

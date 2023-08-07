package com.example.securityEX.config.oauth.provider;
import java.util.Map;
public class GoogleUserInfo implements OAuth2UserInfo{

    private Map<String,Object> attributes; // oauth2User.getAttributes()를 받아올것.

    public GoogleUserInfo(Map<String,Object> attributes){
        this.attributes = attributes;
    }

    // 이제 여기에서 각각에 맞게 찾아서 넣어주기
    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getEmail() {
        return (String)attributes.get("email");
    }

    @Override
    public String getName() {
        return (String)attributes.get("name");
    }
}

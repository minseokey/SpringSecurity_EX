package com.example.securityEX.config.oauth.provider;

// 받아야 하는것들 정규화.
public interface OAuth2UserInfo {

    // 구글이나 네이버에서 제공하는 ID
    String getProviderId();
    // 구글인지 아님 네이버인지...
    String getProvider();
    String getEmail();
    String getName();

}

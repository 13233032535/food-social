package com.imooc.dinners.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Liu Ming on 2020/11/25.
 */
@Getter
@Setter
public class OauthDinnerInfo {
    private String nickname;
    private String avatarUrl;
    private String accessToken;
    private String expireIn;
    private List<String> scopes;
    private String refreshToken;
}

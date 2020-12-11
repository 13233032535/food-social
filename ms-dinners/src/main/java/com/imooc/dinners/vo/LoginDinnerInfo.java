package com.imooc.dinners.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Liu Ming on 2020/11/25.
 */
@Setter
@Getter
public class LoginDinnerInfo implements Serializable {

    private String nickname;
    private String token;
    private String avatarUrl;

}
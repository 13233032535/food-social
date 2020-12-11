package com.imooc.oauth2.server.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.commons.utils.ResultInfoUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.SignInDinerInfo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Liu Ming on 2020/11/25.
 */
@RestController
public class UserController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private RedisTokenStore redisTokenStore;

    @GetMapping("user/me")
    public ResultInfo getCurrentUserInfo(Authentication authentication) {
        // 获取用户登陆信息，然后设置
        SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();

        // 转换为前端视图对象
        SignInDinerInfo dinerInfo = new SignInDinerInfo();

        BeanUtils.copyProperties(signInIdentity, dinerInfo);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerInfo);
    }

    @GetMapping("user/logout")
    public ResultInfo logout(String access_token, String authorization) {
        // 判断 access_token 是否为空，为空将 authorization 赋值给 access_token
        if(StringUtils.isBlank(access_token)){
            access_token = authorization;
        }

        // 判断 authorization 是否为空
        if(StringUtils.isBlank(access_token)){
            return ResultInfoUtil.buildSuccess(request.getServletPath(),"退出成功...");
        }

        // 判断 bearer token 是否为空
        if(access_token.toLowerCase().contains("bearer ".toLowerCase())){
            access_token = access_token.toLowerCase().replace("bearer ", "");
        }

        OAuth2AccessToken oAuth2AccessToken = redisTokenStore.readAccessToken(access_token);
        if(oAuth2AccessToken != null){
            redisTokenStore.removeAccessToken(oAuth2AccessToken);
            OAuth2RefreshToken refreshToken = oAuth2AccessToken.getRefreshToken();
            redisTokenStore.removeRefreshToken(refreshToken);
        }

        return ResultInfoUtil.buildSuccess(request.getServletPath(), "退出成功");
    }
}

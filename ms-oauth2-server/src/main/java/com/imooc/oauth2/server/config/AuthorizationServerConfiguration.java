package com.imooc.oauth2.server.config;

import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.oauth2.server.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * 授权服务
 * Created by Liu Ming on 2020/11/19.
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {


    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private RedisTokenStore redisTokenStore;

    @Resource
    private ClientOAuth2DataConfiguration clientOAuth2DataConfiguration;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserService userService;

    /**
     * 配置令牌端点安全约束
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 允许访问token的公钥,默认/oatuh/token_key是受保护的
        security.tokenKeyAccess("permitAll()")
                // 允许检查token的状态，默认是受保护的
                .checkTokenAccess("permitAll()");
    }

    /**
     * 将配置文件的授权模型读取到内存里
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(clientOAuth2DataConfiguration.getClientId())
                .secret(passwordEncoder.encode(clientOAuth2DataConfiguration.getSecret()))
                .authorizedGrantTypes(clientOAuth2DataConfiguration.getGrantTypes())
                .accessTokenValiditySeconds(clientOAuth2DataConfiguration.getRefreshTokenValidityTime())
                .refreshTokenValiditySeconds(clientOAuth2DataConfiguration.getRefreshTokenValidityTime())
                .scopes(clientOAuth2DataConfiguration.getScopes());
    }

    /**
     * 配置授权以及令牌访问的端点和令牌服务
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 认证器
        endpoints.authenticationManager(authenticationManager)
                // 具体登陆方法
                .userDetailsService(userService)
                // 存储方式:redis
                .tokenStore(redisTokenStore)
                // 令牌增强对象，增强返回结果
                .tokenEnhancer((accessToken, authentication) -> {
                    // 获取用户登陆信息，然后设置
                    SignInIdentity signInIdentity = (SignInIdentity) authentication.getPrincipal();
                    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                    map.put("nickname", signInIdentity.getNickname());
                    map.put("avatarUrl", signInIdentity.getAvatarUrl());
                    DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) accessToken;
                    token.setAdditionalInformation(map);
                    return token;
                });
    }
}

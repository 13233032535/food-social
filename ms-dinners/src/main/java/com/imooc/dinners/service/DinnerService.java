package com.imooc.dinners.service;

import cn.hutool.core.bean.BeanUtil;
import com.imooc.commons.constant.ApiConstant;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.dinners.config.OAuth2ClientConfiguration;
import com.imooc.dinners.domain.OauthDinnerInfo;
import com.imooc.dinners.mapper.DinnersMapper;
import com.imooc.dinners.vo.LoginDinnerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import vo.ShortDinerInfo;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 食客服务业务逻辑层
 * Created by Liu Ming on 2020/11/25.
 */
@Service
public class DinnerService {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;

    @Resource
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;

    @Autowired
    private DinnersMapper dinnersMapper;

    /**
     * 根据 ids 查询食客信息
     *
     * @param ids 主键 id，多个以逗号分隔，逗号之间不用空格
     * @return
     */
    public List<ShortDinerInfo> findByIds(String ids) {
        AssertUtil.isNotEmpty(ids);
        String[] idArr = ids.split(",");
        List<ShortDinerInfo> dinerInfos = dinnersMapper.findByIds(idArr);
        return dinerInfos;
    }


    public ResultInfo signIn(String account, String password, String path) {
        AssertUtil.isNotEmpty("请输入账号....", account);
        AssertUtil.isNotEmpty("请输入密码....", password);

        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 构建请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("username", account);
        body.add("password", password);
        body.setAll(BeanUtil.beanToMap(oAuth2ClientConfiguration));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        // 设置 Authorization
        restTemplate.getInterceptors()
                .add(new BasicAuthenticationInterceptor(oAuth2ClientConfiguration.getClientId(),
                        oAuth2ClientConfiguration.getSecret()));

        // 发送请求
        ResponseEntity<ResultInfo> result = restTemplate.postForEntity(oauthServerName + "oauth/token", entity, ResultInfo.class);

        // 处理返回结果
        AssertUtil.isTrue(result.getStatusCode() != HttpStatus.OK, "登陆失败");
        ResultInfo resultInfo = result.getBody();
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }

        // 这里的 Data 是一个 LinkedHashMap 转成了域对象 OAuthDinerInfo
        OauthDinnerInfo dinnerInfo =
                BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                        new OauthDinnerInfo(), false);
        LoginDinnerInfo loginDinnerInfo = new LoginDinnerInfo();
        loginDinnerInfo.setToken(dinnerInfo.getAccessToken());
        loginDinnerInfo.setAvatarUrl(dinnerInfo.getAvatarUrl());
        loginDinnerInfo.setNickname(dinnerInfo.getNickname());

        return ResultInfoUtil.buildSuccess(path, loginDinnerInfo);
    }
}

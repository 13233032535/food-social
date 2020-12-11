package com.imooc.dinners.service;

import cn.hutool.core.util.RandomUtil;
import com.imooc.commons.constant.RedisKeyConstant;
import com.imooc.commons.utils.AssertUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 发送短信业务逻辑
 * Created by Liu Ming on 2020/11/27.
 */
@Service
public class SendVerifyCodeService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 发送验证码
     * @param phone
     */
    public void send(String phone) {
        AssertUtil.isNotEmpty(phone, "手机号不能为空");
        if (!checkCodeIsExpired(phone)) {
            return;
        }
        // 生成 6 位验证码
        String code = RandomUtil.randomNumbers(6);
        // 调用短信服务发送短信
        // 发送成功，将 code 保存至 Redis，失效时间 60s
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        redisTemplate.opsForValue().set(key, code, 60, TimeUnit.SECONDS);
    }

    /**
     * 根绝手机号查询是否生成验证码
     *
     * @param phone
     * @return
     */
    private boolean checkCodeIsExpired(String phone) {
        String key = RedisKeyConstant.verify_code.getKey() + phone;
        String code = redisTemplate.opsForValue().get(key);
        return StringUtils.isBlank(code) ? true : false;
    }
}

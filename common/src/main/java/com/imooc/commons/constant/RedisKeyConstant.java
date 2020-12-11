package com.imooc.commons.constant;

import lombok.Getter;

/**
 * Created by Liu Ming on 2020/11/27.
 */
@Getter
public enum RedisKeyConstant {
    verify_code("verify_code", "验证码"),
    seckill_vouchers("seckill_vouchers:", "秒杀券的key"),
    lock_key("lockby:", "分布式锁的key");

    private String key;
    private String desc;

    RedisKeyConstant(String key,String desc){
        this.key = key;
        this.desc = desc;
    }
}

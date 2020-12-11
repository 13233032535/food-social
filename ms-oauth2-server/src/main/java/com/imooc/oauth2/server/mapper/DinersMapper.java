package com.imooc.oauth2.server.mapper;

import com.imooc.commons.model.pojo.Diners;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Created by Liu Ming on 2020/11/24.
 */
public interface DinersMapper {

    /**
     * 根据用户名、手机号、邮箱信息查询用户信息
     * @param account
     * @return
     */
    @Select("select id, username, nickname, phone, email, " +
            "password, avatar_url, roles, is_valid from t_dinner where " +
            "(username = #{account} or phone = #{account} or email = #{account})")
    Diners selectByAccountInfo(@Param("account") String account);
}

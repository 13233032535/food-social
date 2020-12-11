package com.imooc.oauth2.server.service;

import com.imooc.commons.model.domain.SignInIdentity;
import com.imooc.commons.model.pojo.Diners;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.oauth2.server.mapper.DinersMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by Liu Ming on 2020/11/24.
 */
@Service
public class UserService implements UserDetailsService {
    @Resource
    private DinersMapper dinersMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AssertUtil.isNotEmpty(username, "请输入输入名");
        Diners diners = dinersMapper.selectByAccountInfo(username);
        if (diners == null) {
            throw new UsernameNotFoundException("用户名不存在....");
        }
        SignInIdentity signInIdentity = new SignInIdentity();
        BeanUtils.copyProperties(diners, signInIdentity);
        return signInIdentity;
    }
}

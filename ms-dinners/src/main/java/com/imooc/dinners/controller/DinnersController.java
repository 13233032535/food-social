package com.imooc.dinners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.dinners.service.DinnerService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Liu Ming on 2020/11/25.
 */
@Api(tags = "食客相关接口")
@RestController
public class DinnersController {

    @Resource
    private DinnerService dinersService;

    @Resource
    private HttpServletRequest request;

    @GetMapping("signin")
    public ResultInfo signIn(String account, String password){
        return dinersService.signIn(account,password,request.getServletPath());
    }
}

package com.imooc.seckill.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.SeckillVouchers;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.seckill.service.SeckillService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by Liu Ming on 2020/11/28.
 */
@RestController
@Api(tags = "秒杀抢购相关")
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @Resource
    private HttpServletRequest request;


    @PostMapping("add")
    public ResultInfo addSeckillVouchers(@RequestBody SeckillVouchers seckillVouchers){
        seckillService.addSeckillVouchers(seckillVouchers);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),"新增成功..");
    }

    /**
     * 秒杀下单
     *
     * @param voucherId
     * @param access_token
     * @return
     */
    @PostMapping("{voucherId}")
    public ResultInfo<String> doSeckill(@PathVariable Integer voucherId, String access_token) {
        ResultInfo resultInfo = seckillService.doSeckill(voucherId, access_token, request.getServletPath());
        return resultInfo;
    }

}

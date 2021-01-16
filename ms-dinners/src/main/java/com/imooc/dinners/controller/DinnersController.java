package com.imooc.dinners.controller;

import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.dinners.service.DinnerService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.ShortDinerInfo;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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


    /**
     * 根据 ids 查询食客信息
     *
     * @param ids
     * @return
     */
    @GetMapping("findByIds")
    public ResultInfo<List<ShortDinerInfo>> findByIds(String ids) {
        List<ShortDinerInfo> dinerInfos = dinersService.findByIds(ids);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), dinerInfos);
    }

}

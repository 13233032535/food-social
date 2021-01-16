package com.imooc.follow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.imooc.commons.constant.ApiConstant;
import com.imooc.commons.constant.RedisKeyConstant;
import com.imooc.commons.exception.ParameterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.Follow;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.follow.mapper.FollowMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vo.ShortDinerInfo;
import vo.SignInDinerInfo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 关注/取关的业务逻辑层
 * Created by Liu Ming on 2020/12/17.
 */
@Service
public class FollowService {

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Value("${service.name.ms-dinners-server}")
    private String dinnerServerName;
    @Resource
    private RestTemplate restTemplate;

    @Resource
    private FollowMapper followMapper;

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * 共同关注列表
     *
     * @param dinnerId
     * @param accessToken
     * @param path
     * @return
     */
    public ResultInfo findCommonsFriends(Integer dinnerId, String accessToken, String path) {
        // 是否选择了查看对象
        AssertUtil.isTrue(dinnerId == null || dinnerId < 1, "请选择要查看的人");
        // 获取登陆用户的信息
        SignInDinerInfo dinerInfo = loadSignInDinnerInfo(accessToken);
        // 获取登陆用户的 关注信息
        String loginDinnerKey = RedisKeyConstant.following.getKey() + dinerInfo.getId();
        // 获取登陆用户查看对象的关注信息
        String dinnerKey = RedisKeyConstant.following.getKey() + dinnerId;

        // 计算交集
        Set<Integer> dinnerIds = redisTemplate.opsForSet().intersect(loginDinnerKey, dinnerKey);
        // 没有的情况
        if (dinnerIds == null || dinnerIds.isEmpty()) {
            return ResultInfoUtil.buildSuccess(path, new ArrayList<SignInDinerInfo>());
        }
        // 根据ids调用食客服务
        ResultInfo resultInfo = restTemplate.getForObject(dinnerServerName + "findByIds?access_token = #{accessToken}&ids={ids}",
                ResultInfo.class, accessToken, StrUtil.join(","), dinnerIds);

        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            resultInfo.setPath(path);
            return resultInfo;
        }
        // 处理结果集
        List<LinkedHashMap> dinnerInfoMaps = (ArrayList<LinkedHashMap>) resultInfo.getData();
        List<ShortDinerInfo> dinnerInfos =
                dinnerInfoMaps.stream().
                        map(dinner -> BeanUtil.fillBeanWithMap(dinner, new ShortDinerInfo(), true))
                        .collect(Collectors.toList());
        return ResultInfoUtil.buildSuccess(path, dinnerInfos);
    }

    /**
     * 关注/取关操作
     *
     * @param followDinnerId 关注的食客ID
     * @param isFoolowed     是否关注  1=关注 0=取关
     * @param accessToken    登陆用户的token
     * @param path
     * @return
     */
    public ResultInfo follow(Integer followDinnerId, int isFoolowed, String accessToken, String path) {
        // 是否选择了关注对象
        AssertUtil.isTrue(followDinnerId == null || followDinnerId < 1, "请选择要关注的人..");
        // 获取用户登陆信息(封装方法)
        SignInDinerInfo dinerInfo = loadSignInDinnerInfo(accessToken);
        // 获取当前登陆用户与需要关注用户的关注信息
        Follow follow = followMapper.selectFollow(dinerInfo.getId(), followDinnerId);

        // 如果没有关注信息，且需要进行关注 -- 添加关注
        if (follow == null && isFoolowed == 1) {
            // 添加关注信息
            int count = followMapper.save(dinerInfo.getId(), followDinnerId);
            // 添加关注列表到redis
            if (count == 1) {
                addToRedisSet(dinerInfo.getId(), followDinnerId);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE, "关注成功", path, "关注成功");
        }

        // 如果有关注信息，且目前是处于取关的状态，且要进行取关注操作 -- 取关操作
        if (follow != null && follow.getIsValid() == 1 && isFoolowed == 0) {
            // 取关
            int count = followMapper.update(follow.getId(), isFoolowed);
            // 移除redis关注列表
            if (count == 1) {
                removeFromRedisSet(dinerInfo.getId(), followDinnerId);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE, "成功取关", path, "成功取关");
        }

        // 如果有关注信息，且目前处于一个取关的一个操作，且要进行关注状态 -- 重新关注
        if (follow != null && follow.getIsValid() == 0 && isFoolowed == 1) {
            // 重新关注
            int count = followMapper.update(follow.getId(), isFoolowed);
            // 添加关注列表到redis
            if (count == 1) {
                addToRedisSet(dinerInfo.getId(), followDinnerId);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE, "成功取关", path, "成功取关");
        }
        return ResultInfoUtil.buildSuccess(path, "操作成功..");
    }

    /**
     * 移除关注列表
     *
     * @param dinnerId
     * @param followDinnerId
     */
    private void removeFromRedisSet(Integer dinnerId, Integer followDinnerId) {
        redisTemplate.opsForSet().remove(RedisKeyConstant.following.getKey() + dinnerId, followDinnerId);
        redisTemplate.opsForSet().remove(RedisKeyConstant.follower.getKey() + followDinnerId, dinnerId);
    }

    /**
     * 添加关注列表到redis
     *
     * @param dinnerId
     * @param followDinnerId
     */
    private void addToRedisSet(Integer dinnerId, Integer followDinnerId) {
        redisTemplate.opsForSet().add(RedisKeyConstant.following.getKey() + dinnerId, followDinnerId);
        redisTemplate.opsForSet().add(RedisKeyConstant.follower.getKey() + followDinnerId, dinnerId);
    }

    /**
     * 获取登陆用户的信息
     *
     * @param accessToken
     * @return
     */
    private SignInDinerInfo loadSignInDinnerInfo(String accessToken) {
        // 必须登录
        AssertUtil.mustLogin(accessToken);
        String url = oauthServerName + "user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            throw new ParameterException(resultInfo.getMessage());
        }
        SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInDinerInfo(), false);
        return dinerInfo;
    }
}

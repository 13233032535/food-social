package com.imooc.seckill.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.imooc.commons.constant.ApiConstant;
import com.imooc.commons.constant.RedisKeyConstant;
import com.imooc.commons.exception.ParameterException;
import com.imooc.commons.model.domain.ResultInfo;
import com.imooc.commons.model.pojo.SeckillVouchers;
import com.imooc.commons.model.pojo.VoucherOrders;
import com.imooc.commons.utils.AssertUtil;
import com.imooc.commons.utils.ResultInfoUtil;
import com.imooc.seckill.mapper.SeckillVouchersMapper;
import com.imooc.seckill.mapper.VoucherOrdersMapper;
import com.imooc.seckill.model.RedisLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestTemplate;
import vo.SignInDinerInfo;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Liu Ming on 2020/11/28.
 */
@Service
public class SeckillService {

    @Resource
    private SeckillVouchersMapper seckillVouchersMapper;
    @Resource
    private VoucherOrdersMapper voucherOrdersMapper;
    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private DefaultRedisScript defaultRedisScript;
    @Resource
    private RedisLock redisLock;
    @Resource
    private RedissonClient redissonClient;


    /**
     * 添加需要抢购懂代金券
     *
     * @param seckillVouchers
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillVouchers(SeckillVouchers seckillVouchers) {
        AssertUtil.isTrue(seckillVouchers.getFkVoucherId() == null, "请输入需要抢购的代金劵");
        AssertUtil.isTrue(seckillVouchers.getAmount() == 0, "请输入抢购的数量");

        Date now = new Date();
        AssertUtil.isNotNull(seckillVouchers.getStartTime(), "请输入开始时间");

        AssertUtil.isNotNull(seckillVouchers.getEndTime(), "请输入结束时间");
        AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "结束时间不能早于开始时间");

        // 注释原始走关系型 数据库的流程
        // 验证数据是否存在该券的抢购活动
//        SeckillVouchers seckillVouchersFromDb = seckillVouchersMapper.selectVoucher(seckillVouchers.getFkVoucherId());
//        AssertUtil.isTrue(seckillVouchersFromDb != null, "该券已经有了抢购活动...");
        // 插入数据库
        // seckillVouchersMapper.save(seckillVouchers);

        // 采用Redis实现
        String key = RedisKeyConstant.seckill_vouchers.getKey() + seckillVouchers.getFkVoucherId();
        // 验证redis是否已经存在该券的秒杀活动
        // 为什么用hash 而不是用String 采用String 存需要进行序列化，存成功取需要反序列化
        Map<String, Object> map = redisTemplate.opsForHash().entries(key);
        AssertUtil.isTrue(!map.isEmpty() && (int) map.get("amount") > 0, "该券已经拥有了");

        // 插入redis
        seckillVouchers.setIsValid(1);
        seckillVouchers.setCreateDate(now);
        seckillVouchers.setUpdateDate(now);
        redisTemplate.opsForHash().putAll(key, BeanUtil.beanToMap(seckillVouchers));
    }


    /**
     * 抢购代金券
     *
     * @param voucherId   代金券ID
     * @param accessToken 登陆token
     * @param path        访问路径
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultInfo doSeckill(Integer voucherId, String accessToken, String path) {
        // 基本参数校验
        AssertUtil.isTrue(voucherId == null || voucherId < 0, "请选择需要抢购代代金券...");
        AssertUtil.isNotEmpty(accessToken, "请登录...");

        // 判断代金券是否假如抢购
        // 注释原始的关系型数据的流程
//        SeckillVouchers seckillVouchers = seckillVouchersMapper.selectVoucher(voucherId);
//        AssertUtil.isTrue(seckillVouchers == null, "该代金券未参加抢购活动...");
//        AssertUtil.isTrue(seckillVouchers.getIsValid() == 0, "该活动已经结束...");

        // 采用redis
        String key = RedisKeyConstant.seckill_vouchers.getKey() + voucherId;
        Map<String, Object> map = redisTemplate.opsForHash().entries(key);
        SeckillVouchers seckillVouchers = BeanUtil.mapToBean(map, SeckillVouchers.class, true);

        // 判断是否开始结束
        Date now = new Date();
        AssertUtil.isTrue(now.before(seckillVouchers.getStartTime()), "该活动还未开始...");
        AssertUtil.isTrue(now.after(seckillVouchers.getEndTime()), "该抢购已经结束...");
        // 判断是否卖完
        AssertUtil.isTrue(seckillVouchers.getAmount() < 1, "该券已经卖完...");

        // 获取用户登陆信息
        String url = oauthServerName + "user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            resultInfo.setPath(path);
            return resultInfo;
        }
        // 这里的data是一个LinkedHashMap，SignInDinerInfo
        SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInDinerInfo(), false);
        // 判断登录用户是否已抢到(一个用户针对这次活动只能买一次)
        VoucherOrders order = voucherOrdersMapper.findDinnerByOrder(dinerInfo.getId(),
                seckillVouchers.getFkVoucherId());
        AssertUtil.isTrue(order != null, "该用户已抢到该代金券，无需再抢");

        // 注释原始关系型数据库的流程
        // 扣库存
//        int count = seckillVouchersMapper.stockDecrease(seckillVouchers.getId());
//        AssertUtil.isTrue(count == 0, "该券已经卖完了");

        String lockName = RedisKeyConstant.lock_key.getKey() + dinerInfo.getId() + ":" + voucherId;
        long expireTime = seckillVouchers.getEndTime().getTime() - now.getTime();

        // 使用自定义redis分布式锁来实现的
//        String lockKey = redisLock.tryLock(lockName, expireTime);

        // redis分布式锁
        RLock lock = redissonClient.getLock(lockName);

        try {

            // 不为空拿到锁 执行下单
            // 自定义分布式锁
//            if (StringUtils.isNotBlank(lockKey)) {

            // redission分布式锁处理
            boolean isLocked = lock.tryLock(expireTime, TimeUnit.MICROSECONDS);
            if (isLocked) {
                // 下单
                VoucherOrders voucherOrders = new VoucherOrders();
                voucherOrders.setFkDinerId(dinerInfo.getId());
                // redis中不需要维护外键信息
//        voucherOrders.setFkSeckillId(seckillVouchers.getId());
                voucherOrders.setFkVoucherId(seckillVouchers.getFkVoucherId());
                String orderNo = IdUtil.getSnowflake(1, 1).nextIdStr();
                voucherOrders.setOrderNo(orderNo);
                voucherOrders.setOrderType(1);
                voucherOrders.setStatus(0);
                long count = voucherOrdersMapper.save(voucherOrders);
                AssertUtil.isTrue(count == 0, "用户抢购失败");

//        count = redisTemplate.opsForHash().increment(key, "amount", -1);
//        AssertUtil.isTrue(count < 0,"该券已经卖光....");
                //采用redis + lua解决问题
                List<String> keys = new ArrayList<>();
                keys.add(key);
                keys.add("amount");
                Long amount = (Long) redisTemplate.execute(defaultRedisScript, keys);
                AssertUtil.isTrue(amount == null || amount < 1, "该券已经卖完...");
            }
        } catch (Exception e) {
            // 手动回滚事物
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            // 解锁
//            redisLock.unlock(lockName, lockKey);
            lock.unlock();
            if (e instanceof ParameterException) {
                return ResultInfoUtil.buildError(0, "该券已经卖完了", path);
            }
        }

        return ResultInfoUtil.buildSuccess(path, "抢购成功...");
    }

}

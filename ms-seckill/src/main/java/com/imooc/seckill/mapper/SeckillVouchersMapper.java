package com.imooc.seckill.mapper;

import com.imooc.commons.model.pojo.SeckillVouchers;
import org.apache.ibatis.annotations.*;

/**
 * 秒杀代金券Mapper
 * Created by Liu Ming on 2020/11/28.
 */
public interface SeckillVouchersMapper {


    /**
     * 新增秒杀活动
     * @param seckillVouchers
     * @return
     */
    @Insert("insert into t_seckill_vouchers (fk_voucher_id, amount, start_time, end_time, is_valid, create_date, update_date) " +
                " values (#{fkVoucherId}, #{amount}, #{startTime}, #{endTime}, 1, now(), now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(SeckillVouchers seckillVouchers);

    /**
     * 根据代金劵id判断该代金劵是否参加抢购活动
     * @param voucherId
     * @return
     */
    @Select("select id,fk_voucher_id,amount,start_time,end_time,is_valid," +
            "create_date,update_date from t_seckill_vouchers where fk_voucher_id = #{voucherId}")
    SeckillVouchers selectVoucher(Integer voucherId);

    /**
     * 减库存
     * @param seckillId
     * @return
     */
    @Update("update t_seckill_vouchers set amount = amount - 1 where fk_voucher_id = #{seckillId} ")
    int stockDecrease(@Param("seckillId") int seckillId);
}

package com.imooc.seckill.mapper;

import com.imooc.commons.model.pojo.VoucherOrders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 代金券订单mapper
 * Created by Liu Ming on 2020/11/28.
 */
public interface VoucherOrdersMapper {


    /**
     * 根据食客id和抢购id查询订单信息
     * @return
     */
    // 根据食客 ID 和秒杀 ID 查询代金券订单
    @Select("select id, order_no, fk_voucher_id, fk_diner_id, qrcode, payment," +
            " status, fk_seckill_id, order_type, create_date, update_date, " +
            " is_valid from t_voucher_order where fk_diner_id = #{dinerId} " +
            " and fk_seckill_id = #{seckillId} and is_valid = 1 and status > -1  ")
    VoucherOrders findDinnerByOrder(@Param("dinerId") Integer dinerId,
                                 @Param("seckillId") Integer seckillId);


    /**
     *  新增代金券订单
     * @param voucherOrders
     * @return
     */
    @Insert("insert into t_voucher_order (order_no, fk_voucher_id, fk_diner_id, " +
                " status, fk_seckill_id, order_type, create_date, update_date,  is_valid)" +
                " values (#{orderNo}, #{fkVoucherId}, #{fkDinerId}, #{status}, #{fkSeckillId}, " +
                " #{orderType}, now(), now(), 1)")
    int save(VoucherOrders voucherOrders);



}

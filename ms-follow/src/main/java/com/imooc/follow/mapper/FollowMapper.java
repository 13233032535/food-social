package com.imooc.follow.mapper;

import com.imooc.commons.model.pojo.Follow;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Created by Liu Ming on 2020/12/17.
 */
public interface FollowMapper {

    // 查询关注信息
    @Select("SELECT id,dinner_id,follow_dinner_id,is_valid,create_date,update_date FROM t_follow WHERE dinner_id = #{dinnerId}  AND follow_dinner_id = #{followDinnerId}")
    Follow selectFollow(@Param("dinnerId") Integer dinnerId, @Param("followDinnerId") Integer followDinnerId);

    // 添加关注信息
    @Insert("insert into t_follow (dinner_id, follow_dinner_id, is_valid, create_date, update_date)" +
            " values(#{dinnerId}, #{followDinnerId}, 1, now(), now())")
    int save(@Param("dinnerId") Integer dinnerId, @Param("followDinnerId") Integer followDinerId);

    // 修改关注信息
    @Update("update t_follow set is_valid = #{isFollowed}, update_date = now() where id = #{id}")
    int update(@Param("id") Integer id, @Param("isFollowed") int isFollowed);
}

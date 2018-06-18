package com.leyou.order.mapper;

import com.leyou.order.pojo.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-04 10:09
 **/
@Mapper
public interface OrderMapper extends tk.mybatis.mapper.common.Mapper<Order> {

    List<Order> queryOrderList(
            @Param("userId") Long userId,
            @Param("status") Integer status);
}

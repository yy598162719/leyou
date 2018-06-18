package com.leyou.order.mapper;

import com.leyou.order.pojo.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.special.InsertListMapper;

/**
 * @author: HuYi.Zhang
 * @create: 2018-05-04 10:09
 **/
@Mapper
public interface OrderDetailMapper extends tk.mybatis.mapper.common.Mapper<OrderDetail>, InsertListMapper<OrderDetail> {
}

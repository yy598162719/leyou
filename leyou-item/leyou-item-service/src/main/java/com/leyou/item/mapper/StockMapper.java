package com.leyou.item.mapper;

import com.leyou.cart.pojo.Stock;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.ids.DeleteByIdsMapper;

/**
 * @author Qin PengCheng
 * @date 2018/6/2
 */
public interface StockMapper extends Mapper<Stock>,DeleteByIdsMapper<Stock>{
}

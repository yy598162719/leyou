package com.leyou.item.mapper;

import com.leyou.cart.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/5/27
 */
public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category,Long> {

    @Select("SELECT * from tb_category WHERE id IN (select category_id FROM tb_category_brand where brand_id = #{bid})")
    List<Category> queryCategoryByBid(@Param("bid") Long bid);

}

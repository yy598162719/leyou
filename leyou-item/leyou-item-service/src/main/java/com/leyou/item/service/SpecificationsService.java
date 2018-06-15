package com.leyou.item.service;

import com.leyou.item.mapper.SpecificationsMapper;
import com.leyou.cart.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Qin PengCheng
 * @date 2018/6/1
 */
@Service
public class SpecificationsService {

    @Autowired
    private SpecificationsMapper specificationsMapper;

    /**
     * 查询分类规格模版的方法
     * @param id
     * @return
     */
    public String querySpecifications(Long id) {
        Specification specification = specificationsMapper.selectByPrimaryKey(id);
        if (specification==null){
            return null;
        }
        String specifications = specification.getSpecifications();
        return specifications;
    }

    /**
     * 添加模版的方法
     * @param specification
     */
    public void addSpecifications(Specification specification) {
       this.specificationsMapper.insert(specification);
    }

    /**
     * 修改模版的方法
     * @param specification
     */
    public void updateSpecifications(Specification specification) {
        this.specificationsMapper.updateByPrimaryKey(specification);
    }
}

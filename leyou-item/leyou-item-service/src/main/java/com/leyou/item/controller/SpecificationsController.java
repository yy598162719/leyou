package com.leyou.item.controller;

import com.leyou.item.service.SpecificationsService;
import com.leyou.cart.pojo.Specification;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Qin PengCheng
 * @date 2018/6/1
 */
@RestController
@RequestMapping("spec")
public class SpecificationsController
{

    @Autowired
    private SpecificationsService specificationsService;

    /**
     * 查询商品的规格
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<String> querySpecifications(@PathVariable("id")Long id){
       String sepcifications =  specificationsService.querySpecifications(id);
       if (StringUtils.isBlank(sepcifications)){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
       }
       return ResponseEntity.status(HttpStatus.OK).body(sepcifications);
    }

    /**
     * 根据分类id查询商品的规格（对外接口）
     * @param cid
     * @return
     */
    @GetMapping("cid")
    public ResponseEntity<String> querySpecificationsBycid(@RequestParam("cid")Long cid){
        String sepcifications =  specificationsService.querySpecifications(cid);
        if (StringUtils.isBlank(sepcifications)){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(sepcifications);
    }
    /**
     * 添加板板的方法
     * @param specification
     * @return
     */
    public ResponseEntity<Void> addSpecifications(Specification specification){
        this.specificationsService.addSpecifications(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * 修改模版的方法
     * @param specification
     * @return
     */
    public ResponseEntity<Void> updateSpecifications(Specification specification){
        this.specificationsService.updateSpecifications(specification);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}

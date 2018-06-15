package com.leyou.goodsDetail.service;


import com.leyou.goodsDetail.client.BrandClient;
import com.leyou.goodsDetail.client.CategoryClient;
import com.leyou.goodsDetail.client.GoodsClient;
import com.leyou.goodsDetail.client.SpuClient;
import com.leyou.cart.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Qin PengCheng
 * @date 2018/6/9
 */
@Service
public class GoodsDetailService {

    @Autowired
    private SpuClient spuClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    private Logger logger = LoggerFactory.getLogger(GoodsDetailService.class);

    public Map<String,Object> getGoodsDetails(Long id) {
        //spu
        ResponseEntity<Spu> spuResponseEntity = this.spuClient.querySpuBySpuId(id);
        if (!spuResponseEntity.hasBody()){
            logger.error("没有spu的信息");
            return null;
        }
        Spu spu = spuResponseEntity.getBody();
        //spuDeail
        ResponseEntity<SpuDetail> spuDetailResponseEntity = this.goodsClient.querySpuDetailById(id);
        if (!spuDetailResponseEntity.hasBody()){
            logger.error("没有spuDetail的信息");
            return null;
        }
        SpuDetail spuDetail = spuDetailResponseEntity.getBody();
        //sku和stock
        ResponseEntity<List<Sku>> listResponseEntity = this.goodsClient.querySkuList(id);
        if (!listResponseEntity.hasBody()){
            logger.error("查询sku信息失败");
            return null;
        }
        List<Sku> skus = listResponseEntity.getBody();
        //查询分类
        ResponseEntity<List<Brand>> brandResponsity = this.brandClient.queryBrandsByBrandIds(Arrays.asList(spu.getBrandId()));
        if (!brandResponsity.hasBody()){
            logger.error("查询品牌信息失败");
            return null;
        }
        List<Brand> brands = brandResponsity.getBody();
        //三级分类
        ResponseEntity<List<Category>> categoryResponseEntity = this.categoryClient.queryParentByCid3(spu.getCid3());
        if (!categoryResponseEntity.hasBody()){
            logger.error("没有查询到category信息");
            return null;
        }
        List<Category> categories = categoryResponseEntity.getBody();

        HashMap<String, Object> map = new HashMap<>();
        map.put("spu",spu);
        map.put("spuDetail",spuDetail);
        map.put("skus",skus);
        map.put("categories",categories);
        map.put("brand",brands.get(0));
        return map;
    }
}

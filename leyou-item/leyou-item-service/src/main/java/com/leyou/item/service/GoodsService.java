package com.leyou.item.service;

import com.leyou.item.Bo.GoodsBo;
import com.leyou.item.mapper.*;
import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;
import com.leyou.pojo.Stock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/2
 */
@Service
public class GoodsService {


    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    /**
     * 添加商品的方法
     *
     * @param goods
     */
    public void addGoods(GoodsBo goods) {

        //保存spu
        Spu spu = new Spu();
        BeanUtils.copyProperties(goods, spu);
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);

        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(date);

        spu.setLastUpdateTime(date);
        spuMapper.insertSelective(spu);

        //保存spuDetail
        SpuDetail spuDetail = goods.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insertSelective(spuDetail);

        //保存sku和store
        List<Sku> skus = goods.getSkus();

        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setEnable(true);
            sku.setCreateTime(date);
            sku.setLastUpdateTime(date);

            skuMapper.insertSelective(sku);
            Stock stock = new Stock();
            Long stocks = sku.getStock();

            stock.setStock(stocks);
            stock.setSkuId(sku.getId());
            stockMapper.insertSelective(stock);
        }

    }
}



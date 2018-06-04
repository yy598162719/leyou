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
@Transactional
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
            if(!sku.getEnable()){
                continue;
            }
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

    /**
     * 查询spudetail的方法
     *
     * @param id
     * @return
     */
    public SpuDetail querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        return spuDetail;
    }

    /**
     * 删除商品的方法
     *
     * @param id
     */
    public void deleteSpuById(Long id) {
        /*删除此商品关联的所有信息，一共四张表*/
        //1.删除spu
        spuMapper.deleteByPrimaryKey(id);
        //2.删除spuDetail
        spuDetailMapper.deleteByPrimaryKey(id);
        //3.删除sku和stock、
        List<Sku> skus = this.querySkusBySpuId(id);
        for (Sku sku : skus) {
            this.skuMapper.deleteByPrimaryKey(sku);
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }
    }

    /**
     * 根据spu查找sku集合的方法
     *
     * @param id
     * @return
     */
    public List<Sku> querySkusBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        return skus;
    }

    /**
     * 更改商品的上下架状态
     * @param id
     * @param selable
     */
    public void updateSealStand(Long id, Boolean selable) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(!selable);
        spuMapper.updateByPrimaryKeySelective(spu);
    }
}



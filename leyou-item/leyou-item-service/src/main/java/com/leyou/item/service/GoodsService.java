package com.leyou.item.service;

import com.leyou.bo.GoodsBo;
import com.leyou.item.mapper.*;
import com.leyou.cart.pojo.Sku;
import com.leyou.cart.pojo.Spu;
import com.leyou.cart.pojo.SpuDetail;
import com.leyou.cart.pojo.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

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

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final Logger logger = LoggerFactory.getLogger(GoodsService.class);
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
        Long id = spu.getId();

        addSkuAndStock(id, date, skus);

        //添加完成后，发送消息
        this.sendMessage(id,"insert");
    }

    /**
     * 添加sku和stock的方法
     *
     * @param id
     * @param date
     * @param skus
     */
    private void addSkuAndStock(Long id, Date date, List<Sku> skus) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            sku.setSpuId(id);
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

        //发送消息
        this.sendMessage(id,"delete");
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
     *
     * @param id
     * @param selable
     */
    public void updateSealStand(Long id, Boolean selable) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(!selable);
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 修改商品的方法
     *
     * @param goods
     */
    public void updateGoods(GoodsBo goods) {
        //首先根据spu的id删除sku和stock
        Sku sku = new Sku();
        sku.setSpuId(goods.getId());
        List<Sku> oldSkus = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(oldSkus)){
            //得到所有的id的集合
            ArrayList<Long> ids = new ArrayList<>();
            for (Sku skus : oldSkus) {
                ids.add(skus.getId());
            }
            //删除stock
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);
            //删除spu
            this.skuMapper.delete(sku);
        }
       /* ArrayList<Long> skuIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(oldSkus)) {
            for (Sku s : oldSkus) {
                skuIds.add(s.getId());
            }
            String str = StringUtils.join(oldSkus, ",");
            this.stockMapper.deleteByIds(str);
            this.skuMapper.deleteByIds(str);
        }*/

        //更新sku
        Spu spu = new Spu();
        BeanUtils.copyProperties(goods, spu);
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date(currentTimeMillis);

        spu.setSaleable(true);
        spu.setValid(true);
        spu.setLastUpdateTime(date);
        spuMapper.updateByPrimaryKeySelective(spu);
        //更新skuDetail
        SpuDetail spuDetail = goods.getSpuDetail();
        spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        //添加sku和skuDetail
        Long id = spu.getId();
        List<Sku> newSkus = goods.getSkus();
        addSkuAndStock(id, date, newSkus);

        //发送消息
        this.sendMessage(id,"update");
    }

    /**
     * 查询sku的列表
     * @param id
     * @return
     */
    public List<Sku> querySkuList(Long id) {

        List<Sku> skus = this.querySkusBySpuId(id);
        /*根据skus的id去查库存*/
        if (!CollectionUtils.isEmpty(skus)){
            ArrayList<Long> ids = new ArrayList<>();
            for (Sku sku : skus) {
                ids.add(sku.getId());
                Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
                sku.setStock(stock.getStock());
            }

        }
        return  skus;
    }

    /**
     * 发送消息的方法
     * @param id
     * @param type
     */
    public void sendMessage(Long id,String type){

        try {
            this.amqpTemplate.convertAndSend("item."+type,id);
        } catch (AmqpException e) {
            e.printStackTrace();
            logger.error("发送消息失败");
        }


    }

    /**
     * 根据sku的id查询sku的信息
     * @param id
     * @return
     */
    public Sku querySkuBySkuId(Long id) {
        Sku sku = this.skuMapper.selectByPrimaryKey(id);
        return sku;
    }
}



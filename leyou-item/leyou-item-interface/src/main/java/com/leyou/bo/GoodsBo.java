package com.leyou.bo;

import com.leyou.cart.pojo.Sku;
import com.leyou.cart.pojo.Spu;
import com.leyou.cart.pojo.SpuDetail;

import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/2
 */
public class GoodsBo extends Spu {


    private SpuDetail spuDetail;

    private List<Sku> skus;

    public SpuDetail getSpuDetail() {
        return spuDetail;
    }

    public void setSpuDetail(SpuDetail spuDetail) {
        this.spuDetail = spuDetail;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }

    @Override
    public String toString() {
        return "GoodsBo{" +
                "spuDetail=" + spuDetail +
                ", skus=" + skus +
                '}';
    }
}

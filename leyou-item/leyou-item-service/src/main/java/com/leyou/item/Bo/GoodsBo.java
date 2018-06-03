package com.leyou.item.Bo;

import com.leyou.pojo.Sku;
import com.leyou.pojo.Spu;
import com.leyou.pojo.SpuDetail;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
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

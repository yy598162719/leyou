package com.leyou.cart.pojo;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="tb_spu_detail")
public class SpuDetail {
    @Id
    private Long spuId;// 对应的SPU的id
    private String description;// 商品描述
    private String specTemplate;// 商品特殊规格的名称及可选值模板
    private String specifications;// 商品的全局规格属性
    private String packingList;// 包装清单
    private String afterService;// 售后服务



    public Long getSpuId() {
        return spuId;
    }

    public void setSpuId(Long spuId) {
        this.spuId = spuId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecTemplate() {
        return specTemplate;
    }

    public void setSpecTemplate(String specTemplate) {
        this.specTemplate = specTemplate;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getPackingList() {
        return packingList;
    }

    public void setPackingList(String packingList) {
        this.packingList = packingList;
    }

    public String getAfterService() {
        return afterService;
    }

    public void setAfterService(String afterService) {
        this.afterService = afterService;
    }

    @Override
    public String toString() {
        return "SpuDetail{" +
                "spuId=" + spuId +
                ", description='" + description + '\'' +
                ", specTemplate='" + specTemplate + '\'' +
                ", specifications='" + specifications + '\'' +
                ", packingList='" + packingList + '\'' +
                ", afterService='" + afterService + '\'' +
                '}';
    }
}
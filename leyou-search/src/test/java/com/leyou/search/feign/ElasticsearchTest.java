package com.leyou.search.feign;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.PageResult;
import com.leyou.bo.SpuBo;
import com.leyou.cart.pojo.Sku;
import com.leyou.cart.pojo.SpuDetail;
import com.leyou.search.LeYouSearchApplication;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpuClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeYouSearchApplication.class)
public class ElasticsearchTest {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpuClient spuClient;

    @Autowired
    private CategoryClient categoryClient;

    @Test
    public void createIndex(){
        // 创建索引
        this.elasticsearchTemplate.createIndex(Goods.class);
        // 配置映射
        this.elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void loadData() throws IOException {
        int page = 1;
        int rows = 10;
        int size = 0;
        do {
            ResponseEntity<PageResult<SpuBo>> resp = this.spuClient.querySpuByPage(null,page, rows, 1);
            // 判断状态
            if (!resp.hasBody()) {
                // 说明没查找到
                break;
            }
            // 有数据
            PageResult<SpuBo> result = resp.getBody();
            List<SpuBo> spus = result.getItems();
            size = spus.size();
            // 创建Goods集合
            List<Goods> goodsList = new ArrayList<>();
            // 遍历spu
            for (SpuBo spu : spus) {
                // 查询sku信息
                ResponseEntity<List<Sku>> skuResp = this.goodsClient.querySkuList(spu.getId());
                // 查询详情
                ResponseEntity<SpuDetail> detailResp = this.goodsClient.querySpuDetailById(spu.getId());
                // 查询商品分类名称
                ResponseEntity<List<String>> categoryResp = this.categoryClient.queryCategoryNamesBycids(
                        Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
                if (!skuResp.hasBody() || !detailResp.hasBody() || !categoryResp.hasBody()) {
                    return;
                }
                List<Sku> skus = skuResp.getBody();
                SpuDetail detail = detailResp.getBody();
                List<String> categoryNames = categoryResp.getBody();

                // 准备sku集合
                List<Map<String, Object>> skuList = new ArrayList<>();
                // 准备价格集合
                Set<Long> price = new HashSet<>();
                for (Sku s : skus) {
                    price.add(s.getPrice());
                    Map<String, Object> sku = new HashMap<>();
                    sku.put("id", s.getId());
                    sku.put("price", s.getPrice());
                    sku.put("image", StringUtils.isBlank(s.getImages()) ? "" : s.getImages().split(",")[0]);
                    sku.put("title", s.getTitle());
                    skuList.add(sku);
                }

                ObjectMapper mapper = new ObjectMapper();
                // 获取商品详情中的规格模板
                List<Map<String, Object>> specTemplate = mapper.readValue(detail.getSpecifications(), new TypeReference<List<Map<String, Object>>>() {
                });
                Map<String, Object> specs = new HashMap<>();
                // 过滤规格模板，把所有可搜索的信息保存到Map中
                specTemplate.forEach(m -> {
                    List<Map<String, Object>> params = (List<Map<String, Object>>) m.get("params");
                    params.forEach(p -> {
                        if ((boolean) p.get("searchable")) {
                            if (p.get("v") != null) {
                                specs.put(p.get("k").toString(), p.get("v"));
                            } else if (p.get("options") != null) {
                                specs.put(p.get("k").toString(), p.get("options"));
                            }
                        }
                    });
                });

                Goods goods = new Goods();
                goods.setBrandId(spu.getBrandId());
                goods.setCid1(spu.getCid1());
                goods.setCid2(spu.getCid2());
                goods.setCid3(spu.getCid3());
                goods.setCreateTime(spu.getCreateTime());
                goods.setId(spu.getId());
                goods.setSubTitle(spu.getSubTitle());
                goods.setAll(spu.getTitle() + " " + StringUtils.join(categoryNames, " ")); //全文检索字段
                goods.setPrice(new ArrayList<>(price));
                goods.setSkus(mapper.writeValueAsString(skuList));
                goods.setSpecs(specs);// TODO 用于搜索的规格参数集合

                goodsList.add(goods);
            }

            this.goodsRepository.saveAll(goodsList);
            page++;
        } while (size == 10);
    }
}
package com.leyou.search.feign;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.bo.SpuBo;
import com.leyou.common.PageResult;
import com.leyou.cart.pojo.Sku;
import com.leyou.cart.pojo.SpuDetail;
import com.leyou.search.LeYouSearchApplication;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpuClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @author Qin PengCheng
 * @date 2018/6/6
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeYouSearchApplication.class)
public class EsTest {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private SpuClient spuClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    //删除名为goods的索引库
    @Test
    public void delete() {
        elasticsearchTemplate.deleteIndex(Goods.class);
    }

    //添加名为goods的索引库和映射
    @Test
    public void create() {
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
    }

    //向数据库中添加所有的数据
    @Test
    public void add() {
        int page = 1;
        int rows = 100;
        //数据库中的每次查出的记录数
        int size = 0;
        //目标，填充Goods中的数据
        do {
            //分页查询所有数据
            ResponseEntity<PageResult<SpuBo>> pageResultResponseEntity = spuClient.querySpuByPage(null, page, rows, 1);
            if (pageResultResponseEntity.getBody()==null) {
                return;
            }
            PageResult<SpuBo> pageResult = pageResultResponseEntity.getBody();
            List<SpuBo> items = pageResult.getItems();
                List<Goods> goodsList = new ArrayList<>();

                for (SpuBo spu : items) {
                    //创建一个goods对象
                    Goods goods = new Goods();
                    Long id = spu.getId();

                    Long cid1 = spu.getCid1();
                    Long cid2 = spu.getCid2();
                    Long cid3 = spu.getCid3();

                    ResponseEntity<SpuDetail> spuDetailResponseEntity = goodsClient.querySpuDetailById(id);
                    ResponseEntity<List<Sku>> listResponseEntity = goodsClient.querySkuList(spu.getId());
                    ResponseEntity<List<String>> categoryNames = categoryClient.queryCategoryNamesBycids(Arrays.asList(cid1, cid2, cid3));
                    if (!spuDetailResponseEntity.hasBody()||!listResponseEntity.hasBody()||!categoryNames.hasBody()){
                        break;
                    }

                    //查询spuDetail
                    SpuDetail spuDetail = spuDetailResponseEntity.getBody();
                    //将可搜索的属性导入
                    String specifications = spuDetail.getSpecifications();
                    //将字符串转为对象
                    List<Map<String, Object>> maps = JsonUtils.nativeRead(specifications, new TypeReference<List<Map<String, Object>>>() {
                    });
                    //map用来存储可搜索属性
                    Map<String, Object> specMap = new HashMap<>();
                    for (Map<String, Object> map : maps) {
                        List<Map<String,Object>>  paramsList = (List<Map<String, Object>>) map.get("params");
                        for (Map<String, Object> paramsMap : paramsList) {
                            Boolean searchable = (Boolean) paramsMap.get("searchable");
                            if (searchable){
                                if (paramsMap.get("v")!=null)
                                specMap.put((String) paramsMap.get("k"),paramsMap.get("v"));
                                else if (paramsMap.get("options")!=null)
                                specMap.put((String) paramsMap.get("k"),paramsMap.get("options"));
                            }
                        }
                    }

                    //获取sku的信息
                    List<Sku> skuList = listResponseEntity.getBody();
                    //sku的信息是一个json对象，里面有很多对象
                    List<Map<String,Object>> skuData = new ArrayList<>();
                    //准备价格的集合,价格不能重复
                    HashSet<Long> prices = new HashSet<>();
                    for (Sku sku : skuList) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id",sku.getId());
                        map.put("title",sku.getTitle());
                        map.put("image", StringUtils.isBlank(sku.getImages())?"":sku.getImages().split(",")[0]);
                        map.put("price",sku.getPrice());
                        prices.add(sku.getPrice());
                        skuData.add(map);
                    }
                    //将sku的集合转为json
                    String skuDatas = JsonUtils.serialize(skuData);

                    //查询分类的集合
                    List<String> categoryNamesBody = categoryNames.getBody();
                    goods.setSubTitle(spu.getSubTitle());
                    goods.setSpecs(specMap);

                    goods.setSkus(skuDatas);

                    goods.setPrice(new ArrayList<>(prices));
                    goods.setAll(spu.getTitle()+StringUtils.join(categoryNamesBody," "));//todo
                    goods.setBrandId(spu.getBrandId());

                    goods.setCreateTime(spu.getCreateTime());
                    goods.setId(spu.getId());

                    goods.setCid1(cid1);
                    goods.setCid2(cid2);
                    goods.setCid3(cid3);

                    goodsList.add(goods);
                }
                goodsRepository.saveAll(goodsList);

            //pageResult的gettotal是每页显示的条数，list。size是个数
            size = items.size();//TODO赋值
            //本页完成后，查询下一页的数据
            page++;
        } while (size == 100);
    }

    @Test
    public void findAll(){
        Iterable<Goods> all = this.goodsRepository.findAll();
        for (Goods goods : all) {
            System.out.println(goods);
        }
    }

}

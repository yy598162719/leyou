package com.leyou.search.service;

import com.leyou.common.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.search.feign.BrandClient;
import com.leyou.search.feign.CategoryClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.vo.SearchRequest;
import com.leyou.search.vo.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    public PageResult<Goods> search2(SearchRequest request) {
        String key = request.getKey();
        // 判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (StringUtils.isBlank(key)) {
            return null;
        }
        // 准备分页参数
        int page = request.getPage();
        int size = request.getSize();

        // 构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 1、通过sourceFilter设置返回的结果字段,我们只需要id、skus、subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id", "skus", "subTitle"}, null));
        // 2、对key进行全文检索查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND));
        // 3、分页
        queryBuilder.withPageable(PageRequest.of(page, size));


        // 4、查询，获取结果
        Page<Goods> pageInfo = this.goodsRepository.search(queryBuilder.build());

        // 封装结果并返回
        return new PageResult<>(pageInfo.getTotalElements(), pageInfo.getContent());
    }


    public SearchResult<Goods> search(SearchRequest searchRequest) {
        //构建一个查询对象
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        if (searchRequest.getKey() == null) {
            return null;
        }
        //基本查询
        this.searchWithPageAndSort(searchRequest,searchQueryBuilder);
        //桶的名称，随意
        String categoryAggName = "category";
        String brandName = "brand";
        //聚合为桶
        searchQueryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        searchQueryBuilder.addAggregation(AggregationBuilders.terms(brandName).field("brandId"));
        //开始查询
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) this.goodsRepository.search(searchQueryBuilder.build());

        //获取每页的大小，用在后面的求总页数
        Integer size = searchRequest.getSize();

        //查询后的结果处理
        SearchResult searchResult = new SearchResult();
        searchResult.setTotal(pageInfo.getTotalElements());
        searchResult.setItems(pageInfo.getContent());
        Long total = pageInfo.getTotalElements();
        Long totalpage = total % size.longValue() == 0 ? total / size : total.intValue() / size + 1;
        searchResult.setTotalPage(totalpage);
        //解析聚合的结果
        List<Category> categories = this.getCategopryAggResult(pageInfo,categoryAggName);
        List<Brand> brands = this.getBrandAggResult(pageInfo,brandName);
        searchResult.setCategories(categories);
        searchResult.setBrands(brands);

        return searchResult;
    }

    /**
     * 解析品牌的聚合结果
     * @param pageInfo
     * @param brandName
     * @return
     */
    private List<Brand> getBrandAggResult(AggregatedPage<Goods> pageInfo, String brandName) {
        //得到分桶的结果
        LongTerms longTerms = (LongTerms) pageInfo.getAggregation(brandName);
        //创建一个id的集合。来接受各个桶里的品牌id
        List<Long> ids = new ArrayList<>();
        for (LongTerms.Bucket bucket : longTerms.getBuckets()) {
            ids.add(bucket.getKeyAsNumber().longValue());
        }
        //根据id的集合去查询出所有的品牌的集合
        ResponseEntity<List<Brand>> brandListResponseEntity = this.brandClient.queryBrandsByBrandIds(ids);
        if (!brandListResponseEntity.hasBody()){
            logger.error("查询品牌出现错误，id为"+ ids);
            return null;
        }
        return brandListResponseEntity.getBody();
    }


    /**
     * 解析分类的聚合结果
     * @param pageInfo
     * @param categoryAggName
     * @return
     */
    private List<Category> getCategopryAggResult(AggregatedPage<Goods> pageInfo, String categoryAggName) {
        //获取分桶的结果
        LongTerms longTerms = (LongTerms) pageInfo.getAggregation(categoryAggName);
        //创建一个list分桶解析出来的id
        List<Long> cids = new ArrayList<>();
        for (LongTerms.Bucket bucket : longTerms.getBuckets()) {
            cids.add(bucket.getKeyAsNumber().longValue());
        }
        //调用微服务，查询所有的商品分类集合
        ResponseEntity<List<Category>> categoryListResponseEntity = this.categoryClient.queryCategoriesByCids(cids);
        if (!categoryListResponseEntity.hasBody()){
            logger.error("查询品牌的id出现错误，id为"+cids);
            return null;
        }
        return categoryListResponseEntity.getBody();
    }

    /**
     * 根据条件进行基本的查询
     *
     * @param searchRequest
     * @param queryBuilder
     * @return
     */
    public void searchWithPageAndSort(SearchRequest searchRequest, NativeSearchQueryBuilder queryBuilder) {
        //获取关键词
        String key = searchRequest.getKey();
        //获取排序方式
        Boolean desc = searchRequest.getDescending();
        String sortBy = searchRequest.getSortBy();

        //获取分页查询的参数
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();

        //根据key分词查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key));
        //排序
        if (StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
        //过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        //分页
        queryBuilder.withPageable(PageRequest.of(page - 1, size));
    }


}
package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.PageResult;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.search.feign.BrandClient;
import com.leyou.search.feign.CategoryClient;
import com.leyou.search.feign.SpecClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.vo.SearchRequest;
import com.leyou.search.vo.SearchResult;
import com.leyou.utils.JsonUtils;
import com.leyou.utils.NumberUtils;
import com.vividsolutions.jts.index.strtree.Interval;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.xml.builders.BooleanQueryBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.HistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.xmlunit.util.Mapper;

import java.security.Key;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecClient specClient;

    @Autowired
    private ElasticsearchTemplate esTemplate;

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


    public SearchResult<Goods> search3(SearchRequest searchRequest) {
        //构建一个查询对象
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        if (searchRequest.getKey() == null) {
            return null;
        }
        //基本查询
        this.searchWithPageAndSort(searchRequest, searchQueryBuilder);
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
        List<Category> categories = this.getCategopryAggResult(pageInfo, categoryAggName);
        List<Brand> brands = this.getBrandAggResult(pageInfo, brandName);
        searchResult.setCategories(categories);
        searchResult.setBrands(brands);

        return searchResult;
    }

    /**
     * 解析品牌的聚合结果
     *
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
        if (ids==null||ids.size()<1){
            return null;
        }
        //根据id的集合去查询出所有的品牌的集合
        ResponseEntity<List<Brand>> brandListResponseEntity = this.brandClient.queryBrandsByBrandIds(ids);
        if (!brandListResponseEntity.hasBody()) {
            logger.error("查询品牌出现错误，id为" + ids);
            return null;
        }
        return brandListResponseEntity.getBody();
    }


    /**
     * 解析分类的聚合结果
     *
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
        if (cids==null&&cids.size()<1){
            return null;
        }
        //调用微服务，查询所有的商品分类集合
        ResponseEntity<List<Category>> categoryListResponseEntity = this.categoryClient.queryCategoriesByCids(cids);
        if (!categoryListResponseEntity.hasBody()) {
            logger.error("查询品牌的id出现错误，id为" + cids);
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


    public SearchResult<Goods> search(SearchRequest searchRequest) {
        //获取查询的条件
        String key = searchRequest.getKey();
        //如果查询的条件不存在，则返回null
        if (StringUtils.isBlank(key)) {
            return null;
        }
        //首先构建一个本地查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        QueryBuilder query = this.getBasicQueryBuilder(searchRequest);
        queryBuilder.withQuery(query);
      /*  //得到分词条件
        MatchQueryBuilder query = QueryBuilders.matchQuery("all", key);
        queryBuilder.withQuery(query);*/
        //过滤字段
        //排序条件
        String sortBy = searchRequest.getSortBy();
        Boolean desc = searchRequest.getDescending();
        if (StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
        //获取分页条件
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        if (page < 1) {
            //小于1肯定是人为操作，直接返回null
            return null;
        }
        queryBuilder.withPageable(PageRequest.of(page, size));
        //品牌和分类肯定要聚合
        String categories = "category";
        //如果分类小于1个，对过滤条件进行聚合
        String brands = "brand";
        //将分类和品牌聚合为桶
        queryBuilder.addAggregation(AggregationBuilders.terms(categories).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brands).field("brandId"));
        //得到结果
        AggregatedPage<Goods> pageInfo = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());
        //封装数据到searchResult
        SearchResult<Goods> goodsSearchResult = new SearchResult<>();
        //页面展示的数据体
        goodsSearchResult.setItems(pageInfo.getContent());
        //总页数
        Integer totalPages = pageInfo.getTotalPages();
        Long total = pageInfo.getTotalElements();
        goodsSearchResult.setTotalPage((total + size - 1) / size);
        //总记录数
        goodsSearchResult.setTotal(total);
        //品牌数据
        List<Brand> brandsList = this.getBrandAggResult(pageInfo, brands);
        goodsSearchResult.setBrands(brandsList);
        //分类数据
        List<Category> categoriesList = this.getCategopryAggResult(pageInfo, categories);
        goodsSearchResult.setCategories(categoriesList);
        //在分类聚合完成之后，判断分类的个数是否等于1个，如果是，则显示过滤条件
        if (categoriesList.size() == 1) {
            //得到过滤条件
            List<Map<String, Object>> specs = this.getSpecsAggResult(query, categoriesList.get(0).getId());
            //将结果添加到searchResult
            goodsSearchResult.setSpecs(specs);
        }
        return goodsSearchResult;
    }

    /**
     * 过滤查询
     *
     * @param searchRequest
     * @return
     */
    private QueryBuilder getBasicQueryBuilder(SearchRequest searchRequest) {
      /*  //基本查询构造器
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //得到查询条件
        String key = searchRequest.getKey();
        //查询基本分词
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", key).operator(Operator.AND));
        //过滤查询构造器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        //得到过滤条件
        Map<String, String> filterMap = searchRequest.getFilter();
        Set<Map.Entry<String, String>> entries = filterMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String keys = entry.getKey();
            String value = entry.getValue();
            String regex = "^(\\d+\\.?\\d*)-(\\d+\\.?\\d*)$";
            //数值类型的用范围查询
            if (value.matches(regex)) {
                Double[] doubles = NumberUtils.searchNumber(value, regex);
                filterQueryBuilder.must(QueryBuilders.rangeQuery("specs." + keys).gte(doubles[0]).lt(doubles[1]));
            } else {
                //字符串类型的用term查询
                if ("cid3".equals(keys) || "brandId".equals(keys)) {
                    filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
                } else {
                    filterQueryBuilder.must(QueryBuilders.termQuery("specs." + key+".keyword", value));
                }
            }
        }
        //返回构造器
        boolQueryBuilder.filter(filterQueryBuilder);
        return boolQueryBuilder;
*/
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = searchRequest.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            // 判断是否是数值类型
            String key = entry.getKey();
            // 判断是否是数值类型
            String value = entry.getValue();
            String regex = "^(\\d+\\.?\\d*)-(\\d+\\.?\\d*)$";

            if (value.matches(regex)) {
                Double[] nums = NumberUtils.searchNumber(value, regex);
                // 数值类型进行范围查询
                filterQueryBuilder.must(QueryBuilders.rangeQuery("specs." + key).gte(nums[0]).lt(nums[1]));
            } else {
                if (key != "cid3" && key != "brandId") {
                    key = "specs." + key + ".keyword";
                }
                // 字符串类型，进行term查询
                filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
            }
        }
        // 添加过滤条件
        queryBuilder.filter(filterQueryBuilder);

        return queryBuilder;

    }

    private List<Map<String, Object>> getSpecsAggResult(QueryBuilder query, Long id) {
        //根据id得到规格模版
        ResponseEntity<String> specResponseEntity = this.specClient.querySpecificationsBycid(id);
        if (!specResponseEntity.hasBody()) {
            logger.error("该分类没有规格模版");
            return null;
        }
        //得到模版中的可搜索属性
        String spec = specResponseEntity.getBody();
        //将spec的json转为集合
        List<Map<String, Object>> specList = null;
        try {
            specList = JsonUtils.nativeRead(spec, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("反序列化json失败");
        }

        //准备好两个仓库，分别存贮字符串类型和数字类型
        Map<String, String> numericSpec = new HashMap<>();
        Set<String> stringSpec = new HashSet<>();
        //遍历spec
        for (Map<String, Object> stringObjectMap : specList) {
            List<Map<String, Object>> params = (List<Map<String, Object>>) stringObjectMap.get("params");
            for (Map<String, Object> param : params) {
                //得到可搜索的属性
                if (param.get("searchable") != null && (Boolean) param.get("searchable")) {
                    //如果是数字类型的
                    if (param.get("numerical") != null && (Boolean) param.get("numerical")) {
                        numericSpec.put(param.get("k").toString(), param.get("unit").toString());
                    } else {
                        stringSpec.add(param.get("k").toString());
                    }
                }
            }

        }
        //字符串类型的得到interval
        Map<String, Double> intervalMap = this.getIntervalMap(id, numericSpec);
        //创建本地的查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //数值型用interval和key做阶梯聚合
        Set<String> intervalSet = intervalMap.keySet();
        queryBuilder.withQuery(query);
        for (String key : intervalSet) {
            queryBuilder.addAggregation(
                    AggregationBuilders
                            .histogram(key)
                            .field("specs." + key)
                            .interval(intervalMap.get(key))
                            .minDocCount(1));
        }
        //字符串也聚合
 /*       for (String str : stringSpec) {
            queryBuilder.addAggregation(
                    AggregationBuilders
                            .terms(str)
                                .field("specs."+str)
                                    .minDocCount(1)
            );
        }*/

        // 聚合字符串
        for (String key : stringSpec) {
            queryBuilder.addAggregation(
                    AggregationBuilders.terms(key).field("specs." + key + ".keyword"));
        }

        List<Map<String, Object>> specs = new ArrayList<>();

        //解析聚合结果，封装数据
        // 解析聚合结果
        Map<String, Aggregation> aggs = this.esTemplate.query(queryBuilder.build(),
                SearchResponse::getAggregations).asMap();
        // 解析数值类型
        intervalMap.entrySet().forEach(entry -> {
            Map<String, Object> spec3 = new HashMap<>();
            String key = entry.getKey();
            spec3.put("k", key);
            spec3.put("unit", numericSpec.get(key));
            InternalHistogram histogram = (InternalHistogram) aggs.get(key);
            spec3.put("options", histogram.getBuckets().stream().map(bucket -> {
                Double begin = (double) bucket.getKey();
                Double end = begin + intervalMap.get(key);
                // 对begin和end取整
                if (NumberUtils.isInt(begin) && NumberUtils.isInt(end)) {
                    // 确实是整数，需要取整
                    return begin.intValue() + "-" + end.intValue();
                } else {
                    // 小数，取2位小数
                    begin = NumberUtils.scale(begin, 2);
                    end = NumberUtils.scale(end, 2);
                    return begin + "-" + end;
                }
            }));
            specs.add(spec3);
        });
        // 解析字符串类型
        stringSpec.forEach(key -> {
            Map<String, Object> spec2 = new HashMap<>();
            spec2.put("k", key);
            StringTerms terms = (StringTerms) aggs.get(key);
            spec2.put("options", terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()));
            specs.add(spec2);
        });
        return specs;

    }

    private Map<String, Double> getIntervalMap(Long cid, Map<String, String> numericSpec) {
        //返回值是一个string，double类型的
        Map<String, Double> numbericalSpecs = new HashMap<>();
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构建查询条件
        queryBuilder.withQuery(QueryBuilders.termQuery("cid3", cid.toString())).withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        Set<String> keySet = numericSpec.keySet();
        for (String key : keySet) {
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        }
        // 添加stats类型的聚合
      /*  keySet.forEach(key -> {
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        });*/
        Map<String, Aggregation> aggs = this.esTemplate.query(queryBuilder.build(),
                SearchResponse::getAggregations).asMap();
        // 解析聚合结果
        keySet.forEach(key -> {
            InternalStats stats = (InternalStats) aggs.get(key);
            numbericalSpecs.put(key, getInterval(stats.getMin(), stats.getMax(), stats.getSum()));
        });
        return numbericalSpecs;
       /* AggregatedPage<Goods> aggregatedPage = (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());

        for (String s : keySet) {
            InternalStats agg = (InternalStats) aggregatedPage.getAggregation(s);
            double interval = this.getInterval(agg.getMin(), agg.getMax(), agg.getSum());
            IntervalMap.put(s,interval);
        }*/
    }

    // 根据最小值，最大值，sum计算interval
    private double getInterval(double min, double max, Double sum) {
        double interval = (max - min) / 6.0d;
        // 判断是否是小数
        if (sum.intValue() == sum) {
            // 不是小数，要取整十、整百这样
            // 根据interval的整数位长度来判断位数
            int length = StringUtils.substringBefore(String.valueOf(interval), ".").length();
            double factor = Math.pow(10.0, length - 1);
            return Math.round(interval / factor) * factor;
        } else {
            // 是小数,我们只保留一位小数
            return NumberUtils.scale(interval, 1);
        }
    }
}
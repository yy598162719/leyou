package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.pojo.Brand;
import com.leyou.pojo.Category;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.SpecClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.vo.SearchRequest;
import com.leyou.search.vo.SearchResult;
import com.leyou.utils.JsonUtils;
import com.leyou.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-06 12:19
 **/
@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandsClient;

    @Autowired
    private SpecClient specificationClient;

    private static Logger logger = LoggerFactory.getLogger(SearchService.class);

    public SearchResult<Goods> search(SearchRequest request) {
        // 创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 1、构建查询条件
        // 1.1.对搜索的结果进行过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 1.2.基本查询
        QueryBuilder basicQuery = this.buildBasicQueryWithFilter(request);
        queryBuilder.withQuery(basicQuery);

        // 1.3、分页
        queryBuilder.withPageable(PageRequest.of(request.getPage() - 1, request.getSize()));
        // 1.4、聚合
        // 对分类聚合
        String categoryAggName = "categoryAgg";
        String brandAggName = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 2、查询
        AggregatedPage<Goods> aggResult =
                (AggregatedPage<Goods>) this.goodsRepository.search(queryBuilder.build());


        // 3、解析结果：
        // 3.1、总条数和总页数
        long total = aggResult.getTotalElements();
        long totalPage = (total + request.getSize() - 1) / request.getSize();

        // 3.2、解析商品分类
        List<Category> categories = getCategoryAgg(aggResult, categoryAggName);
        // 3.3、解析品牌
        List<Brand> brands = getBrandAgg(aggResult, brandAggName);

        // 3.4、处理规格参数
        List<Map<String, Object>> specs = null;
        if (categories.size() == 1) {
            specs = getSpecifications(categories.get(0).getId(), basicQuery);
        }

        return new SearchResult<>(total, totalPage, aggResult.getContent(), categories, brands, specs);
    }

    // 构建基本查询条件
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        // 过滤条件构建器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        // 整理过滤条件
        Map<String, String> filter = request.getFilter();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            // 判断是否是数值类型
            String key = entry.getKey();
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

    private List<Map<String, Object>> getSpecifications(Long id, QueryBuilder basicQuery) {
        // 1、根据分类查询规格
        ResponseEntity<String> specResp = this.specificationClient.querySpecificationsBycid(id);
        if (!specResp.hasBody()) {
            logger.error("查询规格参数出错，cid={}", id);
            return null;
        }
        String jsonSpec = specResp.getBody();
        // 将规格反序列化为集合
        List<Map<String, Object>> specs = null;
        try {
            specs = JsonUtils.nativeRead(jsonSpec, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            logger.error("解析规格参数json出错，json={}", jsonSpec, e);
            return null;
        }


        // 2、过滤出可以搜索的哪些规格参数的名称，分成数值类型、字符串类型
        // 准备集合，保存字符串规格参数名
        Set<String> strSpec = new HashSet<>();
        // 准备map，保存数值规格参数名及单位
        Map<String, String> numericalUnits = new HashMap<>();
        // 解析规格
        for (Map<String, Object> spec : specs) {
            List<Map<String, Object>> params = (List<Map<String, Object>>) spec.get("params");
            for (Map<String, Object> param : params) {
                Boolean searchable = (Boolean) param.get("searchable");
                if (searchable) {
                    // 判断是否是数值类型
                    if (param.containsKey("numerical") && (boolean) param.get("numerical")) {
                        numericalUnits.put(param.get("k").toString(), param.get("unit").toString());
                    } else {
                        strSpec.add(param.get("k").toString());
                    }
                }
            }
        }

        // 3、聚合计算数值类型的interval
        Map<String, Double> numericalInterval = getNumericalInterval(id, numericalUnits.keySet());

        // 4、利用interval聚合计算数值类型的分段
        // 5、对字符串类型的参数进行聚合
        return this.aggForSpec(strSpec, numericalInterval, numericalUnits, basicQuery);
    }

    @Autowired
    private ElasticsearchTemplate template;

    // 根据规格参数，聚合得出过滤条件
    private List<Map<String, Object>> aggForSpec(Set<String> strSpec, Map<String, Double> numericalInterval,
                                                 Map<String, String> numericalUnits, QueryBuilder query) {
        List<Map<String, Object>> specs = new ArrayList<>();
        // 准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(query);
        // 聚合数值类型
        for (Map.Entry<String, Double> entry : numericalInterval.entrySet()) {
            queryBuilder.addAggregation(
                    AggregationBuilders.histogram(entry.getKey())
                            .field("specs." + entry.getKey())
                            .interval(entry.getValue())
                            .minDocCount(1)
            );
        }
        // 聚合字符串
        for (String key : strSpec) {
            queryBuilder.addAggregation(
                    AggregationBuilders.terms(key).field("specs." + key + ".keyword"));
        }

        // 解析聚合结果
        Map<String, Aggregation> aggs = this.template.query(
                queryBuilder.build(),SearchResponse::getAggregations).asMap();

        // 解析数值类型
        for (Map.Entry<String, Double> entry : numericalInterval.entrySet()) {
            Map<String, Object> spec = new HashMap<>();
            String key = entry.getKey();
            spec.put("k", key);
            spec.put("unit", numericalUnits.get(key));
            // 获取聚合结果
            InternalHistogram histogram = (InternalHistogram) aggs.get(key);
            spec.put("options", histogram.getBuckets().stream().map(bucket -> {
                Double begin = (double) bucket.getKey();
                Double end = begin + numericalInterval.get(key);
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
            specs.add(spec);
        }

        // 解析字符串类型
        strSpec.forEach(key -> {
            Map<String, Object> spec = new HashMap<>();
            spec.put("k", key);
            StringTerms terms = (StringTerms) aggs.get(key);
            spec.put("options", terms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()));
            specs.add(spec);
        });
        return specs;
    }

    // 聚合得到interval
    private Map<String, Double> getNumericalInterval(Long cid, Set<String> keySet) {
        Map<String, Double> numbericalSpecs = new HashMap<>();
        // 准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何数据
        queryBuilder.withQuery(QueryBuilders.termQuery("cid3", cid.toString()))
                .withSourceFilter(new FetchSourceFilter(new String[]{""}, null))
                .withPageable(PageRequest.of(0, 1));
        // 添加stats类型的聚合
        for (String key : keySet) {
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        }
        Map<String, Aggregation> aggs = this.template.query(queryBuilder.build(),
                new ResultsExtractor<Map<String, Aggregation>>() {
                    @Override
                    public Map<String, Aggregation> extract(SearchResponse response) {
                        return response.getAggregations().asMap();
                    }
                });

        for (String key : keySet) {
            InternalStats stats = (InternalStats) aggs.get(key);
            double interval = this.getInterval(stats.getMin(), stats.getMax(), stats.getSum());
            numbericalSpecs.put(key, interval);
        }
        return numbericalSpecs;
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

    private List<Brand> getBrandAgg(AggregatedPage<Goods> aggResult, String brandAggName) {
        LongTerms terms = (LongTerms) aggResult.getAggregation(brandAggName);
        List<Long> ids = new ArrayList<>();
        for (LongTerms.Bucket bucket : terms.getBuckets()) {
            ids.add(bucket.getKeyAsNumber().longValue());
        }
        ResponseEntity<List<Brand>> resp = this.brandsClient.queryBrandsByBrandIds(ids);
        if (resp.hasBody()) {
            return resp.getBody();
        }
        return null;
    }

    private List<Category> getCategoryAgg(AggregatedPage<Goods> aggResult, String categoryAggName) {
        LongTerms terms = (LongTerms) aggResult.getAggregation(categoryAggName);
        List<Long> ids = new ArrayList<>();
        for (LongTerms.Bucket bucket : terms.getBuckets()) {
            ids.add(bucket.getKeyAsNumber().longValue());
        }
        // 获取分类名称
        ResponseEntity<List<String>> resp = this.categoryClient.queryCategoryNamesBycids(ids);
        if (!resp.hasBody()) {
            return null;
        }
        List<String> names = resp.getBody();
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Category c = new Category();
            c.setId(ids.get(i));
            c.setName(names.get(i));
            categories.add(c);
        }
        return categories;
    }
}

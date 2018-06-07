package com.leyou.search.service;

import com.leyou.common.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.vo.SearchRequest;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

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


    public PageResult<Goods> search(SearchRequest searchRequest) {
        //构建一个查询对象
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        if (searchRequest.getKey() == null) {
            return null;
        }
        //获取排序方式
        String sortBy = searchRequest.getSortBy();
        Boolean descending = searchRequest.getDescending();
        //过滤
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id", "skus", "subTitle"}, null));
        //分词
        searchQueryBuilder.withQuery(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        //排序
        if (StringUtils.isNotBlank(sortBy)) {
            searchQueryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(descending ? SortOrder.DESC : SortOrder.ASC));
        }
        //分页查询
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        if (page < 1) {
            return null;
        }
        searchQueryBuilder.withPageable(PageRequest.of(page - 1, size));
        Page<Goods> pageGoods = this.goodsRepository.search(searchQueryBuilder.build());

        PageResult<Goods> pageResult = new PageResult<>();
        pageResult.setTotal(pageGoods.getTotalElements());
        pageResult.setItems(pageGoods.getContent());
        Long total = pageGoods.getTotalElements();
        Long totalpage = total % size.longValue() == 0 ? total/size : total.intValue()/size + 1;
        pageResult.setTotalPage(totalpage);
        return pageResult;
    }
}
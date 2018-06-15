package com.leyou.search.mq;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Qin PengCheng
 * @date 2018/6/11
 */
@Component
public class GoodsListener {

    @Autowired
    private SearchService searchService;

    /**
     * 添加或者修改索引的方法
     * @param id
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly.create.index.queue", durable = "true"),
            exchange = @Exchange(
                    value = "ly.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}))
    public void listenCreateAndUpdate(Long id){
        if (id==null){
            return;
        }
        this.searchService.createOrUpdateIndex(id);
        System.out.println("创建或者修改索引成功");
    }

    public void daleteIndex(Long id){
        if (id==null){
            return ;
        }
        this.searchService.deleteIndex(id);
        System.out.println("删除索引库成功");
    }





}

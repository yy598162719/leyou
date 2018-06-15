package com.leyou.goodsDetail.mq;

import com.leyou.goodsDetail.service.FileService;
import org.slf4j.LoggerFactory;
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
public class Goodslistener {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Goodslistener.class);

    @Autowired
    private FileService fileService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly.create.page.queue", durable = "true"),
            exchange = @Exchange(
                    value = "ly.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}))
    public void  listenCreateHtml(Long id){
        if (id==null){
            logger.error("id不存在");
            return;
        }
        this.fileService.createHtml(id);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly.delete.page.queue", durable = "true"),
            exchange = @Exchange(
                    value = "ly.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "item.delete"))
    public void  listenDeleteHtml(Long id){
        if (id==null){
            logger.error("id不存在");
            return;
        }
        this.fileService.deleteHtml(id);
    }

}

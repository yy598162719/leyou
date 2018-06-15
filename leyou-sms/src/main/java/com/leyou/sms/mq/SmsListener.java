package com.leyou.sms.mq;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Qin PengCheng
 * @date 2018/6/12
 */

@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsProperties smsProperties;

    @Autowired
    private SmsUtils smsUtils;

    private static final Logger logger = LoggerFactory.getLogger(SmsListener.class);

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ly.sms.queue", durable = "true"),
            exchange = @Exchange(value = "ly.sms.exchange",
                    ignoreDeclarationExceptions = "true"),
            key = {"sms.verify.code"}))
    public void ListenSms(Map<String,String> map){
        if (map==null||map.size()<1){
            return;
        }
        String phone = map.get("phone");
        String code = map.get("code");
        if (StringUtils.isBlank(phone)||StringUtils.isBlank("code")){
            return;
        }
        try {
            SendSmsResponse sendSmsResponse = smsUtils.sendSms(phone, code, smsProperties.getSignName(), smsProperties.getVerifyCodeTemplate());
            if ("ok".equals(sendSmsResponse.getCode())){
                //发送消息成功，结束方法
                return;
            }
        } catch (ClientException e) {
            logger.error("短信发送出现错误");
            throw new RuntimeException("短信发送错误，重试中");
        }
    }
}

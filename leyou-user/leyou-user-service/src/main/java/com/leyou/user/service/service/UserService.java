package com.leyou.user.service.service;

import com.leyou.user.pojo.User;
import com.leyou.user.service.mapper.UserMapper;
import com.leyou.utils.Md5Utils;
import com.leyou.utils.NumberUtils;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.text.BreakIterator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Qin PengCheng
 * @date 2018/6/11
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private org.slf4j.Logger logger = LoggerFactory.getLogger(UserService.class);

    private static final String KEY_PREFIX = "user:code:phone:";

    public Boolean checkUser(String data, Integer type) {
        User user = new User();
        if (type==1){
            user.setUsername(data);
        }else if (type==2){
            user.setPhone(data);
        }else {
            return null;
        }
        return this.userMapper.selectCount(user)==0;
    }

    /**
     * 发送短信的方法
     * @param phone
     */
    public void sendMessage(String phone) {
        /*封装信息，发送消息*/
        //生成一个验证码
        String code = NumberUtils.generateCode(6);
        System.out.println(code);
        try {
            Map<String, String> map = new HashMap<>();
            map.put("code",code);
            map.put("phone",phone);
            /*rabbitmq发送消息*/
           /* this.amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",map);*/
            //把code放到redis
            this.redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);
        } catch (AmqpException e) {
            logger.error("发送短息失败，phone:{},code:{}",phone,code);
        }
    }

    /**
     * 用户注册的方法
     * @param user
     * @param code
     * @return
     */
    public Boolean register(User user, String code) {
        //判断用户的数据类型是否正确
        String username = user.getUsername();
        String password = user.getPassword();
        String phone = user.getPhone();
        String regex = "^[0-9a-zA-Z_]{1,26}$";
        if (!username.matches(regex)||!password.matches(regex)) {
            return false;
        }
        //判断验证码是否正确
        String redisCode = this.redisTemplate.opsForValue().get(KEY_PREFIX + phone);
        if (redisCode==null&&code==null&&!redisCode.equals(code)){
            return false;
        }
        //对密码进行加密
        String salt = Md5Utils.generate();
        String md5Password = Md5Utils.encryptPassword(password, salt);
        user.setSalt(salt);
        user.setPassword(md5Password);
        Date createTime = new Date(System.currentTimeMillis());
        user.setCreated(createTime);
        //向数据库中添加数据
        Boolean result = this.userMapper.insertSelective(user)==1;
        //注册成功，将redis中的验证码删除
        if (result) {
            try {
                this.redisTemplate.delete(KEY_PREFIX + phone);
            } catch (Exception e) {
                logger.error("删除redis中的数据失败，key：{}"+KEY_PREFIX + phone);
            }
        }
        return result;
    }

    public User queryUserByIdAndPassword(String username, String password) {
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        if (user==null){
            return null;
        }
        String passwordSql = user.getPassword();
        String salt = user.getSalt();
        String md5Password = Md5Utils.encryptPassword(password, salt);
        if (md5Password==null||md5Password==null||!md5Password.equals(passwordSql)){
            return null;
        }
        return user;
    }
}

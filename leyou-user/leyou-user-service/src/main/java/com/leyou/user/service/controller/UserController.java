package com.leyou.user.service.controller;

import com.leyou.user.pojo.User;
import com.leyou.user.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Qin PengCheng
 * @date 2018/6/11
 */

@RestController
@RequestMapping
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验输入的信息
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUser(
            @PathVariable(value = "data", required = true) String data,
            @PathVariable(value = "type") Integer type
    ) {
        Boolean flag = this.userService.checkUser(data, type);
        return ResponseEntity.status(HttpStatus.OK).body(flag);
    }

    /**
     * 发送短信
     *
     * @param phone
     * @return
     */
    @PostMapping("send")
    public ResponseEntity<Void> sendMessage(@RequestParam("phone") String phone) {
        //校验手机号是否正确
        String regex = "^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(18[0,5-9]))\\d{8}$";
        if (!phone.matches(regex)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        this.userService.sendMessage(phone);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 用户注册的方法
     * @param code
     * @param user
     * @return
     */
    @PostMapping("register")
    public ResponseEntity<Void> userRegister(@RequestParam("code")String code, @Valid User user) {
        Boolean boo = this.userService.register(user,code);
        if (boo == null || !boo) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 校验用户名和密码是否正确
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(
            @RequestParam(value = "username",required = true)String username,
            @RequestParam(value = "password",required = true)String password
    ){
      User user = this.userService.queryUserByIdAndPassword(username,password);
      if (user==null){
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
      }
      return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
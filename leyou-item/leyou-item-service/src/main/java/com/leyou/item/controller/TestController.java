package com.leyou.item.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Qin PengCheng
 * @date 2018/5/24
 */
@Controller
@RequestMapping("hello")
public class TestController {

    @GetMapping("test")
    public String test(){
        return "test";
    }


}

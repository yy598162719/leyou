package com.leyou.goodsDetail.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Qin PengCheng
 * @date 2018/6/9
 */
@Controller
@RequestMapping("item")
public class GoodsDetailController {

    @GetMapping("{id}.html")
    public String getItems(Model model,@PathVariable("id") Long id){

        return "item";
    }
}

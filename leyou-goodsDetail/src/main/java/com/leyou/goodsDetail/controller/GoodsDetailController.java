package com.leyou.goodsDetail.controller;

import com.leyou.goodsDetail.service.FileService;
import com.leyou.goodsDetail.service.GoodsDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Qin PengCheng
 * @date 2018/6/9
 */
@Controller
@RequestMapping("item")
public class GoodsDetailController {

    @Autowired
    private GoodsDetailService goodsDetailsService;

    @Autowired
    private FileService fileService;

    @GetMapping("{id}.html")
    public String getGoodsDetails(Model model,@PathVariable("id") Long id){
        Map<String,Object> modelMap = this.goodsDetailsService.getGoodsDetails(id);
        model.addAllAttributes(modelMap);
        if (!fileService.createPath(id).exists()){
            this.fileService.asynCreateHtml(id);
        }
        return "item";
    }
}

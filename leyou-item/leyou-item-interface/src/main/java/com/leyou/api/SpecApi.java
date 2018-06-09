package com.leyou.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Qin PengCheng
 * @date 2018/6/8
 */
@RequestMapping("spec")
public interface SpecApi {

    @GetMapping("cid")
    public ResponseEntity<String> querySpecificationsBycid(@RequestParam("cid")Long cid);
}

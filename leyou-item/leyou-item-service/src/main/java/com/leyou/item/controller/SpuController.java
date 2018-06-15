package com.leyou.item.controller;

import com.leyou.common.PageResult;
import com.leyou.item.service.SpuService;
import com.leyou.bo.SpuBo;
import com.leyou.cart.pojo.Spu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author Qin PengCheng
 * @date 2018/6/1
 */
@RestController
@RequestMapping("spu")
public class SpuController {

/*    // 发起请求
        this.$http.get("/item/spu/page", {
        params: {
            key: this.filter.search, // 搜索条件
                    saleable: this.filter.saleable, // 上下架
                    page: this.pagination.page,// 当前页
                    rows: this.pagination.rowsPerPage,// 每页大小
        }
    }).then(resp => { // 这里使用箭头函数
        this.goodsList = resp.data.items;
        this.totalGoods = resp.data.total;
        // 完成赋值后，把加载状态赋值为false
        this.loading = false;
    })
}*/
/*
private Long total;// 总条数

    private Long totalPage;// 总页数

    private List<T> items;// 当前页数据*/

    @Autowired
    private SpuService spuService;

    /**
     * 分页查询spu的方法
     * @param key
     * @param page
     * @param rows
     * @param saleable
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "rows", defaultValue = "5") int rows,
            @RequestParam(value = "saleable", defaultValue = "0") int saleable
    ) {
        Boolean sa =null;
        if (saleable==1){
            sa = true;
        }else if (saleable==2){
            sa = false;
        }
        PageResult<SpuBo> pageResult = spuService.querySpuByPage(page,rows,key,sa);
        if (pageResult==null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(pageResult);
    }

    /**
     * 根据spu的id查询spu的信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuBySpuId(@PathVariable("id")Long id){
      Spu spu =this.spuService.querySpuBySpuId(id);
      if (spu==null){
          return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
      }
        return ResponseEntity.status(HttpStatus.OK).body(spu);

    }
}

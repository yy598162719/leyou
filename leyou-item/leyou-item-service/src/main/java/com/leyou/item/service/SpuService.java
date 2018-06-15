package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.PageResult;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.bo.SpuBo;
import com.leyou.cart.pojo.Spu;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Qin PengCheng
 * @date 2018/6/1
 */
@Service
public class SpuService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    public PageResult<SpuBo> querySpuByPage(int page, int rows, String key, Boolean saleable) {

        //分页查询查得数据
        PageHelper.startPage(page, Math.min(rows,100));
        //将条件加入sql语句
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        if (!StringUtils.isBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        Page<Spu> pageInfo = (Page<Spu>) spuMapper.selectByExample(example);
        //封装在spubo中
        //获取所有的spu的集合
        List<Spu> spus = pageInfo.getResult();
        //新建一个spuBos的集合，来接收返回的数据
        List<SpuBo> spuBos = new ArrayList<>();
        //遍历spu，查询cname
        for (Spu spu : spus) {
        //新建一个集合，来接受cid的集合
            SpuBo spuBo = new SpuBo();
            //查询cname
           /* ArrayList<Long> cids = new ArrayList<>();
            cids.add(spu.getCid1());
            cids.add(spu.getCid2());
            cids.add(spu.getCid3());*/
           /*可以简化为*/

            List list = categoryService.queryCategoryNameByCids(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            //将list拼接成字符串
            String categoryName = StringUtils.join(list, "/");
            //查询bname
            String brandName = brandService.queryBrandNameByBid(spu.getBrandId());
            //封装bname，cname
            spuBo.setCname(categoryName);
            spuBo.setBname(brandName);
            BeanUtils.copyProperties(spu,spuBo);
            //将这个对象添加到集合中
            spuBos.add(spuBo);
        }
        //根据cid的集合去查询cname，格式是../../../
        /*根据bid去查询bname*/
        PageResult<SpuBo> pageResult = new PageResult<>();
        pageResult.setTotal(pageInfo.getTotal());
        pageResult.setItems(spuBos);
        return pageResult;
    }

    public Spu querySpuBySpuId(Long id) {
        Spu spu = this.spuMapper.selectByPrimaryKey(id);
        return spu;
    }
}

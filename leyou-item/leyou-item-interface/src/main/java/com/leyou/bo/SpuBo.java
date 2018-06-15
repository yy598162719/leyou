package com.leyou.bo;

import com.leyou.cart.pojo.Spu;

/**
 * @author Qin PengCheng
 * @date 2018/6/1
 */
public class SpuBo extends Spu {

    private String cname;

    private String bname;


    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }


}

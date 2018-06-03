package com.leyou.item.Bo;

/**
 * @author Qin PengCheng
 * @date 2018/6/1
 */
public class SpuBo {

    private Long id;

    private String title;

    private String cname;

    private String bname;

    private Boolean saleable; // 上下架

    public Boolean getSaleable() {
        return saleable;
    }

    public void setSaleable(Boolean saleable) {
        this.saleable = saleable;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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

    @Override
    public String toString() {
        return "SpuBo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", cname='" + cname + '\'' +
                ", bname='" + bname + '\'' +
                '}';
    }
}

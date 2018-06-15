package com.leyou.cart.pojo;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Qin PengCheng
 * @date 2018/5/27
 */
@Table(name = "tb_category")
public class Category implements Serializable{

    private static final long serialVersionUID = -3137434892299881897L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "is_parent")
    private Boolean isParent;

    private Integer sort;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Boolean getIsParent() {
        return this.isParent;
    }

    public void setIsParent(Boolean isParent) {
        this.isParent = isParent;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", isParent=" + isParent +
                ", sort=" + sort +
                '}';
    }
}
package com.ebuyworld.scan4pase.models;

import java.math.BigDecimal;

import io.realm.RealmObject;

/**
 * Created by Dhruv on 8/5/16.
 */
public class Product extends RealmObject {
    public String name;
    public String sku;
    public boolean custom;
    private String pv;
    private String bv;
    private String retailCost;
    private String iboCost;

    public Product() {}

    public Product(String name, String sku, boolean custom, String pv, String bv, String retailCost, String iboCost) {
        this.name = name;
        this.sku = sku;
        this.custom = custom;
        this.pv = pv;
        this.bv = bv;
        this.retailCost = retailCost;
        this.iboCost = iboCost;
    }

    public void setPv(BigDecimal pv) {
        this.pv = pv.toString();
    }

    public BigDecimal getPv() {
        return new BigDecimal(this.pv);
    }

    public void setBv(BigDecimal bv) {
        this.bv = bv.toString();
    }

    public BigDecimal getBv() {
        return new BigDecimal(this.bv);
    }

    public void setRetailCost(BigDecimal retailCost) {
        this.retailCost = retailCost.toString();
    }

    public BigDecimal getRetailCost() {
        return new BigDecimal(this.retailCost);
    }

    public void setIboCost(BigDecimal iboCost) {
        this.iboCost = iboCost.toString();
    }

    public BigDecimal getIboCost() {
        return new BigDecimal(this.iboCost);
    }




}

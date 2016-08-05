package com.ebuyworld.scan4pase.models;

import io.realm.RealmObject;

/**
 * Created by Dhruv on 8/5/16.
 */
public class CartProduct extends RealmObject {
    public int quanitity;
    public boolean taxable;
    public Product product;
}

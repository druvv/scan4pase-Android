package com.ebuyworld.scan4pase.helper;

import com.ebuyworld.scan4pase.models.CartProduct;
import com.ebuyworld.scan4pase.models.Product;

import java.text.NumberFormat;

import java.math.RoundingMode;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Dhruv on 8/8/16.
 */
public class Helper {
    public static NumberFormat pointFormatter() {
        NumberFormat pointFormatter = NumberFormat.getNumberInstance();
        pointFormatter.setMaximumFractionDigits(2);
        pointFormatter.setMinimumFractionDigits(2);
        pointFormatter.setRoundingMode(RoundingMode.HALF_UP);
        return pointFormatter;
    }
}

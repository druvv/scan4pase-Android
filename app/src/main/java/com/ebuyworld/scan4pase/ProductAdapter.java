package com.ebuyworld.scan4pase;

import android.content.Context;

import java.text.NumberFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ebuyworld.scan4pase.helper.Helper;
import com.ebuyworld.scan4pase.models.Product;

import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Dhruv on 8/7/16.
 */
class ProductAdapter extends RealmBaseAdapter<Product> implements ListAdapter {

    private NumberFormat mCurrencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    private NumberFormat mPointFormatter = Helper.pointFormatter();

    public ProductAdapter(Context context, OrderedRealmCollection<Product> realmResults) {
        super(context, realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ProductViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_product,parent,false);
            viewHolder = new ProductViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ProductViewHolder) convertView.getTag();
        }

        Product product = adapterData.get(position);
        viewHolder.skuView.setText(context.getResources().getString(R.string.list_product_sku,product.sku, (cartProduct.taxable ? "Taxed" : "Not Taxed")));
        viewHolder.nameView.setText(product.name);
        viewHolder.pvBvView.setText(context.getResources().getString(R.string.list_product_pvbv, mPointFormatter.format(product.getPv()), mPointFormatter.format(product.getBv())));
        viewHolder.iboCostView.setText(mCurrencyFormatter.format(product.getIboCost()));
        viewHolder.retailCostView.setText(mCurrencyFormatter.format(product.getRetailCost()));

        if (product.custom) {
            viewHolder.skuView.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }

        return convertView;
    }

    public class ProductViewHolder {
        TextView skuView;
        TextView nameView;
        TextView pvBvView;
        TextView retailCostView;
        TextView iboCostView;

        public ProductViewHolder(View view) {
            skuView = (TextView) view.findViewById(R.id.list_product_sku);
            nameView = (TextView) view.findViewById(R.id.list_product_name);
            pvBvView = (TextView) view.findViewById(R.id.list_product_pvBv);
            retailCostView = (TextView) view.findViewById(R.id.list_product_retailcost);
            iboCostView = (TextView) view.findViewById(R.id.list_product_ibocost);
        }
    }
}

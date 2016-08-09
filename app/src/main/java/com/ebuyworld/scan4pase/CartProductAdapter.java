package com.ebuyworld.scan4pase;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.ebuyworld.scan4pase.helper.Helper;
import com.ebuyworld.scan4pase.models.CartProduct;
import com.ebuyworld.scan4pase.models.Product;

import java.text.NumberFormat;
import java.util.Locale;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Dhruv on 8/9/16.
 */
public class CartProductAdapter extends RealmBaseAdapter<CartProduct> implements ListAdapter {

    private NumberFormat mCurrencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    private NumberFormat mPointFormatter = Helper.pointFormatter();

    public CartProductAdapter(Context context, OrderedRealmCollection<CartProduct> realmResults) {
        super(context,realmResults);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CartProductViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_cartproduct, parent, false);
            viewHolder = new CartProductViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CartProductViewHolder) convertView.getTag();
        }

        CartProduct cartProduct = adapterData.get(position);
        Product product = cartProduct.product;

        viewHolder.skuView.setText(context.getResources().getString(R.string.list_product_sku,product.sku, (cartProduct.taxable ? "Taxed" : "Not Taxed")));
        viewHolder.nameView.setText(product.name);
        viewHolder.pvBvView.setText(context.getResources().getString(R.string.list_product_pvbv, mPointFormatter.format(product.getPv()), mPointFormatter.format(product.getBv())));
        viewHolder.iboCostView.setText(mCurrencyFormatter.format(product.getIboCost()));
        viewHolder.retailCostView.setText(mCurrencyFormatter.format(product.getRetailCost()));
        viewHolder.quantityView.setText(String.valueOf(cartProduct.quantity));

        if (product.custom) {
            viewHolder.skuView.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }

        if (cartProduct.taxable) {
            String skuText = viewHolder.skuView.getText().toString();
        }

        return convertView;
    }

    class CartProductViewHolder {
        TextView skuView;
        TextView nameView;
        TextView pvBvView;
        TextView retailCostView;
        TextView iboCostView;
        TextView quantityView;

        public CartProductViewHolder(View view) {
            skuView = (TextView) view.findViewById(R.id.list_cartproduct_sku);
            nameView = (TextView) view.findViewById(R.id.list_cartproduct_name);
            pvBvView = (TextView) view.findViewById(R.id.list_cartproduct_pvBv);
            retailCostView = (TextView) view.findViewById(R.id.list_cartproduct_retailcost);
            iboCostView = (TextView) view.findViewById(R.id.list_cartproduct_ibocost);
            quantityView = (TextView) view.findViewById(R.id.list_cartproduct_quantity);
        }
    }
}

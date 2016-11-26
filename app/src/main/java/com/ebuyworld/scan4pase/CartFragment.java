package com.ebuyworld.scan4pase;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import com.ebuyworld.scan4pase.helper.Helper;
import com.ebuyworld.scan4pase.models.*;
import com.ebuyworld.scan4pase.webservice.ProductService;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CartFragment extends Fragment {
    private RealmResults<CartProduct> cartProducts;
    private TextView grandTotal;
    private TextView subtotal;
    private TextView pvBVTotal;

    private BigDecimal iboCostSubtotal = new BigDecimal(0);
    private BigDecimal retailCostSubtotal = new BigDecimal(0);
    private BigDecimal iboCostGrandTotal = new BigDecimal(0);
    private BigDecimal retailCostGrandTotal = new BigDecimal(0);
    private BigDecimal pvTotal = new BigDecimal(0);
    private BigDecimal bvTotal = new BigDecimal(0);

    private RealmChangeListener<RealmResults<CartProduct>> mCartProductChangeListener = new RealmChangeListener<RealmResults<CartProduct>>() {
        @Override
        public void onChange(RealmResults<CartProduct> cartProducts) {
            calculateTotals();
        }
    };


    private OnFragmentInteractionListener mListener;
    private BroadcastReceiver mProductServiceReceiver;

    private ProgressDialog mProgressDialog;
    private CartProductAdapter mCartAdapter;

    public CartFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mProductServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ProductService.ACTION_COMPLETED)) {
                    hideProgressDialog();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    CartProduct cartProduct = realm.createObject(CartProduct.class);
                    cartProduct.quantity = 5;
                    cartProduct.taxable = true;
                    cartProduct.product = realm.where(Product.class).contains("sku","A4300").findFirst();
                    realm.commitTransaction();
                } else if (intent.getAction().equals(ProductService.ACTION_ERROR)) {
                    // TODO: Implement Proper Error Handling
                }
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Realm realm = Realm.getDefaultInstance();
        View rootView = inflater.inflate(R.layout.fragment_cart, container, false);

        grandTotal = (TextView) rootView.findViewById(R.id.fragment_cart_grandTotal);
        subtotal = (TextView) rootView.findViewById(R.id.fragment_cart_subtotal);
        pvBVTotal = (TextView) rootView.findViewById(R.id.fragment_cart_pvbvtotal);

        ListView listView = (ListView) rootView.findViewById(R.id.fragment_cart_listView);
        cartProducts = realm.where(CartProduct.class).findAll();
        cartProducts.addChangeListener(mCartProductChangeListener);
        mCartAdapter = new CartProductAdapter(getActivity(), cartProducts);

        listView.setAdapter(mCartAdapter);
        FrameLayout emptyCartView = (FrameLayout) rootView.findViewById(R.id.fragment_cart_emptyCartView);
        listView.setEmptyView(emptyCartView);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (preferences.getBoolean("shouldLoad",true)) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("shouldLoad",false);
            editor.commit();
            loadProducts();
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_cart, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_product:
                // TODO: Add Launching of Search / Add Activity Here
                Intent intent = new Intent(getActivity(), AddProduct.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void calculateTotals() {
        resetTotals();

        BigDecimal retailCostTaxTotal = new BigDecimal(0);

        for (CartProduct cartProduct : cartProducts) {
            Product product = cartProduct.product;
            BigDecimal quantity = new BigDecimal(cartProduct.quantity);

            BigDecimal qPV = product.getPv().multiply(quantity);
            BigDecimal qBV = product.getBv().multiply(quantity);
            BigDecimal qRetailCost = product.getRetailCost().multiply(quantity);
            BigDecimal qIboCost = product.getIboCost().multiply(quantity);

            // TODO: Implement Tax Percentage From Settings
            if (cartProduct.taxable) {
                BigDecimal taxPercentage = new BigDecimal("0.06");
                BigDecimal retailTax = qRetailCost.multiply(taxPercentage);
                retailCostTaxTotal = retailCostTaxTotal.add(retailTax);
            }

            pvTotal = pvTotal.add(qPV);
            bvTotal = bvTotal.add(qBV);
            retailCostSubtotal = retailCostSubtotal.add(qRetailCost);
            iboCostSubtotal = iboCostSubtotal.add(qIboCost);
        }

        retailCostGrandTotal = retailCostSubtotal.add(retailCostTaxTotal);
        iboCostGrandTotal = iboCostSubtotal.add(retailCostTaxTotal);

        retailCostSubtotal.setScale(2, RoundingMode.HALF_UP);
        iboCostSubtotal.setScale(2, RoundingMode.HALF_UP);
        retailCostGrandTotal.setScale(2, RoundingMode.HALF_UP);
        iboCostGrandTotal.setScale(2, RoundingMode.HALF_UP);

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        NumberFormat pointFormatter = Helper.pointFormatter();

        subtotal.setText(currencyFormatter.format(iboCostSubtotal) + " / " + currencyFormatter.format(retailCostSubtotal));
        grandTotal.setText(currencyFormatter.format(iboCostGrandTotal) + " / " + currencyFormatter.format(retailCostGrandTotal));
        pvBVTotal.setText(pointFormatter.format(pvTotal) + " / " + pointFormatter.format(bvTotal));
    }

    private void resetTotals() {
        iboCostSubtotal = new BigDecimal(0);
        retailCostSubtotal = new BigDecimal(0);
        iboCostGrandTotal = new BigDecimal(0);
        retailCostGrandTotal = new BigDecimal(0);
        pvTotal = new BigDecimal(0);
        bvTotal = new BigDecimal(0);
    }

    private void loadProducts() {
        Intent intent = new Intent(getActivity(), ProductService.class);
        intent.setAction(ProductService.ACTION_LOAD);
        getActivity().startService(intent);
        showProgressDialog();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Updating Products...");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mProductServiceReceiver, ProductService.getIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mProductServiceReceiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onCreateInvoiceClicked();
    }
}

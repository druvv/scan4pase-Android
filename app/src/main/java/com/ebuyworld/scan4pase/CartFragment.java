package com.ebuyworld.scan4pase;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import com.ebuyworld.scan4pase.models.*;
import com.ebuyworld.scan4pase.webservice.ProductService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class CartFragment extends Fragment {
    private RealmResults<CartProduct> cartProducts;

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

        mProductServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(ProductService.ACTION_COMPLETED)) {
                    hideProgressDialog();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    CartProduct cartProduct = realm.createObject(CartProduct.class);
                    cartProduct.quantity = 3;
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

        ListView listView = (ListView) rootView.findViewById(R.id.fragment_cart_listView);
        cartProducts = realm.where(CartProduct.class).findAll();
        cartProducts.addChangeListener(mCartProductChangeListener);
        mCartAdapter = new CartProductAdapter(getActivity(), cartProducts);

        listView.setAdapter(mCartAdapter);
        FrameLayout emptyCartView = (FrameLayout) rootView.findViewById(R.id.fragment_cart_emptyCartView);
        listView.setEmptyView(emptyCartView);

        loadProducts();

        return rootView;
    }

    // TODO: Implement This
    private void calculateTotals() {

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

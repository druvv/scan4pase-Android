package com.ebuyworld.scan4pase.webservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ebuyworld.scan4pase.models.CartProduct;
import com.ebuyworld.scan4pase.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

import com.ebuyworld.scan4pase.helper.Helper;

/**
 * Created by Dhruv on 8/5/16.
 */
public class ProductService extends Service {

    private final static String FILENAME = "products.csv";
    private final static String LOG_TAG = ProductService.class.getSimpleName();

    private final static int COL_SKU = 0;
    private final static int COL_NAME = 1;
    private final static int COL_PV = 2;
    private final static int COL_BV = 3;
    private final static int COL_IBOCOST = 4;
    private final static int COL_RETAILCOST = 5;

    /** Actions **/
    public static final String ACTION_LOAD = "action_load";
    public static final String ACTION_COMPLETED = "action_completed";
    public static final String ACTION_ERROR = "action_error";

    private LocalBroadcastManager mBroadCastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadCastManager = LocalBroadcastManager.getInstance(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_LOAD.equals(intent.getAction())) {
            final File file = new File(getApplicationContext().getFilesDir(), FILENAME);

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReferenceFromUrl("gs://project-2924719563810163534.appspot.com/");
            final StorageReference fileRef = storageRef.child("products.csv");

            storage.setMaxDownloadRetryTimeMillis(500);
            storage.setMaxOperationRetryTimeMillis(500);

            if (file.exists()) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        fileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                parseProducts(getApplicationContext());
                                Log.d(LOG_TAG, "Creating Broadcast");
                                Intent broadcast = new Intent();
                                broadcast.setAction(ACTION_COMPLETED);
                                mBroadCastManager.sendBroadcast(broadcast);
                                Log.d(LOG_TAG, "Broadcast Sent");
                                stopSelf();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Intent broadcast = new Intent(ACTION_COMPLETED);
                                mBroadCastManager.sendBroadcast(broadcast);
                                stopSelf();
                            }
                        });
                    }
                }.start();
            } else {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        fileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                parseProducts(getApplicationContext());
                                Intent broadcast = new Intent(ACTION_COMPLETED);
                                mBroadCastManager.sendBroadcast(broadcast);
                                stopSelf();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(LOG_TAG,"Failed to retrieve csv file from internet, error: " + e.getMessage());
                                Intent broadcast = new Intent(ACTION_ERROR);
                                mBroadCastManager.sendBroadcast(broadcast);
                                stopSelf();
                            }
                        });
                    }
                }.start();
            }
        }
        return START_STICKY;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_COMPLETED);
        filter.addAction(ACTION_ERROR);

        return filter;
    }

    private static void parseProducts(Context context) {
        File file = new File(context.getFilesDir(), FILENAME);
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.delete(CartProduct.class);
        realm.commitTransaction();

        try {
            CSVReader reader = new CSVReader(new FileReader(file), ';');
            String [] nextLine;
            realm.beginTransaction();
            RealmResults<Product> products = realm.where(Product.class).equalTo("custom",false).findAll();

            /*
            We have to keep track of which items which are in the db but not in the csv file.
            After iterating through the csv and marking which positions in the realm query are valid, we can get
            the positions which are invalid by removing the valid positions from a total position list. Then we simply
            delete the invalid products using their calculated positions.
            */

            ArrayList<Integer> positionsToSave = new ArrayList<>();
            ArrayList<Integer> positionsToDelete = new ArrayList<>(products.size());
            for (int i = 0; i < products.size(); positionsToDelete.add(i++));

            while ((nextLine = reader.readNext()) != null) {
                String sku = nextLine[COL_SKU];
                String name = nextLine[COL_NAME];
                String pv = nextLine[COL_PV];
                String bv = nextLine[COL_BV];
                String iboCost = nextLine[COL_IBOCOST];
                String retailCost = nextLine[COL_RETAILCOST];

                /*
                If a product exists in the db then add its position to the positionsToSave array and update its entries.
                Otherwise, create a new product.
                */

                Product product;
                if ((product = products.where().equalTo("sku",sku).findFirst()) != null) {
                    positionsToSave.add(products.indexOf(product));
                } else {
                    product = realm.createObject(Product.class);
                }

                product.name = name;
                product.sku = sku;
                product.custom = false;
                product.setPv(new BigDecimal(pv));
                product.setBv(new BigDecimal(bv));
                product.setIboCost(new BigDecimal(iboCost));
                product.setRetailCost(new BigDecimal(retailCost));
            }
            positionsToDelete.removeAll(positionsToSave);

            for (int i : positionsToDelete) {
                Product product = products.get(i);
                Helper.deleteProduct(product,realm);
            }
            realm.commitTransaction();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to read from file, error: " + e.getMessage());
        } finally {
            if (realm != null) {
                realm.close();
            }
        }


    }
}




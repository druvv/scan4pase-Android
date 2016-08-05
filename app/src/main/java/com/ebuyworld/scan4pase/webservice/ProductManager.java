package com.ebuyworld.scan4pase.webservice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ebuyworld.scan4pase.models.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Dhruv on 8/5/16.
 */
public class ProductManager {

    final static String FILENAME = "products.csv";
    final static String LOG_TAG = ProductManager.class.getSimpleName();

    final static int COL_SKU = 0;
    final static int COL_NAME = 1;
    final static int COL_PV = 2;
    final static int COL_BV = 3;
    final static int COL_IBOCOST = 4;
    final static int COL_RETAILCOST = 5;

    public interface CompletionHandler {
        public void finishedLoadingProducts(boolean success);
    }

    public static void loadProducts(final Context context, final CompletionHandler completion) {

        File file = new File(context.getFilesDir(), FILENAME);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://project-2924719563810163534.appspot.com/");
        StorageReference fileRef = storageRef.child("products.csv");

        storage.setMaxDownloadRetryTimeMillis(500);
        storage.setMaxOperationRetryTimeMillis(500);

        if (file.exists()) {
            fileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    parseProducts(context);
                    completion.finishedLoadingProducts(true);
                }
            });
        }
        else {
            fileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    parseProducts(context);
                    completion.finishedLoadingProducts(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(LOG_TAG,"Failed to retrieve csv file from internet, error: " + e.getMessage());
                    completion.finishedLoadingProducts(false);
                }
            });
        }



    }

    private static void parseProducts(Context context) {
        File file = new File(context.getFilesDir(), FILENAME);
        Realm realm = Realm.getDefaultInstance();

        try {
            CSVReader reader = new CSVReader(new FileReader(file), '*');
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
               products.deleteFromRealm(i);
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




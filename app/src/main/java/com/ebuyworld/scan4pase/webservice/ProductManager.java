package com.ebuyworld.scan4pase.webservice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Dhruv on 8/5/16.
 */
public class ProductManager {

    final static String FILENAME = "products.csv";
    final static String LOG_TAG = ProductManager.class.getSimpleName();

    public interface CompletionHandler {
        public void finishedLoadingProducts(boolean success);
    }

    public static void loadProducts(Context context, final CompletionHandler completion) {

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
                    completion.finishedLoadingProducts(true);
                }
            });
        } else {

        }



    }

    private static void parseProducts(Context context) {
        File file = new File(context.getFilesDir(), FILENAME);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                //TODO: Finish implementing OpenCSV to parse the products
                String[] components = line.
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to read from file " + e.getMessage());
        }


    }
}




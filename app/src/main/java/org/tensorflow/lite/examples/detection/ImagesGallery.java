package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class ImagesGallery {

    public static ArrayList<String> listOfImages(Context context) {
        ArrayList<String> listOfAllImages = new ArrayList<>();

        File f3 = new File(context.getExternalFilesDir(null), "ImageData");
        if (!f3.exists())
            f3.mkdirs();
        File[] listOfFiles = f3.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                listOfAllImages.add(listOfFiles[i].getPath());
                Log.d("File: ", listOfFiles[i].getPath());
            }
        }
        return listOfAllImages;
    }
}

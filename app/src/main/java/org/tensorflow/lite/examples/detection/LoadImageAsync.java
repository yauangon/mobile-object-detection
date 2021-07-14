package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class LoadImageAsync extends AsyncTask<List<DetectedImage>, Void, List<DetectedImage>> {

    private List<DetectedImage> images;
    SharedPreferences mPrefs;
    private ImageAdapter adapter;
    private RecyclerView recyclerView;

    LoadImageAsync(List<DetectedImage> detectedImages, Context context, ImageAdapter adapter, RecyclerView recyclerView) {
        images = detectedImages;
        mPrefs = context.getSharedPreferences(context.getApplicationInfo().name, Context.MODE_PRIVATE);
        this.adapter = adapter;
        this.recyclerView = recyclerView;
    }

    @Override
    protected List<DetectedImage> doInBackground(List<DetectedImage>... lists) {
        try {
            String json = mPrefs.getString(Constants.STORAGE_FILE, "");
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<DetectedImage>>(){}.getType();
                images = gson.fromJson(json, type);

                for (int i = 0; i < images.size(); ++i) {
                    Log.d("Async", "doInBackground: " + images.get(i).getCreationDate().toString());
                }

                adapter.UpdateData(images);
                // adapter.notifyDataSetChanged();
                // recyclerView.setAdapter(adapter);


            }
        } catch (Exception e) {
            // .printStackTrace();
        }
        return images;
    }

    @Override
    protected void onPostExecute(List<DetectedImage> detectedImages) {
        super.onPostExecute(detectedImages);

    }
}

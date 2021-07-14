package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RecentFragment extends Fragment {

    List<DetectedImage> detectedImagesList = new ArrayList<>();
    ImageAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("NULL", "CALLED CREATED RECENT: ");
        View rootview = inflater.inflate(R.layout.fragment_recent, container, false);
        RecyclerView recyclerView = (RecyclerView) rootview.findViewById(R.id.rvAlbum);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        // mAdapter = new ImageAdapter(detectedImagesList);
        // recyclerView.setAdapter(mAdapter);
        SharedPreferences mPrefs = getActivity().getSharedPreferences(getActivity().getApplicationInfo().name, Context.MODE_PRIVATE);
        try {
            String json = mPrefs.getString(Constants.STORAGE_FILE, "");
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<ImageCompressed>>(){}.getType();
                List<ImageCompressed> compresseds = gson.fromJson(json, type);
                for (int i = 0; i < compresseds.size(); ++i) {
                    Log.d("recent", "onCreateView: " + compresseds.get(i).date);
                    DetectedImage newImage = new DetectedImage(compresseds.get(i).date, decodeBase64(compresseds.get(i).bitmap));
                    detectedImagesList.add(newImage);
                }
                // adapter.notifyDataSetChanged();
                // recyclerView.setAdapter(adapter);

            }
        } catch (Exception e) {
            // .printStackTrace();
        }
        mAdapter = new ImageAdapter(detectedImagesList, getContext());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // this is data fro recycler view
        // new LoadImageAsync(detectedImagesList, getContext(), mAdapter, recyclerView).execute();


        return rootview;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

}
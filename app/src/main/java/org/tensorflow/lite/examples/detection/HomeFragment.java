package org.tensorflow.lite.examples.detection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.security.identity.ResultData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.transform.Result;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private final int SELECT_INTENT = 1;
    private final int DETECTION_INTENT = 2;

    private Button realtimeDetectionButton;
    private Button selectFromGalleryButton;
    private Button loadImagesFromGoogleDriveButton;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("NULL", "CALLED CREATED HOME: ");

        view = inflater.inflate(R.layout.fragment_home, container, false);
        realtimeDetectionButton = view.findViewById(R.id.rdButton);
        selectFromGalleryButton = view.findViewById(R.id.sgButton);
        loadImagesFromGoogleDriveButton = view.findViewById(R.id.driveButton);
        realtimeDetectionButton.setOnClickListener(this);
        selectFromGalleryButton.setOnClickListener(this);
        loadImagesFromGoogleDriveButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sgButton) {
            Intent pictureSelectIntent = new Intent(Intent.ACTION_PICK);//Set Action
            pictureSelectIntent.setType("image/");//Set the type of data
            startActivityForResult(pictureSelectIntent, SELECT_INTENT);
        } else if (v.getId() == R.id.rdButton) {
            Intent intent = new Intent(getActivity().getApplicationContext(), DetectorActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.driveButton) {
            // CUONG'S PART
            Intent intent = new Intent(getActivity().getApplicationContext(), GalleryActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_INTENT) {
            try {
                Bitmap image = getPicture(data);
                if (image == null) return;
                Toast.makeText(getActivity(), "Picture selected successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity().getApplicationContext(), EdgeDetectionActivity.class);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] bytes = stream.toByteArray();
                intent.putExtra("BMP",bytes);
                startActivity(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public Bitmap getPicture(Intent data) throws FileNotFoundException {
        /*The code below is to get the picture in the phone*/
        try {
            final Uri imageUri = data.getData();//Get the path of the picture
            final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);//Get the path-based stream file
            final Bitmap selectImage = BitmapFactory.decodeStream(imageStream);//Get the bitmap of the picture
            return selectImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
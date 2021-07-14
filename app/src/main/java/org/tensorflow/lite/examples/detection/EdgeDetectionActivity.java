package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


public class EdgeDetectionActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap bitmap;
    private Bitmap originalBitmap;
    private Button detectionButton;

    private List<ImageCompressed> compresseds = new ArrayList<>();

    private final static int CANNY = 0;
    private final static int HARRIS = 1;
    private final static int HOUGH2 = 3;
    private final static int SOBEL = 4;
    private final static String TAG = "infor";

    private Mat src = null;
    private Mat image = null;
    private Mat des = null;
    private Bitmap resultBitmap;

    private File myExternalFile;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edge_detection_activity);
        imageView = findViewById(R.id.imageView);
        detectionButton = findViewById(R.id.detectionButton);
        detectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imageView.invalidate();

        Intent intent = getIntent();
        // Bitmap img = (Bitmap) intent.getParcelableExtra("image");
        byte[] bytes = intent.getByteArrayExtra("BMP");
        Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(img);
        originalBitmap = img.copy(img.getConfig(), true);
        bitmap = img.copy(img.getConfig(), true);
        resultBitmap = img.copy(img.getConfig(), true);

        DetectedImage newImage = new DetectedImage(Calendar.getInstance().getTime(), img);
        // detectedImages.add(newImage);

        // Get remaining image from prefs
        try {
            SharedPreferences mPrefs = getSharedPreferences(getApplicationInfo().name, Context.MODE_PRIVATE);
            String json = mPrefs.getString(Constants.STORAGE_FILE, "");
            if (json != null) {
                Gson gson = new Gson();
                Type type = new TypeToken<List<ImageCompressed>>(){}.getType();
                compresseds.addAll(gson.fromJson(json, type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageCompressed newCompress = new ImageCompressed();
        newCompress.date = newImage.getCreationDate();
        Log.d(TAG, "onCreate: " + newCompress.date.toString());
        newCompress.bitmap = encodeTobase64(newImage.getImage()) ;
        compresseds.add(newCompress);

        try {
            // Save list of images to prefs
            SharedPreferences mPrefs = getSharedPreferences(getApplicationInfo().name, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = mPrefs.edit();
            ed.clear();
            Gson gson = new Gson();
            ed.putString(Constants.STORAGE_FILE, gson.toJson(compresseds));
            ed.clear().commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        // Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }



    /*Start openCV*/
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    public void EdgeDetectionResult(View view) {
        int id = view.getId();
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        Mat tempImg = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bitmap, tempImg);
        switch (id) {
            case R.id.canny:
                Toast.makeText(EdgeDetectionActivity.this, "Applied Canny edge detection", Toast.LENGTH_SHORT).show();
                try{
                    resultBitmap = canny(tempImg);
                    imageView.setImageBitmap(resultBitmap);
                } catch (Exception e) {
                    Log.d("Edge", "Canny");
                }
                break;
            case R.id.harris:
                Toast.makeText(EdgeDetectionActivity.this, "Applied Harris edge detection", Toast.LENGTH_SHORT).show();
                try{
                    resultBitmap = harris(tempImg);
                    imageView.setImageBitmap(resultBitmap);
                } catch (Exception e) {
                    Log.d("Edge", "Harris");
                }
                break;
            case R.id.hough:
                Toast.makeText(EdgeDetectionActivity.this, "Applied Hough line detection", Toast.LENGTH_SHORT).show();
                try{
                    resultBitmap = houghLine(tempImg);
                    imageView.setImageBitmap(resultBitmap);
                } catch (Exception e) {
                    Log.d("Edge", "Hough");
                }
                break;
            case R.id.sobel:
                Toast.makeText(EdgeDetectionActivity.this, "Applied Sobel gradient detection", Toast.LENGTH_SHORT).show();
                try{
                    resultBitmap = sobel(tempImg);
                    imageView.setImageBitmap(resultBitmap);
                } catch (Exception e) {
                    Log.d("Edge", "Sobel");
                }
                break;
        }
    }

    /*Get picture from album*/
    public Mat getPicture(Intent data) throws FileNotFoundException {
        /*The code below is to get the picture in the phone*/
        final Uri imageUri = data.getData();
        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
        final Bitmap selectImage = BitmapFactory.decodeStream(imageStream);

        src = new Mat(selectImage.getHeight(), selectImage.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(selectImage, src);

        return src;
    }

    public void savePicture(View view) {
        MediaStore.Images.Media.insertImage(getContentResolver(),
                resultBitmap, Calendar.getInstance().getTime().toString(), "Taken by TF Object detection application");
    }

    public Bitmap sobel(Mat src) {
        Bitmap result;
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        Mat sobelEdges = new Mat();
        int ddepth = -1;
        int dx = 1;
        int dy = 1;
        Imgproc.Sobel(grayMat, sobelEdges, ddepth, dx, dy);
        result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(sobelEdges, result);
        return result;
    }

    public Bitmap canny(Mat src) {
        Bitmap result;
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grayMat, cannyEdges, 50, 300);
        result = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(cannyEdges, result);
        return result;
    }

    public Bitmap harris(Mat src) {
        Bitmap resultHarris;
        Mat grayMat = new Mat();
        Mat corners = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        Mat tempDst = new Mat();
        Imgproc.cornerHarris(grayMat, tempDst, 2, 3, 0.04);
        Mat tempDstNorm = new Mat();
        Core.normalize(tempDst, tempDstNorm, 0, 255, Core.NORM_MINMAX);
        Core.convertScaleAbs(tempDstNorm, corners);
        Random r = new Random();
        for (int i = 0; i < tempDstNorm.cols(); i++) {
            for (int j = 0; j < tempDstNorm.rows(); j++) {
                double[] value = tempDstNorm.get(j, i);
                if (value[0] > 250) {
                    Imgproc.circle(corners, new Point(i, j), 5, new Scalar(r.nextInt(255)), 2);
                }
            }
        }
        resultHarris = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(corners, resultHarris);
        return resultHarris;
    }

    /*Hough line detection*/
    public Bitmap houghLine(Mat src) {
        Mat canny = new Mat();
        Imgproc.Canny(src, canny, 50, 200, 3, false);
        Bitmap resultHough;
        Mat cannyColor = new Mat();
        Imgproc.cvtColor(canny, cannyColor, Imgproc.COLOR_GRAY2BGR);
        Mat lines = new Mat();
        Imgproc.HoughLines(canny, lines, 1, Math.PI / 180, 100);
        // Drawing lines on the image
        double[] data;
        double rho, theta;
        Point pt1 = new Point();
        Point pt2 = new Point();
        double a, b;
        double x0, y0;
        for (int i = 0; i < lines.cols(); i++) {
            data = lines.get(0, i);
            rho = data[0];
            theta = data[1];
            a = Math.cos(theta);
            b = Math.sin(theta);
            x0 = a * rho;
            y0 = b * rho;
            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            Imgproc.line(cannyColor, pt1, pt2, new Scalar(0, 100, 255), 6);
        }
        resultHough = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(canny, resultHough);
        return resultHough;
    }

    public void blendImage(View view) {
        Bitmap blendResult = BlendImageOverlay(originalBitmap, resultBitmap);
        // resultBitmap.eraseColor(Color.WHITE);
        imageView.setImageBitmap(blendResult);
        imageView.invalidate();
    }

    private Bitmap BlendImageOverlay(Bitmap bitmap1, Bitmap overlayBitmap) {
        overlayBitmap = replaceColor(overlayBitmap, Color.BLACK, Color.TRANSPARENT);
        overlayBitmap = replaceColor(overlayBitmap, Color.WHITE, Color.RED);
        int bitmap1Width = bitmap1.getWidth();
        int bitmap1Height = bitmap1.getHeight();
        int bitmap2Width = overlayBitmap.getWidth();
        int bitmap2Height = overlayBitmap.getHeight();

        float marginLeft = (float) (bitmap1Width * 0.5 - bitmap2Width * 0.5);
        float marginTop = (float) (bitmap1Height * 0.5 - bitmap2Height * 0.5);

        Bitmap finalBitmap = Bitmap.createBitmap(bitmap1Width, bitmap1Height, bitmap1.getConfig());
        Canvas canvas = new Canvas(finalBitmap);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(overlayBitmap, marginLeft, marginTop, null);
        return finalBitmap;
    }

    public static Bitmap replaceColor(Bitmap src, int colorToReplace, int replacingColor)
    {
        if (src == null)
            return null;
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, 1 * width, 0, 0, width, height);
        for (int x = 0; x < pixels.length; ++x) {
            //    pixels[x] = ~(pixels[x] << 8 & 0xFF000000) & Color.BLACK;
            if(pixels[x] == colorToReplace) pixels[x] = replacingColor;
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
    }

}

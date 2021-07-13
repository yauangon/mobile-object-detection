package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Random;


public class EdgeDetectionActivity extends AppCompatActivity {

    private ImageView imageView;
    private Bitmap bitmap;
    private Button detectionButton;

    private final static int CANNY = 0;
    private final static int HARRIS = 1;
    private final static int HOUGH2 = 3;
    private final static int SOBEL = 4;
    private final static String TAG = "infor";

    private Mat src = null;
    private Mat image = null;
    private Mat des = null;
    private Bitmap resultBitmap;

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
        Bitmap img = (Bitmap) intent.getParcelableExtra("image");
        imageView.setImageBitmap(img);
        bitmap = img;
        resultBitmap = img;
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
                    Log.d("Edge", "Canny");
                }
                break;
            case R.id.hough:
                Toast.makeText(EdgeDetectionActivity.this, "Applied Hough line detection", Toast.LENGTH_SHORT).show();
                try{
                    resultBitmap = houghLine(tempImg);
                    imageView.setImageBitmap(resultBitmap);
                } catch (Exception e) {
                    Log.d("Edge", "Canny");
                }
                break;
            case R.id.sobel:
                Toast.makeText(EdgeDetectionActivity.this, "Applied Sobel gradient detection", Toast.LENGTH_SHORT).show();
                try{
                    resultBitmap = sobel(tempImg);
                    imageView.setImageBitmap(resultBitmap);
                } catch (Exception e) {
                    Log.d("Edge", "Canny");
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

        Intent intent = new Intent(getApplicationContext(), GoogleDriveHelper.class);
        intent.putExtra("image", resultBitmap);
        startActivity(intent);
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
}

package org.tensorflow.lite.examples.detection;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayOutputStream;
import java.util.Random;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button loadSavedImagesButton = (Button) findViewById(R.id.loadSavedImagesButton);

        loadSavedImagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, GoogleDriveHelper.class);

                Bitmap bmp = createTestBitmap(100, 100, null);

                intent.putExtra("image", bmp);
                startActivity(intent);
            }
        });

        Button startDetectingButton = (Button) findViewById(R.id.startDetectingButton);
        startDetectingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, DetectorActivity.class);
                startActivity(intent);
            }
        });
    }
    public static Bitmap createTestBitmap(int w, int h,
                                          @ColorInt Integer color) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        if (color == null) {
            int colors[] = new int[] { Color.BLUE, Color.GREEN, Color.RED,
                    Color.YELLOW, Color.WHITE };
            Random rgen = new Random();
            color = colors[rgen.nextInt(colors.length - 1)];
        }

        canvas.drawColor(color);
        return bitmap;
    }
}
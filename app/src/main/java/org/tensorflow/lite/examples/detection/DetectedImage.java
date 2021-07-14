package org.tensorflow.lite.examples.detection;

import android.graphics.Bitmap;

import java.util.Date;

public class DetectedImage {
    private Date creationDate;
    private Bitmap image;

    public DetectedImage(Date creationDate, Bitmap image) {
        this.creationDate = creationDate;
        this.image = image;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Bitmap getImage() {
        return image;
    }
}

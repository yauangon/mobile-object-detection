package org.tensorflow.lite.examples.detection;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveStorage {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;
    private Activity activity;
    public DriveStorage(Activity activity, Drive mDriveService) {
        this.activity = activity;
        this.mDriveService = mDriveService;
    }

    public Task<String> createImage(Bitmap bmp) {
        return Tasks.call(mExecutor, () -> {
            String path = getTempPathOfBitMap(bmp);
            String filename = path.substring(path.lastIndexOf("/")+1);

            String folderID = createFolderIfNotExists();

            com.google.api.services.drive.model.File fileMetaData = new com.google.api.services.drive.model.File();
            fileMetaData.setName(filename).setParents(Collections.singletonList(folderID));

            File file = new File(path);
            FileContent mediaContent = new FileContent("images/png", file);
            com.google.api.services.drive.model.File myFile = null;
//            Log.d("SADFASDF", "SDFSDF");

            try {
                myFile = mDriveService.files().create(fileMetaData, mediaContent).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (myFile == null) {
//                Toast.makeText(activity.getApplicationContext(), "Upload Failed", Toast.LENGTH_SHORT).show();
                throw new IOException("Upload Failed");
            }
            return myFile.getId();
        });
    }

    private String getTempPathOfBitMap(Bitmap bmp) {

        long timestamp = new Timestamp(System.currentTimeMillis()).getTime();
        String fileName = String.valueOf(timestamp);
        File f3 = new File(activity.getExternalCacheDir(), "ImageData");

        if(!f3.exists())
            f3.mkdirs();
        OutputStream outStream = null;
        File file = new File(f3, fileName+".png");

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 85, outStream);
            outStream.close();
//            Toast.makeText(activity.getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }


    private String createFolderIfNotExists() {
        String folderId = "";

        try {
            folderId = getFolderId();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (folderId.equals("")) {
            try {
                folderId = createFolder();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return folderId;
    }

    private String getFolderId() throws IOException {
        String pageToken = null;
        do {
            FileList result = mDriveService.files().list()
                    .setQ("mimeType='application/vnd.google-apps.folder'")
                    .setQ("name='ObjectDetected'")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();
            for (com.google.api.services.drive.model.File file : result.getFiles()) {
                return file.getId();
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return "";
    }

    private String createFolder() throws IOException {
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName("ObjectDetected");
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        com.google.api.services.drive.model.File file = mDriveService.files().create(fileMetadata)
                .setFields("id")
                .execute();
        return file.getId();
    }

    public Task<ArrayList<String>> getImagePathList() {
        return Tasks.call(mExecutor, () -> {
            ArrayList<String> imageIdList = getImageIdList();
            ArrayList<String> imagePathList = new ArrayList<>();

            File f3 = new File(activity.getExternalFilesDir(null), "ImageData");
            if(!f3.exists())
                f3.mkdirs();

            assert imageIdList != null;
            for (String id : imageIdList) {
                File file = new File(f3, id+".png");

                if (!file.exists()) {
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    mDriveService.files().get(id)
                            .executeMediaAndDownloadTo(fileOutputStream);
                    fileOutputStream.close();
                }

                Log.d("Path: ", file.getPath());
                imagePathList.add(file.getPath());
            }
            return imagePathList;
        });
    }

    private ArrayList<String> getImageIdList() {
        String pageToken = null;
        ArrayList<String> imageIdList = new ArrayList<>();
        String folderId = createFolderIfNotExists();

        try {
            do {
                FileList result = mDriveService.files().list()
                        .setQ("mimeType='image/png'")
                        .setQ("'" + folderId + "' in parents")
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();
                for (com.google.api.services.drive.model.File file : result.getFiles()) {
                    imageIdList.add(file.getId());
                }
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            return null;
        }
        return imageIdList;
    }
}

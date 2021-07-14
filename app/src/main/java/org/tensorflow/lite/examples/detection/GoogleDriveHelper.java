package org.tensorflow.lite.examples.detection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationResponse;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class GoogleDriveHelper extends AppCompatActivity {
    private DriveStorage mDriveStorage;
    private Bitmap bmp = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmp = (Bitmap) getIntent().getParcelableExtra("image");
        if (bmp != null) {
            requestSignIn();
        }
//        }

    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), 400);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 400:
                if (resultCode == RESULT_OK) {
                    handleSignInIntent(data);
                }
                break;
        }
    }

    private void handleSignInIntent(Intent data) {
        GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnSuccessListener(new OnSuccessListener<GoogleSignInAccount>() {
                    @Override
                    public void onSuccess(GoogleSignInAccount googleSignInAccount) {
                        GoogleAccountCredential credential = GoogleAccountCredential
                                .usingOAuth2(GoogleDriveHelper.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveService = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential).
                                setApplicationName("Object Detection")
                                .build();
//                        Log.d("ABCD", "ASDFAS");

                        mDriveStorage = new DriveStorage(GoogleDriveHelper.this, googleDriveService);
                        mDriveStorage.createImage(bmp)
                                .addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Toast.makeText(GoogleDriveHelper.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull @NotNull Exception e) {
                                        Toast.makeText(GoogleDriveHelper.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });

//                        Toast.makeText(GoogleDriveHelper.this, "Verified Successfully", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(GoogleDriveHelper.this, MenuActivity.class);
//                        GoogleDriveHelper.this.startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @org.jetbrains.annotations.NotNull Exception e) {
                        Toast.makeText(GoogleDriveHelper.this, "Verified Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
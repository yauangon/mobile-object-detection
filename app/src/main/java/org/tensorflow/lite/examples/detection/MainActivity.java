package org.tensorflow.lite.examples.detection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private static final int RC_AUTH = 100;
    private AuthorizationService mAuthService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuthService = new AuthorizationService(this);

        Button btn = (Button) findViewById(R.id.OAuthButton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestSignIn();
            }
        });
//        new VerifyTokenAsyncTask(this).execute("");
    }

    private void requestSignIn() {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);
        startActivityForResult(client.getSignInIntent(), 400);

    }
    public void Authorize() {

        AuthorizationServiceConfiguration serviceConfig =
                new AuthorizationServiceConfiguration(
                        Uri.parse(Constants.AUTHORIZATION_ENDPOINT), // authorization endpoint
                        Uri.parse(Constants.TOKEN_ENDPOINT)); // token endpoint

        String clientId = Constants.CLIENT_ID;
        Uri redirectUri = Uri.parse(Constants.REDIRECT_URI);
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(
                serviceConfig,
                clientId,
                ResponseTypeValues.CODE,
                redirectUri
        );

        builder.setScopes(String.format("%s %s", Constants.PROFILE_SCOPE, Constants.DRIVE_SCOPE));

        AuthorizationRequest authRequest = builder.build();

        AuthorizationService authService = new AuthorizationService(this);
        Intent authIntent = authService.getAuthorizationRequestIntent(authRequest);
        startActivityForResult(authIntent, RC_AUTH);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_AUTH:
                AuthorizationResponse resp = AuthorizationResponse.fromIntent(data);
                AuthorizationException ex = AuthorizationException.fromIntent(data);
                if (resp == null) {
                    Log.d("Debug", "Null Response");
                    return;
                } else {
                    Log.d("Debug", "Ok");
                }
                mAuthService.performTokenRequest(
                        resp.createTokenExchangeRequest(),
                        (resp1, ex1) -> {
                            if (resp1 != null) {
                                SharedPreferences sharedPreferences = getSharedPreferences("OAUTH", MODE_PRIVATE);
                                sharedPreferences.edit().putString("ACCESS_TOKEN", resp1.accessToken).apply();
                                sharedPreferences.edit().putString("REFRESH_TOKEN", resp1.refreshToken).apply();
                                /**
                                 * How to retrive token:
                                 *     SharedPreferences sharedPreferences = getSharedPreferences("OAUTH", MODE_PRIVATE);
                                 *     String token = sharedPreferences.getString("ACCESS_TOKEN", "");
                                 */
                                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                                startActivity(intent);
                            } else {
                                // authorization failed, check ex for more details
                            }
                        });
                break;
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
                                .usingOAuth2(MainActivity.this, Collections.singleton(DriveScopes.DRIVE_FILE));
                        credential.setSelectedAccount(googleSignInAccount.getAccount());

                        Drive googleDriveService = new Drive.Builder(
                                AndroidHttp.newCompatibleTransport(),
                                new GsonFactory(),
                                credential).
                                setApplicationName("Object Detection")
                                .build();

                        Toast.makeText(MainActivity.this, "Verified Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                        MainActivity.this.startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @org.jetbrains.annotations.NotNull Exception e) {
                        Toast.makeText(MainActivity.this, "Verified Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

class VerifyTokenAsyncTask extends AsyncTask<String, Void, Boolean>
{
    private Activity activity;
    public VerifyTokenAsyncTask(Activity context) {
        this.activity = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Button btn = (Button) activity.findViewById(R.id.OAuthButton);
        btn.setEnabled(false);

        Toast.makeText(activity, "Verifying Token", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("OAUTH", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("ACCESS_TOKEN", "");

        try {
            URL url = new URL(String.format("https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=%s", token));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            BufferedReader bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line, response = "";
            while ((line = bf.readLine()) != null) {
                response = response.concat(line);
            }
//            Log.d("EEEEE", response);

            JSONObject jsonObject = new JSONObject(response);
//            Log.d("EEEEE", String.valueOf(jsonObject.getInt("expires_in")));
            return jsonObject.getInt("expires_in") != 0;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (aBoolean == Boolean.TRUE) {
            Toast.makeText(activity, "Verified Successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(activity, MenuActivity.class);
            activity.startActivity(intent);
        } else {
            Button btn = (Button) activity.findViewById(R.id.OAuthButton);
            btn.setEnabled(true);
            Toast.makeText(activity, "Verified failed, Please authorize again", Toast.LENGTH_SHORT).show();
        }
    }
}
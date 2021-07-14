package org.tensorflow.lite.examples.detection;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;

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
                Authorize();
            }
        });

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

        if (requestCode == RC_AUTH) {
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
        } else {

        }
    }

}
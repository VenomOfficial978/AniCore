package com.venom.anitrack;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "27691";
    private static final String REDIRECT_URI = "https://venomofficial978.github.io/FirstClass/";

    private TextView welcomeText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcomeText = findViewById(R.id.welcomeText);
        loginButton = findViewById(R.id.loginBtn);

        loginButton.setOnClickListener(v -> launchAniListLogin());
    }

    private void launchAniListLogin() {
        String authUrl = "https://anilist.co/api/v2/oauth/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=token";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl));
        startActivity(browserIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();

        if (uri != null && uri.toString().startsWith(REDIRECT_URI)) {
            String fragment = uri.getFragment(); // access_token is in fragment
            if (fragment != null && fragment.contains("access_token")) {
                String token = fragment.split("access_token=")[1].split("&")[0];
                welcomeText.setText("Token: " + token);
                // TODO: Save to SharedPreferences and call AniList API
            }
        }
    }
}

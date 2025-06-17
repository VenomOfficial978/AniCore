package com.venom.anicore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "27691";
    private static final String REDIRECT_URI = "https://venomofficial978.github.io/FirstClass/";
    private static final String AUTH_URL = "https://anilist.co/api/v2/oauth/authorize?client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=token";

    private SharedPreferences prefs;
    private Button loginButton;
    private Button openDashboardButton;
    private LinearLayout userInfoLayout;
    private TextView userName;
    private ImageView userAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("AniCorePrefs", MODE_PRIVATE);

        loginButton = findViewById(R.id.loginButton);
        openDashboardButton = findViewById(R.id.openDashboardButton);
        userInfoLayout = findViewById(R.id.userInfoLayout);
        userName = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);

        String token = prefs.getString("access_token", null);
        if (token != null) {
            fetchUserData(token);
        }

        loginButton.setOnClickListener(v -> openLoginWebView());
        openDashboardButton.setOnClickListener(v -> openDashboard());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void openLoginWebView() {
        WebView webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(REDIRECT_URI)) {
                    Uri uri = Uri.parse(url.replace("#", "?"));
                    String token = uri.getQueryParameter("access_token");
                    if (token != null) {
                        prefs.edit().putString("access_token", token).apply();
                        setContentView(R.layout.activity_main);
                        initViews();
                        fetchUserData(token);
                    }
                    return true;
                }
                return false;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (url.contains("access_token=")) {
                    view.stopLoading();
                    shouldOverrideUrlLoading(view, url);
                }
            }
        });

        webView.loadUrl(AUTH_URL);
    }

    private void initViews() {
        loginButton = findViewById(R.id.loginButton);
        openDashboardButton = findViewById(R.id.openDashboardButton);
        userInfoLayout = findViewById(R.id.userInfoLayout);
        userName = findViewById(R.id.userName);
        userAvatar = findViewById(R.id.userAvatar);

        loginButton.setOnClickListener(v -> openLoginWebView());
        openDashboardButton.setOnClickListener(v -> openDashboard());
    }

    private void fetchUserData(String token) {
        new Thread(() -> {
            try {
                String query = "{ Viewer { name avatar { large } } }";
                JSONObject jsonBody = new JSONObject().put("query", query);

                URL url = new URL("https://graphql.anilist.co");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                writer.write(jsonBody.toString());
                writer.flush();

                Scanner in = new Scanner(conn.getInputStream());
                StringBuilder sb = new StringBuilder();
                while (in.hasNext()) sb.append(in.nextLine());

                JSONObject response = new JSONObject(sb.toString());
                JSONObject viewer = response.getJSONObject("data").getJSONObject("Viewer");

                String name = viewer.getString("name");
                String avatarUrl = viewer.getJSONObject("avatar").getString("large");

                runOnUiThread(() -> {
                    userInfoLayout.setVisibility(View.VISIBLE);
                    userName.setText("Welcome, " + name + "!");
                    Glide.with(MainActivity.this).load(avatarUrl).into(userAvatar);
                    openDashboardButton.setVisibility(View.VISIBLE);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void openDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
    }
}

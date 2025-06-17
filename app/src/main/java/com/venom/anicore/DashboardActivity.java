package com.venom.anicore;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class DashboardActivity extends AppCompatActivity {

    private LinearLayout listContainer;
    private SharedPreferences prefs;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        prefs = getSharedPreferences("AniCorePrefs", MODE_PRIVATE);
        token = prefs.getString("access_token", null);

        listContainer = findViewById(R.id.listContainer);

        findViewById(R.id.watchingBtn).setOnClickListener(v -> loadCategory("CURRENT"));
        findViewById(R.id.completedBtn).setOnClickListener(v -> loadCategory("COMPLETED"));
        findViewById(R.id.planToWatchBtn).setOnClickListener(v -> loadCategory("PLANNING"));

        loadCategory("CURRENT");
    }

    private void loadCategory(String status) {
        listContainer.removeAllViews();

        new Thread(() -> {
            try {
                String query = "{ MediaListCollection(userId: null, type: ANIME, status: " + status + ") { lists { entries { media { title { romaji } } } } } }";
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
                JSONArray entries = response
                        .getJSONObject("data")
                        .getJSONObject("MediaListCollection")
                        .getJSONArray("lists")
                        .getJSONObject(0)
                        .getJSONArray("entries");

                runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < entries.length(); i++) {
                            String title = entries.getJSONObject(i)
                                    .getJSONObject("media")
                                    .getJSONObject("title")
                                    .getString("romaji");

                            TextView item = new TextView(this);
                            item.setText("• " + title);
                            item.setTextColor(0xFFFFFFFF);
                            item.setTextSize(18f);
                            listContainer.addView(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

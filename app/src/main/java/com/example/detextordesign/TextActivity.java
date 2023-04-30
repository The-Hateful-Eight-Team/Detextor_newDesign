package com.example.detextordesign;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TextActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener  {
    //private final static String FILENAME = "sample.txt"; // имя файла
    private EditText outText;
    SQLiteDatabase db;
    DatabaseHelper sqlHelper;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private static final int SELECT_PICTURE = 1;
    private String url = "https://detextor.community.saturnenterprise.io/";
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private String res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        String res = getIntent().getStringExtra("res");
        sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();
        outText = findViewById(R.id.editText);
        outText.setText(res);
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TEXT, outText.getText().toString());
        cv.put(DatabaseHelper.COLUMN_FAV, 0);
        db.insert(DatabaseHelper.TABLE, null, cv);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
// to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.bt_favourites).setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {

                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COLUMN_TEXT, outText.getText().toString());
                cv.put(DatabaseHelper.COLUMN_FAV, 1);
                db.insert(DatabaseHelper.TABLE, null, cv);

                Intent intent = new Intent(TextActivity.this, FavoritesActivity.class);
                startActivity(intent);

            }
        });
        findViewById(R.id.bt_save).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("label", outText.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(TextActivity.this, "The text is copied to the clipboard", Toast.LENGTH_SHORT).show();
            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
// Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.gal:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), SELECT_PICTURE);
                return true;
            case R.id.scan:
                Intent intent2 = new Intent(TextActivity.this, MainActivity.class);
                startActivity(intent2);
                return true;

            case R.id.fav:
                Intent intent3 = new Intent(TextActivity.this, FavoritesActivity.class);
                startActivity(intent3);
                return true;
            case R.id.his:
                Intent intent5 = new Intent(TextActivity.this, HistoryActivity.class);
                startActivity(intent5);
                return true;
            case R.id.about:
                Intent intent6 = new Intent(TextActivity.this, AboutActivity.class);
                startActivity(intent6);
                return true;

        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE) {
            assert data != null;
            Uri uri = data.getData();
            try {
                sendRequest(url, uri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }

    }

    private RequestBody buildRequestBody(Uri uri) throws FileNotFoundException {
        InputStream imageStream = getContentResolver().openInputStream(uri);
        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        postBodyString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();

        ArrayList<Integer> img_size = new ArrayList<Integer>();


        img_size.add(selectedImage.getHeight());
        img_size.add(selectedImage.getWidth());
        img_size.add(3);

        JSONArray jsArray = new JSONArray(img_size);

        try {
            jsonObject.put("image", postBodyString);
            jsonObject.put("image_size", jsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String json = jsonObject.toString();
        mediaType = MediaType.parse("application/json");
        requestBody = RequestBody.create(json, mediaType);
        System.out.println("==============================REQUEST BODY===========================================");
        System.out.println(requestBody);
        return requestBody;
    }

    private void sendRequest(String URL , Uri uri) throws FileNotFoundException {
        RequestBody requestBody = buildRequestBody(uri);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(1000, TimeUnit.SECONDS)
                .build();
        Request request = new Request
                .Builder()
                .post(requestBody)
                //.get()
                .url(URL + "process_img")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        Toast.makeText(TextActivity.this, "Something went wrong:" + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        System.out.println("Something went wrong:" + " " + e.getMessage());
                        call.cancel();


                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Toast.makeText(TextActivity.this, "Recognition is done", Toast.LENGTH_LONG).show();
                            String answer = Objects.requireNonNull(response.body()).string();
                            System.out.println("========================================ANSWER===========================================");
                            System.out.println(answer);
                            res = answer;
                            Intent intent6 = new Intent(TextActivity.this, TextActivity.class);
                            intent6.putExtra("res", res);
                            startActivity(intent6);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
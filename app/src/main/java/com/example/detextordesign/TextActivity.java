package com.example.detextordesign;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TextActivity extends AppCompatActivity {
    //private final static String FILENAME = "sample.txt"; // имя файла
    private EditText outText;
    SQLiteDatabase db;
    DatabaseHelper sqlHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        String res = getIntent().getStringExtra("res");
        sqlHelper = new DatabaseHelper(this);
        db = sqlHelper.getWritableDatabase();
        outText = findViewById(R.id.editText);
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COLUMN_TEXT, outText.getText().toString());
        cv.put(DatabaseHelper.COLUMN_FAV, 0);
        db.insert(DatabaseHelper.TABLE, null, cv);

        findViewById(R.id.bt_back).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(TextActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

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
}
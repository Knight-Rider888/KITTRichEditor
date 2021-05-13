package com.sample.editor;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import knight.rider.kitt.rich.OnInsertImageIconClickListener;
import knight.rider.kitt.rich.RichTextEditor;
import knight.rider.kitt.rich.RichWebView;

public class MainActivity extends AppCompatActivity {

    private RichTextEditor edit;

    RichWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.e);

        webView.setHint("当前无发布内容....");
        webView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "html:" + webView.getHtmlText(), Toast.LENGTH_SHORT).show();
            }
        });

        edit = findViewById(R.id.edit);
        edit.setOnInsertImageListener(new OnInsertImageIconClickListener() {
            @Override
            public void insertClick() {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        edit.onResume();
    }

    public void click(View view) {
        webView.setVisibility(View.GONE);
    }
}
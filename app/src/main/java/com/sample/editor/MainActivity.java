package com.sample.editor;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import knight.rider.kitt.rich.RichWebView;
import knight.rider.kitt.rich.XRichTextEditor;

public class MainActivity extends AppCompatActivity {

    private RichWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView =  findViewById(R.id.e);
        webView.setHint("当前无发布内容....");

//        String htmlText = "<p><img src=\"https://www.ourpyw.com/upload/20210513/162089224096807682300530911742.jpg\" alt=\"图像\"></p><p>哦哦哦fgfgfgrfgryrytbfoh Ohio后i和 hi hi hi hi预估i给</p><p><br></p>";
//        webView.setContent(htmlText);

    }
}
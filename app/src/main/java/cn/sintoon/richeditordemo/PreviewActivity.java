package cn.sintoon.richeditordemo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PreviewActivity extends AppCompatActivity {

    public static void start(Context context,String html){
        Intent intent = new Intent(context,PreviewActivity.class);
        intent.putExtra("html",html);
        context.startActivity(intent);
    }

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pewiew);
        webView = findViewById(R.id.web);
        WebSettings settings = webView.getSettings();
        //缩放操作
        settings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        settings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        settings.setDisplayZoomControls(false); //隐藏原生的缩放控件
        //自适应屏幕
//        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
//        settings.setUseWideViewPort(true);
//        settings.setLoadWithOverviewMode(true);
        //字符编码
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(true);
        String html = getIntent().getStringExtra("html");
        webView.loadDataWithBaseURL(null, html, "text/html; charset=UTF-8", "utf-8", null);

        webView.setWebViewClient(viewClient);
    }

    protected WebViewClient viewClient = new WebViewClient(){
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            webView.loadUrl(request.getUrl().toString());
            return true;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    };
}

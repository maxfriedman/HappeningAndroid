package city.happening.happening;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by Alex on 10/11/2015.
 */
public class WebActivity extends Activity {

    public static final String EXTRA_URL_ID = "happening.URL_ID";
    WebView mWebView;
    String mUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mUrl = (String)getIntent().getSerializableExtra(this.EXTRA_URL_ID);
        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        finish();
    }
}

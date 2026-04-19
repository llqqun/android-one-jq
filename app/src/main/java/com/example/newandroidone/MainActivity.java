package com.example.newandroidone;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ConsoleMessage;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newandroidone.cardreader.CardInfo;
import com.example.newandroidone.cardreader.CardReaderCallback;
import com.example.newandroidone.cardreader.CardReaderManager;

public class MainActivity extends AppCompatActivity implements CardReaderCallback {

    private WebView webView;
    private CardReaderManager cardReaderManager;
    private DisplayManager mDisplayManager;
    private MyPresentation myPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 隐藏标题栏
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        // 初始化显示管理器
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        // 检查是否有多个显示设备
        updateContents();

        cardReaderManager = new CardReaderManager(this);
        cardReaderManager.setCallback(this);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        webView.addJavascriptInterface(new JavaScriptInterface(), "android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // 对于本地文件，让 WebView 直接处理
                if (request.getUrl().toString().startsWith("file://")) {
                    return false;
                }
                // 对于其他 URL，使用 WebView 加载
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "加载错误: " + description, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Toast.makeText(MainActivity.this, "HTTP 错误: " + errorResponse.getStatusCode(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // 页面加载完成后执行
//                android.util.Log.d("WebView", "页面加载完成: " + url);
                // Toast.makeText(MainActivity.this, "页面加载完成", Toast.LENGTH_SHORT).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // 打印 JavaScript 控制台日志到 Android 日志
                String message = consoleMessage.message();
                String sourceId = consoleMessage.sourceId();
                int lineNumber = consoleMessage.lineNumber();
                ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
                
//                android.util.Log.d("WebView", "[" + level + "] " + message + " -- From line " + lineNumber + " of " + sourceId);
                return super.onConsoleMessage(consoleMessage);
            }
        });

        webView.loadUrl("file:///android_asset/index.html#/");
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cardReaderManager != null) {
            cardReaderManager.release();
        }
        if (myPresentation != null) {
            myPresentation.dismiss();
        }
        if (webView != null) {
            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webView.clearHistory();
            webView.clearCache(true);
            webView.destroy();
            webView = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // 获取显示设备并初始化双屏显示
    public void updateContents() {
        Display[] displays = mDisplayManager.getDisplays();
//        android.util.Log.d("MainActivity", "显示设备数量: " + displays.length);
        
        StringBuilder displayInfo = new StringBuilder();
        displayInfo.append("检测到 " + displays.length + " 个显示设备\n");
        
        for (int i = 0; i < displays.length; i++) {
            Display display = displays[i];
            displayInfo.append("屏幕" + i + ": ");
            displayInfo.append("名称=" + display.getName() + ", ");
            displayInfo.append("ID=" + display.getDisplayId() + ", ");
            displayInfo.append("尺寸=" + display.getWidth() + "x" + display.getHeight() + "\n");
//            android.util.Log.d("MainActivity", "屏幕" + i + ": " + display.getName() + ", ID=" + display.getDisplayId() + ", 尺寸=" + display.getWidth() + "x" + display.getHeight());
        }
        
        if (displays.length >= 2) {
//            android.util.Log.d("MainActivity", "准备在第二个屏幕上显示CvList页面");
            // 只有当myPresentation为null时才创建，避免重复创建
            if (myPresentation == null) {
//                Toast.makeText(this, displayInfo.toString(), Toast.LENGTH_LONG).show();
//                Toast.makeText(this, "准备显示第二个屏幕", Toast.LENGTH_SHORT).show();
                showPresentation(displays[1]);
            }
        } else {
            android.util.Log.d("MainActivity", "未检测到第二个屏幕，只有一个显示设备");
            if (myPresentation != null) {
                android.util.Log.d("MainActivity", "副屏存在但第二个屏幕已移除，关闭副屏");
                myPresentation.dismiss();
                myPresentation = null;
            }
            Toast.makeText(this, "只检测到1个屏幕，双屏功能不可用", Toast.LENGTH_LONG).show();
        }
    }

    // 将内容显示到第二个屏幕
    private void showPresentation(Display display) {
        try {
//            android.util.Log.d("MainActivity", "创建MyPresentation，Display ID: " + display.getDisplayId());
//            Toast.makeText(this, "正在创建第二个屏幕内容...", Toast.LENGTH_SHORT).show();
            
            if (myPresentation == null) {
                myPresentation = new MyPresentation(this, display);
//                android.util.Log.d("MainActivity", "MyPresentation实例已创建");
            }
            
            if (myPresentation != null) {
                myPresentation.show();
//                android.util.Log.d("MainActivity", "MyPresentation.show()调用成功");
//                Toast.makeText(this, "第二个屏幕已显示CvList页面", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.e("MainActivity", "MyPresentation实例为null");
//                Toast.makeText(this, "创建第二个屏幕失败: 实例为null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "显示第二个屏幕失败: " + e.getMessage());
            Toast.makeText(this, "显示失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceConnected(boolean success, String message) {
        String statusType = success ? "success" : "error";
        webView.evaluateJavascript("javascript:showStatus('" + (success ? "打开设备成功，" + message : "打开设备失败") + "', '" + statusType + "')", null);
    }

    @Override
    public void onDeviceDisconnected() {
        webView.evaluateJavascript("javascript:showStatus('设备断开连接', 'info')", null);
    }

    @Override
    public void onCardReadSuccess(CardInfo cardInfo) {
        String jsonData = "{" +
                "\"name\":\"" + cardInfo.getName() + "\"," +
                "\"sex\":\"" + cardInfo.getSex() + "\"," +
                "\"nation\":\"" + cardInfo.getNation() + "\"," +
                "\"birth\":\"" + cardInfo.getBirth() + "\"," +
                "\"idNumber\":\"" + cardInfo.getIdNumber() + "\"," +
                "\"address\":\"" + cardInfo.getAddress() + "\"," +
                "\"depart\":\"" + cardInfo.getDepart() + "\"," +
                "\"validity\":\"" + cardInfo.getValidity() + "\"," +
                "\"photo\":\"" + cardInfo.getPhotoBase64() + "\"" +
                "}";
        webView.evaluateJavascript("javascript:showStatus('读卡成功', 'success')", null);
        webView.evaluateJavascript("javascript:showIdCardInfo(" + jsonData + ")", null);
    }

    @Override
    public void onCardReadFail(String errorMessage) {
        webView.evaluateJavascript("javascript:showStatus('" + errorMessage + "', 'error')", null);
    }

    @Override
    public void onFirmwareVersion(String version) {
        if (version != null && !version.isEmpty()) {
            webView.evaluateJavascript("javascript:showStatus('固件版本号：" + version + "', 'success')", null);
        } else {
            webView.evaluateJavascript("javascript:showStatus('读取固件版本失败！', 'error')", null);
        }
    }

    public class JavaScriptInterface {
        @JavascriptInterface
        public void connectDevice(String serial) {
            cardReaderManager.setSerialName(serial);
            cardReaderManager.openDevice();
        }

        @JavascriptInterface
        public void disconnectDevice() {
            cardReaderManager.closeDevice();
        }

        @JavascriptInterface
        public void readCard() {
            cardReaderManager.readCard();
        }

        @JavascriptInterface
        public void getFirmwareVersion() {
            cardReaderManager.getFirmwareVersion();
        }

        @JavascriptInterface
        public void startAutoRead() {
            cardReaderManager.startAutoRead();
        }

        @JavascriptInterface
        public void stopAutoRead() {
            cardReaderManager.stopAutoRead();
        }

        @JavascriptInterface
        public String getScreenType() {
            // 主屏返回 "main", 副屏在 MyPresentation 中返回 "secondary"
            return "main";
        }
    }
}
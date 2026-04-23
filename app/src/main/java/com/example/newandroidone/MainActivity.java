package com.example.newandroidone;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
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
    private String samId = "";

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

        cardReaderManager = new CardReaderManager(this);
        cardReaderManager.setCallback(this);
        // 自动连接读卡器
        cardReaderManager.openDevice();

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        
        // 增加版本兼容性检查
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        
        // 适配不同Android版本的WebView设置
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

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
                android.util.Log.d("WebView", "页面加载完成: " + url);
                // 页面加载完成后，如果已经获取到SAMID，传递给webView
                if (!samId.isEmpty()) {
                    android.util.Log.d("WebView", "页面加载完成，传递SAMID: " + samId);
                    webView.evaluateJavascript("javascript:setDeviceId('" + samId + "')", null);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                // 忽略SSL错误，继续加载页面
                handler.proceed();
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
                
                android.util.Log.d("WebView", "[" + level + "] " + message + " -- From line " + lineNumber + " of " + sourceId);
                return super.onConsoleMessage(consoleMessage);
            }
        });

        webView.loadUrl("file:///android_asset/index.html#/?time=" + System.currentTimeMillis());
        
        // 初始化显示管理器
        mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        // 检查是否有多个显示设备
        updateContents();
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
        android.util.Log.d("MainActivity", "显示设备数量: " + displays.length);
        
        StringBuilder displayInfo = new StringBuilder();
        displayInfo.append("检测到 " + displays.length + " 个显示设备\n");
        
        for (int i = 0; i < displays.length; i++) {
            Display display = displays[i];
            displayInfo.append("屏幕" + i + ": ");
            displayInfo.append("名称=" + display.getName() + ", ");
            displayInfo.append("ID=" + display.getDisplayId() + ", ");
            displayInfo.append("尺寸=" + display.getWidth() + "x" + display.getHeight() + "\n");
            android.util.Log.d("MainActivity", "屏幕" + i + ": " + display.getName() + ", ID=" + display.getDisplayId() + ", 尺寸=" + display.getWidth() + "x" + display.getHeight());
        }
        
        if (displays.length >= 2) {
            android.util.Log.d("MainActivity", "准备在第二个屏幕上显示CvList页面");
            // 只有当myPresentation为null时才创建，避免重复创建
            if (myPresentation == null) {
                Toast.makeText(this, displayInfo.toString(), Toast.LENGTH_LONG).show();
                Toast.makeText(this, "准备显示第二个屏幕", Toast.LENGTH_SHORT).show();
                showPresentation(displays[1]);
            } else {
                android.util.Log.d("MainActivity", "myPresentation已存在，不需要重新创建");
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
            // android.util.Log.d("MainActivity", "创建MyPresentation，Display ID: " + display.getDisplayId());
            // Toast.makeText(this, "正在创建第二个屏幕内容...", Toast.LENGTH_SHORT).show();
            
            if (myPresentation == null) {
                myPresentation = new MyPresentation(this, display);
                // android.util.Log.d("MainActivity", "MyPresentation实例已创建");
            }
            
            if (myPresentation != null) {
                myPresentation.show();
                // android.util.Log.d("MainActivity", "MyPresentation.show()调用成功");
                // Toast.makeText(this, "第二个屏幕已显示CvList页面", Toast.LENGTH_SHORT).show();
            } else {
                // android.util.Log.e("MainActivity", "MyPresentation实例为null");
                // Toast.makeText(this, "创建第二个屏幕失败: 实例为null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // android.util.Log.e("MainActivity", "显示第二个屏幕失败: " + e.getMessage());
            // Toast.makeText(this, "显示失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onDeviceConnected(boolean success, String message) {
        String statusType = success ? "success" : "error";
        if (success && message.contains("SAMID: ")) {
            samId = message.substring(7); // 提取SAMID
            // 传递SAMID给webView
            // android.util.Log.d("MainActivity", "传递SAMID给主屏: " + samId);
            webView.evaluateJavascript("javascript:setDeviceId('" + samId + "')", null);
            // 如果副屏已创建，也传递给副屏
            // android.util.Log.d("MainActivity", "myPresentation是否为null: " + (myPresentation == null ? "是" : "否"));
            if (myPresentation != null) {
                // android.util.Log.d("MainActivity", "传递SAMID给副屏: " + samId);
                myPresentation.setDeviceId(samId);
            } else {
                // android.util.Log.e("MainActivity", "副屏未创建，无法传递SAMID");
            }
            // 自动开启自动读卡功能
            cardReaderManager.startAutoRead();
        }
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
        // 传递读卡信息给主屏幕
        webView.evaluateJavascript("javascript:showStatus('读卡成功', 'success')", null);
        webView.evaluateJavascript("javascript:showIdCardInfo(" + jsonData + ")", null);
        // 如果副屏已创建，也传递给副屏
        if (myPresentation != null) {
            myPresentation.showIdCardInfo(jsonData);
        }
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
    
    // 处理副屏登录成功的通知
    public void onSecondaryScreenLoginSuccess() {
        // 通知主屏的WebView登录成功
        // android.util.Log.d("MainActivity", "收到副屏登录成功通知");
        // Toast.makeText(this, "收到副屏登录成功通知", Toast.LENGTH_LONG).show();
        try {
            if (webView != null) {
                // Toast.makeText(this, "收到副屏登录成功通知:webView不为空", Toast.LENGTH_LONG).show();
                webView.evaluateJavascript("javascript:onSecondaryScreenLoginSuccess()", null);
                // Toast.makeText(this, "收到副屏登录成功通知:通知主屏WebView登录成功完成", Toast.LENGTH_LONG).show();
            } else {
                // Toast.makeText(this, "webView为null，无法通知主屏", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            // android.util.Log.e("MainActivity", "通知主屏WebView失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 处理从副屏发送过来的消息
    public void notifyMainScreenUpdateFromSecondary(final String event) {
        // 在主线程上执行操作
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (webView != null) {
                        // 转义特殊字符，确保JavaScript语法正确
                        String escapedEvent = escapeJavaScriptString(event);
                        final String jsCode = "javascript:onScreenUpdate('" + escapedEvent + "')";
                        
                        // 执行JavaScript代码
                        webView.evaluateJavascript(jsCode, null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    // 另一种方式：直接修改WebView的URL来实现跳转
    public void navigateToResumeSubmission() {
        // android.util.Log.d("MainActivity", "直接跳转到resumeSubmission页面");
        try {
            if (webView != null) {
                // 直接修改WebView的URL来实现跳转
                webView.loadUrl("file:///android_asset/index.html#/resumeSubmission");
                // android.util.Log.d("MainActivity", "直接跳转完成");
            } else {
                // android.util.Log.e("MainActivity", "webView为null，无法跳转");
            }
        } catch (Exception e) {
            // android.util.Log.e("MainActivity", "直接跳转失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 同步localStorage数据到两个WebView
    private void syncLocalStorageData(String key, String value) {
        try {
            // 转义特殊字符，确保JavaScript语法正确
            String escapedKey = escapeJavaScriptString(key);
            String escapedValue = escapeJavaScriptString(value);
            
            // 同步到主屏幕
            if (webView != null) {
                webView.evaluateJavascript("javascript:setLocalStorage('" + escapedKey + "', '" + escapedValue + "')", null);
            }
            // 同步到副屏
            if (myPresentation != null) {
                myPresentation.syncLocalStorage(key, value);
            }
        } catch (Exception e) {
            // android.util.Log.e("MainActivity", "同步localStorage数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 转义JavaScript字符串中的特殊字符
    private String escapeJavaScriptString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
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
        
        @JavascriptInterface
        public String getDeviceId() {
            // 返回设备ID（SAMID）
            return samId;
        }
        
        @JavascriptInterface
        public void syncLocalStorage(String key, String value) {
            // 同步localStorage数据到两个WebView
            syncLocalStorageData(key, value);
        }
        
        @JavascriptInterface
        public void notifySecondaryScreenUpdate(final String event) {
            // 在主线程上执行操作
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // 通知副屏更新数据
                        if (myPresentation != null) {
                            myPresentation.notifyScreenUpdate(event);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        
        @JavascriptInterface
        public void hideKeyboard() {
            // 收起键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(webView.getWindowToken(), 0);
            }
        }
        
        @JavascriptInterface
        public void notifyMainScreenUpdate(final String event) {
            // 在主线程上执行操作
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(MainActivity.this, "开始发送消息到主屏", Toast.LENGTH_SHORT).show();
                        if (webView != null) {
                            Toast.makeText(MainActivity.this, "主屏WebView已初始化，正在发送消息", Toast.LENGTH_SHORT).show();
                            // 转义特殊字符，确保JavaScript语法正确
                            String escapedEvent = escapeJavaScriptString(event);
                            final String jsCode = "javascript:onScreenUpdate('" + escapedEvent + "')";
                            Toast.makeText(MainActivity.this, "执行JavaScript: " + jsCode, Toast.LENGTH_SHORT).show();
                            
                            // 执行JavaScript代码
                            webView.evaluateJavascript(jsCode, new android.webkit.ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                    if (value != null && !value.equals("null")) {
                                        Toast.makeText(MainActivity.this, "JavaScript执行结果: " + value, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "JavaScript执行成功", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Toast.makeText(MainActivity.this, "消息发送完成", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "主屏WebView未初始化，无法发送消息", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "发送消息时出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
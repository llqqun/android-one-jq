package com.example.newandroidone;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.JavascriptInterface;

public class MyPresentation extends Presentation {

    private static final String TAG = "---MyPresentation---";
    private WebView webView;
    // private TextView statusText;

    public MyPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        Log.d(TAG, "MyPresentation构造函数被调用");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MyPresentation onCreate开始");
        
        try {
            // 创建状态文本视图用于显示加载状态
            // statusText = new TextView(getContext());
            // statusText.setText("正在初始化第二个屏幕...\n显示页面: resumeSubmission");
            // statusText.setTextSize(18);
            // statusText.setPadding(50, 50, 50, 50);
            // statusText.setBackgroundColor(Color.WHITE);
            
            // 创建一个WebView来显示Vue页面
            webView = new WebView(getContext());
            // 配置WebView，确保与主屏幕独立
            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setAllowContentAccess(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            // 禁用缓存和历史记录，避免状态共享
            webSettings.setGeolocationEnabled(false);
            webSettings.setSaveFormData(false);
            // 创建一个独立的WebView，避免与主屏幕共享状态
            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();
            
            // 添加JavaScriptInterface
            webView.addJavascriptInterface(new JavaScriptInterface(), "android");
            
            // 设置WebViewClient监听页面加载状态
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                    try {
                        super.onPageStarted(view, url, favicon);
                        String msg = "页面开始加载: " + url;
                        Log.d(TAG, msg);
                        // if (statusText != null) {
                            // statusText.setText(msg);
                        // }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onPageStarted异常: " + e.getMessage());
                    }
                }
                
                @Override
                public void onPageFinished(WebView view, String url) {
                    try {
                        super.onPageFinished(view, url);
                        String msg = "页面加载完成: " + url;
                        Log.d(TAG, msg);
                        // if (statusText != null) {
                        //     statusText.setText("页面加载完成\n当前页面: resumeSubmission");
                        // }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onPageFinished异常: " + e.getMessage());
                    }
                }
                
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    try {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                        String msg = "页面加载错误: " + description + " (错误码: " + errorCode + ")";
                        Log.e(TAG, msg);
                        // if (statusText != null) {
                        //     statusText.setText("加载错误: " + description);
                        // }
                        if (getContext() != null) {
                            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onReceivedError异常: " + e.getMessage());
                    }
                }
            });
            
            // 设置WebChromeClient用于控制台日志
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                    try {
                        super.onConsoleMessage(message, lineNumber, sourceID);
                        Log.d(TAG, "WebConsole: " + message + " (行: " + lineNumber + ")");
                    } catch (Exception e) {
                        Log.e(TAG, "onConsoleMessage异常: " + e.getMessage());
                    }
                }
            });
            
            // 设置布局
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.VERTICAL);
            // layout.addView(statusText);
            layout.addView(webView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            ));
            setContentView(layout);
            
            Log.d(TAG, "MyPresentation onCreate完成");
            if (getContext() != null) {
                Toast.makeText(getContext(), "第二个屏幕Presentation已创建", Toast.LENGTH_SHORT).show();
            }
            
            // 初始加载home页面
            loadHomePage();
        } catch (Exception e) {
            Log.e(TAG, "MyPresentation onCreate异常: " + e.getMessage());
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "第二个屏幕初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // 加载home页面
    private void loadHomePage() {
        try {
            // 为了避免与主屏幕冲突，添加一个唯一的时间戳参数
            String uniqueParam = "t=" + System.currentTimeMillis();
            // 加载home页面
            String url = "file:///android_asset/index.html#/?" + uniqueParam;
            String msg = "准备加载: " + url;
            Log.d(TAG, msg);
            // if (statusText != null) {
            //     statusText.setText(msg);
            // }
            
            if (webView != null) {
                webView.loadUrl(url);
                Log.d(TAG, "loadUrl调用成功");
            } else {
                String errorMsg = "WebView未初始化";
                Log.e(TAG, errorMsg);
                // if (statusText != null) {
                //     statusText.setText(errorMsg);
                // }
                if (getContext() != null) {
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            String errorMsg = "加载失败: " + e.getMessage();
            Log.e(TAG, errorMsg);
            // if (statusText != null) {
            //     statusText.setText(errorMsg);
            // }
            if (getContext() != null) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        Log.d(TAG, "MyPresentation dismiss");
        if (webView != null) {
            Log.d(TAG, "Destroying WebView");
            webView.destroy();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MyPresentation onStart");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MyPresentation onStop");
    }

    public class JavaScriptInterface {
        @JavascriptInterface
        public String getScreenType() {
            // 副屏返回 "secondary"
            return "secondary";
        }
    }
}
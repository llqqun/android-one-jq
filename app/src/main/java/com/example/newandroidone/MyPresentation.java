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
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.JavascriptInterface;
import android.view.inputmethod.InputMethodManager;
import android.app.Activity;

public class MyPresentation extends Presentation {

    private static final String TAG = "---MyPresentation---";
    private WebView webView;
    // private TextView statusText;
    private String deviceId = "";
    private MainActivity mainActivity;

    // 构造函数，初始化副屏Presentation
    public MyPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        // Log.d(TAG, "MyPresentation构造函数被调用");
        // 保存MainActivity实例
        if (outerContext instanceof MainActivity) {
            mainActivity = (MainActivity) outerContext;
            // Log.d(TAG, "成功保存MainActivity实例");
        } else {
            // Log.e(TAG, "outerContext不是MainActivity实例");
        }
    }

    // Presentation创建时的回调方法，初始化副屏WebView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Log.d(TAG, "MyPresentation onCreate开始");
        
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
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
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
                        // Log.d(TAG, msg);
                        // if (statusText != null) {
                            // statusText.setText(msg);
                        // }
                        if (getContext() != null) {
                            // Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Log.e(TAG, "onPageStarted异常: " + e.getMessage());
                    }
                }
                
                @Override
                public void onPageFinished(WebView view, String url) {
                    try {
                        super.onPageFinished(view, url);
                        String msg = "页面加载完成: " + url;
                        // Log.d(TAG, msg);
                        // if (statusText != null) {
                        //     statusText.setText("页面加载完成\n当前页面: resumeSubmission");
                        // }
                        if (getContext() != null) {
                            // Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        // Log.e(TAG, "onPageFinished异常: " + e.getMessage());
                    }
                }
                
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    try {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                        String msg = "页面加载错误: " + description + " (错误码: " + errorCode + ")";
                        // Log.e(TAG, msg);
                        // if (statusText != null) {
                        //     statusText.setText("加载错误: " + description);
                        // }
                        if (getContext() != null) {
                            // Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        // Log.e(TAG, "onReceivedError异常: " + e.getMessage());
                    }
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    // 忽略SSL错误，继续加载页面
                    handler.proceed();
                }
            });
            
            // 设置WebChromeClient用于控制台日志
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                    try {
                        super.onConsoleMessage(message, lineNumber, sourceID);
                        // Log.d(TAG, "WebConsole: " + message + " (行: " + lineNumber + ")");
                    } catch (Exception e) {
                        // Log.e(TAG, "onConsoleMessage异常: " + e.getMessage());
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
            
            // Log.d(TAG, "MyPresentation onCreate完成");
            if (getContext() != null) {
                // Toast.makeText(getContext(), "第二个屏幕Presentation已创建", Toast.LENGTH_SHORT).show();
            }
            
            // 初始加载home页面
            loadHomePage();
        } catch (Exception e) {
            // Log.e(TAG, "MyPresentation onCreate异常: " + e.getMessage());
            e.printStackTrace();
            if (getContext() != null) {
                // Toast.makeText(getContext(), "第二个屏幕初始化失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
            // Log.d(TAG, msg);
            // if (statusText != null) {
            //     statusText.setText(msg);
            // }
            
            if (webView != null) {
                webView.loadUrl(url);
                // Log.d(TAG, "loadUrl调用成功");
            } else {
                String errorMsg = "WebView未初始化";
                // Log.e(TAG, errorMsg);
                // if (statusText != null) {
                //     statusText.setText(errorMsg);
                // }
                if (getContext() != null) {
                    // Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            String errorMsg = "加载失败: " + e.getMessage();
            // Log.e(TAG, errorMsg);
            // if (statusText != null) {
            //     statusText.setText(errorMsg);
            // }
            if (getContext() != null) {
                // Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
            e.printStackTrace();
        }
    }

    // 关闭副屏Presentation
    @Override
    public void dismiss() {
        super.dismiss();
        // Log.d(TAG, "MyPresentation dismiss");
        if (webView != null) {
            // Log.d(TAG, "Destroying WebView");
            webView.destroy();
        }
    }
    
    // Presentation启动时的回调方法
    @Override
    protected void onStart() {
        super.onStart();
        // Log.d(TAG, "MyPresentation onStart");
    }
    
    // Presentation停止时的回调方法
    @Override
    protected void onStop() {
        super.onStop();
        // Log.d(TAG, "MyPresentation onStop");
    }
    
    // 设置设备ID（SAMID）
    public void setDeviceId(String id) {
        // Log.d(TAG, "收到设备ID: " + id);
        deviceId = id;
        // 传递设备ID给webView
        // Log.d(TAG, "webView是否为null: " + (webView == null ? "是" : "否"));
        if (webView != null) {
            // Log.d(TAG, "传递设备ID给副屏WebView: " + id);
            webView.evaluateJavascript("javascript:setDeviceId('" + id + "')", null);
            // Log.d(TAG, "传递设备ID给副屏WebView完成");
        } else {
            // Log.e(TAG, "webView为null，无法传递设备ID给副屏");
        }
    }
    
    // 显示身份证信息
    public void showIdCardInfo(String jsonData) {
        // 传递读卡信息给副屏
        if (webView != null) {
            webView.evaluateJavascript("javascript:showStatus('读卡成功', 'success')", null);
            webView.evaluateJavascript("javascript:showIdCardInfo(" + jsonData + ")", null);
        }
    }
    
    // 同步localStorage数据
    public void syncLocalStorage(String key, String value) {
        try {
            // 转义特殊字符，确保JavaScript语法正确
            String escapedKey = escapeJavaScriptString(key);
            String escapedValue = escapeJavaScriptString(value);
            
            // 同步到副屏
            if (webView != null) {
                webView.evaluateJavascript("javascript:setLocalStorage('" + escapedKey + "', '" + escapedValue + "')", null);
            }
        } catch (Exception e) {
            // Log.e(TAG, "同步localStorage数据失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 同步localStorage数据到副屏
    private void syncLocalStorageData(String key, String value) {
        try {
            // 转义特殊字符，确保JavaScript语法正确
            String escapedKey = escapeJavaScriptString(key);
            String escapedValue = escapeJavaScriptString(value);
            
            // 同步到副屏
            if (webView != null) {
                webView.evaluateJavascript("javascript:setLocalStorage('" + escapedKey + "', '" + escapedValue + "')", null);
            }
        } catch (Exception e) {
            // Log.e(TAG, "同步localStorage数据失败: " + e.getMessage());
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
    
    // 通知主屏登录成功
    public void notifyMainScreenLoginSuccessFromSecondary() {
        // 通过回调通知主屏
        // Toast.makeText(getContext(), "通知主屏登录成功:notifyMainScreenLoginSuccessFromSecondary", Toast.LENGTH_LONG).show();
        // Log.d(TAG, "开始通知主屏登录成功");
        try {
            // 使用保存的MainActivity实例
            if (mainActivity != null) {
                // Toast.makeText(getContext(), "使用保存的MainActivity实例", Toast.LENGTH_LONG).show();
                // Log.d(TAG, "使用保存的MainActivity实例");
                // Log.d(TAG, "调用mainActivity.onSecondaryScreenLoginSuccess()");
                mainActivity.onSecondaryScreenLoginSuccess();
                // Log.d(TAG, "通知主屏登录成功完成");
            } else {
                // Log.e(TAG, "mainActivity实例为null，无法通知主屏");
                // 尝试通过getContext()获取
                Context context = getContext();
                // Log.d(TAG, "Context类型: " + (context != null ? context.getClass().getName() : "null"));
                
                if (context instanceof MainActivity) {
                    // Toast.makeText(getContext(), "Context是MainActivity实例", Toast.LENGTH_LONG).show();
                    // Log.d(TAG, "Context是MainActivity实例");
                    MainActivity activity = (MainActivity) context;
                    // Log.d(TAG, "调用activity.onSecondaryScreenLoginSuccess()");
                    activity.onSecondaryScreenLoginSuccess();
                    // Log.d(TAG, "通知主屏登录成功完成");
                    
                    // 额外调用直接跳转方法，作为备用方案
                    // Log.d(TAG, "调用备用方案: activity.navigateToResumeSubmission()");
                    activity.navigateToResumeSubmission();
                    // Log.d(TAG, "备用方案执行完成");
                } else {
                    // Toast.makeText(getContext(), "Context不是MainActivity实例，无法通知主屏", Toast.LENGTH_LONG).show();
                    // Log.e(TAG, "Context不是MainActivity实例，无法通知主屏");
                    // 尝试另一种方式获取MainActivity实例
                    if (context != null) {
                        Activity activity = (Activity) context;
                        if (activity.getParent() instanceof MainActivity) {
                            // Log.d(TAG, "通过getParent()获取到MainActivity实例");
                            MainActivity parentActivity = (MainActivity) activity.getParent();
                            parentActivity.onSecondaryScreenLoginSuccess();
                            // Log.d(TAG, "通知主屏登录成功完成");
                            
                            // 额外调用直接跳转方法，作为备用方案
                            // Log.d(TAG, "调用备用方案: parentActivity.navigateToResumeSubmission()");
                            parentActivity.navigateToResumeSubmission();
                            // Log.d(TAG, "备用方案执行完成");
                        } else {
                            // Log.e(TAG, "无法获取MainActivity实例");
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log.e(TAG, "通知主屏登录成功失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 接收主屏的通知并更新数据
    public void notifyScreenUpdate(String event) {
        // 通知副屏的WebView更新数据
        if (webView != null) {
            // 转义特殊字符，确保JavaScript语法正确
            String escapedEvent = escapeJavaScriptString(event);
            final String jsCode = "javascript:onScreenUpdate('" + escapedEvent + "')";
            
            // 检查webView是否可用
            if (webView != null) {
                // 在主线程上执行WebView操作
                if (mainActivity != null) {
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 执行JavaScript代码
                                webView.evaluateJavascript(jsCode, null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }
    }

    // JavaScript接口类，用于副屏WebView与Android原生代码通信
    public class JavaScriptInterface {
        // 获取屏幕类型（副屏返回"secondary"）
        @JavascriptInterface
        public String getScreenType() {
            // 副屏返回 "secondary"
            return "secondary";
        }
        
        // 获取设备ID（SAMID）
        @JavascriptInterface
        public String getDeviceId() {
            // 返回设备ID（SAMID）
            return deviceId;
        }
        
        // 同步localStorage数据到副屏
        @JavascriptInterface
        public void syncLocalStorage(String key, String value) {
            // 同步localStorage数据到副屏
            syncLocalStorageData(key, value);
        }
        
        // 通知主屏登录成功
        @JavascriptInterface
        public void notifyMainScreenLoginSuccess() {
            // Toast.makeText(getContext(), "通知主屏登录成功::notifyMainScreenLoginSuccess", Toast.LENGTH_LONG).show();
            // 通知主屏登录成功
            // Log.d(TAG, "JavaScript调用notifyMainScreenLoginSuccess");
            notifyMainScreenLoginSuccessFromSecondary();
        }

        // 收起键盘
        @JavascriptInterface
        public void hideKeyboard() {
            // 收起键盘
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(webView.getWindowToken(), 0);
            }
        }
        
        // 通知主屏更新数据
        @JavascriptInterface
        public void notifyMainScreenUpdate(final String event) {
            // 通知主屏更新数据
            if (mainActivity != null) {
                // 调用MainActivity的方法来发送消息到主屏
                mainActivity.notifyMainScreenUpdateFromSecondary(event);
            }
        }
    }
}
package com.example.newandroidone.qrcode;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 二维码读卡器管理器
 */
public class QRCodeReaderManager {
    private static final String TAG = "QRCodeReaderManager";
    private static final String DEFAULT_SERIAL_NAME = "/dev/ttyS4";
    private static final int DEFAULT_BAUD_RATE = 9600;
    private static final int MIN_QR_CODE_LENGTH = 8;

    private Context context;
    private QRCodeCallback callback;
    private Handler mainHandler;
    private ReadThread readThread;
    private boolean isReading;
    private boolean isConnected;
    private String serialName;
    private int baudRate;
    private InputStream inputStream;
    private OutputStream outputStream;
    private File logFile;
    private FileWriter logWriter;
    private SimpleDateFormat dateFormat;

    /**
     * 二维码读取回调接口
     */
    public interface QRCodeCallback {
        /**
         * 二维码读取成功回调
         * @param qrCodeData 二维码数据
         */
        void onQRCodeRead(String qrCodeData);
        
        /**
         * 设备连接状态回调
         * @param success 是否连接成功
         * @param message 连接消息
         */
        void onDeviceConnected(boolean success, String message);
        
        /**
         * 设备断开连接回调
         */
        void onDeviceDisconnected();
        
        /**
         * 错误回调
         * @param errorMessage 错误信息
         */
        void onError(String errorMessage);
    }

    /**
     * 构造函数，初始化二维码读卡器管理器
     * @param context Android上下文
     */
    public QRCodeReaderManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.serialName = DEFAULT_SERIAL_NAME;
        this.baudRate = DEFAULT_BAUD_RATE;
        this.isReading = false;
        this.isConnected = false;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        initLogFile();
    }

    /**
     * 初始化日志文件
     */
    private void initLogFile() {
        try {
            File logDir = new File(Environment.getExternalStorageDirectory(), "QRCodeLogs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            String logFileName = "qrcode_log_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            logFile = new File(logDir, logFileName);
            logWriter = new FileWriter(logFile, true);
            writeLog("=== 二维码日志文件初始化完成 ===");
            writeLog("日志文件路径: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "初始化日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 写入日志
     * @param message 日志消息
     */
    private void writeLog(String message) {
        try {
            if (logWriter != null) {
                String timestamp = dateFormat.format(new Date());
                logWriter.write("[" + timestamp + "] " + message + "\n");
                logWriter.flush();
            }
        } catch (Exception e) {
            Log.e(TAG, "写入日志失败: " + e.getMessage());
        }
    }

    /**
     * 调试日志，同时输出到Logcat和日志文件
     * @param message 日志消息
     */
    private void logDebug(String message) {
        Log.d(TAG, message);
        writeLog(message);
    }

    /**
     * 错误日志，同时输出到Logcat和日志文件
     * @param message 错误消息
     * @param e 异常对象
     */
    private void logError(String message, Throwable e) {
        Log.e(TAG, message, e);
        String errorLog = message;
        if (e != null) {
            errorLog += " | " + e.toString();
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (int i = 0; i < Math.min(3, stackTrace.length); i++) {
                errorLog += "\n    at " + stackTrace[i].toString();
            }
        }
        writeLog("[ERROR] " + errorLog);
    }

    /**
     * 设置回调接口
     * @param callback 回调接口
     */
    public void setCallback(QRCodeCallback callback) {
        this.callback = callback;
    }

    /**
     * 设置串口名称
     * @param serialName 串口名称
     */
    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }

    /**
     * 设置波特率
     * @param baudRate 波特率
     */
    public void setBaudRate(int baudRate) {
        this.baudRate = baudRate;
    }

    /**
     * 检查设备是否已连接
     * @return 是否已连接
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * 检查是否正在读取
     * @return 是否正在读取
     */
    public boolean isReading() {
        return isReading;
    }

    /**
     * 打开二维码设备（异步）
     */
    public void openDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doOpenDevice();
            }
        }).start();
    }

    /**
     * 实际执行打开设备操作
     */
    private void doOpenDevice() {
        try {
            File serialFile = new File(serialName);
            logDebug("尝试打开串口: " + serialName);
            
            if (!serialFile.exists()) {
                logDebug("串口设备不存在: " + serialName);
                notifyConnected(false, "串口设备不存在: " + serialName);
                return;
            }

            logDebug("设置串口权限...");
            setSerialPortPermissions(serialFile);
            
            logDebug("设置串口参数...");
            setSerialPortSettings();

            logDebug("打开串口输入流...");
            inputStream = new FileInputStream(serialFile);
            logDebug("打开串口输出流...");
            outputStream = new FileOutputStream(serialFile);
            isConnected = true;

            logDebug("串口打开成功: " + serialName + ", 波特率: " + baudRate);
            notifyConnected(true, "串口连接成功");

        } catch (Exception e) {
            logError("打开串口失败: " + e.getMessage(), e);
            isConnected = false;
            notifyConnected(false, "打开串口失败: " + e.getMessage());
        }
    }

    /**
     * 设置串口权限
     * @param serialFile 串口文件
     */
    private void setSerialPortPermissions(File serialFile) {
        try {
            Process process = Runtime.getRuntime().exec("chmod 666 " + serialFile.getAbsolutePath());
            process.waitFor();
            logDebug("串口权限设置完成: " + serialFile.getAbsolutePath());
        } catch (Exception e) {
            logError("设置串口权限失败: " + e.getMessage(), e);
        }
    }
    /**
     * 设置串口参数
     * 波特率、8位数据、无校验位、1停止位、原始模式，避免回显
     */
    private void setSerialPortSettings() {
        try {
            String[] commands = {
                "stty -F " + serialName + " " + baudRate,
                "stty -F " + serialName + " -echo",
                "stty -F " + serialName + " cs8",
                "stty -F " + serialName + " -cstopb",
                "stty -F " + serialName + " -parenb",
                "stty -F " + serialName + " raw"
            };

            for (String command : commands) {
                logDebug("执行命令: " + command);
                Process process = Runtime.getRuntime().exec(command);
                process.waitFor();
            }
            logDebug("串口参数设置完成");
        } catch (Exception e) {
            logError("设置串口参数失败: " + e.getMessage(), e);
        }
    }

    /**
     * 关闭二维码设备（异步）
     */
    public void closeDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doCloseDevice();
            }
        }).start();
    }

    /**
     * 实际执行关闭设备操作
     */
    private void doCloseDevice() {
        stopReading();
        
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            inputStream = null;
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        isConnected = false;
        notifyDisconnected();
        Log.d(TAG, "串口已关闭");
    }

    /**
     * 开始读取二维码
     */
    public void startReading() {
        if (!isConnected) {
            notifyError("设备未连接");
            return;
        }

        if (isReading) {
            logDebug("已经在读取状态");
            return;
        }

        isReading = true;
        readThread = new ReadThread();
        readThread.start();
        notifyError("开始监听二维码输入...");
    }

    /**
     * 停止读取二维码
     */
    public void stopReading() {
        isReading = false;
        if (readThread != null) {
            readThread.interrupt();
            readThread = null;
        }
        logDebug("停止监听二维码输入");
    }

    /**
     * 读取线程类，负责从串口读取二维码数据
     */
    private class ReadThread extends Thread {
        private StringBuilder buffer = new StringBuilder();
        private String lastQRCodeData = "";
        private long lastReadTime = 0;
        private static final long READ_COOLDOWN_MS = 2000;

        @Override
        public void run() {
            byte[] readBuffer = new byte[1024];
            int bytesRead;

            logDebug("读取线程启动，开始监听串口数据...");
            while (isReading && isConnected && inputStream != null) {
                try {
                    logDebug("等待读取数据...");
                    bytesRead = inputStream.read(readBuffer);
                    logDebug("读取到字节数: " + bytesRead);
                    if (bytesRead > 0) {
                        String data = new String(readBuffer, 0, bytesRead);
                        logDebug("原始数据: [" + data + "]");
                        // 处理数据
                        processData(data);
                    } else if (bytesRead == 0) {
                        logDebug("读取到0字节数据");
                    } else {
                        logDebug("输入流已关闭");
                        break;
                    }
                } catch (java.io.InterruptedIOException e) {
                    logDebug("读取线程被中断");
                    break;
                } catch (Exception e) {
                    logError("读取数据失败: " + e.getMessage(), e);
                    notifyError("读取数据失败: " + e.getMessage());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }
            isReading = false;
            logDebug("读取线程退出");
        }

        /**
         * 处理接收到的数据，提取二维码信息
         * @param data 原始数据
         */
        private void processData(String data) {
            buffer.append(data);
            logDebug("缓冲区内容: [" + buffer.toString() + "]");
            
            int endIndex = buffer.indexOf("\n");
            if (endIndex == -1) {
                endIndex = buffer.indexOf("\r");
            }
            logDebug("换行符位置: " + endIndex);
            
            if (endIndex != -1) {
                String qrCodeData = buffer.substring(0, endIndex).trim();
                buffer.delete(0, endIndex + 1);
                
                logDebug("提取的数据: [" + qrCodeData + "]，长度: " + qrCodeData.length());
                
                if (qrCodeData.length() >= MIN_QR_CODE_LENGTH) {
                    long currentTime = System.currentTimeMillis();
                    logDebug("当前时间: " + currentTime + ", 上次时间: " + lastReadTime + ", 间隔: " + (currentTime - lastReadTime));
                    
                    if (qrCodeData.equals(lastQRCodeData) && (currentTime - lastReadTime) < READ_COOLDOWN_MS) {
                        logDebug("重复数据，过滤: " + qrCodeData);
                        return;
                    }
                    
                    lastQRCodeData = qrCodeData;
                    lastReadTime = currentTime;
                    logDebug("二维码数据读取完成: " + qrCodeData);
                    notifyQRCodeRead(qrCodeData);
                } else {
                    logDebug("数据长度不足最小要求(" + MIN_QR_CODE_LENGTH + ")，忽略");
                }
            }
            
            if (buffer.length() > 1024) {
                buffer.setLength(0);
                logDebug("缓冲区溢出，已清空");
            }
        }
    }

    /**
     * 通知连接状态
     * @param success 是否连接成功
     * @param message 连接消息
     */
    private void notifyConnected(final boolean success, final String message) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onDeviceConnected(success, message);
                }
            }
        });
    }

    /**
     * 通知设备断开连接
     */
    private void notifyDisconnected() {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onDeviceDisconnected();
                }
            }
        });
    }

    /**
     * 通知二维码读取成功
     * @param qrCodeData 二维码数据
     */
    private void notifyQRCodeRead(final String qrCodeData) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onQRCodeRead(qrCodeData);
                }
            }
        });
    }

    /**
     * 通知错误信息
     * @param errorMessage 错误信息
     */
    private void notifyError(final String errorMessage) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
        });
    }

    /**
     * 释放资源
     */
    public void release() {
        stopReading();
        closeDevice();
        callback = null;
        closeLogFile();
    }

    /**
     * 关闭日志文件
     */
    private void closeLogFile() {
        try {
            if (logWriter != null) {
                writeLog("=== 日志文件关闭 ===");
                logWriter.close();
                logWriter = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "关闭日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志文件路径
     * @return 日志文件路径
     */
    public String getLogFilePath() {
        if (logFile != null && logFile.exists()) {
            return logFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * 获取日志内容
     * @return 日志内容
     */
    public String getLogContent() {
        try {
            if (logFile != null && logFile.exists()) {
                StringBuilder content = new StringBuilder();
                BufferedReader reader = new BufferedReader(new java.io.FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                return content.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, "读取日志内容失败: " + e.getMessage());
        }
        return "日志文件不存在或读取失败";
    }

    /**
     * 清空日志文件
     */
    public void clearLog() {
        try {
            if (logWriter != null) {
                logWriter.close();
                logWriter = null;
            }
            if (logFile != null && logFile.exists()) {
                logWriter = new FileWriter(logFile, false); // false表示覆盖，清空文件
                writeLog("=== 日志文件已清空 ===");
                writeLog("日志文件路径: " + logFile.getAbsolutePath());
                logDebug("二维码日志文件已清空");
            }
        } catch (Exception e) {
            logError("清空日志文件失败: " + e.getMessage(), e);
        }
    }
}

package com.example.newandroidone.cardreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.core.utils.ToolUtils;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.IDCardType;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.module.idcard.meta.IDPRPCardInfo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CardReaderManager {
    private static final String TAG = "CardReaderManager";
    private IDCardReader idCardReader = null;
    private boolean bStarted = false;
    private boolean autoReading = false;
    private String serialName = "/dev/ttyS3";
    private Context context;
    private CardReaderCallback callback;
    private Handler mainHandler;
    private AutoReadThread autoReadThread;

    // 日志相关
    private File logFile;
    private FileWriter logWriter;
    private SimpleDateFormat dateFormat;

    /**
     * 构造函数，初始化读卡器管理器
     * 
     * @param context Android上下文
     */
    public CardReaderManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        initLogFile();
    }

    /**
     * 初始化日志文件
     */
    private void initLogFile() {
        try {
            File logDir = new File(Environment.getExternalStorageDirectory(), "CardReaderLogs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            String logFileName = "cardreader_log_"
                    + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
            logFile = new File(logDir, logFileName);
            logWriter = new FileWriter(logFile, true);
            writeLog("=== 读卡器日志文件初始化完成 ===");
            writeLog("日志文件路径: " + logFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "初始化日志文件失败: " + e.getMessage());
        }
    }

    /**
     * 写入日志
     * 
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
     * 
     * @param message 日志消息
     */
    private void logDebug(String message) {
        Log.d(TAG, message);
        writeLog(message);
    }

    /**
     * 错误日志，同时输出到Logcat和日志文件
     * 
     * @param message 错误消息
     * @param e       异常对象
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
     * 获取日志文件路径
     * 
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
     * 
     * @return 日志内容
     */
    public String getLogContent() {
        try {
            if (logFile != null && logFile.exists()) {
                StringBuilder content = new StringBuilder();
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
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
                logDebug("日志文件已清空");
            }
        } catch (Exception e) {
            logError("清空日志文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 设置回调接口
     * 
     * @param callback 回调接口
     */
    public void setCallback(CardReaderCallback callback) {
        this.callback = callback;
    }

    /**
     * 设置串口名称
     * 
     * @param serialName 串口名称
     */
    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }

    /**
     * 检查设备是否已连接
     * 
     * @return 是否已连接
     */
    public boolean isConnected() {
        return bStarted;
    }

    /**
     * 初始化身份证读卡器
     */
    private void startIDCardReader() {
        if (null != idCardReader) {
            IDCardReaderFactory.destroy(idCardReader);
            idCardReader = null;
        }
        LogHelper.setLevel(Log.VERBOSE);
        Map idrparams = new HashMap();
        idrparams.put(ParameterHelper.PARAM_SERIAL_SERIALNAME, serialName);
        idrparams.put(ParameterHelper.PARAM_SERIAL_BAUDRATE, 115200);
        idCardReader = IDCardReaderFactory.createIDCardReader(context, TransportType.SERIALPORT, idrparams);
    }

    /**
     * 打开读卡器设备（异步）
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
        logDebug("开始打开读卡器设备...");
        startIDCardReader();
        try {
            logDebug("尝试打开读卡器: " + serialName);
            idCardReader.open(0);
            bStarted = true;
            final String samid = idCardReader.getSAMID(0);
            logDebug("读卡器打开成功，SAMID: " + samid);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onDeviceConnected(true, "SAMID: " + samid);
                    }
                }
            });
        } catch (IDCardReaderException e) {
            logError("打开读卡器失败: " + e.getMessage(), e);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onDeviceConnected(false, "打开设备失败");
                    }
                }
            });
        }
    }

    /**
     * 关闭读卡器设备（异步）
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
        if (bStarted) {
            try {
                idCardReader.close(0);
            } catch (IDCardReaderException e) {
                e.printStackTrace();
            }
            bStarted = false;
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onDeviceDisconnected();
                    }
                }
            });
        }
    }

    /**
     * 读取身份证信息（异步）
     */
    public void readCard() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doReadCard();
            }
        }).start();
    }

    /**
     * 实际执行读卡操作
     */
    private void doReadCard() {
        logDebug("开始读卡...");
        if (!bStarted) {
            logDebug("读卡失败：设备未连接");
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onCardReadFail("设备未连接");
                    }
                }
            });
            return;
        }

        long nTickstart = System.currentTimeMillis();
        try {
            logDebug("尝试寻卡...");
            idCardReader.findCard(0);
            logDebug("寻卡成功，尝试选卡...");
            idCardReader.selectCard(0);
            logDebug("选卡成功");
        } catch (IDCardReaderException e) {
            logDebug("寻卡/选卡失败，继续尝试读卡: " + e.getMessage());
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int cardType = 0;
        try {
            logDebug("尝试读取卡片数据...");
            cardType = idCardReader.readCardEx(0, 0);
            logDebug("读卡成功，卡片类型: " + getCardTypeName(cardType));
        } catch (IDCardReaderException e) {
            logError("读卡失败: " + e.getMessage(), e);
            final String errorMsg = e.getMessage();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onCardReadFail("读卡失败：" + errorMsg);
                    }
                }
            });
            return;
        }

        if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_PRP ||
                cardType == IDCardType.TYPE_CARD_GAT || cardType == IDCardType.TYPE_CARD_PRP2) {
            long nTickCommuUsed = (System.currentTimeMillis() - nTickstart);
            if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_GAT) {
                IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                logDebug("读取到身份证信息:");
                logDebug("  姓名: " + idCardInfo.getName());
                logDebug("  性别: " + idCardInfo.getSex());
                logDebug("  民族: " + idCardInfo.getNation());
                logDebug("  出生日期: " + idCardInfo.getBirth());
                logDebug("  身份证号: " + idCardInfo.getId());
                logDebug("  签发机关: " + idCardInfo.getDepart());
                logDebug("  有效期: " + idCardInfo.getValidityTime());
                logDebug("  地址: " + idCardInfo.getAddress());
                logDebug("  照片长度: " + idCardInfo.getPhotolength());
                logDebug("  通信耗时: " + nTickCommuUsed + "ms");

                CardInfo cardInfo = new CardInfo();
                cardInfo.setName(idCardInfo.getName());
                cardInfo.setSex(idCardInfo.getSex());
                cardInfo.setNation(idCardInfo.getNation());
                cardInfo.setBirth(idCardInfo.getBirth());
                cardInfo.setIdNumber(idCardInfo.getId());
                cardInfo.setDepart(idCardInfo.getDepart());
                cardInfo.setValidity(idCardInfo.getValidityTime());
                cardInfo.setAddress(idCardInfo.getAddress());
                cardInfo.setCardType(cardType);

                if (idCardInfo.getPhotolength() > 0) {
                    byte[] buf = new byte[WLTService.imgLength];
                    if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                        Bitmap bmpPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                        cardInfo.setPhotoBase64(bitmapToBase64(bmpPhoto));
                        logDebug("照片转换成功");
                    }
                }

                // 读取身份证物理卡号（HEX）
                // readIDCardSnrHex(cardInfo);

                final long commuTime = nTickCommuUsed;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onCardReadSuccess(cardInfo);
                        }
                    }
                });
            } else {
                IDPRPCardInfo idprpCardInfo = idCardReader.getLastPRPIDCardInfo();
                logDebug("读取到PRP卡片信息:");
                logDebug("  姓名: " + idprpCardInfo.getCnName());
                logDebug("  性别: " + idprpCardInfo.getSex());
                logDebug("  国籍: " + idprpCardInfo.getCountry() + "/" + idprpCardInfo.getCountryCode());
                logDebug("  出生日期: " + idprpCardInfo.getBirth());
                logDebug("  证件号: " + idprpCardInfo.getId());
                logDebug("  有效期: " + idprpCardInfo.getValidityTime());
                logDebug("  照片长度: " + idprpCardInfo.getPhotolength());

                CardInfo cardInfo = new CardInfo();
                cardInfo.setName(idprpCardInfo.getCnName());
                cardInfo.setSex(idprpCardInfo.getSex());
                cardInfo.setNation(idprpCardInfo.getCountry() + "/" + idprpCardInfo.getCountryCode());
                cardInfo.setBirth(idprpCardInfo.getBirth());
                cardInfo.setIdNumber(idprpCardInfo.getId());
                cardInfo.setValidity(idprpCardInfo.getValidityTime());
                cardInfo.setCardType(cardType);

                if (idprpCardInfo.getPhotolength() > 0) {
                    byte[] buf = new byte[WLTService.imgLength];
                    if (1 == WLTService.wlt2Bmp(idprpCardInfo.getPhoto(), buf)) {
                        Bitmap bmpPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                        cardInfo.setPhotoBase64(bitmapToBase64(bmpPhoto));
                    }
                }

                // 读取身份证物理卡号（HEX）
                // readIDCardSnrHex(cardInfo);

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onCardReadSuccess(cardInfo);
                        }
                    }
                });
            }
        } else {
            // 身份证读取失败，尝试读取普通IC卡物理卡号
            CardInfo icCardInfo = new CardInfo();
            if (readICSnrHex(icCardInfo)) {
                logDebug("读取到普通IC卡");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onCardReadSuccess(icCardInfo);
                        }
                    }
                });
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onCardReadFail("读卡失败：不支持的卡片类型");
                        }
                    }
                });
            }
        }
    }

    /**
     * 获取固件版本（异步）
     */
    public void getFirmwareVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doGetFirmwareVersion();
            }
        }).start();
    }

    /**
     * 实际执行获取固件版本操作
     */
    private void doGetFirmwareVersion() {
        if (!bStarted) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onFirmwareVersion(null);
                    }
                }
            });
            return;
        }

        byte[] firmwareVersion = new byte[64];
        try {
            boolean ret = idCardReader.Get_VersionNum(0, firmwareVersion);
            if (ret) {
                final String version = newStringBytes(firmwareVersion);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFirmwareVersion(version);
                        }
                    }
                });
            } else {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.onFirmwareVersion(null);
                        }
                    }
                });
            }
        } catch (IDCardReaderException e) {
            e.printStackTrace();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onFirmwareVersion(null);
                    }
                }
            });
        }
    }

    /**
     * 将字节数组转换为字符串（去除末尾的0）
     * 
     * @param stringBytes 字节数组
     * @return 转换后的字符串
     */
    private String newStringBytes(byte[] stringBytes) {
        int nLen = stringBytes.length;
        for (int i = 0; i < nLen; ++i) {
            if (stringBytes[i] == 0) {
                nLen = i;
                break;
            }
        }
        return new String(stringBytes, 0, nLen).trim();
    }

    /**
     * 读取身份证物理卡号（HEX格式）
     * 
     * @param cardInfo 卡片信息对象
     */
    private void readIDCardSnrHex(CardInfo cardInfo) {
        if (!bStarted || idCardReader == null) {
            return;
        }
        byte mode = 0x26;
        byte halt = 0x0;
        byte[] idsnrBuf = new byte[64];
        try {
            boolean ret = idCardReader.MF_GET_NIDCardNum(0, mode, halt, idsnrBuf);
            if (ret) {
                String snrHex = ToolUtils.bytesToHexString(idsnrBuf, 0, 8);
                cardInfo.setSnrHex(snrHex);
                logDebug("身份证物理卡号（HEX）: " + snrHex);
            } else {
                logDebug("读取身份证物理卡号失败");
            }
        } catch (IDCardReaderException e) {
            logDebug("读取身份证物理卡号异常: " + e.getMessage());
        }
    }

    /**
     * 读取普通IC卡物理卡号（HEX格式）
     * 
     * @param cardInfo 卡片信息对象
     * @return 是否读取成功
     */
    private boolean readICSnrHex(CardInfo cardInfo) {
        if (!bStarted || idCardReader == null) {
            return false;
        }
        byte mode = 0x26;
        byte halt = 0x0;
        try {
            String snrHex = idCardReader.MF_GET_SNR_HEX(0, mode, halt);
            if (snrHex != null && !snrHex.isEmpty()) {
                cardInfo.setSnrHex(snrHex);
                // 写死类型6,sdk提供的类型中没有这个类型
                cardInfo.setCardType(6);
                logDebug("普通IC卡物理卡号（HEX）: " + snrHex);
                return true;
            }
        } catch (IDCardReaderException e) {
            logDebug("读取普通IC卡物理卡号异常: " + e.getMessage());
        }
        return false;
    }

    /**
     * 将Bitmap转换为Base64编码
     * 
     * @param bitmap 位图对象
     * @return Base64编码字符串
     */
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    /**
     * 开启自动读卡功能
     */
    public void startAutoRead() {
        if (!bStarted) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onCardReadFail("设备未连接");
                    }
                }
            });
            return;
        }

        if (!autoReading) {
            autoReading = true;
            autoReadThread = new AutoReadThread();
            autoReadThread.start();
        }
    }

    /**
     * 停止自动读卡功能
     */
    public void stopAutoRead() {
        autoReading = false;
        if (autoReadThread != null) {
            autoReadThread.interrupt();
            autoReadThread = null;
        }
    }

    /**
     * 检查是否正在自动读卡
     * 
     * @return 是否正在自动读卡
     */
    public boolean isAutoReading() {
        return autoReading;
    }

    /**
     * 自动读卡线程类
     */
    private class AutoReadThread extends Thread {
        @Override
        public void run() {
            logDebug("自动读卡线程启动");
            while (autoReading && bStarted) {
                try {
                    // 寻卡
                    boolean findCardSuccess = false;
                    try {
                        logDebug("自动读卡 - 尝试寻卡...");
                        idCardReader.findCard(0);
                        findCardSuccess = true;
                        logDebug("自动读卡 - 寻卡成功");
                    } catch (IDCardReaderException e) {
                        // 寻卡失败，继续循环
                        logDebug("自动读卡 - 寻卡失败，继续尝试: " + e.getMessage());
                        try {
                            logDebug("自动读卡 - 读取普通IC卡");
                            // 寻卡失败，尝试读取普通IC卡物理卡号
                            CardInfo icCardInfo = new CardInfo();
                            if (readICSnrHex(icCardInfo)) {
                                logDebug("自动读卡 - 读取到普通IC卡");
                                final CardInfo finalIcCardInfo = icCardInfo;
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (callback != null && autoReading) {
                                            callback.onCardReadSuccess(finalIcCardInfo);
                                        }
                                    }
                                });
                            }
                        } catch (Exception e1) {
                            logDebug("读取普通IC卡失败: " + e1.getMessage());
                        }
                    }

                    if (findCardSuccess) {
                        // 选卡
                        try {
                            logDebug("自动读卡 - 尝试选卡...");
                            idCardReader.selectCard(0);
                            logDebug("自动读卡 - 选卡成功");

                            // 读卡
                            logDebug("自动读卡 - 尝试读取卡片数据...");
                            int cardType = idCardReader.readCardEx(0, 0);
                            logDebug("自动读卡 - 读卡成功，卡片类型: " + getCardTypeName(cardType));

                            if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_PRP ||
                                    cardType == IDCardType.TYPE_CARD_GAT || cardType == IDCardType.TYPE_CARD_PRP2) {
                                if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_GAT) {
                                    IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                                    logDebug("自动读卡 - 读取到身份证信息:");
                                    logDebug("  姓名: " + idCardInfo.getName());
                                    logDebug("  性别: " + idCardInfo.getSex());
                                    logDebug("  民族: " + idCardInfo.getNation());
                                    logDebug("  出生日期: " + idCardInfo.getBirth());
                                    logDebug("  身份证号: " + idCardInfo.getId());
                                    logDebug("  签发机关: " + idCardInfo.getDepart());
                                    logDebug("  有效期: " + idCardInfo.getValidityTime());
                                    logDebug("  地址: " + idCardInfo.getAddress());
                                    logDebug("  照片长度: " + idCardInfo.getPhotolength());

                                    CardInfo cardInfo = new CardInfo();
                                    cardInfo.setName(idCardInfo.getName());
                                    cardInfo.setSex(idCardInfo.getSex());
                                    cardInfo.setNation(idCardInfo.getNation());
                                    cardInfo.setBirth(idCardInfo.getBirth());
                                    cardInfo.setIdNumber(idCardInfo.getId());
                                    cardInfo.setDepart(idCardInfo.getDepart());
                                    cardInfo.setValidity(idCardInfo.getValidityTime());
                                    cardInfo.setAddress(idCardInfo.getAddress());
                                    cardInfo.setCardType(cardType);

                                    if (idCardInfo.getPhotolength() > 0) {
                                        byte[] buf = new byte[WLTService.imgLength];
                                        if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                                            Bitmap bmpPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                                            cardInfo.setPhotoBase64(bitmapToBase64(bmpPhoto));
                                        }
                                    }

                                    // 读取身份证物理卡号（HEX）
                                    readIDCardSnrHex(cardInfo);

                                    final CardInfo finalCardInfo = cardInfo;
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (callback != null && autoReading) {
                                                callback.onCardReadSuccess(finalCardInfo);
                                            }
                                        }
                                    });
                                } else {
                                    IDPRPCardInfo idprpCardInfo = idCardReader.getLastPRPIDCardInfo();
                                    logDebug("自动读卡 - 读取到PRP卡片信息:");
                                    logDebug("  姓名: " + idprpCardInfo.getCnName());
                                    logDebug("  性别: " + idprpCardInfo.getSex());
                                    logDebug("  国籍: " + idprpCardInfo.getCountry() + "/"
                                            + idprpCardInfo.getCountryCode());
                                    logDebug("  出生日期: " + idprpCardInfo.getBirth());
                                    logDebug("  证件号: " + idprpCardInfo.getId());
                                    logDebug("  有效期: " + idprpCardInfo.getValidityTime());
                                    logDebug("  照片长度: " + idprpCardInfo.getPhotolength());

                                    CardInfo cardInfo = new CardInfo();
                                    cardInfo.setName(idprpCardInfo.getCnName());
                                    cardInfo.setSex(idprpCardInfo.getSex());
                                    cardInfo.setNation(
                                            idprpCardInfo.getCountry() + "/" + idprpCardInfo.getCountryCode());
                                    cardInfo.setBirth(idprpCardInfo.getBirth());
                                    cardInfo.setIdNumber(idprpCardInfo.getId());
                                    cardInfo.setValidity(idprpCardInfo.getValidityTime());
                                    cardInfo.setCardType(cardType);

                                    if (idprpCardInfo.getPhotolength() > 0) {
                                        byte[] buf = new byte[WLTService.imgLength];
                                        if (1 == WLTService.wlt2Bmp(idprpCardInfo.getPhoto(), buf)) {
                                            Bitmap bmpPhoto = IDPhotoHelper.Bgr2Bitmap(buf);
                                            cardInfo.setPhotoBase64(bitmapToBase64(bmpPhoto));
                                            logDebug("自动读卡 - PRP卡片照片转换成功");
                                        }
                                    }

                                    // 读取身份证物理卡号（HEX）
                                    // readIDCardSnrHex(cardInfo);

                                    final CardInfo finalCardInfo = cardInfo;
                                    mainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (callback != null && autoReading) {
                                                callback.onCardReadSuccess(finalCardInfo);
                                            }
                                        }
                                    });
                                }
                            }
                        } catch (IDCardReaderException e) {
                            logError("自动读卡 - 选卡或读卡失败: " + e.getMessage(), e);
                        }
                    }

                    // 休眠1200ms，避免频繁读卡
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    Log.d(TAG, "自动读卡线程被中断");
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "自动读卡异常", e);
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
            autoReading = false;
        }
    }

    /**
     * 获取卡片类型名称
     * 
     * @param cardType 卡片类型代码
     * @return 卡片类型名称
     */
    private String getCardTypeName(int cardType) {
        switch (cardType) {
            case IDCardType.TYPE_CARD_SFZ:
                return "TYPE_CARD_SFZ (身份证)";
            case IDCardType.TYPE_CARD_PRP:
                return "TYPE_CARD_PRP (港澳通行证)";
            case IDCardType.TYPE_CARD_GAT:
                return "TYPE_CARD_GAT (港澳台居民居住证)";
            case IDCardType.TYPE_CARD_PRP2:
                return "TYPE_CARD_PRP2 (台湾通行证)";
            default:
                return "UNKNOWN (" + cardType + ")";
        }
    }

    /**
     * 释放读卡器资源
     */
    public void release() {
        logDebug("释放读卡器资源...");
        stopAutoRead();
        if (bStarted) {
            try {
                idCardReader.close(0);
            } catch (IDCardReaderException e) {
                logError("关闭读卡器失败", e);
            }
            bStarted = false;
        }
        if (idCardReader != null) {
            IDCardReaderFactory.destroy(idCardReader);
            idCardReader = null;
        }
        logDebug("读卡器资源释放完成");
    }
}
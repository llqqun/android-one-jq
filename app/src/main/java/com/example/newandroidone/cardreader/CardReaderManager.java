package com.example.newandroidone.cardreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.IDCardType;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.module.idcard.meta.IDPRPCardInfo;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
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

    public CardReaderManager(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setCallback(CardReaderCallback callback) {
        this.callback = callback;
    }

    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }

    public boolean isConnected() {
        return bStarted;
    }

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

    public void openDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doOpenDevice();
            }
        }).start();
    }

    private void doOpenDevice() {
        startIDCardReader();
        try {
            idCardReader.open(0);
            bStarted = true;
            final String samid = idCardReader.getSAMID(0);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.onDeviceConnected(true, "SAMID: " + samid);
                    }
                }
            });
        } catch (IDCardReaderException e) {
            e.printStackTrace();
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

    public void closeDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doCloseDevice();
            }
        }).start();
    }

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

    public void readCard() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doReadCard();
            }
        }).start();
    }

    private void doReadCard() {
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

        long nTickstart = System.currentTimeMillis();
        try {
            idCardReader.findCard(0);
            idCardReader.selectCard(0);
        } catch (IDCardReaderException e) {
            Log.d(TAG, "寻卡/选卡失败，继续尝试读卡");
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int cardType = 0;
        try {
            cardType = idCardReader.readCardEx(0, 0);
        } catch (IDCardReaderException e) {
            e.printStackTrace();
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

    public void getFirmwareVersion() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doGetFirmwareVersion();
            }
        }).start();
    }

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

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

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

    public void stopAutoRead() {
        autoReading = false;
        if (autoReadThread != null) {
            autoReadThread.interrupt();
            autoReadThread = null;
        }
    }

    public boolean isAutoReading() {
        return autoReading;
    }

    private class AutoReadThread extends Thread {
        @Override
        public void run() {
            while (autoReading && bStarted) {
                try {
                    // 寻卡
                    boolean findCardSuccess = false;
                    try {
                        idCardReader.findCard(0);
                        findCardSuccess = true;
                    } catch (IDCardReaderException e) {
                        // 寻卡失败，继续循环
                        Log.d(TAG, "寻卡失败，继续尝试");
                    }

                    if (findCardSuccess) {
                        // 选卡
                        try {
                            idCardReader.selectCard(0);
                            // 读卡
                            int cardType = idCardReader.readCardEx(0, 0);

                            if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_PRP ||
                                    cardType == IDCardType.TYPE_CARD_GAT || cardType == IDCardType.TYPE_CARD_PRP2) {
                                if (cardType == IDCardType.TYPE_CARD_SFZ || cardType == IDCardType.TYPE_CARD_GAT) {
                                    IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
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
                            Log.d(TAG, "选卡或读卡失败: " + e.getMessage());
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

    public void release() {
        stopAutoRead();
        if (bStarted) {
            try {
                idCardReader.close(0);
            } catch (IDCardReaderException e) {
                e.printStackTrace();
            }
            bStarted = false;
        }
        if (idCardReader != null) {
            IDCardReaderFactory.destroy(idCardReader);
            idCardReader = null;
        }
    }
}
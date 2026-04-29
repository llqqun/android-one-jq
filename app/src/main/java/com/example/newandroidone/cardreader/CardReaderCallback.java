package com.example.newandroidone.cardreader;

/**
 * 读卡器回调接口，用于通知读卡器操作的结果
 */
public interface CardReaderCallback {
    /**
     * 设备连接状态回调
     * @param success 是否连接成功
     * @param message 连接消息（包含SAMID信息）
     */
    void onDeviceConnected(boolean success, String message);
    
    /**
     * 设备断开连接回调
     */
    void onDeviceDisconnected();
    
    /**
     * 读卡成功回调
     * @param cardInfo 读取到的身份证信息
     */
    void onCardReadSuccess(CardInfo cardInfo);
    
    /**
     * 读卡失败回调
     * @param errorMessage 错误信息
     */
    void onCardReadFail(String errorMessage);
    
    /**
     * 获取固件版本回调
     * @param version 固件版本号
     */
    void onFirmwareVersion(String version);
}
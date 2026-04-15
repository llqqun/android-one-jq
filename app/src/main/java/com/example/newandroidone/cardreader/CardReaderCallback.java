package com.example.newandroidone.cardreader;

public interface CardReaderCallback {
    void onDeviceConnected(boolean success, String message);
    void onDeviceDisconnected();
    void onCardReadSuccess(CardInfo cardInfo);
    void onCardReadFail(String errorMessage);
    void onFirmwareVersion(String version);
}
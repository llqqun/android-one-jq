package com.example.newandroidone.cardreader;

/**
 * 身份证信息数据类，存储从身份证读取的个人信息
 */
public class CardInfo {
    private String name;      // 姓名
    private String sex;       // 性别
    private String nation;    // 民族
    private String birth;     // 出生日期
    private String idNumber;  // 身份证号码
    private String address;   // 地址
    private String depart;    // 签发机关
    private String validity;  // 有效期
    private String photoBase64; // 照片Base64编码
    private int cardType;     // 卡片类型

    /**
     * 默认构造函数
     */
    public CardInfo() {
    }

    /**
     * 获取姓名
     * @return 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * 设置姓名
     * @param name 姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取性别
     * @return 性别
     */
    public String getSex() {
        return sex;
    }

    /**
     * 设置性别
     * @param sex 性别
     */
    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * 获取民族
     * @return 民族
     */
    public String getNation() {
        return nation;
    }

    /**
     * 设置民族
     * @param nation 民族
     */
    public void setNation(String nation) {
        this.nation = nation;
    }

    /**
     * 获取出生日期
     * @return 出生日期
     */
    public String getBirth() {
        return birth;
    }

    /**
     * 设置出生日期
     * @param birth 出生日期
     */
    public void setBirth(String birth) {
        this.birth = birth;
    }

    /**
     * 获取身份证号码
     * @return 身份证号码
     */
    public String getIdNumber() {
        return idNumber;
    }

    /**
     * 设置身份证号码
     * @param idNumber 身份证号码
     */
    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    /**
     * 获取地址
     * @return 地址
     */
    public String getAddress() {
        return address;
    }

    /**
     * 设置地址
     * @param address 地址
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取签发机关
     * @return 签发机关
     */
    public String getDepart() {
        return depart;
    }

    /**
     * 设置签发机关
     * @param depart 签发机关
     */
    public void setDepart(String depart) {
        this.depart = depart;
    }

    /**
     * 获取有效期
     * @return 有效期
     */
    public String getValidity() {
        return validity;
    }

    /**
     * 设置有效期
     * @param validity 有效期
     */
    public void setValidity(String validity) {
        this.validity = validity;
    }

    /**
     * 获取照片Base64编码
     * @return 照片Base64编码
     */
    public String getPhotoBase64() {
        return photoBase64;
    }

    /**
     * 设置照片Base64编码
     * @param photoBase64 照片Base64编码
     */
    public void setPhotoBase64(String photoBase64) {
        this.photoBase64 = photoBase64;
    }

    /**
     * 获取卡片类型
     * @return 卡片类型
     */
    public int getCardType() {
        return cardType;
    }

    /**
     * 设置卡片类型
     * @param cardType 卡片类型
     */
    public void setCardType(int cardType) {
        this.cardType = cardType;
    }
}
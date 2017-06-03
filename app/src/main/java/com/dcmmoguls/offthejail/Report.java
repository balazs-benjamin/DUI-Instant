package com.dcmmoguls.offthejail;

/*
 * Created by troy379 on 04.04.17.
 */
public class Report {

    public String senderId;
    public String senderName;
    public String senderAddress;
    public String senderEmail;
    public String senderPhone;
    public String createdAt;

    public Report() {
    }

    public Report(String senderId, String senderName, String senderAddress, String senderEmail, String senderPhone, String createdAt) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.senderEmail = senderEmail;
        this.senderPhone = senderPhone;
        this.createdAt = createdAt;
    }
}

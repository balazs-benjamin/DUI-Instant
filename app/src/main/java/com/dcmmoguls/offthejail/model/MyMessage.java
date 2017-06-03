package com.dcmmoguls.offthejail.model;

/*
 * Created by troy379 on 04.04.17.
 */
public class MyMessage {

    public String senderId;
    public String senderName;
    public String text;
    public String photoURL;
    public String createdAt;

    public MyMessage() {
    }

    public MyMessage(String senderId, String senderName, String text, String photoURL, String createdAt) {
        this.senderId = senderId;
        this.text = text;
        this.senderName = senderName;
        this.photoURL = photoURL;
        this.createdAt = createdAt;
    }
}

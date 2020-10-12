package com.example.mychatapp.Model;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private String Key;

    public Chat(String sender, String receiver, String message,boolean isseen,String Key) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen  = isseen;
        this.Key = Key;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getKey() {
        return Key;
    }

    public void setKey(String key) {
        Key = key;
    }
}

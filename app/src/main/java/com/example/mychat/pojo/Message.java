package com.example.mychat.pojo;

public class Message {
    private String author;
    private String textOfMessage;
    private long date;
    private String urlImage;

    public Message(String author, String textOfMessage, long date,String urlImage) {
        this.author = author;
        this.textOfMessage = textOfMessage;
        this.date = date;
        this.urlImage = urlImage;
    }
    public Message(){}

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTextOfMessage() {
        return textOfMessage;
    }

    public void setTextOfMessage(String textOfMessage) {
        this.textOfMessage = textOfMessage;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }
}

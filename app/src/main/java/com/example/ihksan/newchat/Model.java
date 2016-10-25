package com.example.ihksan.newchat;

/**
 * Created by
 * Name         : Ihksan Sukmawan
 * Email        : iksandecade@gmail.com
 * Company      : Meridian.Id
 * Date         : 21/10/16
 * Project      : NewChat
 */

public class Model {

    String userId;
    String name;
    String message;
    String location;
    long timeStamp;
    boolean image;
    boolean audio;

    public Model(){

    }
    public Model(String userId, String name, String message, String location, long timeStamp, boolean Image, boolean audio){
        this.userId = userId;
        this.name = name;
        this.message = message;
        this.location = location;
        this.timeStamp = timeStamp;
        this.image = image;
        this.audio = audio;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Boolean getImage() {
        return image;
    }

    public void setImage(Boolean image) {
        this.image = image;
    }

    public Boolean getAudio() {
        return audio;
    }

    public void setAudio(Boolean audio) {
        this.audio = audio;
    }
}

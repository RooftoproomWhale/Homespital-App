package com.kosmo.homespital.model;

import java.util.ArrayList;

import kr.co.prnd.YouTubePlayerView;

/**
 * Created by jaisel on 25/11/17.
 */

public class Message {
    private int id;
    private String text;
    private String youTubePlayerView;
    private String userId;
    private long timeStamp;
    private Status status;
    private MessageType messageType;
    private ArrayList<Cards> cards;

    public String getYouTubePlayerView() {
        return youTubePlayerView;
    }

    public void setYouTubePlayerView(String youTubePlayerView) {
        this.youTubePlayerView = youTubePlayerView;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ArrayList<Cards> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Cards> cards) {
        this.cards = cards;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timestamp) {
        this.timeStamp = timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}

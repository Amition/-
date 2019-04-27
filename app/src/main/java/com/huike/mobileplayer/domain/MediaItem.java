package com.huike.mobileplayer.domain;

import java.io.Serializable;

public class MediaItem  implements Serializable {
    private String name;
    private Long duration;
    private Long SIZE;
    private String data;

    private String image;
    private String desc;

    private String artist;

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getSIZE() {
        return SIZE;
    }

    public void setSIZE(Long SIZE) {
        this.SIZE = SIZE;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MediaItem{" +
                "name='" + name + '\'' +
                ", duration=" + duration +
                ", SIZE=" + SIZE +
                ", data='" + data + '\'' +
                '}';
    }
}

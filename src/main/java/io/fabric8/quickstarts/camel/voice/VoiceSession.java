package io.fabric8.quickstarts.camel.voice;

import java.sql.Date;
import java.sql.Timestamp;

public class VoiceSession {


    private String voiceid; 
    private String creation_date;
    private Integer duration;

    public String getVoiceid() {
        return voiceid;
    }

    public void setVoiceid(String voiceid) {
        this.voiceid = voiceid;
    }

 public String getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

  


}
package io.fabric8.quickstarts.camel.voice;

import java.util.ArrayList;

public class Result {
  
    private ArrayList <VoiceSession>  params;
    private String status; 
  
   // Getter Methods 
  
    public ArrayList <VoiceSession> getResult() {
      return params;
    }
  
   // Setter Methods 
  
    public  void setResult(ArrayList <VoiceSession> params) {
      this.params = params;
    }

    public String getStatus() {
      return status;
  }

  public void setStatus(String status) {
      this.status = status;
  }

    
  }
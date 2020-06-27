package io.fabric8.quickstarts.camel;

import java.util.ArrayList;

public class Result {
  
    private ArrayList <ImageSession>  params;
  
   // Getter Methods 
  
    public ArrayList <ImageSession> getResult() {
      return params;
    }
  
   // Setter Methods 
  
    public  void setResult(ArrayList <ImageSession> params) {
      this.params = params;
    }
  }
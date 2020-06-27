package io.fabric8.quickstarts.camel.instructor;

import java.util.ArrayList;

public class Result {
  
    private ArrayList <Params>  params;
    private String status; 
  
   // Getter Methods 
  
    public ArrayList <Params> getResult() {
      return params;
    }
  
   // Setter Methods 
  
    public  void setResult(ArrayList <Params> params) {
      this.params = params;
    }

    public String getStatus() {
      return status;
  }

  public void setStatus(String status) {
      this.status = status;
  }

    
  }
package io.fabric8.quickstarts.camel;

import javax.xml.bind.annotation.XmlElement;

public class p {

    private java.lang.String name;

    private java.lang.Integer age;

    private Integer BilledAmount;
    
    private Boolean previous;

    private String CreditHistory;

    private String MemberType;

	


    
    public java.lang.String getCreditHistory() {
		return this.CreditHistory;
	}

	public void setCreditHistory(java.lang.String CreditHistory) {
		this.CreditHistory = CreditHistory;
    }

    public java.lang.String getMemberType() {
		return this.MemberType;
	}

	public void setMemberType(java.lang.String MemberType) {
		this.MemberType = MemberType;
    }
    



   
    public java.lang.Integer getBilledAmount() {
		return this.BilledAmount;
	}

	public void setBilledAmount(java.lang.Integer BilledAmount) {
		this.BilledAmount = BilledAmount;
	}

   
}
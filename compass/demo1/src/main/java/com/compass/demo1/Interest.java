package com.compass.demo1;

public class Interest {
    
    private String interestId;
    private String interestName;

    public Interest(String interestId, String interestName) {
        this.interestId = interestId;
        this.interestName = interestName;
    }

    public String getInterestId() { 
        return interestId; 
    }

    public void setInterestId(String interestId) { 
        this.interestId = interestId; 
    }
 
    public String getInterestName() { 
        return interestName; 
    }
    
    public void setInterestName(String interestName) { 
        this.interestName = interestName; 
    }
}

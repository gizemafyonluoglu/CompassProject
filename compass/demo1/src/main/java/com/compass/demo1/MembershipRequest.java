package com.compass.demo1;

import java.time.LocalDate;

public class MembershipRequest {
    
    private String requestId;
    private String documentPath;
    private String clubName;
    private String status;
    private LocalDate submissionDate;
    private String userId;

    public MembershipRequest(String requestId, String documentPath, String clubName, LocalDate submissionDate, String userId) {
        this.requestId = requestId;
        this.documentPath = documentPath;
        this.clubName = clubName;
        this.status = "pending";
        this.submissionDate = submissionDate;
        this.userId = userId;
    }

    public void approve() {
        this.status = "approved";
    }
 
    public void reject() {
        this.status = "rejected";
    }

    public String getRequestId() { 
        return requestId; 
    }
    
    public void setRequestId(String requestId) { 
        this.requestId = requestId; 
    }

    public String getDocumentPath() { 
        return documentPath; 
    }
    
    public void setDocumentPath(String documentPath) { 
        this.documentPath = documentPath; 
    }

    public String getClubName() { 
        return clubName; 
    }
    
    public void setClubName(String clubName) { 
        this.clubName = clubName; 
    }

    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }

    public LocalDate getSubmissionDate() { 
        return submissionDate; 
    }
    
    public void setSubmissionDate(LocalDate submissionDate) { 
        this.submissionDate = submissionDate; 
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public String getUserId() { 
        return userId; 
    }
}

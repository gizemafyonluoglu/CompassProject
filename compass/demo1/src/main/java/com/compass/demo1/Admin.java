package com.compass.demo1;

public class Admin extends User{
    
    public Admin(String userId, String name, String surname, String email, String password) {
        super(userId, name, surname, email, password);
    }

    public void approveRequest(MembershipRequest request) {
        if (request != null) {
            request.approve();
        }
    }
 
    public void rejectRequest(MembershipRequest request) {
        if (request != null) {
            request.reject();
        }
    }
}

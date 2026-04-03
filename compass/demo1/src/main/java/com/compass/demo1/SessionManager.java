package com.compass.demo1;

public class SessionManager {
    private static User currentUser;

    public static User getCurrentUser() {
        if (currentUser == null) {
            currentUser = new User("U001", "Yağmur Zehra", "Ünal", "yzu@ug.bilkent.edu.tr", "12345");

            currentUser.getInterests().add(new Interest("I1", "Music"));
            currentUser.getInterests().add(new Interest("I2", "Arts"));
            currentUser.getInterests().add(new Interest("I3", "Cinema"));
        }
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }
}

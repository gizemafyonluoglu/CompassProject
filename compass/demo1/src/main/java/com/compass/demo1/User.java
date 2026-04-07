package com.compass.demo1;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String userId;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String biography;
    private String profilePhotoPath;
    private String profilePhotoBase64;  
    private boolean isVerified;
    private boolean availabilityVisible;
    private List<User> friends;
    private List<User> blockedUsers;
    private List<Interest> interests;
    private Calendar calendar;
    private List<Activity> createdActivities;
    private List<Activity> attendedActivities;

    public User(String userId, String name, String surname, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.biography = "";
        this.profilePhotoPath = "";
        this.profilePhotoBase64 = "";
        this.isVerified = false;
        this.availabilityVisible = true;
        this.friends = new ArrayList<>();
        this.blockedUsers = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.calendar = new Calendar();
        this.createdActivities = new ArrayList<>();
        this.attendedActivities = new ArrayList<>();
    }

    public void addFriend(User user) {
        if (user != null && !user.equals(this) && !friends.contains(user) && !blockedUsers.contains(user)) {
            friends.add(user);
        }
    }

    public void removeFriend(User user) {
        friends.remove(user);
    }

    public void blockUser(User user) {
        if (user != null && !user.equals(this) && !blockedUsers.contains(user)) {
            blockedUsers.add(user);
            friends.remove(user);
        }
    }

    public void unblockUser(User user) {
        blockedUsers.remove(user);
    }

    public void updateBiography(String text) {
        this.biography = text;
    }

    public void updateProfilePhoto(String path) {
        this.profilePhotoPath = path;
    }

    public void updateInterests(List<Interest> interests) {
        this.interests = new ArrayList<>(interests);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBiography() {
        return biography;
    }

    public String getProfilePhotoPath() {
        return profilePhotoPath;
    }
    public String getProfilePhotoBase64() {
        return profilePhotoBase64;
    }

    public void setProfilePhotoBase64(String profilePhotoBase64) {
        this.profilePhotoBase64 = profilePhotoBase64;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public boolean isAvailabilityVisible() {
        return availabilityVisible;
    }

    public void setAvailabilityVisible(boolean availabilityVisible) {
        this.availabilityVisible = availabilityVisible;
    }

    public List<User> getFriends() {
        return new ArrayList<>(friends);
    }

    public List<User> getBlockedUsers() {
        return new ArrayList<>(blockedUsers);
    }

    public List<Interest> getInterests() {
        return new ArrayList<>(interests);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public List<Activity> getCreatedActivities() {
        return new ArrayList<>(createdActivities);
    }

    public void setCreatedActivities(List<Activity> createdActivities) {
        this.createdActivities = new ArrayList<>(createdActivities);
    }

    public List<Activity> getAttendedActivities() {
        return new ArrayList<>(attendedActivities);
    }

    public void setAttendedActivities(List<Activity> attendedActivities) {
        this.attendedActivities = new ArrayList<>(attendedActivities);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        User other = (User) obj;
        return userId != null && userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return userId != null ? userId.hashCode() : 0;
    }
    public void setInterests(List<Interest> interests) {
    this.interests = new ArrayList<>(interests);
}

    public void addInterest(Interest interest) {
        if (interest != null && !interests.contains(interest)) {
            interests.add(interest);
        }
    }
    public void removeInterest(Interest interest) {
        interests.remove(interest);
    }
}

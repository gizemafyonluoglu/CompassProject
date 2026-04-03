package com.compass.demo1;

import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class Database {
    // baglayıvı variable zımbırtıları
    private static Database instance;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> usersCol;
    private MongoCollection<Document> activitiesCol;
    private MongoCollection<Document> membershipCol;
     private MongoCollection<Document> interestsCol;
    //constuctor
    private Database() {
        mongoClient = MongoClients.create(MongoConfig.CONNECTION_URI);
        database = mongoClient.getDatabase(MongoConfig.DATABASE_NAME);
        usersCol = database.getCollection("users");
        activitiesCol = database.getCollection("activities");
        membershipCol = database.getCollection("membership_requests");
        interestsCol = database.getCollection("interests");
        createIndexes();
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public void close() {
        mongoClient.close();
    }

    private void createIndexes() {
        usersCol.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
        activitiesCol.createIndex(Indexes.text("activityName"));
        membershipCol.createIndex(Indexes.ascending("status"));
    }

    // kullanıcı kısımları

    public void saveUser(User user) {
        if (user == null) {
            return;
        }
        Document doc = userToDocument(user);
        usersCol.replaceOne(eq("userId", user.getUserId()), doc, new ReplaceOptions().upsert(true));
    }

    public User getUser(String email) {
        if (email == null) {
            return null;
        }
        Document doc = usersCol.find(eq("email", email)).first();
        if (doc == null) {
            return null;
        }
        return documentToUser(doc);
    }

    public User getUserById(String userId) {
        if (userId == null) {
            return null;
        }
        Document doc = usersCol.find(eq("userId", userId)).first();
        if (doc == null) {
            return null;
        }
        return documentToUser(doc);
    }

    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        for (Document doc : usersCol.find()) {
            User u = documentToUser(doc);
            if (u != null) {
                result.add(u);
            }
        }
        return result;
    }

    public List<User> searchUsersByPrefix(String prefix) {
        List<User> result = new ArrayList<>();
        if (prefix == null) {
            return result;
        }

        List<Document> conditions = new ArrayList<>();
        conditions.add(new Document("name", new Document("$regex", "^" + prefix).append("$options", "i")));
        conditions.add(new Document("surname", new Document("$regex", "^" + prefix).append("$options", "i")));
        Document filter = new Document("$or", conditions);

        for (Document doc : usersCol.find(filter)) {
            User u = documentToUser(doc);
            if (u != null) {
                result.add(u);
            }
        }
        return result;
    }

    public void setUserVerified(String userId, boolean verified) {
        usersCol.updateOne(eq("userId", userId), set("isVerified", verified));
    }

    public void updatePassword(String userId, String newPassword) {
        usersCol.updateOne(eq("userId", userId), set("password", newPassword));
    }

    public void deleteUser(String userId) {
        usersCol.deleteOne(eq("userId", userId));
    }

    // aktiviteler kısmı

    public void saveActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        Document doc = activityToDocument(activity);
        activitiesCol.replaceOne(eq("activityId", activity.getActivityId()), doc, new ReplaceOptions().upsert(true));
    }

    public List<Activity> getActivities() {
        List<Activity> activities = new ArrayList<>();
        for (Document doc : activitiesCol.find(eq("isCancelled", false))) {
            Activity a = documentToActivity(doc);
            if (a != null) {
                activities.add(a);
            }
        }
        return activities;
    }

    public Activity getActivityById(String activityId) {
        if (activityId == null) {
            return null;
        }
        Document doc = activitiesCol.find(eq("activityId", activityId)).first();
        if (doc == null) {
            return null;
        }
        return documentToActivity(doc);
    }

    
    public void removeActivity(String activityId) {
        if (activityId == null) {
            return;
        }
        activitiesCol.updateOne(eq("activityId", activityId), set("isCancelled", true));
    }

    public void addParticipantToActivity(String activityId, String userId) {
        activitiesCol.updateOne(eq("activityId", activityId), addToSet("joinedUserIds", userId));
    }

    public void removeParticipantFromActivity(String activityId, String userId) {
        activitiesCol.updateOne(eq("activityId", activityId), pull("joinedUserIds", userId));
    }

    public List<Activity> searchActivities(String keyword) {
        List<Activity> result = new ArrayList<>();
        if (keyword == null) {
            return result;
        }

        List<Document> orConditions = new ArrayList<>();
        orConditions.add(new Document("activityName", new Document("$regex", keyword).append("$options", "i")));
        orConditions.add(new Document("description", new Document("$regex", keyword).append("$options", "i")));
        orConditions.add(new Document("category", new Document("$regex", keyword).append("$options", "i")));

        List<Document> andConditions = new ArrayList<>();
        andConditions.add(new Document("isCancelled", false));
        andConditions.add(new Document("$or", orConditions));

        Document filter = new Document("$and", andConditions);
        for (Document doc : activitiesCol.find(filter)) {
            Activity a = documentToActivity(doc);
            if (a != null) {
                result.add(a);
            }
        }
        return result;
    }

    public void saveMembershipRequest(MembershipRequest request) {
        if (request == null) {
            return;
        }
        Document doc = membershipRequestToDocument(request);
        membershipCol.replaceOne(eq("requestId", request.getRequestId()), doc, new ReplaceOptions().upsert(true));
    }

    public List<MembershipRequest> getMembershipRequests() {
        List<MembershipRequest> requests = new ArrayList<>();
        for (Document doc : membershipCol.find()) {
            MembershipRequest r = documentToMembershipRequest(doc);
            if (r != null) {
                requests.add(r);
            }
        }
        return requests;
    }

    public List<MembershipRequest> getPendingMembershipRequests() {
        List<MembershipRequest> requests = new ArrayList<>();
        for (Document doc : membershipCol.find(eq("status", "pending"))) {
            MembershipRequest r = documentToMembershipRequest(doc);
            if (r != null) {
                requests.add(r);
            }
        }
        return requests;
    }

    
    public void updateRequestStatus(String requestId, String status) {
        if (requestId == null || status == null) {
            return;
        }
        membershipCol.updateOne(eq("requestId", requestId), set("status", status));
    }

    // ilgi

    public void saveInterest(Interest interest) {
        if (interest == null) {
            return;
        }
        Document doc = new Document();
        doc.append("interestId", interest.getInterestId());
        doc.append("interestName", interest.getInterestName());
        interestsCol.replaceOne(eq("interestId", interest.getInterestId()), doc, new ReplaceOptions().upsert(true));
    }

    public List<Interest> getAllInterests() {
        List<Interest> interests = new ArrayList<>();
        for (Document doc : interestsCol.find()) {
            String id = doc.getString("interestId");
            String name = doc.getString("interestName");
            interests.add(new Interest(id, name));
        }
        return interests;
    }

    private Document calendarToDocument(Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        List<Document> planDocs = new ArrayList<>();
        for (Plan plan : calendar.getPlans()) {
            if (plan != null) {
                planDocs.add(planToDocument(plan));
            }
        }

        Document calendarDoc = new Document();
        calendarDoc.append("calendarId", calendar.getCalendarId());
        calendarDoc.append("plans", planDocs);

        return calendarDoc;
    }

    private Calendar documentToCalendar(Document doc) {
        if (doc == null) {
            return new Calendar();
        }

        Calendar calendar = new Calendar();

        String calendarId = doc.getString("calendarId");
        calendar.setCalendarId(calendarId);

        List<Plan> plans = new ArrayList<>();
        List<Document> planDocs = doc.getList("plans", Document.class);

        if (planDocs != null) {
            for (Document planDoc : planDocs) {
                Plan plan = documentToPlan(planDoc);
                if (plan != null) {
                    plans.add(plan);
                }
            }
        }

        calendar.setPlans(plans);
        return calendar;
    }
    
    private Document userToDocument(User user) {
        List<String> friendIds = new ArrayList<>();
        for (User f : user.getFriends()) {
            friendIds.add(f.getUserId());
        }

        List<String> blockedIds = new ArrayList<>();
        for (User b : user.getBlockedUsers()) {
            blockedIds.add(b.getUserId());
        }

        List<String> interestIds = new ArrayList<>();
        for (Interest i : user.getInterests()) {
            interestIds.add(i.getInterestId());
        }

        List<String> createdIds = new ArrayList<>();
        for (Activity a : user.getCreatedActivities()) {
            createdIds.add(a.getActivityId());
        }

        List<String> attendedIds = new ArrayList<>();
        for (Activity a : user.getAttendedActivities()) {
            attendedIds.add(a.getActivityId());
        }
        
        Document doc = new Document();
        doc.append("userId", user.getUserId());
        doc.append("name", user.getName());
        doc.append("surname", user.getSurname());
        doc.append("email", user.getEmail());
        doc.append("password", user.getPassword());
        doc.append("biography", user.getBiography());
        doc.append("profilePhotoPath", user.getProfilePhotoPath());
        doc.append("isVerified", user.isVerified());
        doc.append("availabilityVisible", user.isAvailabilityVisible());
        doc.append("friendIds", friendIds);
        doc.append("blockedIds", blockedIds);
        doc.append("interestIds", interestIds);
        doc.append("createdActivityIds", createdIds);
        doc.append("attendedActivityIds", attendedIds);
        doc.append("calendar", calendarToDocument(user.getCalendar()));

        if (user instanceof Admin) {
            doc.append("userType", "Admin");
        } else if (user instanceof ClubBoardMember) {
            ClubBoardMember cbm = (ClubBoardMember) user;
            doc.append("userType", "ClubBoardMember");
            doc.append("clubName", cbm.getClubName());
            doc.append("boardStatusApproved", cbm.isBoardStatusApproved());
        } else {
            doc.append("userType", "User");
        }

        return doc;
    }

    private User documentToUser(Document doc) {
        if (doc == null) {
            return null;
        }

        String userId = doc.getString("userId");
        String name = doc.getString("name");
        String surname = doc.getString("surname");
        String email = doc.getString("email");
        String password = doc.getString("password");
        String userType = doc.getString("userType");

        User user;
        if ("Admin".equals(userType)) {
            user = new Admin(userId, name, surname, email, password);

        } else if ("ClubBoardMember".equals(userType)) {
            String clubName = doc.getString("clubName");
            ClubBoardMember cbm = new ClubBoardMember(userId, name, surname, email, password, clubName);
            Boolean approved = doc.getBoolean("boardStatusApproved");
            if (approved != null && approved) {
                cbm.setBoardStatusApproved(true);
            } else {
                cbm.setBoardStatusApproved(false);
            }
            user = cbm;

        } else {
            user = new User(userId, name, surname, email, password);
        }

        Boolean verified = doc.getBoolean("isVerified");
        if (verified != null && verified) {
            user.setVerified(true);
        } else {
            user.setVerified(false);
        }

        Boolean availVis = doc.getBoolean("availabilityVisible");
        if (availVis != null && availVis) {
            user.setAvailabilityVisible(true);
        } else {
            user.setAvailabilityVisible(false);
        }

        String bio = doc.getString("biography");
        if (bio != null && !bio.isEmpty()) {
            user.updateBiography(bio);
        }

        String photo = doc.getString("profilePhotoPath");
        if (photo != null && !photo.isEmpty()) {
            user.updateProfilePhoto(photo);
        }

        Document calendarDoc = doc.get("calendar", Document.class);
        if (calendarDoc != null) {
            user.setCalendar(documentToCalendar(calendarDoc));
        } else {
            user.setCalendar(new Calendar());
        }

        return user;
    }

    private Document activityToDocument(Activity activity) {
        List<String> joinedUserIds = new ArrayList<>();
        for (User u : activity.getJoinedUsers()) {
            joinedUserIds.add(u.getUserId());
        }

        String dateStr = null;
        if (activity.getDate() != null) {
            dateStr = activity.getDate().toString();
        }

        String timeStr = null;
        if (activity.getTime() != null) {
            timeStr = activity.getTime().toString();
        }

        Document doc = new Document();
        doc.append("activityId", activity.getActivityId());
        doc.append("activityName", activity.getActivityName());
        doc.append("description", activity.getDescription());
        doc.append("category", activity.getCategory());
        doc.append("place", activity.getPlace());
        doc.append("date", dateStr);
        doc.append("time", timeStr);
        doc.append("quota", activity.getQuota());
        doc.append("visibility", activity.getVisibility());
        doc.append("activityType", activity.getActivityType());
        doc.append("isCancelled", activity.isCancelled());
        doc.append("joinedUserIds", joinedUserIds);

        if (activity instanceof ClubActivity) {
            ClubActivity ca = (ClubActivity) activity;
            doc.append("activityClass", "ClubActivity");
            doc.append("clubName", ca.getClubName());
            doc.append("isOfficialClubEvent", ca.isOfficialClubEvent());
        } else {
            doc.append("activityClass", "Activity");
        }

        return doc;
    }

    private Activity documentToActivity(Document doc) {
        if (doc == null) {
            return null;
        }

        String activityId = doc.getString("activityId");
        String activityName = doc.getString("activityName");
        String description = doc.getString("description");
        String category = doc.getString("category");
        String place = doc.getString("place");
        int quota = doc.getInteger("quota", 0);
        String visibility = doc.getString("visibility");
        String activityType = doc.getString("activityType");

        LocalDate date = null;
        if (doc.getString("date") != null) {
            date = LocalDate.parse(doc.getString("date"));
        }

        LocalTime time = null;
        if (doc.getString("time") != null) {
            time = LocalTime.parse(doc.getString("time"));
        }

        Activity activity;
        String activityClass = doc.getString("activityClass");

        if ("ClubActivity".equals(activityClass)) {
            String clubName = doc.getString("clubName");
            boolean isOfficial = false;
            Boolean officialVal = doc.getBoolean("isOfficialClubEvent");
            if (officialVal != null && officialVal) {
                isOfficial = true;
            }
            activity = new ClubActivity(activityId, activityName, description, category, place, date, time, quota, visibility, activityType, clubName, isOfficial);
        } else {
            activity = new Activity(activityId, activityName, description, category, place, date, time, quota, visibility, activityType);
        }

        Boolean cancelled = doc.getBoolean("isCancelled");
        if (cancelled != null && cancelled) {
            activity.cancelActivity();
        }

        return activity;
    }
    //planlar
    private Document planToDocument(Plan plan) {
        String dateStr = null;
        if (plan.getDate() != null) {
            dateStr = plan.getDate().toString();
        }

        String startStr = null;
        if (plan.getStartTime() != null) {
            startStr = plan.getStartTime().toString();
        }

        String endStr = null;
        if (plan.getEndTime() != null) {
            endStr = plan.getEndTime().toString();
        }

        Document doc = new Document();
        doc.append("planId", plan.getPlanId());
        doc.append("title", plan.getTitle());
        doc.append("date", dateStr);
        doc.append("startTime", startStr);
        doc.append("endTime", endStr);
        doc.append("planType", plan.getPlanType());
        return doc;
    }

    private Plan documentToPlan(Document doc) {
        if (doc == null) {
            return null;
        }

        String planId = doc.getString("planId");
        String title = doc.getString("title");
        String planType = doc.getString("planType");

        LocalDate date = null;
        if (doc.getString("date") != null) {
            date = LocalDate.parse(doc.getString("date"));
        }

        LocalTime startTime = null;
        if (doc.getString("startTime") != null) {
            startTime = LocalTime.parse(doc.getString("startTime"));
        }

        LocalTime endTime = null;
        if (doc.getString("endTime") != null) {
            endTime = LocalTime.parse(doc.getString("endTime"));
        }

        Plan plan;
        if ("Course Plan".equals(planType)) {
            plan = new CoursePlan(planId, title, date, startTime, endTime);
        } else if ("Permanent Plan".equals(planType)) {
            plan = new PermanentPlan(planId, title, date, startTime, endTime);
        } else {
            plan = new OneTimePlan(planId, title, date, startTime, endTime);
        }

        return plan;
    }

    private Document membershipRequestToDocument(MembershipRequest req) {
        String dateStr = null;
        if (req.getSubmissionDate() != null) {
            dateStr = req.getSubmissionDate().toString();
        }

        Document doc = new Document();
        doc.append("requestId", req.getRequestId());
        doc.append("documentPath", req.getDocumentPath());
        doc.append("clubName", req.getClubName());
        doc.append("status", req.getStatus());
        doc.append("submissionDate", dateStr);
        return doc;
    }

    private MembershipRequest documentToMembershipRequest(Document doc) {
        if (doc == null) {
            return null;
        }

        String requestId = doc.getString("requestId");
        String documentPath = doc.getString("documentPath");
        String clubName = doc.getString("clubName");
        String status = doc.getString("status");

        LocalDate submissionDate = null;
        if (doc.getString("submissionDate") != null) {
            submissionDate = LocalDate.parse(doc.getString("submissionDate"));
        }

        MembershipRequest req = new MembershipRequest(requestId, documentPath, clubName, submissionDate);
        req.setStatus(status);
        return req;
    }
}
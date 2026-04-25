package com.electivenavigator.servlets;

import com.electivenavigator.utils.MongoDBConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

import com.electivenavigator.utils.EmailService;

@WebServlet("/api/auth/*")
public class StudentAuthServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        
        try {
            if ("/register".equals(pathInfo)) {
                handleRegister(request, response);
            } else if ("/login".equals(pathInfo)) {
                handleLogin(request, response);
            } else if ("/update-profile".equals(pathInfo)) {
                handleUpdateProfile(request, response);
            } else if ("/send-otp".equals(pathInfo)) {
                handleSendOtp(request, response);
            } else if ("/reset-password".equals(pathInfo)) {
                handleResetPassword(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(new ApiResponse(false, "Endpoint not found")));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new ApiResponse(false, "Internal Server Error: " + e.getMessage())));
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonBody = getJsonBody(request);

        String name = jsonBody.has("name") ? jsonBody.get("name").getAsString() : "";
        String studentId = jsonBody.get("studentId").getAsString();
        String password = jsonBody.get("password").getAsString();

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("students");

        // Check if student exists
        Document existingStudent = collection.find(new Document("studentId", studentId)).first();
        if (existingStudent != null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(new ApiResponse(false, "Student with this ID already exists.")));
            return;
        }

        Document newStudent = new Document("name", name)
                .append("studentId", studentId)
                .append("password", password); // Simple text as requested

        collection.insertOne(newStudent);

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write(gson.toJson(new ApiResponse(true, "Registration successful.")));
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonBody = getJsonBody(request);
        String studentId = jsonBody.get("studentId").getAsString();

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("students");

        Document updateFields = new Document();
        String[] fields = {
            "name", "fatherName", "motherName", "dob", "personalEmail", 
            "officialEmail", "phone", "college", "course", "branch", 
            "specialization", "semester", "section", "classRollNo", 
            "enrollNo", "universityRollNo"
        };

        for (String field : fields) {
            if (jsonBody.has(field) && !jsonBody.get(field).isJsonNull()) {
                updateFields.append(field, jsonBody.get(field).getAsString());
            }
        }

        // Handle Profile Image Upload to Cloudinary if provided as Base64
        if (jsonBody.has("profileImage") && !jsonBody.get("profileImage").isJsonNull()) {
            String imageData = jsonBody.get("profileImage").getAsString();
            if (imageData.startsWith("data:image")) {
                try {
                    String imageUrl = com.electivenavigator.utils.CloudinaryConfig.uploadImage(imageData);
                    updateFields.append("profileImage", imageUrl);
                } catch (Exception e) {
                    System.err.println("Cloudinary Upload Error: " + e.getMessage());
                }
            } else if (imageData.startsWith("http")) {
                // Already a URL, just save it
                updateFields.append("profileImage", imageData);
            }
        }

        Document updateDoc = new Document("$set", updateFields);
        collection.updateOne(new Document("studentId", studentId), updateDoc);

        // Fetch the updated student data to return it
        Document updatedStudent = collection.find(new Document("studentId", studentId)).first();
        if (updatedStudent != null) {
            updatedStudent.remove("password");
            updatedStudent.remove("_id");
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Profile updated successfully");
            result.add("data", gson.fromJson(updatedStudent.toJson(), JsonObject.class));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(new ApiResponse(false, "Student not found.")));
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonBody = getJsonBody(request);

        String studentId = jsonBody.get("studentId").getAsString();
        String password = jsonBody.get("password").getAsString();

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("students");

        Document student = collection.find(new Document("studentId", studentId).append("password", password)).first();

        if (student != null) {
            student.remove("password"); // Don't send password back
            student.remove("_id"); // Remove ObjectId to simplify JSON parsing client-side if needed
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Login successful");
            result.add("data", gson.fromJson(student.toJson(), JsonObject.class));

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(gson.toJson(new ApiResponse(false, "Invalid Student ID or password.")));
        }
    }

    private void handleSendOtp(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonBody = getJsonBody(request);
        if (!jsonBody.has("studentId")) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write(gson.toJson(new ApiResponse(false, "Student ID is required.")));
             return;
        }
        String studentId = jsonBody.get("studentId").getAsString();

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("students");

        Document student = collection.find(new Document("studentId", studentId)).first();
        if (student == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(new ApiResponse(false, "No student found with this ID.")));
            return;
        }

        String email = student.getString("personalEmail");
        if (email == null || email.trim().isEmpty()) {
            email = student.getString("officialEmail");
        }

        if (email == null || email.trim().isEmpty()) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write(gson.toJson(new ApiResponse(false, "No email registered for this account. Please contact admin.")));
             return;
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        long expiryTime = System.currentTimeMillis() + (10 * 60 * 1000); // 10 minutes

        // Save OTP and Expiry to user document
        collection.updateOne(new Document("studentId", studentId), 
            new Document("$set", new Document("resetOtp", otp).append("resetOtpExpiry", expiryTime)));

        try {
            EmailService.sendOtpEmail(email, otp);
            
            // Do not send the actual email back for security, maybe mask it.
            String[] emailParts = email.split("@");
            String maskedEmail = emailParts[0].substring(0, Math.min(2, emailParts[0].length())) + "****@" + emailParts[1];
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(new ApiResponse(true, "OTP sent to " + maskedEmail)));
        } catch (Exception e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(new ApiResponse(false, "Error sending email. Please try again later.")));
        }
    }

    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JsonObject jsonBody = getJsonBody(request);
        
        if (!jsonBody.has("studentId") || !jsonBody.has("otp") || !jsonBody.has("newPassword")) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write(gson.toJson(new ApiResponse(false, "studentId, otp, and newPassword are required.")));
             return;
        }

        String studentId = jsonBody.get("studentId").getAsString();
        String otp = jsonBody.get("otp").getAsString();
        String newPassword = jsonBody.get("newPassword").getAsString();

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("students");

        Document student = collection.find(new Document("studentId", studentId)).first();
        if (student == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(new ApiResponse(false, "Invalid request. User not found.")));
            return;
        }

        String storedOtp = student.getString("resetOtp");
        Long expiry = student.getLong("resetOtpExpiry");

        if (storedOtp == null || expiry == null) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write(gson.toJson(new ApiResponse(false, "No OTP request found for this user.")));
             return;
        }

        if (System.currentTimeMillis() > expiry) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write(gson.toJson(new ApiResponse(false, "OTP has expired. Please request a new one.")));
             return;
        }

        if (!storedOtp.equals(otp)) {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
             response.getWriter().write(gson.toJson(new ApiResponse(false, "Invalid OTP.")));
             return;
        }

        // OTP is valid. Reset password and clear OTP fields.
        collection.updateOne(new Document("studentId", studentId), 
            new Document("$set", new Document("password", newPassword))
                .append("$unset", new Document("resetOtp", "").append("resetOtpExpiry", "")));

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(new ApiResponse(true, "Password has been successfully changed.")));
    }

    private JsonObject getJsonBody(HttpServletRequest request) throws IOException {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return JsonParser.parseString(buffer.toString()).getAsJsonObject();
    }

    // Basic API response model
    private static class ApiResponse {
        boolean success;
        String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}

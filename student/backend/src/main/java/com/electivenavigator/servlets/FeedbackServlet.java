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
import java.util.Date;

@WebServlet("/api/feedback")
public class FeedbackServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            JsonObject jsonBody = getJsonBody(request);

            if (!jsonBody.has("studentId") || !jsonBody.has("subjectName") || !jsonBody.has("rating") || !jsonBody.has("comment")) {
                sendError(response, "Missing required fields for feedback.");
                return;
            }

            String studentId = jsonBody.get("studentId").getAsString();
            String subjectName = jsonBody.get("subjectName").getAsString();
            String semester = jsonBody.has("semester") ? jsonBody.get("semester").getAsString() : "unknown";
            int rating = jsonBody.get("rating").getAsInt();
            String comment = jsonBody.get("comment").getAsString();

            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("feedback");

            Document feedbackDoc = new Document("studentId", studentId)
                    .append("subjectName", subjectName)
                    .append("semester", semester)
                    .append("rating", rating)
                    .append("comment", comment)
                    .append("createdAt", new Date());

            collection.insertOne(feedbackDoc);

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Feedback submitted successfully.");
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "Internal Server Error: " + e.getMessage());
        }
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

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("message", message);
        response.getWriter().write(gson.toJson(error));
    }
}

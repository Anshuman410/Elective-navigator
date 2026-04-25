package com.electivenavigator.servlets;

import com.electivenavigator.utils.MongoDBConnection;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/api/announcements")
public class AnnouncementServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("announcements");

            JsonArray announcementsArray = new JsonArray();
            
            // Fetch all announcements, sort by createdAt descending
            try (MongoCursor<Document> cursor = collection.find().sort(new Document("createdAt", -1)).iterator()) {
                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    JsonObject notice = new JsonObject();
                    notice.addProperty("id", doc.getObjectId("_id").toString());
                    notice.addProperty("title", doc.getString("title"));
                    notice.addProperty("content", doc.getString("content"));
                    Long createdAt = doc.getLong("createdAt");
                    if (createdAt != null) {
                        notice.addProperty("createdAt", createdAt);
                    }
                    announcementsArray.add(notice);
                }
            }

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.add("data", announcementsArray);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Internal Server Error: " + e.getMessage());
            response.getWriter().write(gson.toJson(error));
        }
    }
}

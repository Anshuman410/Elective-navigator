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
import java.util.Date;

@WebServlet("/api/queries")
public class QueryServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String studentId = request.getParameter("studentId");
        if (studentId == null || studentId.trim().isEmpty()) {
            sendError(response, "Student ID is required.");
            return;
        }

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("queries");

        Document query = new Document("studentId", studentId);
        JsonArray queriesArray = new JsonArray();

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                doc.remove("_id"); // Exclude ObjectId
                
                JsonObject queryObj = gson.fromJson(doc.toJson(), JsonObject.class);
                queriesArray.add(queryObj);
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.add("data", queriesArray);
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            JsonObject requestBody = gson.fromJson(request.getReader(), JsonObject.class);

            if (requestBody == null || !requestBody.has("studentId") || !requestBody.has("studentName") || !requestBody.has("semester") || !requestBody.has("question")) {
                sendError(response, "Missing required fields.");
                return;
            }

            String studentId = requestBody.get("studentId").getAsString();
            String studentName = requestBody.get("studentName").getAsString();
            String semester = requestBody.get("semester").getAsString();
            String question = requestBody.get("question").getAsString();

            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("queries");

            Document doc = new Document("studentId", studentId)
                    .append("studentName", studentName)
                    .append("semester", semester)
                    .append("question", question)
                    .append("answer", "")
                    .append("resolved", false)
                    .append("createdAt", new Date());

            collection.insertOne(doc);

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Query submitted successfully.");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            sendError(response, "Failed to submit query.");
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("message", message);
        response.getWriter().write(gson.toJson(error));
    }
}

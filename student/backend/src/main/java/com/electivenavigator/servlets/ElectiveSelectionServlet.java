package com.electivenavigator.servlets;

import com.electivenavigator.utils.MongoDBConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/electives/select")
public class ElectiveSelectionServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            JsonObject jsonBody = getJsonBody(request);

            if (!jsonBody.has("studentId") || !jsonBody.has("subjectName")) {
                sendError(response, "Missing required fields: studentId or subjectName.");
                return;
            }

            String studentId = jsonBody.get("studentId").getAsString();
            String subjectName = jsonBody.get("subjectName").getAsString();

            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> collection = db.getCollection("students");

            // Update the student with the selected elective
            var updateResult = collection.updateOne(
                    Filters.eq("studentId", studentId),
                    Updates.set("selectedElective", subjectName)
            );

            if (updateResult.getMatchedCount() == 0) {
                sendError(response, "Student not found.");
                return;
            }

            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "Elective finalized successfully.");
            result.addProperty("selectedElective", subjectName);
            
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

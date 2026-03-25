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

@WebServlet("/api/electives")
public class ElectivesServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String semester = request.getParameter("semester");

        if (semester == null || semester.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "Semester parameter is required.");
            response.getWriter().write(gson.toJson(error));
            return;
        }

        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> collection = db.getCollection("electives");

        // Fetch electives that match the exact semester
        Document query = new Document("semester", semester);
        
        JsonArray electivesArray = new JsonArray();
        
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                doc.remove("_id"); // Exclude ObjectId for clean UI rendering
                
                JsonObject electiveObj = gson.fromJson(doc.toJson(), JsonObject.class);
                electivesArray.add(electiveObj);
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.add("data", electivesArray);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(result));
    }
}

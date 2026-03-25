package com.electivenavigator.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {

    // Using the same URI provided in the admin module
    private static final String URI = "mongodb+srv://sakacoders:saka123@electivenavigator.ognfxkl.mongodb.net/?appName=ElectiveNavigator";
    private static final String DB_NAME = "electivenavigator";
    
    // Singleton approach for the MongoClient instance to manage thread pools efficiently
    private static MongoClient client;

    private MongoDBConnection() {}

    public static synchronized MongoDatabase getDatabase() {
        if (client == null) {
            client = MongoClients.create(URI);
        }
        return client.getDatabase(DB_NAME);
    }
}

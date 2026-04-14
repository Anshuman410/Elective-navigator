package com.electivenavigator.utils;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Map;

public class CloudinaryConfig {
    private static Cloudinary cloudinary;

    static {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dhq04cfj6",
            "api_key", "831544465778852",
            "api_secret", "Lmpygz4mWLLtnqIZ8BrtiCBgWxU",
            "secure", true
        ));
    }

    public static String uploadImage(String fileData) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(fileData, ObjectUtils.emptyMap());
        return (String) uploadResult.get("secure_url");
    }
}

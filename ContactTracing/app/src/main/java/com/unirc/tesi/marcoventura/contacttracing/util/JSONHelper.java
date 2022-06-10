package com.unirc.tesi.marcoventura.contacttracing.util;

import com.google.gson.Gson;

public class JSONHelper {


    public static String getJsonFromObject(Object object_class){
        Gson gson = new Gson();
        return gson.toJson(object_class);
    }

    public static Object getObjectFromJson(String json, Class object_class){
        Gson gson = new Gson();
        return gson.fromJson(json, object_class);
    }
}

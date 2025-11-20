package com.job.util;


public class JsonUtil {
    
   
    public static String escapeJsonString(String str) {
        if (str == null) return "null";
        return "\"" + str.replace("\\", "\\\\")
                          .replace("\"", "\\\"")
                          .replace("\n", "\\n")
                          .replace("\r", "\\r")
                          .replace("\t", "\\t") + "\"";
    }
    
    
    public static String createSuccessResponse(String message) {
        return "{\"status\":\"success\",\"message\":" + escapeJsonString(message) + "}";
    }
    
    /**
     * Create simple error response  
     */
    public static String createErrorResponse(String message) {
        return "{\"status\":\"error\",\"message\":" + escapeJsonString(message) + "}";
    }
    
    
    public static StringBuilder startJsonObject() {
        return new StringBuilder("{");
    }
    
  
    public static String endJsonObject(StringBuilder json) {
        return json.append("}").toString();
    }
    
    /**
     * Add string property
     */
    public static void addProperty(StringBuilder json, String key, String value, boolean isLast) {
        json.append("\"").append(key).append("\":").append(escapeJsonString(value));
        if (!isLast) json.append(",");
    }
    
    /**
     * Add number property
     */
    public static void addProperty(StringBuilder json, String key, int value, boolean isLast) {
        json.append("\"").append(key).append("\":").append(value);
        if (!isLast) json.append(",");
    }
    
    /**
     * Add boolean property
     */
    public static void addProperty(StringBuilder json, String key, boolean value, boolean isLast) {
        json.append("\"").append(key).append("\":").append(value);
        if (!isLast) json.append(",");
    }
    
    /**
     * Start JSON array
     */
    public static StringBuilder startJsonArray() {
        return new StringBuilder("[");
    }
    
    /**
     * End JSON array
     */
    public static String endJsonArray(StringBuilder json) {
        return json.append("]").toString();
    }
}
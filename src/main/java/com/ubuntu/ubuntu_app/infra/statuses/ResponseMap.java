package com.ubuntu.ubuntu_app.infra.statuses;

import java.util.HashMap;
import java.util.Map;

public class ResponseMap { 
    /*
     * Método para crear respuestas de operaciones HTTP
     */
    public static Map<String, String> createResponse(String message){
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("Estado", message);
        return responseMap;
    }
}

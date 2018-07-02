package com.example.android.laptopcontroller;

import com.google.gson.Gson;

/**
 * Created by hp on 28-06-2018.
 */

public class CommandParser {
    public static String parseCommand(OperationData operation) {
        Gson gson = new Gson();
        return gson.toJson(operation, OperationData.class);
    }
}

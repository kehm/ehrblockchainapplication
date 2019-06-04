/* SPDX-License-Identifier: Apache-2.0 */
package com.example.hlfnetworkapplication.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * JSON Parser utility class
 * 
 * @author kehm
 */
public class JSONParser {

    /**
     * Gets JSON pretty print
     *
     * @param strings Array to print
     * @return Pretty string
     */
    public static String getPrettyPrint(String[] strings) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String output = "";
        for (String string : strings) {
            JsonElement element = new JsonParser().parse(string);
            output = output + "\n" + gson.toJson(element);
        }
        return output;
    }
}

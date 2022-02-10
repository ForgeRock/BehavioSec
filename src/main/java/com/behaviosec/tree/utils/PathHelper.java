package com.behaviosec.tree.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static com.behaviosec.isdk.utils.Debug.printDebugMesssage;

public class PathHelper {
    private static String W = "w";
    private static String C = "c";
    public static String replacePath(String timingData, String replacement) {
        String timingData2 = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<ArrayList> jsonList = objectMapper.readValue(timingData, List.class);

            jsonList.forEach((entry) -> process(entry, replacement));
            timingData2 = objectMapper.writeValueAsString(jsonList);
            printDebugMesssage(timingData2);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            timingData2 = timingData;
        }
        return timingData2;
    }

    private static void process(ArrayList entry, String replacement) {
        String initialElement = (String)entry.get(0);
        if (W.equals(initialElement) || C.equals(initialElement)) {
            int size = entry.size();
            entry.remove(size - 1);
            entry.add(replacement);
        }
    }
}

package com.example.mrm.mobile;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

public class StockItem {
    public static String TAG = "stock_item";

    public HashMap<StockItemFields, String> infoMap = new HashMap<>();

    private final String mFallbackString;

    // Builds the internal representation based on the json received from the backend
    public StockItem(String itemInfo, String fallbackString) {
        // Updated by the activity that calls it so a translatable string can be used
        mFallbackString = fallbackString;

        try {
            JSONObject itemInfoJSON = new JSONObject(itemInfo);

            for (StockItemFields field : StockItemFields.values()) {
                infoMap.put(field, parseJSONItem(itemInfoJSON, field.toString()));
            }
        } catch (Exception e) {
            // TODO: Improve error handling
            Log.d(TAG, "Error parsing stock item json: " + e.getMessage());
        }
    }

    public String getMachineCode() {
        return infoMap.get(StockItemFields.code);
    }

    private String parseJSONItem(JSONObject itemInfoJSON, String infoToLookup) {
        String value = itemInfoJSON.optString(infoToLookup, mFallbackString);
        if (value.equals("null")) {
            value = mFallbackString;
        }
        return value;
    }
}

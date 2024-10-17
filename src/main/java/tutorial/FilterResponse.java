package tutorial;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FilterResponse {
    public static List<JSONObject> filterRecords(String jsonResponse) {
        List<JSONObject> filteredRecords = new ArrayList<>();

        // Parse the JSON response
        JSONObject responseObject = new JSONObject(jsonResponse);
        JSONArray records = responseObject.getJSONArray("records");

        // Iterate through the records
        for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i);
            // Check if the record has a probability field
            if (record.has("probability") && record.has("overvalue")) {
                double probability = record.getDouble("probability");
                double overvalue = record.getDouble("overvalue");
                // Filter based on the minimum probability
                if (probability >= 0.25) {
                    double thresholdOvervalue = 0.1 - (probability * 0.05);
                    if(overvalue - 1 >= thresholdOvervalue) {
                        filteredRecords.add(record);
                    }
                }
            }
        }

        return filteredRecords;
    }
}

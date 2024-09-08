package com.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class Main {
    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        JSONObject config = getConfig();

        String steamid = config.getString("steamid64");

        String URL = "https://kztimerglobal.com/api/v2.0/records/top?steamid64=" + steamid
                + "&tickrate=128&stage=0&modes_list_string=kz_timer&has_teleports=false&limit=1000";

        JSONArray records = new JSONArray(getRecords(URL));

        List<String> mapIds = new ArrayList<>();

        for (int i = 0; i < records.length(); i++) {
            JSONObject record = records.getJSONObject(i);
            String mapId = Integer.toString(record.getInt("map_id"));

            if(record.getInt("points") > 800){
                mapIds.add(mapId);
            }
        }

        int start = config.getInt("start");
        int end = config.getInt("end");

        for(String map: getMapsWhereSteamIdAppearsInRange(steamid, start, end, mapIds)) {
            System.out.println(map);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + "ms");
    }

    private static List<String> getMapsWhereSteamIdAppearsInRange(String steamid, int start, int end, List<String> mapIds) {

        List<String> maps = new ArrayList<>();

        int index = 0;
        for(String id: mapIds) {

            System.out.println("" + index++ + " / " + mapIds.size());

            String URL = "https://kztimerglobal.com/api/v2.0/records/top?map_id=" + id + "&tickrate=128&stage=0&modes_list_string=kz_timer&has_teleports=false&limit=" + end;

            JSONArray recordsForMap = new JSONArray(getRecords(URL));

            for (int i = 0; i < recordsForMap.length(); i++) {
                JSONObject record = recordsForMap.getJSONObject(i);
                String steamid64 = record.getString("steamid64");
                if (steamid64.equals(steamid) && i >= start) {
                    maps.add(record.getString("map_name"));
                    break;
                }
            }
        }

        return maps;
    }

    private static String getRecords(String URL) {

        try {
            URL url = new URL(URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                return response.toString();
            } else {
                System.out.println("GET request failed.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        };

        return null;
    }

    private static JSONObject getConfig() {

        StringBuilder result = new StringBuilder();

        try {
            File configFile = new File("kzmapranks\\src\\main\\resources\\config.json");
            Scanner reader = new Scanner(configFile);
            while (reader.hasNextLine()) {
                result.append(reader.nextLine());
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new JSONObject(result.toString());
    }
}
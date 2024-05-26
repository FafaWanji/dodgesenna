package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import java.io.IOException;

public class LeagueClient {

    private static final String API_KEY = "RGAPI-7e3ce78c-84f5-4ace-8a1f-688c8949fd1e";
    private static final String BASE_URL = "https://euw1.api.riotgames.com/lol/";

    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();
        Gson gson = new Gson();

        String summonerName = "Zagreus#Rito"; // replace with an actual summoner name
        String url = BASE_URL + "summoner/v4/summoners/by-name/" + summonerName + "?api_key=" + API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Parse the response
            String jsonData = response.body().string();
            Summoner summoner = gson.fromJson(jsonData, Summoner.class);
            System.out.println("Summoner Name: " + summoner.name);
            System.out.println("Summoner Level: " + summoner.summonerLevel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Define a Summoner class to map the JSON response
    public static class Summoner {
        String id;
        String accountId;
        String puuid;
        String name;
        int profileIconId;
        long revisionDate;
        long summonerLevel;
    }
}

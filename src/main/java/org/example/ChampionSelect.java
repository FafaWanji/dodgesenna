package org.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Credentials;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChampionSelect {

    public static void main(String[] args) {
        String[] apiDetails = getApiDetailsFromLockfile("C:\\Riot Games\\League of Legends\\lockfile");
        if (apiDetails != null) {
            String API_PORT = apiDetails[0];
            String API_TOKEN = apiDetails[1];

            OkHttpClient client = new OkHttpClient();
            Gson gson = new Gson();

            String baseUrl = "https://127.0.0.1:" + API_PORT + "/";
            String champSelectEndpoint = baseUrl + "lol-champ-select/v1/session";

            // Bypassing SSL verification (for local development only)
            OkHttpClient unsafeClient = getUnsafeOkHttpClient();

            boolean championSelectDataAvailable = false;
            while (!championSelectDataAvailable) {
                try {
                    Request request = new Request.Builder()
                            .url(champSelectEndpoint)
                            .header("Authorization", Credentials.basic("riot", API_TOKEN))
                            .build();

                    Response response = unsafeClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String jsonData = response.body().string();
                        JsonObject session = gson.fromJson(jsonData, JsonObject.class);

                        // Check if champion select data is available
                        if (session != null && session.has("actions")) {
                            // Extract picks and bans
                            JsonElement actionsElement = session.get("actions");
                            if (actionsElement.isJsonArray()) {
                                // Iterate over the array of actions
                                boolean allPicksCompleted = true;
                                List<String> picks = new ArrayList<>();
                                for (JsonElement actionElement : actionsElement.getAsJsonArray()) {
                                    for (JsonElement ac : actionElement.getAsJsonArray()) {
                                        var action = ac.getAsJsonObject();
                                        String type = action.get("type").getAsString();
                                        if (type.equals("pick")) {
                                            String championId = action.get("championId").getAsString();
                                            String actorCellId = action.get("actorCellId").getAsString();
                                            String championName = getChampionName(championId);
                                            String team = (Integer.parseInt(actorCellId) <= 5) ? "Blue" : "Red";
                                            picks.add(team + " team picks: " + championName);

                                            // Check if the action is completed
                                            boolean isCompleted = action.get("completed").getAsBoolean();
                                            if (!isCompleted) {
                                                allPicksCompleted = false;
                                            }

                                            // If the enemy team picks a certain champion, play a beep sound
                                            if (team.equals("Red") && championId.equals("84")) {
                                                System.out.println("Akali picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                            else if (team.equals("Red") && championId.equals("235")) {
                                                System.out.println("Senna picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                            else if (team.equals("Red") && championId.equals("29")) {
                                                System.out.println("Twitch picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                            else if (team.equals("Red") && championId.equals("67")) {
                                                System.out.println("Vayne picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                            else if (team.equals("Blue") && championId.equals("84")) {
                                                System.out.println("Akali picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                            else if (team.equals("Blue") && championId.equals("235")) {
                                                System.out.println("Senna picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                            else if (team.equals("Blue") && championId.equals("29")) {
                                                System.out.println("Twitch picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }
                                            else if (team.equals("Blue") && championId.equals("67")) {
                                                System.out.println("Vayne picked");
                                                Toolkit.getDefaultToolkit().beep();
                                            }

                                        }
                                    }
                                }
                                if (allPicksCompleted) {
                                    championSelectDataAvailable = true;
                                    for (String pick : picks) {
                                        System.out.println(pick);
                                    }
                                }
                            }
                        }
                    }
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Wait for a short period before polling again (e.g., 1 second)
                try {
                    Thread.sleep(1000); // 1000 milliseconds = 1 second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("Could not read API details from lockfile.");
        }
    }

    private static String getChampionName(String championId) {
        // This method should map champion IDs to champion names using a mapping file or API
        // For simplicity, it returns a placeholder string
        // You can implement this method based on your specific requirements
        return "Champion " + championId;
    }

    private static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                    new javax.net.ssl.X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (javax.net.ssl.X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String[] getApiDetailsFromLockfile(String lockfilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(lockfilePath))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split(":");
                if (parts.length >= 4) {
                    String apiPort = parts[2];
                    String apiToken = parts[3];
                    return new String[]{apiPort, apiToken};
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

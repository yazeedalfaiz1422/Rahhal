package com.example.rahhal;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class ChatGPT {

    public interface ChatGPTListener {
        void onChatGPTResponse(String response);
    }

    private static class ChatGPTAsyncTask extends AsyncTask<String, Void, String> {

        private ChatGPTListener listener;

        ChatGPTAsyncTask(ChatGPTListener listener) {
            this.listener = listener;
        }

        @Override
        protected String doInBackground(String... params) {
            return query(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            listener.onChatGPTResponse(result);
        }
    }

    public void queryAsync(String message, ChatGPTListener listener) {
        new ChatGPTAsyncTask(listener).execute(message);
    }

    private static String query(String message) {
        String url = "https://api.openai.com/v1/chat/completions";
        String apiKey = "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"; // API key goes here
        String model = "gpt-3.5-turbo"; // current model of chatgpt api

        try {
            // Create the HTTP POST request
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");

            // Build the request body
            String body = "{\"model\": \"" + model + "\", \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}]}";
            con.setDoOutput(true);
            OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
            writer.write(body);
            writer.flush();
            writer.close();

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get the response
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Extract and return the response content
                return extractContentFromResponse(response.toString());
            } else {
                // Handle the error (e.g., log it or throw an exception)
                throw new RuntimeException("HTTP error code: " + responseCode);
            }

        } catch (IOException e) {
            // Handle IO exceptions (e.g., log it or throw an exception)
            throw new RuntimeException(e);
        }
    }

    private static String extractContentFromResponse(String response) {
        int startMarker = response.indexOf("content")+11;
        int endMarker = response.indexOf("\"", startMarker);
        return response.substring(startMarker, endMarker);
    }
}

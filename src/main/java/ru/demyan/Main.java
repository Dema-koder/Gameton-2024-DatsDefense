package ru.demyan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Main {
    private static final String PARTICIPATE = "/play/zombidef/participate";
    private static final String COMMAND = "/play/zombidef/command";
    private static final String GET_INFO = "/play/zombidef/units";
    private static final String GET_ZOMBIE_SPOT = "/play/zombidef/world";
    private static final String GAME_ROUND = "/rounds/zombidef";
    private static final String URL = "https://games-test.datsteam.dev";
    private static final String token = System.getenv("TOKEN");

    private static int numTurns = 0;
    private static Cell[][] table = new Cell[1000][1000];

    private static String participate() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPut request = new HttpPut(URL + PARTICIPATE);
            request.addHeader("X-Auth-Token", token);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("HTTP Status Code: " + statusCode);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    System.out.println("Response: " + result);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "pzdc";
    }

    private static String gameRound() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(URL + GAME_ROUND);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("HTTP Status Code: " + statusCode);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    System.out.println("Response: " + result);
                    return result;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "pzdc";
    }

    private static ResponseGetZombieSpots getZombieSpots() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(URL + GET_ZOMBIE_SPOT);
            request.addHeader("X-Auth-Token", token);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("HTTP Status Code: " + statusCode);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    System.out.println("Response: " + result);

                    ObjectMapper objectMapper = new ObjectMapper();
                    ResponseGetZombieSpots responseObject = objectMapper.readValue(result, ResponseGetZombieSpots.class);
                    return responseObject;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ResponseGetUnits getUnits() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpGet request = new HttpGet(URL + GET_INFO);
            request.addHeader("X-Auth-Token", token);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("HTTP Status Code: " + statusCode);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    System.out.println("Response: " + result);

                    ObjectMapper objectMapper = new ObjectMapper();
                    ResponseGetUnits responseObject = objectMapper.readValue(result, ResponseGetUnits.class);
                    return responseObject;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void makeMove() {
        numTurns++;

    }

    public static void main(String[] args) {
        while (true) {

        }
    }
}
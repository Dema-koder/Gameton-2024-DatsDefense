package ru.demyan;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
    private static boolean[][] used = new boolean[1000][1000];
    private static int dx = 0;
    private static int dy = 0;

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

    private static ArrayList<Position> makeMove() {
        Queue<Position>q = new LinkedList<>();
        for (int i = 0; i < 1000; i++)
            for (int j = 0; j < 1000; j++) {
                used[i][j] = (table[i][j].getType() == TypeCell.DEFAULT);
                q.add(new Position(i, j));
            }

        ArrayList<Position>positionsToBuild = new ArrayList<>();
        List<Position>moves = new ArrayList<>();
        moves.add(new Position(-1, 0));
        moves.add(new Position(1, 0));
        moves.add(new Position(0, -1));
        moves.add(new Position(0, 1));
        while (!q.isEmpty()) {
            var v = q.poll();

            for (var move: moves) {
                int x = move.getX() + v.getX();
                int y = move.getY() + v.getY();

                if (table[x][y].getType() == TypeCell.EMPTY) {
                    positionsToBuild.add(new Position(x - dx, y - dy));
                }
            }
        }
        return positionsToBuild;
    }

    private static void makeRequest() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(URL);
            postRequest.addHeader("Content-Type", "application/json");
            postRequest.addHeader("X-Auth-Token", token);

            // вот тут создать запрос
            RequestBody requestBody = new RequestBody();
            requestBody.setAttack(new Attack[] {
                    new Attack("f47ac10b-58cc-0372-8562-0e02b2c3d479", new Target(1, 1))
            });
            requestBody.setBuild(new Build[] {
                    new Build(1, 1)
            });
            requestBody.setMoveBase(new MoveBase(1, 1));
            //////////////////

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(requestBody);

            StringEntity entity = new StringEntity(json);
            postRequest.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                System.out.println("HTTP Status Code: " + statusCode);

                if (response.getEntity() != null) {
                    String responseBody = new String(response.getEntity().getContent().readAllBytes());
                    System.out.println("Response: " + responseBody);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processDiagonal(int startX, int startY, int dx, int dy) {
        int x = startX;
        int y = startY;
        while (x >= 0 && x < 1000 && y >= 0 && y < 1000) {
            table[x][y].setType(TypeCell.NON);
            x += dx;
            y += dy;
        }
    }

    private static void template() {
        processDiagonal(500, 499, -1, -1);
        processDiagonal(499, 499, -1, -1);
        processDiagonal(499, 501, -1, 1);
        processDiagonal(499, 502, -1, 1);
        processDiagonal(501, 502, 1, 1);
        processDiagonal(502, 502, 1, 1);
        processDiagonal(502, 500, 1, -1);
        processDiagonal(502, 499, 1, -1);

        for (int i = 497; i >= 0; i -= 7) {
            processDiagonal(501, i, -3, -3);
            processDiagonal(501, i - 1, -3, -3);
            processDiagonal(501, i - 2, -3, -3);
            processDiagonal(501, i - 3, -3, -3);
            processDiagonal(500, i - 1, -3, -3);
            processDiagonal(500, i - 2, -3, -3);
            processDiagonal(502, i - 1, -3, -3);
            processDiagonal(502, i - 2, -3, -3);

            processDiagonal(501, i, 3, -3);
            processDiagonal(501, i - 1, 3, -3);
            processDiagonal(501, i - 2, 3, -3);
            processDiagonal(501, i - 3, 3, -3);
            processDiagonal(500, i - 1, 3, -3);
            processDiagonal(500, i - 2, 3, -3);
            processDiagonal(502, i - 1, 3, -3);
            processDiagonal(502, i - 2, 3, -3);
        }

        for (int i = 504; i < 1000; i += 7) {
            processDiagonal(i, 501, 3, -3);
            processDiagonal(i + 1, 501, 3, -3);
            processDiagonal(i + 2, 501, 3, -3);
            processDiagonal(i + 3, 501, 3, -3);
            processDiagonal(i + 1, 500, 3, -3);
            processDiagonal(i + 2, 500, 3, -3);
            processDiagonal(i + 1, 502, 3, -3);
            processDiagonal(i + 2, 502, 3, -3);

            processDiagonal(i, 501, 3, 3);
            processDiagonal(i + 1, 501, 3, 3);
            processDiagonal(i + 2, 501, 3, 3);
            processDiagonal(i + 3, 501, 3, 3);
            processDiagonal(i + 1, 500, 3, 3);
            processDiagonal(i + 2, 500, 3, 3);
            processDiagonal(i + 1, 502, 3, 3);
            processDiagonal(i + 2, 502, 3, 3);
        }

        for (int i = 497; i >= 0; i -= 7) {
            processDiagonal(i, 500, -3, -3);
            processDiagonal(i - 1, 500, -3, -3);
            processDiagonal(i - 2, 500, -3, -3);
            processDiagonal(i - 3, 500, -3, -3);
            processDiagonal(i - 1, 501, -3, -3);
            processDiagonal(i - 2, 501, -3, -3);
            processDiagonal(i - 1, 499, -3, -3);
            processDiagonal(i - 2, 499, -3, -3);

            processDiagonal(i, 500, -3, 3);
            processDiagonal(i - 1, 500, -3, 3);
            processDiagonal(i - 2, 500, -3, 3);
            processDiagonal(i - 3, 500, -3, 3);
            processDiagonal(i - 1, 501, -3, 3);
            processDiagonal(i - 2, 501, -3, 3);
            processDiagonal(i - 1, 499, -3, 3);
            processDiagonal(i - 2, 499, -3, 3);
        }

        for (int i = 504; i < 1000; i += 7) {
            processDiagonal(500, i, 3, 3);
            processDiagonal(500, i + 1, 3, 3);
            processDiagonal(500, i + 2, 3, 3);
            processDiagonal(500, i + 3, 3, 3);
            processDiagonal(499, i + 1, 3, 3);
            processDiagonal(499, i + 2, 3, 3);
            processDiagonal(501, i + 1, 3, 3);
            processDiagonal(501, i + 2, 3, 3);

            processDiagonal(500, i, -3, 3);
            processDiagonal(500, i + 1, -3, 3);
            processDiagonal(500, i + 2, -3, 3);
            processDiagonal(500, i + 3, -3, 3);
            processDiagonal(499, i + 1, -3, 3);
            processDiagonal(499, i + 2, -3, 3);
            processDiagonal(501, i + 1, -3, 3);
            processDiagonal(501, i + 2, -3, 3);
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                table[i][j] = new Cell();
                table[i][j].setType(TypeCell.EMPTY);
            }
        }
        ResponseGetUnits response = getUnits();
        for (var base: response.getBase()) {
            if (base.isHead()) {
                dx = 500 - base.getX();
                dy = 500 - base.getY();
                break;
            }
        }

        for (var base: response.getBase()) {
            int x = base.getX() + dx;
            int y = base.getY() + dy;
            if (x >= 0 && x <= 999 && y >= 0 && y <= 999) {
                table[x][y].setType(base.isHead() ? TypeCell.TOWNHALL :TypeCell.DEFAULT);
            }
        }

        template();

        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++)
                System.out.print(table[i][j].toString());
            System.out.println();
        }
    }
}
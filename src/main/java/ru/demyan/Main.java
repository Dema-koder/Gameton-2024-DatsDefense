package ru.demyan;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.lang.model.type.NullType;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

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

    private static int phase = 0;

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



    private static String makeFire(){
        List<AttackingBlock> fire = new ArrayList<>();

        ResponseGetUnits response = getUnits();
        List<Zombie> zombies = response.getZombies();
        List<EnemyBlock> enemy_blocks = response.getEnemyBlocks();
        zombies.sort(new ZombieComparator());
        for (var base: response.getBase()){
            double min_dist = 15;
            Zombie target_zombie = null;
            int x_base = base.getX();
            int y_base = base.getY();
            for (var zombie: zombies) {
                int x_zombie = zombie.getX();
                int y_zombie = zombie.getY();
                double distance = Math.sqrt(Math.pow(x_base - x_zombie, 2) + Math.pow(y_base - y_zombie, 2));

                if (distance < base.getRange()) {
                    if(distance < min_dist){
                        min_dist = distance;
                        target_zombie = zombie;
                    }
                }
            }
            if(target_zombie != null){
                AttackingBlock block_to_attack = new AttackingBlock(base.getId(), new Target(target_zombie.getX(), target_zombie.getY()));
                fire.add(block_to_attack);
            } else{
                double min_enemy_base_health = 1000;
                EnemyBlock target_block = null;
                for (var enemy_base: enemy_blocks){
                    int x_enemy = enemy_base.getX();
                    int y_enemy = enemy_base.getY();
                    double distance = Math.sqrt(Math.pow(x_base - x_enemy, 2) + Math.pow(y_base - y_enemy, 2));
                    if(enemy_base.isHead() && distance < base.getRange()){
                        target_block = enemy_base;
                        break;
                    } else {
                        if(distance < base.getRange() && enemy_base.getHealth() < min_enemy_base_health){
                            min_enemy_base_health = enemy_base.getHealth();
                            target_block = enemy_base;
                        }
                    }
                }
                if(target_block != null){
                    AttackingBlock block_to_attack = new AttackingBlock(base.getId(), new Target(target_block.getX(), target_block.getY()));
                    fire.add(block_to_attack);
                }
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(fire);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String headRunner(int dx, int dy){
        ResponseGetUnits response = getUnits();
        int x_start = 500;
        int y_start = 500;

        List<Cell> nodes = new ArrayList<>();
        nodes.add(table[x_start+1][y_start-1]); //0
        nodes.add(table[x_start-1][y_start]); //1
        nodes.add(table[x_start][y_start+2]);//2
        nodes.add(table[x_start+2][y_start+1]);//3

        nodes.add(table[x_start-2][y_start-4]);//4
        nodes.add(table[x_start+4][y_start-4]);//5
        nodes.add(table[x_start-4][y_start-3]);//6
        nodes.add(table[x_start-4][y_start+3]);
        nodes.add(table[x_start-3][y_start+5]);
        nodes.add(table[x_start+3][y_start+5]);
        nodes.add(table[x_start+5][y_start-2]);
        nodes.add(table[x_start+5][y_start+4]);

        nodes.add(table[x_start-5][y_start-7]);
        nodes.add(table[x_start+1][y_start-7]);
        nodes.add(table[x_start+7][y_start-7]);
        nodes.add(table[x_start-7][y_start]);
        nodes.add(table[x_start-7][y_start-6]);
        nodes.add(table[x_start-7][y_start+6]);
        nodes.add(table[x_start-6][y_start+8]);
        nodes.add(table[x_start][y_start+8]);
        nodes.add(table[x_start+6][y_start+8]);
        nodes.add(table[x_start+8][y_start-5]);
        nodes.add(table[x_start+8][y_start+1]);
        nodes.add(table[x_start+8][y_start+7]);

        List<Pair<Integer, Integer>> to_come = new ArrayList<>();
        if(nodes.get(4).getType()==TypeCell.DEFAULT && nodes.get(5).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start+1, y_start-1));
        }
        if(nodes.get(6).getType()==TypeCell.DEFAULT && nodes.get(7).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start-1, y_start));
        }
        if(nodes.get(8).getType()==TypeCell.DEFAULT && nodes.get(9).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start, y_start+2));
        }
        if(nodes.get(10).getType()==TypeCell.DEFAULT && nodes.get(11).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start+2, y_start+1));
        }

        if(nodes.get(12).getType()==TypeCell.DEFAULT && nodes.get(13).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start-2, y_start-4));
        }
        if(nodes.get(13).getType()==TypeCell.DEFAULT && nodes.get(14).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start+4, y_start-4));
        }
        if(nodes.get(15).getType()==TypeCell.DEFAULT && nodes.get(16).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start-4, y_start-3));
        }
        if(nodes.get(15).getType()==TypeCell.DEFAULT && nodes.get(17).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start-4, y_start+3));
        }

        if(nodes.get(18).getType()==TypeCell.DEFAULT && nodes.get(19).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start-3, y_start+5));
        }
        if(nodes.get(19).getType()==TypeCell.DEFAULT && nodes.get(20).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start+3, y_start+5));
        }
        if(nodes.get(21).getType()==TypeCell.DEFAULT && nodes.get(22).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start+5, y_start-2));
        }
        if(nodes.get(22).getType()==TypeCell.DEFAULT && nodes.get(23).getType()==TypeCell.DEFAULT){
            to_come.add(new Pair<Integer, Integer>(x_start+5, y_start+4));
        }
        List<Pair<Integer, Integer>> to_final_come = new ArrayList<>();

        int town_hall_health = 0;
        int x_hall = 0;
        int y_hall = 0;
        for(var base: response.getBase()){
            if(base.isHead()){
                town_hall_health = base.getHealth();
                x_hall = base.getX();
                y_hall = base.getY();
                break;
            }
        }
        if(town_hall_health < 250){
            phase = 2 ;
        } else if(phase != 2 && !to_come.isEmpty()){
            phase = 1;
        }


        if(phase == 0){
            return "No movement";
        } else if(phase == 1){
            List<Zombie> zombies = response.getZombies();
            List<EnemyBlock> enemy_blocks = response.getEnemyBlocks();
            for(var node: to_come){
                int x_node = node.getKey();
                int y_node = node.getValue();
                boolean accepted = true;
                for (var zombie: zombies) {
                    int x_zombie = zombie.getX();
                    int y_zombie = zombie.getY();
                    double distance = Math.sqrt(Math.pow(x_node - x_zombie, 2) + Math.pow(y_node - y_zombie, 2));

                    if (distance < 5) {
                        accepted = false;
                        break;
                    }
                }
                if(!accepted){
                    continue;
                }
                for (var enemy_base: enemy_blocks){
                    int x_enemy = enemy_base.getX();
                    int y_enemy = enemy_base.getY();
                    double distance = Math.sqrt(Math.pow(x_node - x_enemy, 2) + Math.pow(y_node - y_enemy, 2));
                    if(distance < 5){
                        accepted = false;
                        break;
                    }
                }
                if(accepted){
                    to_final_come.add(node);
                }
            }
            if (!to_final_come.isEmpty()) {
                // Create a Random instance
                Random random = new Random();

                // Generate a random index within the list size
                int randomIndex = random.nextInt(to_final_come.size());

                // Get the random element from the list
                Pair<Integer, Integer> to_jump = to_final_come.get(randomIndex);
                return to_jump.toString();
            } else {
                phase = 2;
            }

        } else {
            List<Zombie> zombies = response.getZombies();
            List<EnemyBlock> enemy_blocks = response.getEnemyBlocks();
            Pair<Integer, Integer> the_best_place = new Pair<>(x_hall, y_hall);
            int min_score = 10000000;
            for(var base: response.getBase()){
                int zombie_count = 0;
                int enemies_count = 0;
                for (var zombie: zombies) {
                    int x_zombie = zombie.getX();
                    int y_zombie = zombie.getY();
                    double distance = Math.sqrt(Math.pow(x_hall - x_zombie, 2) + Math.pow(y_hall - y_zombie, 2));

                    if (distance < base.getRange()) {
                        zombie_count += 1;
                    }
                }
                for (var enemy: enemy_blocks) {
                    int x_zombie = enemy.getX();
                    int y_zombie = enemy.getY();
                    double distance = Math.sqrt(Math.pow(x_hall - x_zombie, 2) + Math.pow(y_hall - y_zombie, 2));

                    if (distance < base.getRange()) {
                        zombie_count += 1;
                        enemies_count += 1;
                    }
                }
                if(3*zombie_count + enemies_count < min_score){
                    min_score = 3*zombie_count + enemies_count;
                    the_best_place = new Pair<>(base.getX(), base.getY());
                }
            }
            return the_best_place.toString();
        }
        return "Nothing";
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
package ru.demyan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseGetUnits {
    private List<Unit> base;
    private List<EnemyBlock> enemyBlocks;
    private Player player;
    private String realmName;
    private int turn;
    private int turnEndsInMs;
    private List<Zombie> zombies;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Unit {
    private int attack;
    private int health;
    private String id;
    private boolean isHead;
    private Position lastAttack;
    private int range;
    private int x;
    private int y;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class EnemyBlock extends Unit {
    private String name;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Player {
    private int enemyBlockKills;
    private String gameEndedAt;
    private int gold;
    private String name;
    private int points;
    private int zombieKills;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Zombie {
    private int attack;
    private String direction;
    private int health;
    private String id;
    private int speed;
    private String type;
    private int waitTurns;
    private int x;
    private int y;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class Position {
    private int x;
    private int y;
}
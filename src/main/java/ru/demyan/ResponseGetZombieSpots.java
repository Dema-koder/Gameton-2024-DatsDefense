package ru.demyan;

import lombok.Data;

import java.util.List;

@Data
public class ResponseGetZombieSpots {
    private String realmName;
    private List<Zpot> zpots;
}

@Data
class Zpot {
    private int x;
    private int y;
    private String type;
}

package ru.demyan;

import lombok.Data;

@Data
public class Cell {
    private TypeCell type;

    private int where; // 0 - влево, 1 - вправо, 2 - и влево и вправо

    @Override
    public String toString() {
        switch (type) {
            case EMPTY -> {
                return " ";
            }
            case DEFAULT -> {
                return "B";
            }
            case TOWNHALL -> {
                return "X";
            }
            case ZSPOT -> {
                return "#";
            }
        }
        return "";
    }
}

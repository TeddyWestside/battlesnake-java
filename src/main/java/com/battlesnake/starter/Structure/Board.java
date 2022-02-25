package com.battlesnake.starter.Structure;

public record Board(int height,
                    int width,
                    Coord[] food,
                    Battlesnake[] snakes,
                    Coord[] hazards) {
}

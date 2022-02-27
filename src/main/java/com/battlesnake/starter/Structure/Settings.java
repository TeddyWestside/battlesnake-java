package com.battlesnake.starter.Structure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class Settings {
    public long foodSpawnChance;
    public long minimumFood;
    public long hazardDamagePerTurn;
    public Royale royale;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public Squad squad;
}

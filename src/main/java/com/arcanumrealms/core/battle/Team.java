package com.arcanumrealms.core.battle;

/** Les deux camps d'un duel. */
public enum Team {
    /** Les sorciers (joueurs et leurs serviteurs). */
    PLAYERS,
    /** Les creatures adverses. */
    ENEMIES;

    public Team opposite() { return this == PLAYERS ? ENEMIES : PLAYERS; }
}

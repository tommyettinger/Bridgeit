package com.dbcgames.bridgeit;

// Keeps track of the player's current run totals so non-gameplay screens can display them.
public final class RunStats {
    private static int totalScrap;
    private static int totalTreasure;

    private RunStats() { }

    public static void resetTotals() {
        totalScrap = 0;
        totalTreasure = 0;
    }

    public static void setTotals(int totalTreasure, int totalScrap) {
        RunStats.totalTreasure = Math.max(0, totalTreasure);
        RunStats.totalScrap = Math.max(0, totalScrap);
    }

    public static int getTotalScrap() {
        return totalScrap;
    }

    public static int getTotalTreasure() {
        return totalTreasure;
    }
}

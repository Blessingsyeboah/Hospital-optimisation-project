package com.hospitaloptimizer.shared.util;

/**
 * Algorithm parameters derived from team member index numbers, as required
 * by Section 2 ("AI-resistance and localisation requirements") of the brief:
 * "Each team must derive at least three algorithm parameters from member
 * index numbers, for example priority weight, route penalty, hash-table
 * size, random seed or budget constraint."
 *
 * ACTION REQUIRED BEFORE SUBMISSION:
 * Replace the placeholder index numbers below with real member index numbers
 * and recompute the derived constants. Keep the derivation formulas visible
 * in your report (Section 11) so the examiner can verify them live, as they
 * may ask you to "change a hash table size" or similar on the spot.
 */
public final class Params {

    // Real member index numbers -- Pod 1..5, three members each (matches README pod table).
    public static final String[] MEMBER_INDEX_NUMBERS = {
        // Pod 1 -- Data, Database & Delivery
        "22067133", // Ninson Abigail
        "22324067", // Akuffo Samuel
        "22272233", // Dogbe Nicole Eyram
        // Pod 2 -- Lists & Search/Sort
        "22040350", // Lucius Jackson
        "22400499", // Bartah Mohammed Arimeyaw
        "22337184", // Atta-Penkrah Nana Abena Boah
        // Pod 3 -- Queues, Priority & Smart Assignment
        "22196450", // Munkailu Yakubu (Group Leader)
        "22263990", // Okyere Oscar Oroe
        "22302892", // Lamptey Glykeria Odarkor
        // Pod 4 -- Trees, Fast Lookup & Planning Ahead
        "22198877", // Irene Adu
        "22375141", // Ayeh-Kumi Ferdinand Koram
        "22393891", // Awortwe Benedict Kofi
        // Pod 5 -- Hashing, Grouping & Maps
        "22143348", // Yeboah Owusu Blessings
        "22020289", // Alidu Radiyatu
        "22387646"  // Ibrahim Hidayat
    };

    /** Sum of the last two digits of every member index number. */
    public static final int INDEX_DIGIT_SUM = computeDigitSum();

    /** Used as: default hash table starting capacity (must be prime-ish & > team size). */
    public static final int HASH_TABLE_SIZE = nextOddAtLeast(31 + (INDEX_DIGIT_SUM % 20));

    /** Used as: priority weight multiplier in the urgency-based scheduler (Pod 3). */
    public static final double PRIORITY_WEIGHT = 1.0 + (INDEX_DIGIT_SUM % 10) / 10.0;

    /** Used as: route penalty added per unit of road-condition weight (Pod 5 Dijkstra). */
    public static final double ROUTE_PENALTY = 2.0 + (INDEX_DIGIT_SUM % 7);

    /** Used as: random seed for reproducible demos / benchmarks across the team. */
    public static final long RANDOM_SEED = 1000L + INDEX_DIGIT_SUM;

    /** Used as: budget constraint (minutes) for the DP knapsack-style request selection (Pod 4). */
    public static final int BUDGET_MINUTES = 200 + (INDEX_DIGIT_SUM * 3);

    private static int computeDigitSum() {
        int sum = 0;
        for (String idx : MEMBER_INDEX_NUMBERS) {
            String lastTwo = idx.length() >= 2 ? idx.substring(idx.length() - 2) : idx;
            for (char c : lastTwo.toCharArray()) {
                if (Character.isDigit(c)) sum += (c - '0');
            }
        }
        return sum;
    }

    private static int nextOddAtLeast(int n) {
        int v = Math.max(n, 11);
        if (v % 2 == 0) v++;
        return v;
    }

    private Params() {}
}

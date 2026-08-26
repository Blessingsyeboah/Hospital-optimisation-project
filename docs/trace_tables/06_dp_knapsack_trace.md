# Trace Table: DP Request Selection (0/1 Knapsack, budget=25 minutes)

| Request | Urgency (value) | Estimated cost (minutes) |
|---------|------------------|---------------------------|
| R1 | 5 | 10 |
| R2 | 4 | 15 |
| R3 | 3 | 25 |
| R4 | 1 | 45 |

## Tabulation table dp[i][b] (rows = requests considered, cols = budget 0..25, showing every 5)

| i \\ b | 0 | 5 | 10 | 15 | 20 | 25 |
|---|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| 1 | 0 | 0 | 5 | 5 | 5 | 5 |
| 2 | 0 | 0 | 5 | 5 | 5 | 9 |
| 3 | 0 | 0 | 5 | 5 | 5 | 9 |
| 4 | 0 | 0 | 5 | 5 | 5 | 9 |

## Reconstruction

Chosen requests: R1 R2 
Total value = 9, total cost = 25 minutes

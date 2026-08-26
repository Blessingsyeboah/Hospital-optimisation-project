# Trace Table: Kruskal's MST (with Disjoint-Set connectivity trace)

Edges sorted by weight: A-C(1), C-B(2), A-B(4), B-D(5), C-D(8)

| Step | Edge considered | find(u) | find(v) | Same set? | Action |
|------|-------------------|---------|---------|-----------|--------|
| 1 | A-C (w=1) | A | C | no | ACCEPT into MST |
| 2 | C-B (w=2) | A | B | no | ACCEPT into MST |
| 3 | A-B (w=4) | A | A | yes | REJECT (would form a cycle) |
| 3 | B-D (w=5) | A | D | no | ACCEPT into MST |
| 4 | C-D (w=8) | A | A | yes | REJECT (would form a cycle) |

MST edges chosen: A-C, C-B, B-D. Total weight = 8.0

Cross-check via Prim's algorithm: total weight = 8.0 (should match Kruskal's 8.0)

# Trace Table: Dijkstra Shortest Path (from A)

Graph edges: A-B(4), A-C(1), C-B(2), B-D(5), C-D(8)

(Note: the actual GraphAlgorithms.dijkstra() adds Params.ROUTE_PENALTY to every hop; this trace uses raw edge weights for a readable step-by-step illustration.)

| Step | Settled node | Distance table (A,B,C,D) |
|------|--------------|---------------------------|
| 1 | A | A=0, B=4, C=1, D=Infinity |
| 2 | C | A=0, B=3, C=1, D=9 |
| 3 | B | A=0, B=3, C=1, D=8 |
| 4 | D | A=0, B=3, C=1, D=8 |

Final shortest distances from A: {A=0.0, B=3.0, C=1.0, D=8.0}
Predecessor path A->D: A -> C -> B -> D (cost 1+2+5=8, cheaper than A-D direct at 8 -- tie broken by discovery order in this small example).

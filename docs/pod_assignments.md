# Pod & Individual Task Assignments

Per Section 2 of the brief: **"Every member must defend one data structure and one algorithm during the oral demonstration."** The table below assigns each person a specific structure/algorithm pair so nobody walks in unprepared — swap these around as your team sees fit, but keep the list up to date and make sure it matches who actually wrote the code (git blame should back this up).

## Pod 1 — Data, Database & Delivery
*Folder: `pod1_data_database/`*

| Member | Primary files | Structure to defend | Algorithm to defend |
|---|---|---|---|
| Ninson Abigail (L300) | `DataLoader.java`, `schema.sql` | The `algorithm_runs` / `audit_events` schema design | CSV → DB batch-insert loading strategy |
| Akuffo Samuel (L200) | `DispatchDeliveryService.java` | `CustomStack` (via Pod 3) used as the audit/undo log | Undo-via-stack-pop mechanism |
| Dogbe Nicole Eyram (L200) | `Main.java`, `Benchmark.java` | N/A (integration) | The empirical benchmarking methodology (3-run averaging) |

## Pod 2 — Lists & Search/Sort
*Folder: `pod2_lists_search_sort/`*

| Member | Primary files | Structure to defend | Algorithm to defend |
|---|---|---|---|
| Lucius Jackson (L300) | `CustomArrayList.java` | Dynamic array (resize doubling/halving) | — |
| Bartah Mohammed Arimeyaw (L200) | `CustomLinkedList.java` | Doubly linked list + iterator | Linear search |
| Atta-Penkrah Nana Abena Boah (L200) | `SearchAlgorithms.java`, `SortAlgorithms.java` | — | Binary search + merge sort/quicksort |

## Pod 3 — Queues, Priority & Smart Assignment
*Folder: `pod3_queues_priority/`*

| Member | Primary files | Structure to defend | Algorithm to defend |
|---|---|---|---|
| Munkailu Yakubu (L300) | `CustomQueue.java`, `CircularQueue.java` | Circular queue (wrap-around) | FIFO dispatch rule |
| Okyere Oscar Oroe (L200) | `CustomDeque.java`, `CustomStack.java` | Deque (urgent-jumps-front) | — |
| Lamptey Glykeria Odarkor (L200) | `CustomPriorityQueue.java`, `SchedulingEngine.java` | Binary heap | Greedy resource assignment (incl. the documented failure counterexample) |

## Pod 4 — Trees, Fast Lookup & Planning Ahead
*Folder: `pod4_trees_lookup/`*

| Member | Primary files | Structure to defend | Algorithm to defend |
|---|---|---|---|
| Irene Adu (L300) | `CustomBST.java`, `IndexEngine.java` | Binary search tree | Inorder traversal / search-path trace |
| Ayeh-Kumi Ferdinand Koram (L200) | `RedBlackTree.java` | Left-leaning red-black tree (rotations/recoloring) | — |
| Awortwe Benedict Kofi (L200) | `BTree.java`, `DynamicProgramming.java` | B-tree (node splitting) | 0/1 knapsack DP with reconstruction |

## Pod 5 — Hashing, Grouping & Maps
*Folder: `pod5_hashing_grouping_maps/`*

| Member | Primary files | Structure to defend | Algorithm to defend |
|---|---|---|---|
| Yeboah Owusu Blessings (L300) | `CustomHashMap.java`, `CustomHashSet.java` | Separate-chaining hash table | Collision handling / load-factor resize |
| Alidu Radiyatu (L300) | `DisjointSet.java` | Union-find (union by rank + path compression) | Kruskal cycle detection |
| Ibrahim Hidayat (L200) | `Graph.java`, `GraphAlgorithms.java` | Adjacency list + matrix | BFS/DFS/Dijkstra/Prim |

## Suggested git workflow so everyone has commits to point to

1. Each pod works on a branch named `pod<N>-<short-topic>` (e.g. `pod3-priority-queue`).
2. Each person on a pod owns at least one file end-to-end (see "Primary files" above) — write it, write its tests, commit it yourself.
3. Open a PR into `main` per pod once that pod's tests pass locally (`cd podX_.../ && ./test.sh`).
4. Keep commits small and message them by what changed, e.g. `pod3: implement CircularQueue wrap-around + tests`.

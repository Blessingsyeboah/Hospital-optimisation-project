# Hospital Smart Service Operations Optimizer

DCIT 204/308 — Joint Data Structures & Algorithms Project. Team of 15, split into 5 pods of 3. Built over 7 days.

A Ghana-context hospital/clinic dispatch platform: it stores service requests, resources, locations and roads in a SQLite database, loads that data into **custom-built** data structures (no `java.util.HashMap`, `PriorityQueue`, `TreeMap`, etc. for core logic), applies search/sort/graph/greedy/DP algorithms to answer real dispatch questions, and measures how each algorithm scales.

## Quick start (no Maven, no internet required)

```bash
./build.sh   # compiles everything with plain javac
./run.sh     # launches the console menu app
./test.sh    # builds + runs all 92 unit tests across every pod
```

That's it — the only third-party code in this repo is the vendored SQLite JDBC driver in `lib/`, so building never depends on Maven Central being reachable.

## Team & Pod Assignments

| Pod | Members | Owns |
|---|---|---|
| Pod 1 | Ninson Abigail (L300), Akuffo Samuel (L200), Dogbe Nicole Eyram (L200) | Data, Database & Delivery |
| Pod 2 | Lucius Jackson (L300), Bartah Mohammed Arimeyaw (L200), Atta-Penkrah Nana Abena Boah (L200) | Lists & Search/Sort |
| Pod 3 | Munkailu Yakubu (L300), Okyere Oscar Oroe (L200), Lamptey Glykeria Odarkor (L200) | Queues, Priority & Smart Assignment |
| Pod 4 | Irene Adu (L300), Ayeh-Kumi Ferdinand Koram (L200), Awortwe Benedict Kofi (L200) | Trees, Fast Lookup & Planning Ahead |
| Pod 5 | Yeboah Owusu Blessings (L300), Alidu Radiyatu (L300), Ibrahim Hidayat (L200) | Hashing, Grouping & Maps |

Full per-person task breakdown: see [docs/pod_assignments.md](docs/pod_assignments.md).

## Repository structure

```
hospital-optimizer/
├── docs/                        Report evidence: trace tables, performance graphs, notes
├── shared/                      Domain models, SQLite wrapper, CSV utils, index-derived Params, SimpleTest
├── pod1_data_database/          CSV→DB loader, DispatchDeliveryService, Benchmark, Main (console app)
├── pod2_lists_search_sort/      CustomArrayList, CustomLinkedList, search & sort algorithms
├── pod3_queues_priority/        Stack, Queue, CircularQueue, Deque, heap PQ, SchedulingEngine
├── pod4_trees_lookup/           BST, Red-Black Tree, B-Tree, IndexEngine, DP (knapsack) selection
├── pod5_hashing_grouping_maps/  HashMap, HashSet, DisjointSet, Graph, BFS/DFS/Dijkstra/Prim/Kruskal
├── tests/                       Aggregate test runner + trace-table generator
├── lib/                         Vendored sqlite-jdbc.jar (no internet needed to build)
├── build.sh / run.sh / test.sh  Root build/run/test scripts
└── schema.sql, data/*.csv       Seed data (50 locations, 100 roads, 300 requests, 30 resources)
```

Each pod folder is **independently buildable and testable** — see the `build.sh` / `test.sh` inside each pod folder. That's deliberate: every person should be able to work on their own branch, run their own tests, and push without needing anyone else's code to compile first (only `shared/` is a common dependency, by design — it's genuinely shared, not owned by one pod).

## Why no Maven?

This project targets an environment where Maven Central isn't reachable, so instead of `pom.xml` dependency resolution we vendor the one real dependency (`sqlite-jdbc.jar`) directly in `lib/`, and use a small dependency-free test framework (`shared/.../util/SimpleTest.java`) instead of JUnit. If your machine *does* have normal internet access, you're welcome to convert this to a Maven/Gradle project — the package structure (`com.hospitaloptimizer.*`) already matches what Maven expects.



## License / academic integrity

Submitted as coursework for DCIT 204/308. See `docs/evidence_note.md` for the AI-assistance disclosure required by the brief.

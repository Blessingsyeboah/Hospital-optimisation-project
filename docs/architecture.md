# System Architecture & Module Design

## High-level flow

```
CSV seed files (shared/src/main/resources/data/*.csv)
        │  (one-time, only if tables are empty)
        ▼
   SQLite database (hospital_optimizer.db)   <-- Pod 1 (DataLoader, Database)
        │  (every startup)
        ▼
Plain Java model lists: List<Location>, List<Road>, List<ServiceRequest>, List<Resource>
        │
        ├──▶ Pod 2 custom structures (CustomArrayList / CustomLinkedList) + search/sort
        ├──▶ Pod 3 custom structures (Stack/Queue/CircularQueue/Deque/Heap) + SchedulingEngine
        ├──▶ Pod 4 custom structures (BST/RedBlackTree/BTree) + IndexEngine + DynamicProgramming
        └──▶ Pod 5 custom structures (HashMap/HashSet/DisjointSet/Graph) + GraphAlgorithms
        │
        ▼
   Main.java console menu (Pod 1) -- orchestrates all of the above,
   and Benchmark.java (Pod 1) times each algorithm and writes results
   back to the algorithm_runs table + docs/performance/results.csv
```

**Key design decision:** the database is only ever touched by `Database` and
`DataLoader` (both in `shared`/`pod1`). Every other pod works purely with
plain Java model objects (`Location`, `Road`, `ServiceRequest`, `Resource`)
and their own custom structures — this keeps JDBC out of algorithm code and
means Pods 2–5 can be developed and tested with zero database dependency
(see each pod's own unit tests, which never touch SQL).

## Module responsibilities (maps to Section 6 of the brief)

| Module | Owning pod | Responsibility |
|---|---|---|
| M1. Input-output & problem spec | All pods (see `docs/pod_assignments.md`) | Pseudocode/preconditions documented per-class in Javadoc comments |
| M2. Database & data loader | Pod 1 | `Database.java`, `DataLoader.java`, `schema.sql` |
| M3. Custom data-structure library | Pods 2–5 | See each pod's `src/main/java` |
| M4. Searching & sorting engine | Pod 2 | `SearchAlgorithms.java`, `SortAlgorithms.java` |
| M5. Service scheduling engine | Pod 3 | `SchedulingEngine.java` |
| M6. Indexing engine | Pod 4 | `IndexEngine.java` |
| M7. Graph route engine | Pod 5 | `Graph.java`, `GraphAlgorithms.java` |
| M8. Optimisation engine | Pod 3 (greedy) + Pod 4 (DP) | `SchedulingEngine.greedyAssignResources()`, `DynamicProgramming.java` |
| M9. Testing & correctness evidence | All pods | `src/test/java` in every pod, `tests/` aggregate runner |
| M10. Empirical efficiency lab | Pod 1 | `Benchmark.java`, `docs/performance/` |

## Database schema

See `schema.sql` at the repo root (also embedded in `shared/src/main/resources/schema.sql`, loaded automatically on startup). Six tables: `locations`, `roads`, `service_requests`, `resources`, `algorithm_runs`, `audit_events` — matching Section 4's suggested schema exactly.

## Why a console menu, not a GUI

Section 1 explicitly states *"This is not a UI-design project."* `Main.java`
provides the console menu required by Section 8 ("a console menu or simple
GUI that allows an examiner to run demonstrations without editing source
code") without spending the team's limited time on UI polish that isn't
assessed.

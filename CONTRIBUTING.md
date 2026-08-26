# Contributing

This is a 5-pod, 15-person team project. To keep everyone unblocked and give
each person real commits to point to, please follow this workflow.

## Setup

You need a JDK (17+). Nothing else — no Maven, no internet connection
required. Clone the repo and run:

```bash
./build.sh   # compiles everything
./run.sh     # launches the console app
./test.sh    # runs all 92+ unit tests
```

## Working on your pod

Each pod folder is independently buildable/testable:

```bash
cd pod3_queues_priority
./build.sh   # compiles just this pod (+ shared)
./test.sh    # runs just this pod's tests
```

Do your work inside your pod's folder. `shared/` is common ground everyone
depends on — if you need to change something there, flag it to the other
pods first, since it affects all of them.

## Branching

- One branch per pod, or per feature within a pod: `pod4-red-black-tree`,
  `pod5-dijkstra`, etc.
- Small, focused commits. Message them by what changed:
  `pod2: implement CustomLinkedList.insertAfter + tests`.
- Every class you add needs at least one test covering: a normal case, a
  boundary case (empty/single-element/etc.), and an invalid-input case
  where relevant (see the existing tests for examples of this pattern).

## Before opening a PR into `main`

1. `cd` into your pod folder and run `./test.sh` — it must pass.
2. From the repo root, run `./test.sh` too — this catches integration
   issues between pods (e.g. Pod 1's `Main.java` calling into your code).
3. Update `docs/pod_assignments.md` if who-owns-what changed.

## Adding a new custom data structure or algorithm

- Put it in the right pod's `src/main/java/com/hospitaloptimizer/podN/`
  package — check `README.md`'s structure table if unsure which pod owns
  it.
- Do NOT use `java.util.HashMap`, `PriorityQueue`, `TreeMap`, `Stack`,
  `ArrayDeque` or similar for core assessed logic (Section 8 of the
  brief) — that's the whole point of this project. `java.util.*` is fine
  for file I/O, JDBC, or scaffolding around your own implementation (e.g.
  `List<T>` to hold results, `Comparator<T>` interfaces).
- Add tests in the matching `src/test/java/...` package, then wire your
  test class into `tests/src/main/java/com/hospitaloptimizer/tests/RunAllTests.java`
  so it's included in the full suite.

## Trace tables / evidence

If your algorithm needs a trace table for the report (binary search,
sorts, Dijkstra, Kruskal/Prim, DP — see Section 10), add a generator method
to `tests/src/main/java/com/hospitaloptimizer/tests/TraceTableGenerator.java`
and re-run it to refresh `docs/trace_tables/`.

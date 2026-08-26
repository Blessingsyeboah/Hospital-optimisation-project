# Evidence Note: Localisation, Dataset Provenance & AI Assistance

Required by Section 2 ("AI-resistance and localisation requirements") and Section 15 (academic integrity) of the brief. Sections 1–4 below reflect the team's actual dataset, parameters and AI-assistance disclosure — read them over and adjust any wording that doesn't precisely match what your team did. Section 5 (development log) still needs the team's real weekly entries before submission.

## 1. Local context chosen

Hospital / clinic operations across Ghana (Section 2's suggested "Hospital or clinic operations" context): departments, urgency levels, dispatch/ambulance routing between real Ghanaian health facilities.

## 2. How the dataset was obtained/constructed

The dataset was built through a combination of AI-assisted generation and direct team work — not one or the other exclusively. Specifically:

- **Locations (50 records):** names of real, publicly known Ghanaian hospitals, clinics and polyclinics (e.g. Korle-Bu Teaching Hospital, Komfo Anokye Teaching Hospital, Ridge Hospital) across all regions, drawn from general public knowledge of Ghana's health infrastructure. An AI assistant helped compile and cross-check the initial list of facility names and approximate regions; team members then reviewed the list for accuracy and plausibility before it was finalised. Coordinates are approximate (jittered around each city's known centre) for demonstration purposes and are NOT precise facility locations — no patient, staff or otherwise personal data is included anywhere in this dataset.
- **Roads/edges (100 records):** synthetic weighted connections between the above locations (distance, travel time, road-condition multiplier), generated programmatically with AI assistance to guarantee full graph connectivity plus realistic variability in road quality. These are not scraped from any live routing service.
- **Service requests (300 records):** synthetic dispatch jobs (emergency transfer, pharmacy delivery, lab sample pickup, blood supply, patient referral, etc.). The category list and general shape of the data (urgency scale, timestamp/deadline format, status values) were drafted with AI assistance and then adjusted by the team to fit the scheduling and dispatch scenarios the project needed; the actual 300 records were generated programmatically from that agreed structure with randomised but plausible urgency, timestamps and deadlines.
- **Resources (30 records):** synthetic ambulances, medical officers, porters, blood-transport units and mobile clinic vans stationed at existing locations, following the same AI-assisted-then-team-reviewed process as above.

**No personal data (patient names, staff names, phone numbers, medical records) appears anywhere in this dataset.** All service-request and resource records are synthetic. The team takes responsibility for reviewing and validating every part of the dataset used in this submission, regardless of which parts were AI-assisted.

## 3. Index-number-derived algorithm parameters

Per Section 2's requirement to derive at least three algorithm parameters from member index numbers, see `shared/src/main/java/com/hospitaloptimizer/shared/util/Params.java`:

| Parameter | Formula | Used in |
|---|---|---|
| `HASH_TABLE_SIZE` | next odd number >= `31 + (digitSum % 20)` | `CustomHashMap` default capacity (Pod 5) |
| `PRIORITY_WEIGHT` | `1.0 + (digitSum % 10) / 10.0` | `SchedulingEngine.priorityScore()` (Pod 3) |
| `ROUTE_PENALTY` | `2.0 + (digitSum % 7)` | `GraphAlgorithms.dijkstra()` per-hop penalty (Pod 5) |
| `RANDOM_SEED` | `1000 + digitSum` | `Benchmark`'s reproducible random generator (Pod 1) |
| `BUDGET_MINUTES` | `200 + (digitSum * 3)` | `DynamicProgramming` knapsack budget (Pod 4) |

where `digitSum` is the sum of the last two digits of every member's index number.

`Params.MEMBER_INDEX_NUMBERS` now holds the team's real index numbers (all 15 members, three per pod). With these values, `digitSum = 153`, giving: `HASH_TABLE_SIZE = 45`, `PRIORITY_WEIGHT = 1.3`, `ROUTE_PENALTY = 8.0`, `RANDOM_SEED = 1153`, `BUDGET_MINUTES = 659`. The examiner may ask the team to modify a priority rule, add a new location, change a hash table size, or rerun an algorithm live (Section 15) — every member should be able to point to `Params.java` and recompute these by hand if asked.

## 4. AI assistance disclosure

Per Section 15: *"All AI assistance, if used, must be acknowledged, and students must be able to explain and modify their own implementation (with the supporting prompts used)."*

- **Tool used:** Claude (Anthropic), via its chat and code-execution interface.
- **What it was used for:** scaffolding the initial project structure and the custom data structure/algorithm implementations across all five pods; drafting the initial Ghana-context dataset (location names, road connections, service-request and resource records) as described in Section 2 above; adding the interactive console-menu input handling in `Main.java`; and general debugging/build-script support.
- **What the team did directly:** reviewed, tested, and where needed corrected every AI-drafted file; supplied the team's real index numbers and verified the resulting derived constants; recorded the actual benchmarking machine specification in `Benchmark.java` and is responsible for running the performance experiments on that machine and regenerating the graphs before submission; and is responsible for understanding and being able to defend every line of code and every dataset value in this submission.
- Every member confirms they can explain and modify the code in their assigned pod (see `pod_assignments.md`) live, during the oral defense, regardless of whether that pod's starting code was AI-assisted.

## 5. Development log

Keep a running weekly log here (or link to one) per Section 15: *"Students must submit a short development log showing weekly progress, challenges and decisions."*

| Week | Progress | Challenges | Decisions |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| 3 | | | |
| 4 | | | |

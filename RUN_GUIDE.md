# How to Run CareRoute — Hospital Operations Console

This guide walks you through building and running the project from scratch on Windows, macOS, or Linux.

---

## Prerequisites

| Requirement | Version | Check |
|---|---|---|
| **JDK** | 17 or later | `java -version` |
| **Git** (optional) | Any | `git --version` |

No Maven, Gradle, Node.js, or internet connection needed. The only external dependency (`sqlite-jdbc.jar`) is included in the repo.

---

## Step 1 — Clone the repository

```bash
git clone https://github.com/samuelakuffo123/Hospital-optimisation-project.git
cd Hospital-optimisation-project
```

---

## Step 2 — Build

### Linux / macOS (Git Bash, WSL, terminal)

```bash
chmod +x build.sh run.sh test.sh
./build.sh
```

### Windows (PowerShell)

```powershell
# From the project root directory
$SEP = ";"
$CP = "lib\sqlite-jdbc.jar;build\app"

if (-not (Test-Path build\app)) { New-Item -ItemType Directory -Force -Path build\app | Out-Null }

# Compile shared
$files = Get-ChildItem -Recurse shared\src\main\java -Filter *.java | Select-Object -ExpandProperty FullName
javac -d build\app -cp "lib\sqlite-jdbc.jar" @files

# Compile each pod in order
foreach ($pod in @("pod2_lists_search_sort","pod3_queues_priority","pod4_trees_lookup","pod5_hashing_grouping_maps","pod1_data_database")) {
    $files = Get-ChildItem -Recurse "$pod\src\main\java" -Filter *.java | Select-Object -ExpandProperty FullName
    javac -d build\app -cp $CP @files
}

Write-Host "Build succeeded."
```

> **Tip:** You can save the PowerShell commands above as `build.ps1` and run `.\build.ps1`.

A successful build creates compiled `.class` files under `build/app/`.

---

## Step 3 — Run the Web Frontend (CareRoute GUI)

### Linux / macOS

```bash
java -cp "lib/sqlite-jdbc.jar;build/app" com.hospitaloptimizer.pod1.WebServer
```

### Windows (PowerShell)

```powershell
java -cp "lib\sqlite-jdbc.jar;build\app" com.hospitaloptimizer.pod1.WebServer
```

You will see:

```
CareRoute frontend: http://127.0.0.1:8091/
```

Open **http://127.0.0.1:8091** in your browser. The GUI is ready to use.

---

## Step 4 — Run the Console App (Alternative)

If you prefer the text-based menu instead of the browser:

### Linux / macOS

```bash
java -cp "lib/sqlite-jdbc.jar;build/app" com.hospitaloptimizer.pod1.Main
```

### Windows

```powershell
java -cp "lib\sqlite-jdbc.jar;build\app" com.hospitaloptimizer.pod1.Main
```

Choose from 8 interactive demos by typing 1–8.

---

## Step 5 — Run Unit Tests

### Linux / macOS

```bash
java -cp "lib/sqlite-jdbc.jar;build/app" com.hospitaloptimizer.tests.RunAllTests
```

### Windows

```powershell
java -cp "lib\sqlite-jdbc.jar;build\app" com.hospitaloptimizer.tests.RunAllTests
```

This runs all 92 unit tests across every pod and reports pass/fail.

---

## Using the Web Frontend

The browser GUI has **8 views** that mirror every console demo:

### 1. Overview (Dashboard)
- Open request count, critical priority count, available resources, active locations
- Priority dispatch queue (top 5 highest-urgency requests)
- Status breakdown bar chart

### 2. Service Requests
- Full table of all 300 requests with search, status filter, and urgency filter
- Click **Assign** on any PENDING request to open the resource assignment dialog
- Click **New request** to create a new dispatch request (source, destination, category, urgency)

### 3. Resources
- Grid of all 30 resources with availability status (AVAILABLE / BUSY / OFFLINE)

### 4. Route Planner
- Select a FROM and TO location from the dropdown
- Click **Calculate route** to run Dijkstra's algorithm
- Shows weighted minutes, full path with location names, and hop count

### 5. Dispatch Console (Pod 1)
- Click **Run greedy dispatch** to auto-assign resources to pending requests in urgency order
- View the assignment results (request -> resource)
- Click **Undo last** to pop the most recent action from the audit stack

### 6. Search & Sort Lab (Pod 2)
- **Sorting:** Choose algorithm (QuickSort, MergeSort, InsertionSort, SelectionSort), direction (ascending/descending urgency), and top N to display. Shows execution time.
- **Searching:** Enter a request ID to compare linear search O(n) vs binary search O(log n). Shows index, comparison count, and execution time for both.

### 7. Algorithm Lab
- **Scheduling Engine:** Compare FIFO, Urgency, and Priority Score dispatch orders
- **Budget Selection:** Drag the slider to set a response-time budget (60–720 min). The 0/1 Knapsack DP selects the optimal set of requests.
- **Tree Lookup:** Enter a request ID to search BST, Red-Black Tree, and B-Tree indexes. Shows tree heights and search results for each.
- **MST:** Run Prim and Kruskal algorithms to find the minimum-cost road network. Expand to see full edge lists.
- **Graph Traversal:** Pick a starting location and run BFS (breadth-first) or DFS (depth-first). Shows visited order and execution time.
- **DP Trace:** Shows the knapsack budget selection summary.

### 8. Benchmarks
- Click **Execute benchmarks** to run all Section 9 performance experiments (search, sort, HashMap, heap, graph algorithms)
- Results are displayed in comparison tables and saved to the database

---

## Troubleshooting

| Problem | Fix |
|---|---|
| `java: command not found` | Install JDK 17+ from [Adoptium](https://adoptium.net/) and make sure `java` is on your PATH |
| `Could not find or load main class` | Make sure you ran the build step first and are in the project root directory |
| `hospital_optimizer.db` locked | Close any other instance of the app before starting a new one |
| Port 8091 already in use | Pass a different port: `java -cp "lib\sqlite-jdbc.jar;build/app" com.hospitaloptimizer.pod1.WebServer 9090` |
| Frontend shows "Start the Java WebServer" | The Java backend is not running — start it with the WebServer command above |
| CRLF warnings on Windows | These are harmless — Git is normalising line endings |

---

## API Reference

All endpoints are served from the same process at `http://127.0.0.1:8091`.

| Endpoint | Method | Description |
|---|---|---|
| `/api/bootstrap` | GET | Returns all requests, resources, and locations as JSON |
| `/api/assign` | POST | Assign a resource to a request (body: `requestId`, `resourceId`) |
| `/api/route?from=X&to=Y` | GET | Dijkstra shortest path between two locations |
| `/api/requests` | POST | Create a new service request |
| `/api/algorithm?action=schedule&mode=fifo\|urgency\|score` | GET | Scheduling engine demo |
| `/api/algorithm?action=budget&minutes=N` | GET | DP budget selection |
| `/api/algorithm?action=index&requestId=X` | GET | Tree index lookup |
| `/api/algorithm?action=mst` | GET | MST total weights |
| `/api/sort?algorithm=X&direction=Y&topN=N` | GET | Sort requests (algorithms: quickSort, mergeSort, insertionSort, selectionSort) |
| `/api/search?requestId=X` | GET | Linear vs binary search comparison |
| `/api/dispatch` | POST | Greedy resource assignment |
| `/api/undo` | POST | Undo last dispatch action |
| `/api/audit-log` | GET | Audit log depth |
| `/api/benchmarks` | GET | Run all performance experiments |
| `/api/graph-traversal?start=X&algorithm=bfs\|dfs` | GET | BFS or DFS traversal |
| `/api/mst-detail` | GET | Prim + Kruskal with full edge lists |
| `/api/index-detail?requestId=X` | GET | BST/RedBlackTree/B-Tree lookup with tree heights |

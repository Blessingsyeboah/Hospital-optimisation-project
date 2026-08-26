/* ================================================================
   CareRoute  --  Frontend application logic
   All algorithm demos that the console provides are exposed here.
   ================================================================ */

// ---- Fallback data (offline mode, mirrors CSVs) ----
const fallbackRequests = [
  ["REQ0001","L039","L021","Outreach Visit",3,"2026-06-03 11:15","2026-06-03 23:15","PENDING"],
  ["REQ0007","L012","L009","Patient Referral",5,"2026-06-06 11:15","2026-06-06 12:15","ASSIGNED"],
  ["REQ0009","L032","L035","Emergency Transfer",3,"2026-06-05 11:00","2026-06-05 23:00","ASSIGNED"],
  ["REQ0010","L048","L027","Lab Sample Pickup",5,"2026-06-02 22:00","2026-06-02 23:00","ASSIGNED"],
  ["REQ0011","L004","L007","Equipment Transport",3,"2026-06-06 22:30","2026-06-07 10:30","PENDING"],
  ["REQ0013","L013","L011","Blood Supply",2,"2026-06-04 06:30","2026-06-05 06:30","PENDING"],
  ["REQ0015","L041","L048","Outreach Visit",5,"2026-06-05 19:45","2026-06-05 20:45","PENDING"]
];
const fallbackResources = [
  ["R002","Porter","L019",2,"AVAILABLE"],
  ["R003","Blood Transport Unit","L026",1,"AVAILABLE"],
  ["R006","Mobile Clinic Van","L034",1,"AVAILABLE"],
  ["R008","Ambulance","L018",2,"AVAILABLE"],
  ["R011","Medical Officer","L041",1,"AVAILABLE"],
  ["R017","Medical Officer","L039",4,"AVAILABLE"],
  ["R020","Ambulance","L002",2,"BUSY"],
  ["R021","Ambulance","L012",1,"OFFLINE"]
];

let requests = [], resources = [], locations = [], selectedRequest = null, auditLogDepth = 0;
const $ = s => document.querySelector(s);
const $$ = s => [...document.querySelectorAll(s)];

// ---- Utility ----
function parseCSV(text) {
  const [head, ...rows] = text.trim().split(/\r?\n/);
  const keys = head.split(',');
  return rows.map(row => Object.fromEntries(row.split(',').map((v, i) => [keys[i], v])));
}

async function load(path, fallback, keys) {
  try {
    const result = await fetch(path);
    if (!result.ok) throw Error();
    return parseCSV(await result.text());
  } catch {
    return fallback.map(row => Object.fromEntries(row.map((v, i) => [keys[i], String(v)])));
  }
}

function urgency(n) {
  const labels = { 5: 'Critical', 4: 'High', 3: 'Medium', 2: 'Low', 1: 'Routine' };
  return `<span class="urgency u${n}">${labels[n] || n} \u00b7 ${n}/5</span>`;
}

function openRequests() {
  return requests.filter(r => !['COMPLETED', 'CANCELLED'].includes(r.status));
}

function fmtNs(ns) {
  if (ns < 1000) return ns + ' ns';
  if (ns < 1e6) return (ns / 1e3).toFixed(1) + ' \u03bcs';
  if (ns < 1e9) return (ns / 1e6).toFixed(2) + ' ms';
  return (ns / 1e9).toFixed(3) + ' s';
}

function locationName(id) {
  const loc = locations.find(l => l.locationId === id);
  return loc ? loc.name : id;
}

// ---- DASHBOARD ----
function renderDashboard() {
  const open = openRequests();
  const available = resources.filter(r => r.availabilityStatus === 'AVAILABLE');
  $('#open-total').textContent = open.length;
  $('#critical-total').textContent = open.filter(r => +r.urgency >= 5).length;
  $('#available-total').textContent = available.length;
  $('#location-total').textContent = locations.length || 50;
  $('#resource-total').textContent = resources.length;
  $('#pending-count').textContent = open.length;
  $('#audit-depth').textContent = auditLogDepth;

  const priority = [...open].sort((a, b) => b.urgency - a.urgency).slice(0, 5);
  $('#priority-list').innerHTML = priority.map(r =>
    `<div class="priority-item"><div class="priority-icon">${r.category[0]}</div><div><strong>${r.requestId} \u00b7 ${r.category}</strong><small>${r.source} \u2192 ${r.destination} \u00b7 Due ${r.deadline.slice(11)}</small></div>${urgency(r.urgency)}</div>`
  ).join('') || '<p class="muted">No active requests.</p>';

  const counts = ['PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED'].map(s => [s, requests.filter(r => r.status === s).length]);
  const max = Math.max(...counts.map(x => x[1]), 1);
  $('#status-bars').innerHTML = counts.map(([s, n]) =>
    `<div class="bar-row"><span>${s.replace('_', ' ')}</span><div class="bar"><i style="width:${n / max * 100}%"></i></div><b>${n}</b></div>`
  ).join('');
}

// ---- SERVICE REQUESTS ----
function renderRequests() {
  const q = $('#request-search').value.toLowerCase();
  const status = $('#status-filter').value;
  const urg = $('#urgency-filter').value;
  const list = requests.filter(r =>
    (status === 'ALL' || r.status === status) &&
    (urg === 'ALL' || r.urgency === urg) &&
    Object.values(r).join(' ').toLowerCase().includes(q)
  );
  $('#results-count').textContent = `${list.length} shown`;
  $('#requests-table').innerHTML = list.map(r =>
    `<tr>
      <td><strong>${r.requestId}</strong><br><span class="muted">${r.category}</span></td>
      <td>${r.source} <span class="muted">\u2192</span> ${r.destination}</td>
      <td>${urgency(r.urgency)}</td>
      <td><span class="status ${r.status}">${r.status.replace('_', ' ')}</span></td>
      <td>${r.deadline}</td>
      <td>${r.status === 'PENDING' ? `<button class="assign-button" data-request="${r.requestId}">Assign</button>` : ''}</td>
    </tr>`
  ).join('') || '<tr><td colspan="6" class="muted">No service requests match these filters.</td></tr>';
}

// ---- RESOURCES ----
function renderResources() {
  const list = [...resources].sort((a, b) => a.availabilityStatus.localeCompare(b.availabilityStatus));
  $('#resource-grid').innerHTML = list.map(r =>
    `<article class="resource"><div class="resource-top"><strong>${r.resourceId}</strong><span class="availability ${r.availabilityStatus}">${r.availabilityStatus}</span></div><p>${r.type}</p><p>Home base: ${r.homeLocation} \u00b7 Capacity ${r.capacity}</p></article>`
  ).join('');
}

// ---- ROUTE PLANNER ----
function populateRoutes() {
  const opts = (locations.length ? locations : [{ locationId: 'L039', name: 'Regional Hospital' }, { locationId: 'L021', name: 'District Hospital' }])
    .map(l => `<option value="${l.locationId}">${l.locationId} \u2014 ${l.name}</option>`).join('');
  ['#route-from', '#route-to', '#new-source', '#new-destination'].forEach(s => $(s).innerHTML = opts);
  if (locations.length) {
    $('#route-from').value = locations[0].locationId;
    $('#route-to').value = locations[Math.min(10, locations.length - 1)].locationId;
  }
  // Populate traversal start dropdown
  const traversalOpts = (locations.length ? locations : []).map(l =>
    `<option value="${l.locationId}">${l.locationId} \u2014 ${l.name}</option>`
  ).join('');
  if ($('#traversal-start')) $('#traversal-start').innerHTML = traversalOpts || '<option>No locations loaded</option>';
}

async function calcRoute() {
  const a = $('#route-from').value, b = $('#route-to').value;
  if (a === b) { $('#route-result').innerHTML = '<strong>Choose two different locations.</strong>'; return; }
  try {
    const response = await fetch(`/api/route?from=${encodeURIComponent(a)}&to=${encodeURIComponent(b)}`);
    const data = await response.json();
    if (!response.ok) throw Error(data.error);
    const hops = data.path.length;
    $('#route-result').innerHTML =
      `<strong>${data.minutes} weighted minutes</strong>` +
      `<br><span class="muted">Dijkstra path (${hops - 1} hops): ${data.path.map(id => `${id} (${locationName(id)})`).join(' \u2192 ')}</span>` +
      `<br><span class="muted">Includes ${hops - 1}x route penalty per hop (Params.ROUTE_PENALTY = 8.0)</span>`;
    // Update visualization nodes
    const nodes = $$('.route-node');
    data.path.forEach((id, i) => { if (nodes[i]) nodes[i].textContent = id; });
  } catch (error) {
    $('#route-result').innerHTML = `<strong>Route unavailable</strong><br><span class="muted">${error.message}</span>`;
  }
}

// ---- SCHEDULING (Algorithm Lab) ----
async function renderSchedule(order = 'fifo') {
  try {
    const data = await (await fetch(`/api/algorithm?action=schedule&mode=${order}`)).json();
    const list = data.requests || [];
    const modeLabels = { fifo: 'FIFO (first-come-first-served)', urgency: 'Urgency (highest first)', score: 'Priority score (urgency \u00d7 weight)' };
    $('#schedule-list').innerHTML = `<li class="schedule-meta">${modeLabels[order] || order} \u2014 ${list.length} requests</li>` +
      list.slice(0, 12).map((r, i) =>
        `<li><strong>${i + 1}. ${r.requestId}</strong> ${urgency(r.urgency)}<small>${r.category} \u00b7 ${r.source} \u2192 ${r.destination}</small></li>`
      ).join('');
  } catch {
    $('#schedule-list').innerHTML = '<li>Start the Java WebServer to run this algorithm.</li>';
  }
}

// ---- BUDGET SELECTION (Dynamic Programming) ----
async function renderBudget() {
  const budget = +$('#budget-input').value;
  $('#budget-value').textContent = `${budget} min`;
  try {
    const data = await (await fetch(`/api/algorithm?action=budget&minutes=${budget}`)).json();
    const valueWeight = data.requests.length > 0
      ? `<br>Value/cost ratio: ${(data.totalValue / (data.totalCost || 1)).toFixed(2)}`
      : '';
    $('#budget-result').innerHTML =
      `<strong>${data.requests.length} requests selected</strong>` +
      `<br><span class="muted">Total priority value: ${data.totalValue} \u00b7 Response time: ${data.totalCost} minutes</span>` +
      valueWeight +
      `<br><br>${data.requests.map(r => `<span class="pill neutral">${r.requestId} (urg=${r.urgency})</span>`).join(' ')}`;
  } catch {
    $('#budget-result').textContent = 'Start the Java WebServer to run the dynamic programme.';
  }
}

// ---- TREE INDEX LOOKUP (Algorithm Lab, detailed) ----
async function runIndexDetail() {
  const id = $('#index-input').value.trim().toUpperCase();
  if (!id) return;
  try {
    const data = await (await fetch(`/api/index-detail?requestId=${encodeURIComponent(id)}`)).json();
    if (!data.bstResult) {
      $('#index-result').innerHTML = `<strong>No request found</strong><br><span class="muted">${id} does not exist in the ${data.totalRecords}-record dataset.</span>`;
      return;
    }
    const bstFound = data.bstResult && data.bstResult.requestId ? 'Found' : 'Not found';
    const rbFound = data.rbResult && data.rbResult.requestId ? 'Found' : 'Not found';
    const btFound = data.btResult && data.btResult.requestId ? 'Found' : 'Not found';
    $('#index-result').innerHTML =
      `<strong>${data.bstResult.requestId} \u00b7 ${data.bstResult.category}</strong>` +
      `<br><span class="muted">${data.bstResult.source} \u2192 ${data.bstResult.destination} \u00b7 Urgency ${data.bstResult.urgency}</span>` +
      `<br><br><strong>Index comparison over ${data.totalRecords} records:</strong>` +
      `<br><span class="muted">BST \u2014 Height: ${data.bstHeight} \u2014 ${bstFound}` +
      `<br>Red-Black Tree \u2014 Height: ${data.redBlackHeight} \u2014 ${rbFound}` +
      `<br>B-Tree \u2014 Height: ${data.bTreeHeight} \u2014 ${btFound}</span>` +
      `<br><br><span class="muted">Red-Black tree guarantees O(log n) worst case. BST degrades to O(n) in worst case.</span>`;
  } catch {
    $('#index-result').textContent = 'Start the Java WebServer to run tree index lookup.';
  }
}

// ---- MST DETAIL (Algorithm Lab) ----
async function runMSTDetail() {
  try {
    const data = await (await fetch('/api/mst-detail')).json();
    $('#mst-result').innerHTML =
      `<strong>Prim: ${data.primWeight} (${data.primEdgeCount} edges)</strong>` +
      `<br><span class="muted">Kruskal: ${data.kruskalWeight} (${data.kruskalEdgeCount} edges) weighted minutes</span>`;
    // Show edge lists
    let html = '<details style="margin-top:12px"><summary class="muted" style="cursor:pointer">Show full edge lists</summary>';
    html += '<div class="mst-edges"><div><strong>Prim edges:</strong>';
    data.primEdges.forEach(e => { html += `<br><span class="muted">${e.from} \u2194 ${e.to} (${e.weight})</span>`; });
    html += '</div><div><strong>Kruskal edges:</strong>';
    data.kruskalEdges.forEach(e => { html += `<br><span class="muted">${e.from} \u2194 ${e.to} (${e.weight})</span>`; });
    html += '</div></div></details>';
    $('#mst-detail').innerHTML = html;
  } catch {
    $('#mst-result').textContent = 'Start the Java WebServer to run MST algorithms.';
  }
}

// ---- GRAPH TRAVERSAL (BFS/DFS) ----
async function runTraversal() {
  const start = $('#traversal-start').value;
  const algo = document.querySelector('[data-traversal].selected')?.dataset.traversal || 'bfs';
  if (!start) { $('#traversal-result').innerHTML = '<span class="muted">Select a starting location.</span>'; return; }
  try {
    const data = await (await fetch(`/api/graph-traversal?start=${encodeURIComponent(start)}&algorithm=${algo}`)).json();
    const algoLabel = algo === 'dfs' ? 'Depth-First Search (iterative, explicit stack)' : 'Breadth-First Search (queue-based)';
    $('#traversal-result').innerHTML =
      `<strong>${algoLabel}</strong>` +
      `<br><span class="muted">Starting from ${data.start} (${locationName(data.start)})</span>` +
      `<br><span class="muted">Visited ${data.visitedCount} nodes in ${fmtNs(data.timeNs)}</span>` +
      `<br><div class="traversal-order">${data.order.map((id, i) => `<span class="traversal-node">${i + 1}. ${id}</span>`).join(' ')}</div>`;
  } catch {
    $('#traversal-result').innerHTML = '<span class="muted">Start the Java WebServer to run graph traversal.</span>';
  }
}

// ---- DISPATCH CONSOLE ----
async function runDispatch() {
  try {
    const data = await (await fetch('/api/dispatch', { method: 'POST' })).json();
    auditLogDepth = data.auditLogSize;
    if (data.assignments.length === 0) {
      $('#dispatch-result').innerHTML =
        `<div class="dispatch-empty"><strong>No assignments made</strong>` +
        `<br><span class="muted">${data.pendingCount} pending requests, but ${data.availableResources} available resources.</span></div>`;
    } else {
      $('#dispatch-result').innerHTML =
        `<strong>${data.assignments.length} resources assigned</strong>` +
        `<br><span class="muted">Audit log depth: ${data.auditLogSize}</span>` +
        `<div class="dispatch-list">${data.assignments.map(a =>
          `<div class="dispatch-entry"><span class="pill neutral">${a.requestId}</span> \u2192 <span class="pill teal">${a.resourceId}</span></div>`
        ).join('')}</div>`;
    }
    renderDashboard();
    refreshAuditLog();
  } catch {
    $('#dispatch-result').innerHTML = '<span class="muted">Start the Java WebServer for dispatch.</span>';
  }
}

async function undoDispatch() {
  try {
    const data = await (await fetch('/api/undo', { method: 'POST' })).json();
    auditLogDepth = data.auditLogSize;
    if (data.ok) {
      $('#audit-info').innerHTML = `<span class="muted">Undid: <strong>${data.undone}</strong> \u2014 Audit log depth now ${data.auditLogSize}</span>`;
    } else {
      $('#audit-info').innerHTML = `<span class="muted">${data.message}</span>`;
    }
    renderDashboard();
    refreshAuditLog();
  } catch {
    $('#audit-info').innerHTML = '<span class="muted">Start the Java WebServer for undo.</span>';
  }
}

async function refreshAuditLog() {
  try {
    const data = await (await fetch('/api/audit-log')).json();
    auditLogDepth = data.auditLogSize;
    $('#audit-info').innerHTML = `<span class="muted">Audit log depth: <strong>${data.auditLogSize}</strong></span>`;
    renderDashboard();
  } catch {}
}

// ---- SEARCH & SORT LAB ----
async function runSort() {
  const algorithm = $('#sort-algorithm').value;
  const direction = $('#sort-direction').value;
  const topN = $('#sort-topn').value;
  const algoLabels = {
    quickSort: 'Quick Sort (Lomuto partition, O(n log n) avg)',
    mergeSort: 'Merge Sort (stable, O(n) extra space)',
    insertionSort: 'Insertion Sort (stable, O(n\u00b2), in-place)',
    selectionSort: 'Selection Sort (NOT stable, O(n\u00b2), in-place)'
  };
  try {
    const data = await (await fetch(`/api/sort?algorithm=${algorithm}&direction=${direction}&topN=${topN}`)).json();
    $('#sort-result').innerHTML =
      `<strong>${algoLabels[data.algorithm] || data.algorithm}</strong>` +
      `<br><span class="muted">Direction: ${data.direction} \u00b7 Sorted ${data.totalSorted} requests in ${fmtNs(data.timeNs)}</span>` +
      `<div class="sort-table"><table><thead><tr><th>#</th><th>Request</th><th>Category</th><th>Route</th><th>Urgency</th><th>Status</th></tr></thead><tbody>` +
      data.requests.map((r, i) =>
        `<tr><td>${i + 1}</td><td><strong>${r.requestId}</strong></td><td>${r.category}</td><td>${r.source} \u2192 ${r.destination}</td><td>${urgency(r.urgency)}</td><td><span class="status ${r.status}">${r.status}</span></td></tr>`
      ).join('') +
      `</tbody></table></div>`;
  } catch {
    $('#sort-result').innerHTML = '<span class="muted">Start the Java WebServer to run sorting.</span>';
  }
}

async function runSearch() {
  const targetId = $('#search-input').value.trim().toUpperCase();
  if (!targetId) return;
  try {
    const data = await (await fetch(`/api/search?requestId=${encodeURIComponent(targetId)}`)).json();
    if (!data.request) {
      $('#search-result').innerHTML = `<strong>No request found</strong><br><span class="muted">${targetId} does not exist.</span>`;
      return;
    }
    const r = data.request;
    const efficiency = data.linear.timeNs > 0
      ? `${(data.binary.timeNs / data.linear.timeNs * 100).toFixed(1)}% of linear time`
      : '';
    $('#search-result').innerHTML =
      `<strong>${r.requestId} \u00b7 ${r.category}</strong>` +
      `<br><span class="muted">${r.source} \u2192 ${r.destination} \u00b7 Urgency ${r.urgency} \u00b7 ${r.status}</span>` +
      `<br><br><strong>Search comparison (${data.sortedArraySize} records):</strong>` +
      `<br><span class="muted">Linear search: index ${data.linear.index}, ${fmtNs(data.linear.timeNs)}</span>` +
      `<br><span class="muted">Binary search: index ${data.binary.index}, ${data.binary.comparisons} comparisons, ${fmtNs(data.binary.timeNs)}</span>` +
      `<br><span class="muted">${efficiency}</span>`;
  } catch {
    $('#search-result').innerHTML = '<span class="muted">Start the Java WebServer to run search.</span>';
  }
}

// ---- BENCHMARKS ----
async function runBenchmarks() {
  $('#benchmark-run').disabled = true;
  $('#benchmark-status').innerHTML = '<span class="muted">Running benchmarks... this may take a few seconds.</span>';
  try {
    const data = await (await fetch('/api/benchmarks')).json();
    $('#benchmark-status').innerHTML = `<strong style="color:var(--teal)">Benchmarks completed.</strong> Results saved to database and CSV.`;
    // Fetch results from bootstrap and display recent ones
    await loadBenchmarkResults();
  } catch {
    $('#benchmark-status').innerHTML = '<span class="muted">Start the Java WebServer to run benchmarks.</span>';
  }
  $('#benchmark-run').disabled = false;
}

async function loadBenchmarkResults() {
  // Parse the CSV results file and display summary tables
  try {
    const response = await fetch('../docs/performance/results.csv');
    if (!response.ok) return;
    const text = await response.text();
    const rows = parseCSV(text);
    const grouped = {};
    rows.forEach(r => {
      const key = r.algorithmName;
      if (!grouped[key]) grouped[key] = [];
      grouped[key].push(r);
    });

    renderBenchTable('#bench-search', 'LinearSearch', 'BinarySearch', grouped);
    renderBenchTable('#bench-sort', 'SelectionSort', 'InsertionSort', grouped, 'MergeSort', 'QuickSort');
    renderBenchTable('#bench-ds', 'HashMapInsert_loadFactorTest', 'HeapInsertExtract', grouped);
    renderBenchTable('#bench-graph', 'BFS', 'DFS', grouped, 'Dijkstra', 'Prim');
  } catch {}
}

function renderBenchTable(selector, ...algoNames) {
  const grouped = algoNames[algoNames.length - 1] instanceof Object && !algoNames[algoNames.length - 1].algorithmName
    ? algoNames.pop()
    : {};
  const container = $(selector);
  if (!container) return;

  let html = '<table><thead><tr><th>Size</th>';
  const algos = algoNames.filter(a => grouped[a]);
  algos.forEach(a => { html += `<th>${a}</th>`; });
  html += '</tr></thead><tbody>';

  const sizes = [...new Set(algos.flatMap(a => (grouped[a] || []).map(r => +r.inputSize)))].sort((a, b) => a - b);
  sizes.forEach(size => {
    html += `<tr><td><strong>${size}</strong></td>`;
    algos.forEach(a => {
      const match = (grouped[a] || []).find(r => +r.inputSize === size);
      html += `<td>${match ? fmtNs(+match.timeNs) : '-'}</td>`;
    });
    html += '</tr>';
  });
  html += '</tbody></table>';
  container.innerHTML = html;
}

// ---- DP TRACE ----
async function renderDPTrace() {
  try {
    const data = await (await fetch('/api/algorithm?action=budget&minutes=300')).json();
    const pending = requests.filter(r => r.status === 'PENDING');
    $('#dp-trace').innerHTML =
      `<strong>0/1 Knapsack \u2014 Response-time budget = 300 min</strong>` +
      `<br><span class="muted">${pending.length} pending requests considered</span>` +
      `<br><span class="muted">Selected: ${data.requests.length} requests, Total value: ${data.totalValue}, Total cost: ${data.totalCost} min</span>` +
      `<br><span class="muted">Algorithm: Bottom-up tabulation with reconstruction \u2014 O(n \u00d7 B)</span>`;
  } catch {}
}

// ---- VIEW MANAGEMENT ----
function showView(id) {
  $$('.view').forEach(v => v.classList.toggle('active-view', v.id === id));
  $$('.nav-item').forEach(b => b.classList.toggle('active', b.dataset.view === id));
  const titles = {
    dashboard: ['Operations console', 'Network overview'],
    requests: ['Service requests', 'Dispatch register'],
    resources: ['Fleet and team', 'Resources'],
    routing: ['Route optimization', 'Route planner'],
    dispatch: ['Pod 1: Greedy dispatch', 'Dispatch console'],
    'search-sort': ['Pod 2: Sorting and search', 'Search & sort lab'],
    algorithms: ['Decision support', 'Algorithm lab'],
    benchmarks: ['Performance experiments', 'Benchmarks']
  };
  const t = titles[id] || ['CareRoute', id];
  $('#page-kicker').textContent = t[0];
  $('#page-title').textContent = t[1];

  // Lazy-load data for specific views
  if (id === 'search-sort') runSort();
  if (id === 'algorithms') { renderSchedule(); renderBudget(); renderDPTrace(); }
}

// ---- ASSIGNMENT DIALOG ----
function openAssignment(id) {
  selectedRequest = requests.find(r => r.requestId === id);
  const available = resources.filter(r => r.availabilityStatus === 'AVAILABLE');
  $('#dialog-title').textContent = `Assign ${selectedRequest.requestId}`;
  $('#dialog-copy').textContent = `Choose a ready resource for this ${selectedRequest.category.toLowerCase()} request.`;
  $('#assignment-resource').innerHTML = available.map(r =>
    `<option value="${r.resourceId}">${r.resourceId} \u00b7 ${r.type} (${r.homeLocation})</option>`
  ).join('') || '<option>No resources available</option>';
  $('#assignment-dialog').showModal();
}

// ---- INIT ----
async function refresh() {
  try {
    const response = await fetch('/api/bootstrap');
    if (!response.ok) throw Error();
    ({ requests, resources, locations } = await response.json());
  } catch {
    [requests, resources, locations] = await Promise.all([
      load('../data/service_requests.csv', fallbackRequests, ['requestId', 'source', 'destination', 'category', 'urgency', 'timeSubmitted', 'deadline', 'status']),
      load('../data/resources.csv', fallbackResources, ['resourceId', 'type', 'homeLocation', 'capacity', 'availabilityStatus']),
      load('../data/locations.csv', [], ['locationId', 'name', 'area', 'type', 'latitude', 'longitude'])
    ]);
  }
  await refreshAuditLog();
  renderDashboard();
  renderRequests();
  renderResources();
  populateRoutes();
}

async function init() {
  await refresh();

  // Navigation
  $$('.nav-item').forEach(b => b.onclick = () => showView(b.dataset.view));
  $('[data-go="requests"]').onclick = () => showView('requests');

  // Request filters
  $('#request-search').oninput = renderRequests;
  $('#status-filter').onchange = renderRequests;
  $('#urgency-filter').onchange = renderRequests;
  $('#requests-table').onclick = e => { if (e.target.matches('[data-request]')) openAssignment(e.target.dataset.request); };

  // Route planner
  $('#route-button').onclick = calcRoute;

  // Scheduling
  $$('[data-order]').forEach(b => b.onclick = () => {
    $$('[data-order]').forEach(x => x.classList.toggle('selected', x === b));
    renderSchedule(b.dataset.order);
  });

  // Budget
  $('#budget-input').oninput = renderBudget;

  // Tree index
  $('#index-button').onclick = runIndexDetail;
  $('#index-input').onkeydown = e => { if (e.key === 'Enter') runIndexDetail(); };

  // MST
  $('#mst-button').onclick = runMSTDetail;

  // Graph traversal
  $('#traversal-button').onclick = runTraversal;
  $$('[data-traversal]').forEach(b => b.onclick = () => {
    $$('[data-traversal]').forEach(x => x.classList.toggle('selected', x === b));
  });

  // Dispatch console
  $('#dispatch-button').onclick = runDispatch;
  $('#undo-button').onclick = undoDispatch;

  // Search & Sort
  $('#sort-button').onclick = runSort;
  $('#search-button').onclick = runSearch;
  $('#search-input').onkeydown = e => { if (e.key === 'Enter') runSearch(); };

  // Benchmarks
  $('#benchmark-run').onclick = runBenchmarks;

  // Dialogs
  $('#new-request').onclick = () => $('#request-dialog').showModal();
  $$('.dialog-close').forEach(b => b.onclick = () => b.closest('dialog').close());

  // Confirm assignment
  $('#confirm-assignment').onclick = async e => {
    e.preventDefault();
    if (!selectedRequest) return;
    const body = new URLSearchParams({ requestId: selectedRequest.requestId, resourceId: $('#assignment-resource').value });
    const response = await fetch('/api/assign', { method: 'POST', headers: { 'Content-Type': 'application/x-www-form-urlencoded' }, body });
    if (response.ok) { $('#assignment-dialog').close(); await refresh(); }
    else { alert((await response.json()).error); }
  };

  // Create request
  $('#request-form').onsubmit = async e => {
    e.preventDefault();
    const response = await fetch('/api/requests', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams(new FormData(e.target))
    });
    if (response.ok) { $('#request-dialog').close(); e.target.reset(); await refresh(); showView('requests'); }
    else { alert((await response.json()).error); }
  };

  // Load benchmarks on the benchmarks view
  await loadBenchmarkResults();
}

init();

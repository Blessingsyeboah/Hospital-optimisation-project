# Trace Table: Binary Search

Array (sorted): [2, 5, 8, 12, 16, 23, 38, 45, 56, 72, 91]

Target: 45

| Step | lo | hi | mid | arr[mid] | Comparison | Action |
|------|----|----|----|-----------|-----------|--------|
| 1 | 0 | 10 | 5 | 23 | arr[mid] < target | lo = mid+1 = 6 |
| 2 | 6 | 10 | 8 | 56 | arr[mid] > target | hi = mid-1 = 7 |
| 3 | 6 | 7 | 6 | 38 | arr[mid] < target | lo = mid+1 = 7 |
| 4 | 7 | 7 | 7 | 45 | arr[mid] == target | FOUND at index 7 |

Result: found at index 7 in 4 comparisons (theoretical max = ceil(log2(11))+1 = 4).

# Trace Table: Merge Sort & Quicksort (recurrence decomposition)

## Merge Sort on [8, 3, 5, 1, 9, 2]

| Call | Sub-array | Action |
|------|-----------|--------|
| split | [8, 3, 5, 1, 9, 2] | split into [0..2] and [3..5] |
| split | [8, 3, 5] | split into [0..1] and [2..2] |
| split | [8, 3] | split into [0..0] and [1..1] |
| split | [8] | base case, size 1 |
| split | [3] | base case, size 1 |
| merge | [3, 8] | merged [0..0] and [1..1] |
| split | [5] | base case, size 1 |
| merge | [3, 5, 8] | merged [0..1] and [2..2] |
| split | [1, 9, 2] | split into [3..4] and [5..5] |
| split | [1, 9] | split into [3..3] and [4..4] |
| split | [1] | base case, size 1 |
| split | [9] | base case, size 1 |
| merge | [1, 9] | merged [3..3] and [4..4] |
| split | [2] | base case, size 1 |
| merge | [1, 2, 9] | merged [3..4] and [5..5] |
| merge | [1, 2, 3, 5, 8, 9] | merged [0..2] and [3..5] |

## Quicksort on [8, 3, 5, 1, 9, 2] (Lomuto partition, pivot = last element)

| Call | Sub-array | Pivot | Partition result |
|------|-----------|-------|-------------------|
| partition | [1, 2, 5, 8, 9, 3] | 2 | pivot placed at index 1 |
| partition | [3, 8, 9, 5] | 3 | pivot placed at index 2 |
| partition | [5, 9, 8] | 5 | pivot placed at index 3 |
| partition | [8, 9] | 8 | pivot placed at index 4 |

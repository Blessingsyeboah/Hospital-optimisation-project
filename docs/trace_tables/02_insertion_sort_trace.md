# Trace Table: Insertion Sort

Initial array: [29, 10, 14, 37, 14]

(Two equal keys, 14 and 14, included to show stability: their relative order is preserved.)

| i | key | Shifted elements | Array after this pass |
|---|-----|-------------------|------------------------|
| 1 | 10 | [29] | [10, 29, 14, 37, 14] |
| 2 | 14 | [29] | [10, 14, 29, 37, 14] |
| 3 | 37 | [] | [10, 14, 29, 37, 14] |
| 4 | 14 | [37, 29] | [10, 14, 14, 29, 37] |

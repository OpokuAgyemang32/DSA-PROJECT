# Trace tables — Role 11 (Search & Sort)

These cover 2 of the 6 trace tables required in Section 10 of the project
brief. The other four (merge sort/quicksort, Dijkstra, Kruskal/Prim, DP)
belong to Roles 12/14.

## 1. Binary search trace

Sorted array of `requestId` values (ascending), matching a subset of
`data/service_requests.csv` after `SortAlgorithms.insertionSortByRequestId`:

```
index:      0   1   2   3   4   5   6
requestId:  1   3   4   5   9  14  26
```

**Search target: requestId = 9**

| Step | low | high | mid | arr[mid] | Comparison        | Action           |
|------|-----|------|-----|----------|--------------------|------------------|
| 1    | 0   | 6    | 3   | 5        | 5 < 9              | low = mid + 1 = 4|
| 2    | 4   | 6    | 5   | 14       | 14 > 9             | high = mid - 1 = 4|
| 3    | 4   | 4    | 4   | 9        | 9 == 9             | **found at index 4** |

Result: index 4, in 3 comparisons instead of the 5 a linear scan would need.

**Search target: requestId = 20 (not present)**

| Step | low | high | mid | arr[mid] | Comparison | Action            |
|------|-----|------|-----|----------|------------|-------------------|
| 1    | 0   | 6    | 3   | 5        | 5 < 20     | low = 4           |
| 2    | 4   | 6    | 5   | 14       | 14 < 20    | low = 6           |
| 3    | 6   | 6    | 6   | 26       | 26 > 20    | high = 5           |
| 4    | 6   | 5    | —   | —        | low > high | **loop ends, return -1** |

Precondition reminder: this trace is only valid because the array was sorted
ascending first. Running binary search on the raw, unsorted CSV order would
give a wrong answer silently if we did not check for it — which is why
`SearchAlgorithms.binarySearch` verifies `isSortedAscending` and throws
`IllegalStateException` instead of returning a bad index. That check is
exercised in `SearchAlgorithmsTest`.

---

## 2. Insertion sort trace

Sorting the first 5 urgency values pulled from `data/service_requests.csv`
(`5, 2, 5, 3, 4`) ascending with `SortAlgorithms.insertionSortByUrgency`-style
comparator:

| i | key | Array before shifting | Shifts (j walks left while arr[j] > key) | Array after step |
|---|-----|------------------------|-------------------------------------------|-------------------|
| 1 | 2   | [5, 2, 5, 3, 4]         | arr[0]=5 > 2 → shift; insert 2 at index 0 | [2, 5, 5, 3, 4]   |
| 2 | 5   | [2, 5, 5, 3, 4]         | arr[1]=5 not > 5 → no shift                | [2, 5, 5, 3, 4]   |
| 3 | 3   | [2, 5, 5, 3, 4]         | arr[2]=5 > 3, arr[1]=5 > 3 → shift both; insert 3 at index 1 | [2, 3, 5, 5, 4] |
| 4 | 4   | [2, 3, 5, 5, 4]         | arr[3]=5 > 4, arr[2]=5 > 4 → shift both; insert 4 at index 2 | [2, 3, 4, 5, 5] |

Final: `[2, 3, 4, 5, 5]`.

**Best case** (already sorted, e.g. `[1, 2, 3, 4, 5]`): the inner `while` never
executes because `arr[j] > key` is immediately false at every step — O(n)
total comparisons, no shifts.

**Worst case** (reverse sorted, e.g. `[5, 4, 3, 2, 1]`): every new key has to
shift past all previously-placed elements — O(n²) comparisons and shifts.

**Stability**: note step 2 above — when `key = 5` meets the existing `5` at
`arr[1]`, the loop condition is `arr[j] > key`, strictly greater, so equal
elements are never shifted past each other. The two requests that both have
`urgency = 5` keep their original relative order. This is why insertion sort
is used (rather than selection sort) wherever the team cares about tie-order
being preserved, e.g. dispatching same-urgency requests in submission order.
Selection sort does not have this guarantee (see class-level Javadoc on
`SortAlgorithms`), because a swap can move an element past an equal one it
hasn't been compared against yet.

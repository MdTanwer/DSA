# Arrays: Traversal, Insertion, Deletion

An array is a **contiguous** block of memory that stores elements of the same type, each sitting at a numbered **index**. Almost every later DSA topic (hashing, heaps, dynamic programming tables, two pointers) is built on this picture.

This note covers the three operations you must be able to do by hand and in code:

1. **Traversal** — visit every element
2. **Insertion** — put a new value in
3. **Deletion** — remove a value

Java examples use a **fixed** `int[]` **plus a logical size** `n`. That is how you should think in interviews. (`ArrayList` does the same work inside; we look at that at the end.)

---



## Table of Contents

1. [What an array really is](#1-what-an-array-really-is)
2. [The Java model we will use](#2-the-java-model-we-will-use)
3. [Traversal](#3-traversal)
4. [Insertion](#4-insertion)
5. [Deletion](#5-deletion)
6. [Full Java example](#6-full-java-example)
7. [Complexity cheat sheet](#7-complexity-cheat-sheet)
8. [Common mistakes](#8-common-mistakes)

---



## 1. What an array really is



### 1.1 Memory picture

The machine stores an array as one continuous strip of cells. If each `int` is 4 bytes and the array starts at address `base`:

```
index:      0        1        2        3        4
         ┌────────┬────────┬────────┬────────┬────────┐
value:   │  10    │  20    │  30    │  40    │  50    │
         └────────┴────────┴────────┴────────┴────────┘
address:  base    base+4   base+8   base+12  base+16
```

**Address of index** `i`**:**

```text
address(a[i]) = base + i × element_size
```

Addition and multiplication of fixed-size integers are **O(1)**. Jumping to `a[i]` does **not** walk from 0 to `i`. That is why **random access is Θ(1)**.

A linked list cannot do this: to reach node `i` you follow `i` next-pointers → Θ(i).

### 1.2 Capacity vs length vs logical size

In Java:


| Word             | Meaning                                                                   |
| ---------------- | ------------------------------------------------------------------------- |
| `a.length`       | How many slots the array **owns** (capacity). Fixed after `new int[cap]`. |
| logical size `n` | How many slots currently hold **real data**. `0 ≤ n ≤ a.length`.          |


```
capacity = 8, n = 5

 index   0    1    2    3    4    5    6    7
       ┌────┬────┬────┬────┬────┬────┬────┬────┐
       │ 10 │ 20 │ 30 │ 40 │ 50 │    │    │    │
       └────┴────┴────┴────┴────┴────┴────┴────┘
         used ──────────────┘    unused (garbage / 0)
```

Insertion and deletion change `n`. They do **not** change `a.length` unless you allocate a new array.

### 1.3 Why insert/delete in the middle is slow

Cells cannot “open a gap.” To free index `2`, every element to the right must **slide**.

```
insert 99 at index 2

before:  [10, 20, 30, 40, 50,  _,  _,  _]
                      ▲
shift:   [10, 20, 30, 30, 40, 50,  _,  _]   ← copy right-to-left
write:   [10, 20, 99, 30, 40, 50,  _,  _]
```

If you insert at index `i` and there are `n` live elements, you move `n - i` items.  
Worst case (`i = 0`): move **n** items → **Θ(n)**.  
Best case (`i = n`, append): move **0** items → **Θ(1)** (if capacity remains).

Same story for deletion, except you slide **left**.

---



## 2. The Java model we will use

Java arrays cannot grow. So we keep:

```java
int[] a;   // the slots
int n;     // how many values are live
```

All methods below assume:

- valid data lives in `a[0 .. n-1]`
- `n < a.length` is required before insert (or we refuse / resize)
- `n > 0` is required before delete

This is the same idea as `ArrayList`: an internal `elementData[]` plus a `size`.

---



## 3. Traversal



### 3.1 What it means

**Traversal** = visit each live element exactly once, in some order (usually index 0 → n-1).

You do this to print, sum, search, find max, copy, etc.

```
[10] → [20] → [30] → [40] → [50]
  0      1      2      3      4
```



### 3.2 Complexity


|                         | Time     | Extra space |
| ----------------------- | -------- | ----------- |
| Forward / backward scan | **Θ(n)** | **Θ(1)**    |
| Access one known index  | **Θ(1)** | **Θ(1)**    |


You must look at every element if the answer depends on all of them (sum, max). That is also **Ω(n)** — you cannot do better in the worst case.

### 3.3 Java: traverse

```java
// Visit every live element (index 0 .. n-1)
static void traverse(int[] a, int n) {
    for (int i = 0; i < n; i++) {
        System.out.print(a[i] + " ");
    }
    System.out.println();
}
```

Backward traversal (same cost):

```java
static void traverseReverse(int[] a, int n) {
    for (int i = n - 1; i >= 0; i--) {
        System.out.print(a[i] + " ");
    }
    System.out.println();
}
```

Enhanced-for (`for (int x : a)`) walks `a.length`, including unused tail slots. For a logical size `n`, use the index loop.

### 3.4 Java: search while traversing (linear search)

```java
// Return the first index of key, or -1 if missing
static int linearSearch(int[] a, int n, int key) {
    for (int i = 0; i < n; i++) {
        if (a[i] == key) {
            return i;          // best case Θ(1) if found at 0
        }
    }
    return -1;                 // worst case Θ(n)
}
```


| Case    | When                            | Time |
| ------- | ------------------------------- | ---- |
| Best    | key at index 0                  | Θ(1) |
| Worst   | key last or absent              | Θ(n) |
| Average | key equally likely at any index | Θ(n) |


---



## 4. Insertion

You always need a **free slot**. If `n == a.length`, the array is full. Either reject the insert or allocate a bigger array and copy (that copy is Θ(n) — this is how `ArrayList` doubles capacity; amortized O(1) append).

### 4.1 Insert at the end (append)

```
n = 5, insert 60 at end

[10, 20, 30, 40, 50,  _,  _,  _]
                      ▲
[10, 20, 30, 40, 50, 60,  _,  _]
n becomes 6
```

No shifting. One write + increment `n`.

```java
static int insertAtEnd(int[] a, int n, int value) {
    if (n == a.length) {
        throw new IllegalStateException("array is full");
    }
    a[n] = value;
    return n + 1;   // new logical size
}
```

**Time: Θ(1)** (if not full)  
**Space: Θ(1)**

### 4.2 Insert at a given index (the general case)

To insert `value` at index `pos`:

1. Check `0 ≤ pos ≤ n` and `n < a.length`.
2. Shift `a[n-1]`, `a[n-2]`, …, `a[pos]` **one step right**.
3. Write `a[pos] = value`.
4. `n = n + 1`.

**Shift right-to-left**, or you overwrite values you still need.

```
insert 99 at pos = 2, n = 5

 i:        0    1    2    3    4    5
before:  [10] [20] [30] [40] [50] [  ]

step 1:  [10] [20] [30] [40] [50] [50]   copy a[4] → a[5]
step 2:  [10] [20] [30] [40] [40] [50]   copy a[3] → a[4]
step 3:  [10] [20] [30] [30] [40] [50]   copy a[2] → a[3]
write:   [10] [20] [99] [30] [40] [50]
```

Number of copies = `n - pos`.

```java
static int insertAt(int[] a, int n, int pos, int value) {
    if (n == a.length) {
        throw new IllegalStateException("array is full");
    }
    if (pos < 0 || pos > n) {
        throw new IndexOutOfBoundsException("pos=" + pos);
    }

    // Shift right: start from the last live element
    for (int i = n; i > pos; i--) {
        a[i] = a[i - 1];
    }
    a[pos] = value;
    return n + 1;
}
```

Insert at the **beginning** is just `insertAt(a, n, 0, value)` — worst case, every element moves.

### 4.3 Complexity of insert

Let `n` = current live length, `pos` = insertion index.

```text
T(n, pos) = Θ(n - pos)     // the shifts
```


| Position             | Shifts | Time     |
| -------------------- | ------ | -------- |
| End (`pos = n`)      | 0      | **Θ(1)** |
| Middle (`pos = n/2`) | n/2    | **Θ(n)** |
| Start (`pos = 0`)    | n      | **Θ(n)** |
| Worst case           | n      | **Θ(n)** |


Extra space: **Θ(1)** (in-place shift).  
If you grow the array: allocate `new int[2 * cap]` and copy all `n` → **Θ(n)** that one time.

### 4.4 Why the loop runs `n - pos` times (math)

```text
i goes from n down to pos+1
number of iterations = n - pos
each iteration: one assignment = O(1)
T = Θ(n - pos)
```

For a random `pos` uniform in `0 .. n`:

```text
T_avg = (1/(n+1)) · Σ_{k=0}^{n} Θ(k) = Θ(n)
```

So “insert somewhere” is **average Θ(n)**, not Θ(1). Only append is cheap.

---



## 5. Deletion

Deletion at index `pos` **closes the hole** by sliding the right side **left**.

### 5.1 Delete at the end

```
[10, 20, 30, 40, 50]  n = 5
                   ▲
just do n = n - 1

[10, 20, 30, 40, 50]  n = 4
 live ──────────┘      50 is now unused
```

You do **not** have to zero `a[n]`. Logical size is what matters.

```java
static int deleteAtEnd(int[] a, int n) {
    if (n == 0) {
        throw new IllegalStateException("array is empty");
    }
    return n - 1;
}
```

**Time: Θ(1)**  
**Space: Θ(1)**

### 5.2 Delete at a given index

To delete index `pos`:

1. Check `0 ≤ pos < n`.
2. Shift `a[pos+1]`, `a[pos+2]`, …, `a[n-1]` **one step left**.
3. `n = n - 1`.

**Shift left-to-right**, or you lose values.

```
delete index 1 (value 20), n = 5

 i:        0    1    2    3    4
before:  [10] [20] [30] [40] [50]

step 1:  [10] [30] [30] [40] [50]   a[1] = a[2]
step 2:  [10] [30] [40] [40] [50]   a[2] = a[3]
step 3:  [10] [30] [40] [50] [50]   a[3] = a[4]
n = 4    [10] [30] [40] [50]  ..
```

Number of copies = `n - pos - 1`.

```java
static int deleteAt(int[] a, int n, int pos) {
    if (n == 0) {
        throw new IllegalStateException("array is empty");
    }
    if (pos < 0 || pos >= n) {
        throw new IndexOutOfBoundsException("pos=" + pos);
    }

    // Shift left: pull everything after pos one step toward the front
    for (int i = pos; i < n - 1; i++) {
        a[i] = a[i + 1];
    }
    return n - 1;
}
```



### 5.3 Delete by value (first match)

Traverse to find the key, then delete that index.

```java
static int deleteValue(int[] a, int n, int key) {
    int pos = linearSearch(a, n, key);
    if (pos == -1) {
        return n;   // not found: size unchanged
    }
    return deleteAt(a, n, pos);
}
```

Time: **Θ(n)** search + **Θ(n)** shift = **Θ(n)**.

### 5.4 Complexity of delete

```text
T(n, pos) = Θ(n - pos - 1)
```


| Position          | Shifts         | Time     |
| ----------------- | -------------- | -------- |
| End (`pos = n-1`) | 0              | **Θ(1)** |
| Middle            | ~ n/2          | **Θ(n)** |
| Start (`pos = 0`) | n-1            | **Θ(n)** |
| Delete by value   | search + shift | **Θ(n)** |


Extra space: **Θ(1)**.

### 5.5 Insert vs delete — same underlying cost

```
INSERT at i          DELETE at i

  open a gap           close a gap
  by sliding right     by sliding left

  [  |  |  |  |  ]     [  |  |  |  |  ]
       ▲ hole                ▲ hole
  move the tail        move the tail
```

Both are **Θ(distance to the end)**. The array is fast at the **end**, slow at the **front**.

That is why a queue implemented as “insert/delete at index 0 of an array” is a bad idea (every op is Θ(n)). Use a ring buffer or a linked list / deque instead.

---



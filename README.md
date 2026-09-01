# Foundations of DSA: Time & Space Complexity

This note is a first-principles guide to how we measure algorithms. The goal is not just to memorize names like `O(n)` — it is to understand **why** those names exist, **what the math is doing**, and **how to read a piece of code and estimate its cost**.

---

## Table of Contents

1. [Why complexity exists](#1-why-complexity-exists)
2. [Time complexity](#2-time-complexity)
3. [Space complexity](#3-space-complexity)
4. [The language of growth: Big-O, Big-Ω, Big-Θ](#4-the-language-of-growth-big-o-big-ω-big-θ)
5. [Best, worst, and average case](#5-best-worst-and-average-case)
6. [Amortized analysis](#6-amortized-analysis)
7. [Common complexity classes](#7-common-complexity-classes)
8. [How to analyze code in practice](#8-how-to-analyze-code-in-practice)
9. [Quick reference](#9-quick-reference)

---

## 1. Why complexity exists

A computer does a finite amount of work per second. An algorithm is a recipe that turns an input of size `n` into an output. As `n` grows, the recipe usually needs more steps and more memory.

We do **not** measure “this ran in 12 ms on my laptop.” That number depends on:

- CPU speed
- language and compiler
- cache, RAM, OS load
- how the input is laid out in memory

Those things change. What does **not** change is the **shape of the growth**. If an algorithm does about `n²` comparisons, doubling `n` roughly quadruples the work, on any machine.

Complexity theory answers:

> If the input gets 10× larger, how much more work / memory do we need?

That is why we talk in **functions of n**, not milliseconds.

```
input size n ──────────────────────────────────────────►

work
 ▲
 │                                          ●  2ⁿ  (explodes)
 │                                     ●
 │                                ●
 │                    ●●●●●  n²
 │               ●●●
 │          ●●●  n log n
 │     ●●●  n
 │ ●●  log n
 │●  1
 └──────────────────────────────────────────► n
```

The picture is the whole subject: we care about **which curve we are on** when `n` is large.

---

## 2. Time complexity

### 2.1 What we actually count

Time complexity is the number of **primitive operations** as a function of input size `n`.

A primitive operation is something the CPU can do in roughly constant time:

- arithmetic (`+`, `-`, `*`, `/`, `%`)
- comparison (`<`, `==`)
- assignment
- array index `a[i]`
- pointer follow
- function call overhead (not the body)

We do **not** count wall-clock time. We count **steps**.

Example:

```text
sum = 0
for i from 1 to n:
    sum = sum + a[i]
```

Inside the loop: one add, one assign, one index, one increment of `i`, one comparison. That is a **constant** number of primitives per iteration. The loop runs `n` times. Total steps ≈ `c · n` for some constant `c`.

We write that as **Θ(n)** (exact order) or loosely **O(n)** (upper bound). The constant `c` is dropped. Why? See the math below.

### 2.2 The model underneath (RAM model)

The usual mental machine is the **Random Access Machine**:

```
 ┌────────────┐     address      ┌──────────────────────┐
 │  CPU       │ ◄──────────────► │  memory cells        │
 │  (1 step   │                  │  [0] [1] [2] ... [n] │
 │   per op)  │                  │  each cell: O(1)     │
 └────────────┘                  └──────────────────────┘
```

Assumptions:

1. Any memory cell is reachable in **O(1)** time (no disk seek).
2. Integers that fit in a machine word are O(1) to add/compare.
3. We ignore cache lines, branch prediction, and paging — those matter in real systems, but not in this first model.

This is why `a[i]` is O(1) and walking a linked list of `n` nodes is O(n): each next-pointer hop is one step.

### 2.3 From exact count to a function

Suppose a loop does this:

```text
T(n) = 3n + 7
```

Meaning: 3 operations per element, plus 7 setup operations.

For `n = 1`:   T = 10  
For `n = 1000`: T = 3007  
For `n = 10⁶`:  T = 3,000,007

The `+ 7` is noise. The `3` is a hardware/language constant. The **interesting** part is “grows linearly with n.” Complexity notation throws away constants and lower-order terms so that `3n + 7`, `n`, and `100n + 50` are all the same class: **linear**.

---

## 3. Space complexity

Space complexity is the extra memory the algorithm needs, as a function of `n`.

Split it into two buckets:

| Kind | Meaning | Example |
|------|---------|---------|
| **Input space** | Memory that already holds the input | the array you were given |
| **Auxiliary space** | Extra memory **you** allocate | a copy, a stack, a hash map, recursion frames |

When people say “this sort is O(1) space,” they almost always mean **auxiliary** space. The input array of size `n` is already there.

```
total memory = input + auxiliary + output (sometimes counted separately)

   ┌──────────┐  ┌────────────┐  ┌──────────┐
   │ input    │  │ extra work │  │ result   │
   │  O(n)    │  │  ???       │  │  O(n)    │
   └──────────┘  └────────────┘  └──────────┘
                      ▲
                      this is what we usually analyze
```

### Recursion uses space

Each recursive call pushes a **stack frame** (locals, return address). Depth `d` means Θ(d) extra space, even if you allocate no arrays.

```
factorial(n)
  └─ factorial(n-1)
       └─ factorial(n-2)
            └─ ...
                 └─ factorial(1)     ← d = n frames → Θ(n) space
```

Binary search recursion has depth ≈ log₂ n, so Θ(log n) stack space (or O(1) if you write it as a loop).

### Time vs space tradeoff

You can often buy time with memory (hash map for O(1) lookup) or buy memory with time (recompute instead of store). Complexity analysis makes that trade visible.

---

## 4. The language of growth: Big-O, Big-Ω, Big-Θ

These are **sets of functions**. They describe how `T(n)` behaves for **large n** (asymptotics).

Let `T(n)` be the true cost. Let `f(n)` be a simple function like `n`, `n²`, `log n`.

### 4.1 Big-O — upper bound (“at most”)

**Definition.**  
`T(n) = O(f(n))` if there exist constants `c > 0` and `n₀ ≥ 0` such that

```text
0 ≤ T(n) ≤ c · f(n)     for all n ≥ n₀
```

In words: eventually, `T` never grows faster than a constant times `f`.

```
      cost
        ▲
        │          c · f(n)   ← ceiling we never break (after n₀)
        │         /
        │        /   ●●  T(n) stays under the ceiling
        │       /  ●
        │      / ●
        │     /●
        │    ●
        └────┬──────────────────► n
            n₀
```

**Intuition:** Big-O is a **guarantee against the worst growth**.  
“This sort is O(n²)” means: I promise it will not grow worse than quadratic (for large n), up to a constant factor.

**Important:** O is an **upper** bound, so it can be loose.

- Binary search is O(log n) — tight and useful
- Binary search is also O(n), O(n²), O(2ⁿ) — mathematically true, practically useless

That is why we also need Ω and Θ.

**Worked math.**  
Is `3n + 7 = O(n)`?

We need `3n + 7 ≤ c · n` for large n.

```text
3n + 7 ≤ c n
7 ≤ (c - 3) n
```

Pick `c = 4`. Then `7 ≤ n`, which is true for all `n ≥ 7`.  
So `n₀ = 7`, `c = 4`. Yes: `3n + 7 = O(n)`.

Is `n² = O(n)`?  
We would need `n² ≤ c n` ⇒ `n ≤ c` for all large n. No constant `c` works forever. So **no**.

### 4.2 Big-Ω (Omega) — lower bound (“at least”)

**Definition.**  
`T(n) = Ω(f(n))` if there exist `c > 0` and `n₀` such that

```text
0 ≤ c · f(n) ≤ T(n)     for all n ≥ n₀
```

```
      cost
        ▲
        │    ●
        │   ●  T(n) stays above the floor
        │  ●
        │ ●  /
        │●  /  c · f(n)
        │  /
        └────┬──────────────────► n
            n₀
```

**Intuition:** you cannot do better than this, infinitely often / in the long run.

Comparison-based sorting is **Ω(n log n)** in the worst case: there are `n!` possible orders, and each comparison has 2 outcomes, so you need at least `log₂(n!)` comparisons. Stirling’s approximation:

```text
n! ≈ √(2πn) · (n/e)ⁿ

log₂(n!) ≈ n log₂ n - n log₂ e + O(log n)
         = Ω(n log n)
```

So no comparison sort can be o(n log n) in the worst case. That is a **lower bound on the problem**, not just on one algorithm.

### 4.3 Big-Θ (Theta) — tight bound (“exactly this order”)

**Definition.**  
`T(n) = Θ(f(n))` if `T(n)` is both O(f(n)) **and** Ω(f(n)). Equivalently, there exist `c₁, c₂ > 0` and `n₀` such that

```text
c₁ · f(n) ≤ T(n) ≤ c₂ · f(n)     for all n ≥ n₀
```

```
      cost
        ▲
        │        c₂ · f(n)   upper sandwich
        │       /
        │      / ●● T(n) trapped between two multiples of f
        │     /●
        │    ●/
        │   /  c₁ · f(n)   lower sandwich
        └──────────────────────────► n
```

**Intuition:** `T` and `f` grow at the **same rate**.  
`3n + 7 = Θ(n)` is the honest statement. Saying only `O(n)` is also correct, but weaker.

In conversation, people say “this is O(n)” when they mean Θ(n). In proofs, be precise.

### 4.4 Little-o and little-ω (optional but useful)

| Notation | Meaning |
|----------|---------|
| `T(n) = o(f(n))` | `T` grows **strictly slower** than `f`. `T(n)/f(n) → 0` |
| `T(n) = ω(f(n))` | `T` grows **strictly faster** than `f`. `T(n)/f(n) → ∞` |

Example: `n = o(n²)`, `n log n = o(n²)`, `n² ≠ o(n²)`.

### 4.5 Limit test (the calculus behind the definitions)

If the limit exists:

```text
          T(n)
    L = lim ──────
        n→∞  f(n)
```

| Result | Conclusion |
|--------|------------|
| `0 ≤ L < ∞` | `T(n) = O(f(n))` |
| `0 < L ≤ ∞` | `T(n) = Ω(f(n))` |
| `0 < L < ∞` | `T(n) = Θ(f(n))` |
| `L = 0` | `T(n) = o(f(n))` |
| `L = ∞` | `T(n) = ω(f(n))` |

Example: `T(n) = 5n² + 3n`, `f(n) = n²`

```text
    (5n² + 3n) / n² = 5 + 3/n  →  5
```

`L = 5`, finite and positive ⇒ `T(n) = Θ(n²)`.

### 4.6 Dropping terms — why it is legal

Suppose `T(n) = an² + bn + c` with `a > 0`.

```text
T(n) / n² = a + b/n + c/n²  →  a
```

So `T(n) = Θ(n²)`. The linear and constant terms are **dominated**.  
Rule of thumb:

1. Drop constant factors (`5n` → `n`).
2. Keep only the fastest-growing term (`n² + n` → `n²`).
3. Logs grow slower than any positive power: `log n = o(n^ε)` for every `ε > 0`.
4. Polynomials grow slower than exponentials: `nᵏ = o(aⁿ)` for any `a > 1`.

Growth ladder (slow → fast):

```text
1  ≪  log n  ≪  √n  ≪  n  ≪  n log n  ≪  n²  ≪  n³  ≪  2ⁿ  ≪  n!  ≪  nⁿ
```

`≪` here means “little-o”: the left one over the right one goes to 0.

---

## 5. Best, worst, and average case

Big-O describes a **function**. That function can still depend on **which input** of size `n` you get.

```
all inputs of size n
        │
        ├── best case     → fewest steps   T_best(n)
        ├── typical mix   → average steps  T_avg(n)
        └── worst case    → most steps     T_worst(n)
```

These are **three different functions**. Each can have its own Θ.

### 5.1 Linear search (the clean example)

Search for `x` in an array of `n` elements.

```text
for i from 0 to n-1:
    if a[i] == x: return i
return not_found
```

| Case | When | Comparisons | Complexity |
|------|------|-------------|------------|
| **Best** | `x` is at index 0 | 1 | Θ(1) |
| **Worst** | `x` is last, or missing | n | Θ(n) |
| **Average** | `x` equally likely at any index, and present | (n+1)/2 | Θ(n) |

**Average-case math.**  
If `x` is at position `i` (1-indexed) with probability `1/n`:

```text
T_avg(n) = (1/n) · (1 + 2 + ... + n)
         = (1/n) · n(n+1)/2
         = (n+1)/2
         = Θ(n)
```

Best is Θ(1), worst and average are Θ(n). When people say “linear search is O(n),” they mean **worst case** (or average — same order).

### 5.2 Why interviews almost always want worst case

- Worst case is a **promise**: the algorithm will never be slower than this.
- Average case needs a **probability model** of inputs. Real data is rarely uniform.
- Best case is easy to game and rarely useful alone.

Exception: randomized algorithms (quicksort with random pivot, hashing) are often discussed in **expected** time, which is a kind of average over the algorithm’s coins, not over the input.

### 5.3 Quicksort: all three cases matter

```
partition around a pivot, recurse left, recurse right
```

**Worst case** — pivot is always the smallest or largest element:

```
n
└── n-1
    └── n-2
        └── ...
            └── 1
```

Recurrence:

```text
T(n) = T(n-1) + Θ(n)     (partition scans n elements)
T(1) = Θ(1)

T(n) = Θ(n + (n-1) + ... + 1) = Θ(n²)
```

**Best case** — pivot always splits in half:

```
            n
         /     \
       n/2     n/2
       / \     / \
     n/4 n/4 n/4 n/4
```

Recurrence (Master theorem, case 2):

```text
T(n) = 2 T(n/2) + Θ(n)
T(n) = Θ(n log n)
```

**Average case** — random pivot, or random input. The split is “good enough” on average. Result: **Θ(n log n)**.

This is why textbooks say: quicksort worst O(n²), average O(n log n). With a random pivot, the **expected** time is O(n log n) for **every** input.

### 5.4 Do not mix the two axes

There are two independent ideas:

```
                    bound type
                 O          Θ          Ω
              ┌────────┬────────┬────────┐
    best      │        │        │        │
    average   │   a cell is one statement:
    worst     │   “worst-case time is Θ(n²)”
              └────────┴────────┴────────┘
```

“Best case O(n²)” is almost meaningless (best case of insertion sort is Θ(n), which is also O(n²), but that hides the truth). Prefer: **“worst-case Θ(n²), best-case Θ(n).”**

---

## 6. Amortized analysis

Sometimes a single operation is expensive, but a **sequence** of operations is cheap on average — not because of probability, but because the expensive one **cannot happen often**.

**Amortized cost** of an operation = (total cost of a sequence) / (number of operations).

This is **not** average-case. Average-case uses randomness. Amortized analysis is a **worst-case bound on a sequence**.

```
cost per op
 ▲
 │ █                         █
 │ █                         █          rare expensive ops
 │ █  ▄  ▄  ▄  ▄  ▄  ▄  ▄  ▄ █  ▄ ...  many cheap ops
 └──────────────────────────────────────► time

 total area / number of bars  =  amortized cost
```

Three standard methods: aggregate, accounting (banker's), potential (physicist's).

### 6.1 Dynamic array (the classic)

A vector / `ArrayList` / Python list:

- append into free capacity: **Θ(1)**
- when full, allocate `2n` slots, copy `n` elements: **Θ(n)**

```
capacity: 1 → 2 → 4 → 8 → 16 → ...

append #   1  2  3  4  5  6  7  8  9 ...
cost:      1  2  1  4  1  1  1  8  1 ...
           ▲     ▲           ▲
           copies happen at powers of 2
```

**Aggregate method.**  
Cost of `n` appends, starting from empty:

```text
copies happen at sizes 1, 2, 4, ..., 2ᵏ  where 2ᵏ < n ≤ 2ᵏ⁺¹

cost of copies = 1 + 2 + 4 + ... + 2ᵏ = 2ᵏ⁺¹ - 1 < 2n

plus n cheap writes = n

total < 3n
amortized cost per append = total / n  <  3  =  O(1)
```

So: **worst-case single append is O(n), amortized append is O(1).**

**Accounting method (intuition).**  
Each append pays $3:

- $1 for the write itself
- $2 saved on that element for a future copy

When we resize from `n` to `2n`, we copy `n` elements. Those `n` elements have $2 each in the bank — exactly enough to pay for the copy. The bank never goes negative. Therefore every append is prepaid at $3 = O(1).

```
  new element:  pay $3
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
     write $1    save $1     save $1
                 (this copy) (next resize)
```

**Potential method (the math form).**  
Let `Φ` be a “stored work” function. After `i` operations:

```text
amortized cost âᵢ = actual cost cᵢ + Φ(Dᵢ) - Φ(Dᵢ₋₁)
```

Sum telescopes:

```text
Σ âᵢ = Σ cᵢ + Φ(final) - Φ(start)
```

If `Φ ≥ 0` and `Φ(start) = 0`, then `Σ cᵢ ≤ Σ âᵢ`. Bounding amortized costs bounds real total cost.

For the table: `Φ = 2n - capacity` (or similar). After a doubling, potential drops by the copy cost; between doublings it rises by a constant per insert.

### 6.2 Other amortized structures

| Structure | Cheap op | Rare expensive op | Amortized |
|-----------|----------|-------------------|-----------|
| Dynamic array | append | resize + copy | O(1) append |
| Splay tree | — | rotations to bring node to root | O(log n) per access |
| Union-Find + path compression | find | long path compression | ≈ O(α(n)) ≈ O(1) |
| Incrementing a binary counter | flip some bits | flip all bits (0111… → 1000…) | O(1) per increment |

**Binary counter** (short proof).  
Incrementing an n-bit counter from 0 to `m` flips bit 0 every time, bit 1 every second time, bit 2 every fourth time, …

```text
total bit flips = m + m/2 + m/4 + ... < 2m
amortized flips per increment = O(1)
```

Worst increment (all 1s → 1000…) is Θ(number of bits), but that is rare.

---

## 7. Common complexity classes

For each class: what it means, the math, a picture of the work, and a typical algorithm.

Assume one “step” is a primitive. Tables use a machine that does **10⁸ operations per second** — a common rough figure — so you can feel the difference.

```
n        O(1)    O(log n)   O(n)      O(n log n)   O(n²)      O(2ⁿ)        O(n!)
10       instant instant    instant   instant      instant    instant      4 ms
20       instant instant    instant   instant      instant    10 ms        77 years
30       instant instant    instant   instant      instant    10 s         age of universe
100      instant instant    instant   instant      0.1 ms     10¹⁴ years   —
10⁶      instant instant    10 ms     0.2 s        3 hours    —            —
10⁹      instant instant    10 s      5 min        300 years  —            —
```

(Orders of magnitude only — constants change the exact seconds.)

---

### 7.1 O(1) — constant time

**Meaning.** Work does **not** grow with `n`. Same number of steps for 10 elements or 10 million.

**Math.** `T(n) ≤ c` for some constant `c`, for all large n.  
So `T(n) = O(1)`. Also `T(n) = Θ(1)` if it is bounded below by a positive constant (it always does at least one thing).

**Picture.**

```
steps
 ▲
 │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─  c
 │
 └────────────────────────► n
```

**Code pattern.**

```text
return a[0]
return a[i]          # index known
hash_map.get(key)    # expected, with a good hash
x = x + 1
```

**Not O(1):** “we loop from 1 to 100.” That is O(1) in `n` if 100 is a **fixed** constant, but if 100 is really `n`, it is O(n). The variable that grows is what matters.

**Space:** a few integers is O(1) extra space.

---

### 7.2 O(log n) — logarithmic time

**Meaning.** Each step **throws away a constant fraction** of the remaining work. Usually the fraction is 1/2.

**Math.** How many times can you divide `n` by 2 until you hit 1?

```text
n / 2 / 2 / ... / 2 = 1
n / 2ᵏ = 1
2ᵏ = n
k = log₂ n
```

So a loop that halves the search space each time runs **Θ(log n)** iterations.

Change of base is a constant:

```text
log_b n = log_k n / log_k b = Θ(log n)
```

So O(log₂ n), O(log₁₀ n), O(ln n) are the **same** complexity class. We write **O(log n)**.

**Picture — binary search.**

```
array of n elements

[ | | | | | | | | | | | | | | | ]   n
                ▲
         compare mid, go left or right

[ | | | | | | | ]                   n/2
        ▲

[ | | | ]                           n/4
    ▲

[ | ]                               n/8
 ...
[x]                                 1     ← log₂ n steps
```

**Recurrence.**

```text
T(n) = T(n/2) + Θ(1)
T(1) = Θ(1)

unwind: T(n) = Θ(1) · (how many halvings) = Θ(log n)
```

**Also logarithmic:** balanced BST search/insert/delete, binary exponentiation (`pow(x, n)` in log n multiplies), finding the highest set bit.

**Growth feel:** from n = 1,000 to n = 1,000,000, log₂ n goes from ~10 to ~20. A million times more data, only **twice** the steps.

---

### 7.3 O(n) — linear time

**Meaning.** You touch each element a **constant** number of times. You cannot do less if you must read the whole input (that is also Ω(n) — you have to look at every item).

**Math.** `T(n) = an + b = Θ(n)`.

**Picture.**

```
[e0][e1][e2][e3] ... [eₙ₋₁]
  ●   ●   ●   ●         ●     one pass
```

**Code pattern.**

```text
for i in 0..n-1:          # Θ(n)
    do O(1) work

# still Θ(n):
for i in 0..n-1:
    do O(1)
for j in 0..n-1:
    do O(1)
# two passes: 2n = Θ(n)
```

**Examples:** find max, sum, count, Kadane’s algorithm, sliding window of fixed work per index, checking if a string is palindrome (two pointers).

**Two pointers / sliding window** are often O(n) even if they look nested, because each pointer only moves forward:

```text
left = 0
for right in 0..n-1:
    while window is invalid:
        left += 1          # left never goes backward
# left and right each move ≤ n times → Θ(n)
```

---

### 7.4 O(n log n) — linearithmic time

**Meaning.** Do a **linear** amount of work at each of **log n** levels, or do a log-cost operation n times.

This is the “efficient sort / efficient conquer” zone. It is the best possible worst-case for comparison sorting (see Ω(n log n) above).

**Picture — mergesort.**

```
                    [ n elements ]          work to merge: n
                   /              \
              [n/2]                [n/2]    work: n/2 + n/2 = n
             /     \              /     \
          [n/4]  [n/4]        [n/4]  [n/4]  work: n
           ...

height of tree = log₂ n levels
work per level = Θ(n)
total = Θ(n log n)
```

**Recurrence (Master theorem).**

```text
T(n) = 2 T(n/2) + Θ(n)
```

Master theorem: `T(n) = a T(n/b) + nᵈ`  
Here `a = 2`, `b = 2`, `d = 1`. Compare `a` with `bᵈ`: `2 = 2¹`.  
Equal ⇒ `T(n) = Θ(nᵈ log n) = Θ(n log n)`.

**Heap / tree picture.**  
Building a heap is O(n). Each of `n` extract-min operations is O(log n) ⇒ heapsort is O(n log n).

Inserting n keys into a balanced BST: n · O(log n) = O(n log n).

**Examples:** mergesort, heapsort, well-implemented quicksort (average), sorting-based solutions, many divide-and-conquer algorithms.

---

### 7.5 O(n²) — quadratic time

**Meaning.** For each of n items, you do Θ(n) work. Nested independent loops over the same `n`.

**Math.**

```text
for i in 1..n:
    for j in 1..n:
        O(1)

iterations = n · n = n²
```

Or the triangle (still quadratic):

```text
for i in 1..n:
    for j in i+1..n:
        O(1)

iterations = (n-1) + (n-2) + ... + 1 = n(n-1)/2 = Θ(n²)
```

**Picture.**

```
j →
i   ● ● ● ● ●
↓   ● ● ● ● ●
    ● ● ● ● ●
    ● ● ● ● ●
    ● ● ● ● ●     n × n cells → n²
```

**Examples:** bubble / insertion / selection sort (worst or all cases), checking all pairs, naive string matching, Floyd-Warshall is O(n³) — one more nested loop.

**Why it dies at large n:**  
n = 10⁴ → n² = 10⁸ (about 1 second).  
n = 10⁶ → n² = 10¹² (hours to days).  
Interview rule of thumb: if n ≤ 4000, O(n²) may pass; if n ≤ 10⁵, you usually need O(n log n) or better.

**Not automatically O(n²):** a loop inside a loop if the inner loop is `log n` or `n/i`:

```text
for i in 1..n:                 # n
    for j = i; j < n; j *= 2:  # log(n/i) ≤ log n
        ...
# total O(n log n), not O(n²)
```

Harmonic nested loop:

```text
for i in 1..n:
    for j in 1..n step i:
        ...
# ≈ n/1 + n/2 + ... + n/n = n · Hₙ = Θ(n log n)
```

`Hₙ = 1 + 1/2 + ... + 1/n ≈ ln n + γ` (harmonic number).

---

### 7.6 O(2ⁿ) — exponential time

**Meaning.** The work **doubles** when `n` grows by 1. Typical of “try every subset” or naive recursion that branches 2 ways without reuse.

**Math.** Number of subsets of an n-element set is `2ⁿ`.  
If you enumerate them all: Θ(2ⁿ) subsets, often times poly(n) work each ⇒ O(n · 2ⁿ) still called exponential.

**Picture — naive Fibonacci.**

```text
fib(n) = fib(n-1) + fib(n-2)

                    fib(5)
                   /      \
              fib(4)        fib(3)
             /     \        /    \
         fib(3)  fib(2)  fib(2) fib(1)
         /   \
     fib(2) fib(1)
      ...
```

The recursion tree has size ~ `Fₙ` nodes, and `Fₙ ≈ φⁿ / √5` where `φ = (1+√5)/2 ≈ 1.618`.  
So naive fib is **Θ(φⁿ)** — exponential, slightly better than 2ⁿ, same “unusable for n > 40” family.

Recurrence:

```text
T(n) = T(n-1) + T(n-2) + Θ(1)
T(n) = Θ(φⁿ)
```

**Subset / knapsack brute force.**

```
each item: take it or leave it

item 1   item 2   item 3   ...
  0        0        0
  0        0        1
  0        1        0
  ...
  1        1        1        ← 2ⁿ leaves
```

**Fix:** dynamic programming / memoization often turns O(2ⁿ) into O(n) or O(n · W) or O(n · 2^k) with k much smaller than n.

Memoized fib: each of n states computed once → **Θ(n)**. Same recurrence, different algorithm.

---

### 7.7 O(n!) — factorial time

**Meaning.** You generate **every permutation**, or you explore a search tree whose branching is n, then n-1, then n-2, …

**Math.**

```text
n! = n · (n-1) · (n-2) · ... · 1
```

Stirling:

```text
n! ≈ √(2πn) · (n/e)ⁿ
```

So n! grows **faster than cⁿ** for any constant c (because of the `(n/e)ⁿ` term with n in the base).

**Picture — permutations of 4 items.**

```
                  start
        /      /      \      \
       A      B        C      D          4 choices
      /|\    /|\      /|\    /|\
     B C D  A C D    A B D  A B C        3 left
     ...                                 2 left
                                         1 left
leaves = 4! = 24
```

**Examples:** naive traveling salesman (try all city orders), generating all permutations to test a condition, some brute-force constraint solvers.

n = 12 → 12! ≈ 479 million (maybe computable).  
n = 15 → 1.3 trillion.  
n = 20 → 2.4 × 10¹⁸. Stop.

Backtracking with pruning is still O(n!) in the worst case, but often much faster in practice. Complexity still reports the worst envelope.

---

### 7.8 Side-by-side growth (same n)

```
                    operations as n grows

 n = 8
 ─────────────────────────────────────────────
 O(1)       █
 O(log n)   ███                         3
 O(n)       ████████                    8
 O(n log n) ████████████████████████    24
 O(n²)      ████ ... (64)
 O(2ⁿ)      ████ ... (256)
 O(n!)      ████ ... (40320)

 n = 16  (only doubled)
 O(n)       16          ×2
 O(n log n) 64          ×2.7
 O(n²)      256         ×4
 O(2ⁿ)      65536       ×256
 O(n!)      2.1e13      disaster
```

**Rule:** if n doubles,

| Class | Work becomes about |
|-------|---------------------|
| O(1) | same |
| O(log n) | +1 step (log₂) |
| O(n) | ×2 |
| O(n log n) | slightly more than ×2 |
| O(n²) | ×4 |
| O(2ⁿ) | squared (2ⁿ → 2²ⁿ = (2ⁿ)²) |
| O(n!) | × (n+1) when n increases by 1; far worse than doubling |

---

## 8. How to analyze code in practice

### 8.1 A checklist

1. **What is n?** Array length, number of nodes, bits, value of the integer — pick the input that grows.
2. **Loops:** independent nested loops multiply. Sequential loops add (then take the max term).
3. **Recursion:** write `T(n) = …`, then solve (unfold, Master theorem, recursion tree).
4. **Hidden costs:** `sort()`, `insert` in the middle of an array, hashing expected vs worst, string concat in a loop.
5. **Space:** arrays you allocate + recursion depth + data structures.
6. **State the case:** worst / average / amortized.

### 8.2 Recursion tree method (general)

```
T(n) = a T(n/b) + f(n)

                    f(n)                  level cost f(n)
                   /    \
              f(n/b)    ... a children    level cost a · f(n/b)
              /    \
         f(n/b²) ...                      level cost a² · f(n/b²)
              ...
         Θ(1) leaves                      a^{log_b n} = n^{log_b a} leaves
```

- If level costs **shrink** geometrically → T is dominated by the root: ~ f(n).
- If level costs **stay equal** → T = f(n) · (number of levels) = f(n) log n.
- If level costs **grow** → T is dominated by the leaves: ~ n^{log_b a}.

That is the Master theorem in picture form.

### 8.3 Common traps

| Code | People say | Truth |
|------|------------|--------|
| two sequential O(n) loops | O(n²) | O(n) |
| binary search inside an O(n) loop | O(n) | O(n log n) |
| `for i in n: for j in i: ...` | O(n²) | yes, Θ(n²) |
| Python `list.insert(0, x)` n times | O(n) | O(n²) — each insert shifts |
| `s = s + char` n times (immutable string) | O(n) | O(n²) |
| Hash map operations | O(1) | **expected** O(1), worst O(n) unless worst-case hashing |
| DFS on a graph | O(n) | O(V + E) |

### 8.4 Space examples

| Algorithm | Extra space |
|-----------|-------------|
| Iterative binary search | O(1) |
| Recursive binary search | O(log n) stack |
| Mergesort | O(n) for merge buffers (+ O(log n) stack) |
| Heapsort | O(1) extra (in-place heap) |
| Quicksort | O(log n) stack average, O(n) worst |
| Hash set of all elements | O(n) |
| DP table `dp[n][n]` | O(n²) |

---

## 9. Quick reference

### Notation

| Symbol | Reads as | Formal idea |
|--------|----------|-------------|
| **O(f)** | at most order f | T ≤ c f |
| **Ω(f)** | at least order f | T ≥ c f |
| **Θ(f)** | exactly order f | c₁ f ≤ T ≤ c₂ f |
| **o(f)** | strictly slower than f | T/f → 0 |
| **amortized O(f)** | average over a **worst-case sequence** | total / count |

### Cases

| Case | Question it answers |
|------|---------------------|
| Best | luckiest input of size n |
| Worst | adversary’s input of size n |
| Average | expected T under a distribution |
| Amortized | cost of one op inside a long sequence |

### Classes you must recognize on sight

```
O(1)        hash get, array index, swap
O(log n)    binary search, balanced tree, binary exp
O(n)        single pass, two pointers
O(n log n)  efficient comparison sort, divide & conquer
O(n²)       all pairs, simple quadratic DP, slow sorts
O(2ⁿ)       subsets, naive recursion
O(n!)       permutations, naive TSP
```

### One sentence to remember

> **Drop constants, keep the dominant term, say which case you mean, and never confuse “one operation can be slow” with “a sequence of operations is slow” (that last one is amortization).**

---

## What to learn next

Once this page feels natural, the next DSA foundations are usually:

1. How arrays, linked lists, stacks, and queues implement these costs in their operations
2. Recursion and recurrence relations (Master theorem in more detail)
3. Searching and sorting, now that you can **compare** them by complexity

When you read an algorithm from here on, always write three lines:

```text
Time (worst):
Time (avg / amortized if it matters):
Extra space:
```

That habit is the whole skill.

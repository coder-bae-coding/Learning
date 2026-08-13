# Java 8 Stream API — 30 Interview Questions & Answers

## Question 1: What is a Stream?

**Answer:** A Stream is an interface introduced in Java 8 in the `java.util.stream` package that represents a sequence of elements supporting functional-style operations to process data.

**Explanation:** Streams provide methods for filtering, mapping, iterating, and reducing data cleanly without mutating the underlying data source.

---

## Question 2: Which of the following is correct?

**Options:**
- A) Stream interface is part of the `java.util.stream` package
- B) Stream is a data structure
- C) Stream extends the Collection interface
- D) None of the above

**Answer:** **A) Stream interface is part of the `java.util.stream` package**

**Explanation:** Streams are not data structures because they do not store elements in memory, and they do not extend the Collection interface.

---

## Question 3: Which of the following statements about Stream is correct?

**Options:**
- A) A stream cannot be reused once closed
- B) A stream can be sequential or parallel
- C) A stream supports functional-style operations
- D) All of the above

**Answer:** **D) All of the above**

**Explanation:** Once a terminal operation is called, the stream is consumed and cannot be reused. Streams can process elements sequentially or concurrently in parallel, and stream operations work closely with functional interfaces such as `Predicate`, `Function`, and `Consumer`.

---

## Question 4: What is the difference between Collection and Stream?

| Aspect | Collection | Stream |
|---|---|---|
| Purpose | Used to store data in memory | Used to process data |
| Reusability | Can be reused and traversed multiple times | Consumed once used |
| Modification | Elements can be added or removed | Does not modify the source data |
| Iteration | External iteration using explicit `for` / `while` loops | Internal iteration managed by the framework |

---

## Question 5: What is the difference between Stream and `stream()` method?

- **`Stream`** is the core interface in `java.util.stream`.
- **`stream()`** is a default method declared inside the `Collection` interface that converts a collection such as a `List` or `Set` into a sequential stream.

---

## Question 6: Match the stream method with its typical use case

**Methods:** `filter`, `map`, `forEach`

**Use cases:**
- a) Transform each element to another value
- b) Iterate and perform an action on each element
- c) Select elements that satisfy a given condition

**Answer:**
- `filter` → **c) Select elements that satisfy a given condition**
- `map` → **a) Transform each element to another value**
- `forEach` → **b) Iterate and perform an action on each element**

---

## Question 7: Match the stream method with its corresponding functional interface

**Methods:** `filter`, `map`, `forEach`

**Interfaces:** `Consumer`, `Predicate`, `Function`

**Answer:**
- `filter` → **Predicate** — takes a condition returning `boolean`
- `map` → **Function** — takes an element and transforms it
- `forEach` → **Consumer** — takes an element and performs an action without returning anything

---

## Question 8: What will the following code do?

The code filters even numbers, maps them to their squares, and prints them with `forEach`.

**Options:**
- A) Print all numbers
- B) Print squares of even numbers
- C) Print only even numbers
- D) Compilation error

**Answer:** **B) Print squares of even numbers**

**Explanation:** `filter` retains numbers where `n % 2 == 0`, `map` transforms those values using `n * n`, and `forEach` prints the final values.

---

## Question 9: Which of the following statements about `collect` is correct?

**Options:**
- A) `collect(Collectors.toList())` preserves the order of elements
- B) `collect(Collectors.toSet())` preserves duplicates
- C) `collect(Collectors.toMap())` can have duplicate keys
- D) None of the above

**Answer:** **A) `collect(Collectors.toList())` preserves the order of elements**

**Explanation:** `toList()` maintains insertion order. `toSet()` removes duplicates automatically, and `toMap()` throws an exception when duplicate keys are encountered without a merge function.

---

## Question 10: What is the difference between `collect`, `Collector`, and `Collectors`?

- **`collect()`** — a terminal method on the `Stream` interface used to perform a mutable reduction.
- **`Collector`** — an interface defining how accumulation into a container should happen.
- **`Collectors`** — a utility class providing pre-built factory methods that return common `Collector` implementations such as `toList()`, `toSet()`, and `groupingBy()`.

---

## Question 11: What is the difference between `Collectors.partitioningBy` and `Collectors.groupingBy`?

- **`partitioningBy`** — accepts a `Predicate` and splits elements into exactly two groups mapped under Boolean keys: `true` and `false`.
- **`groupingBy`** — accepts a classification `Function` and splits elements into multiple arbitrary groups based on the evaluated key.

---

## Question 12: Which of the following is an intermediate operation?

**Options:**
- A) `filter`
- B) `map`
- C) `sorted`
- D) All of the above

**Answer:** **D) All of the above**

**Explanation:** Intermediate operations return a new `Stream` and can be chained together without triggering execution immediately.

---

## Question 13: Which of the following is a terminal operation?

**Options:**
- A) `forEach`
- B) `collect`
- C) `reduce`
- D) All of the above

**Answer:** **D) All of the above**

**Explanation:** Terminal operations end the stream pipeline and produce a result or side effect. No further stream operations can be chained after them.

---

## Question 14: Which statement about intermediate operations is correct?

**Options:**
- A) They are executed immediately when called
- B) They are executed lazily only when a terminal operation is invoked
- C) They store elements permanently in memory
- D) None of the above

**Answer:** **B) They are executed lazily only when a terminal operation is invoked**

**Explanation:** Intermediate operations build an execution pipeline; no elements pass through until a terminal operation is invoked.

---

## Question 15: What will be the output of the code where `filter` and `map` are chained with print statements, but no terminal operation is added?

**Answer:** **Nothing is printed to the console.**

**Explanation:** Intermediate operations are lazy, so they are never executed unless a terminal operation such as `collect` or `forEach` triggers the pipeline.

---

## Question 16: Which of these statements is true in a stream?

**Options:**
- A) `findFirst` returns the first element according to encounter order
- B) `findAny` may return any element in the stream
- C) `findFirst` and `findAny` behave differently
- D) All of the above

**Answer:** **D) All of the above**

**Explanation:** `findFirst` strictly respects encounter order, while `findAny` is optimized for parallel streams where retrieving any arbitrary matching element quickly is desired.

---

## Question 17: What is the return type of `findFirst()`?

**Options:**
- A) `String`
- B) `Optional<T>`
- C) `List<T>`
- D) `T`

**Answer:** **B) `Optional<T>`**

**Explanation:** It returns an `Optional` containing the element if found, or an empty `Optional` if the stream is empty.

---

## Question 18: Is `findFirst()` a short-circuiting operation?

**Answer:** **True**

**Explanation:** As soon as `findFirst()` evaluates the very first matching element, processing stops immediately without traversing the remainder of the stream.

---

## Question 19: What is the return type of `anyMatch()`, `allMatch()`, and `noneMatch()`?

**Options:**
- A) `Stream`
- B) `boolean`
- C) `Optional`
- D) `List`

**Answer:** **B) `boolean`**

**Explanation:** All matching methods evaluate conditions and return a primitive boolean result.

---

## Question 20: Match the stream method with its correct description

**Methods:** `anyMatch`, `allMatch`, `noneMatch`

**Answer:**
- `anyMatch` → **Returns `true` if any element matches**
- `allMatch` → **Returns `true` if all elements match**
- `noneMatch` → **Returns `true` if no element matches**

---

## Question 21: What will be the output of running `anyMatch`, `allMatch`, and `noneMatch` on a list of names?

Conditions tested:
- `anyMatch` for containing `"Charlie"`
- `allMatch` for starting with `'C'`
- `noneMatch` for ending with `'Z'`

**Answer:** `true, false, true`

**Explanation:**
1. `anyMatch` found `"Charlie"` → `true`.
2. `allMatch` failed because not every name starts with `'C'` → `false`.
3. `noneMatch` succeeded because no name ends with `'Z'` → `true`.

---

## Question 22: What does the `sorted()` method do in a stream?

**Answer:** `sorted()` is an intermediate operation used to sort stream elements.

**Explanation:** Without arguments, it sorts elements according to their natural order using `Comparable`. It can also accept a custom `Comparator` to apply custom sorting logic.

---

## Question 23: What is the difference between `Stream.sorted()` and `Collections.sort()`?

- **`Stream.sorted()`** — intermediate, lazy operation that operates on streams and returns a new stream without modifying the original source.
- **`Collections.sort()`** — eager utility method that operates directly on `List` structures and mutates the list in place.

---

## Question 24: Which of the following statements is true?

**Options:**
- A) `sorted` is a terminal operation
- B) `sorted` is an intermediate operation
- C) `sorted` modifies the original collection
- D) None of the above

**Answer:** **B) `sorted` is an intermediate operation**

**Explanation:** It produces a sorted stream pipeline without mutating the underlying collection.

---

## Question 25: What are primitive streams in Java?

**Answer:** Primitive streams are specialized stream implementations: `IntStream`, `LongStream`, and `DoubleStream`.

**Explanation:** They work directly with primitive types instead of object wrappers. They eliminate automatic boxing and unboxing overhead, leading to faster and more memory-efficient execution.

---

## Question 26: Which thread pool does parallel stream use by default?

**Options:**
- A) Custom thread pool
- B) `ForkJoinPool`
- C) Single thread
- D) `ExecutorService`

**Answer:** **B) `ForkJoinPool`**

**Explanation:** Parallel streams use Java's shared `ForkJoinPool.commonPool()` to split workloads across available processor cores.

---

## Question 27: How do you preserve encounter order when using parallel streams?

**Options:**
- A) Use `forEachOrdered`
- B) Use `forEach`
- C) Use `map`
- D) You cannot preserve order

**Answer:** **A) Use `forEachOrdered`**

**Explanation:** Standard `forEach` does not guarantee element ordering when executing concurrently. `forEachOrdered` forces processing to preserve the original encounter order.

---

## Question 28: What is the difference between `map` and `flatMap`?

- **`map`** — transforms each input element into a single output element (1-to-1 transformation).
- **`flatMap`** — transforms each input element into a stream of elements and flattens multiple nested streams into a single flat stream (1-to-many transformation, removing nesting).

---

## Question 29: What is the difference between `reduce` and `collect`?

- **`reduce`** — combines stream elements immutably into a single scalar value such as a sum, product, or maximum.
- **`collect`** — accumulates stream elements into a mutable result container such as a `List`, `Set`, or `Map`.

---

## Question 30: Which of the following is correct to sum integers using streams?

**Options:**
- A) `list.stream().reduce(0, Integer::sum)`
- B) `list.stream().collect(Collectors.summingInt(Integer::intValue))`
- C) `list.stream().mapToInt(Integer::intValue).sum()`
- D) All of the above

**Answer:** **D) All of the above**

**Explanation:** All three approaches are valid techniques in the Stream API for calculating integer sums cleanly.

---

## Quick Revision

| Topic | Key Point |
|---|---|
| `Stream` | Functional data-processing interface |
| `filter()` | Selects elements using a `Predicate` |
| `map()` | Transforms elements using a `Function` |
| `forEach()` | Performs an action using a `Consumer` |
| Intermediate operations | Lazy and return a new stream |
| Terminal operations | Trigger execution and finish the stream |
| `findFirst()` | First element according to encounter order |
| `findAny()` | Any available element; useful when order is unimportant |
| `partitioningBy()` | Exactly two Boolean groups |
| `groupingBy()` | Multiple classification groups |
| `sorted()` | Intermediate sorting operation |
| Primitive streams | `IntStream`, `LongStream`, `DoubleStream` |
| Parallel streams | Use `ForkJoinPool.commonPool()` by default |
| `forEachOrdered()` | Preserves encounter order |
| `map()` vs `flatMap()` | 1-to-1 transformation vs flattening nested streams |
| `reduce()` vs `collect()` | Scalar reduction vs mutable container accumulation |

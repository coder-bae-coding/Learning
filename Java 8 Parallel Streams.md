# Java 8 Parallel Streams

Parallel Streams process parts of a stream concurrently using multiple threads.

```java
list.parallelStream();
// or
list.stream().parallel();
```

> **Key idea:** Parallel streams are a performance optimization, not automatically faster.

---

## Sequential vs Parallel

| Sequential | Parallel |
|---|---|
| Usually one thread | Multiple threads may execute concurrently |
| Lower overhead | Higher overhead |
| Good for small/simple work | Better for large, independent CPU-heavy work |
| `forEach()` follows encounter order for ordered sources | `forEach()` does not guarantee encounter order |

---

## `forEach()` vs `forEachOrdered()`

```java
names.parallelStream()
     .forEach(System.out::println);
```

Order is **not guaranteed**.

```java
names.parallelStream()
     .forEachOrdered(System.out::println);
```

Preserves the stream's **encounter order**, but can reduce parallel performance.

---

## When to Use

Parallel streams are most useful when:

- The dataset is large enough to keep workers busy.
- Each element requires meaningful CPU work.
- Operations are independent/stateless.
- The workload is CPU-bound.

```java
List<Integer> result = numbers.parallelStream()
        .map(this::expensiveCalculation)
        .collect(Collectors.toList());
```

### Avoid when

- Dataset is small.
- Operations are very cheap.
- Code modifies shared mutable state.
- Tasks spend significant time blocked on I/O such as HTTP/database calls.

Splitting, scheduling and combining work add overhead, so sequential execution may be faster.

---

## Shared Mutable State — Common Pitfall

### ❌ Bad

```java
List<Integer> result = new ArrayList<>();

numbers.parallelStream()
       .map(n -> n * n)
       .forEach(result::add);
```

`ArrayList` is not safe for concurrent mutation.

### ✅ Good

```java
List<Integer> result = numbers.parallelStream()
        .map(n -> n * n)
        .collect(Collectors.toList());
```

Prefer stateless operations and collectors instead of manually mutating shared state.

---

## Reduction

Parallel reductions combine partial results, so the operation should generally be **associative**:

```text
(a op b) op c == a op (b op c)
```

Good:

```java
int sum = numbers.parallelStream()
        .mapToInt(Integer::intValue)
        .sum();
```

Be careful with order-dependent operations such as subtraction.

---

## ForkJoinPool

Parallel streams use the **Fork/Join framework** and, by default, the common pool:

```java
ForkJoinPool.commonPool()
```

Inspect its configured parallelism:

```java
ForkJoinPool.commonPool().getParallelism();
```

A common interview simplification is that default parallelism is commonly related to `availableProcessors() - 1`; do not assume every stream always runs with exactly that many active threads.

---

## Custom ForkJoinPool

A computation can be submitted to a custom pool when isolation is needed:

```java
ForkJoinPool pool = new ForkJoinPool(4);

try {
    pool.submit(() ->
        numbers.parallelStream()
               .forEach(System.out::println)
    ).get();
} finally {
    pool.shutdown();
}
```

Use this deliberately; custom pools add complexity.

---

## Performance

Never assume parallel is faster. Test the real workload.

For serious JVM benchmarking, use **JMH** rather than relying on a single `currentTimeMillis()` measurement.

Consider:

- Dataset size
- CPU work per element
- Number of processors
- Splitting/combining overhead
- Ordering requirements

---

## Quick Example

```java
List<Integer> squares = numbers.parallelStream()
        .filter(n -> n % 2 == 0)
        .map(n -> n * n)
        .collect(Collectors.toList());
```

This is a good parallel candidate only when the real workload is large/expensive enough to justify the overhead.

---

## Interview Points to Remember

1. `parallelStream()` enables concurrent stream processing.
2. Default execution uses the **ForkJoin common pool**.
3. `forEach()` does not guarantee encounter order in parallel execution.
4. `forEachOrdered()` preserves encounter order but may reduce speedup.
5. Best fit: **large, CPU-bound, independent work**.
6. Avoid shared mutable state.
7. Use collectors/reductions instead of manual shared mutation.
8. Parallel streams are **not automatically faster**.
9. Be cautious with blocking I/O in the common pool.
10. Benchmark before choosing parallel execution.

# Java 8 Parallel Streams

Parallel Streams extend the Java 8 Stream API with a convenient way to process stream elements concurrently. A sequential stream normally executes its pipeline on the calling thread, while a parallel stream partitions the stream's data and processes partitions using multiple threads.

> **Core idea:** parallel streams can improve throughput for sufficiently large, CPU-bound workloads, but they are not automatically faster. The cost of splitting, scheduling, synchronization, and combining results can outweigh the benefit for small or cheap operations.

---

## 1. What Is a Parallel Stream?

A normal sequential stream is created with:

```java
employees.stream();
```

A parallel stream can be created with:

```java
employees.parallelStream();
```

or by converting an existing stream:

```java
employees.stream().parallel();
```

A parallel stream does **not** mean that every element gets its own thread. Instead, the stream framework partitions the source into chunks and executes parts of the pipeline concurrently.

### Basic Example

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

numbers.parallelStream()
       .map(n -> n * n)
       .forEach(System.out::println);
```

The output order is not guaranteed because different chunks may finish at different times.

---

## 2. Sequential Stream vs. Parallel Stream

| Feature | Sequential Stream | Parallel Stream |
|---|---|---|
| Execution | Usually one thread | Multiple threads may execute concurrently |
| Creation | `stream()` | `parallelStream()` / `parallel()` |
| Main execution mechanism | Normal stream pipeline | Fork/Join framework |
| Typical benefit | Predictable and low overhead | Potential speedup for suitable workloads |
| Ordering with `forEach()` | Encounter order for ordered sources is observed by the sequential execution | Not guaranteed to be observed |
| Overhead | Low | Higher |
| Best use case | Small/medium work or cheap operations | Large, independent, CPU-heavy work |

Parallelism should be treated as a **performance optimization**, not as a default replacement for sequential streams.

---

## 3. How a Parallel Stream Works

Conceptually, a parallel stream follows these stages:

1. **Source** – for example, an `ArrayList` or another splittable source.
2. **Partitioning** – the stream framework uses a `Spliterator` to divide the source into smaller portions.
3. **Parallel execution** – tasks are submitted to the Fork/Join framework.
4. **Intermediate operations** – operations such as `map`, `filter`, and `sorted` execute over the partitions.
5. **Combination** – partial results are combined to produce the terminal result.
6. **Terminal operation** – the final action such as `collect`, `reduce`, or `forEach` completes the pipeline.

The framework handles most of this automatically; you generally only declare the pipeline.

---

## 4. `forEach()` vs. `forEachOrdered()`

One of the most important interview points is ordering.

### `forEach()`

`forEach()` does not guarantee that output is produced in encounter order when the stream is parallel.

```java
List<String> names = Arrays.asList("Amit", "Rahul", "Priya", "Anjali");

names.parallelStream()
     .forEach(System.out::println);
```

A possible output is:

```text
Priya
Anjali
Amit
Rahul
```

The exact output can vary between runs.

### `forEachOrdered()`

`forEachOrdered()` respects the stream's encounter order when the stream has one.

```java
names.parallelStream()
     .forEachOrdered(System.out::println);
```

Output:

```text
Amit
Rahul
Priya
Anjali
```

### Important Performance Note

`forEachOrdered()` can reduce some of the benefit of parallel execution because the framework has to respect ordering constraints. It should therefore be used when ordering is actually required, not merely by habit.

---

## 5. Encounter Order vs. Processing Order

These are not exactly the same concept.

A stream may have an **encounter order**, meaning there is a defined order in which elements are logically encountered. An ordered source such as an `ArrayList` has an encounter order.

Parallel execution can still process elements in different threads and in a different physical order.

Therefore:

```java
parallelStream().forEach(...)
```

can process elements concurrently without preserving the observable output order, while:

```java
parallelStream().forEachOrdered(...)
```

preserves the encounter order for the terminal action.

---

## 6. Why Parallel Streams Are Often Worse for Small Datasets

Parallel execution has overhead. Before multiple threads can do useful work, the framework has to divide the source, schedule tasks, coordinate work, and combine the results.

For a tiny dataset such as four employees:

```java
List<Employee> employees = Arrays.asList(
    new Employee("Amit", "IT", 50000),
    new Employee("Rahul", "HR", 60000),
    new Employee("Priya", "Finance", 70000),
    new Employee("Anjali", "IT", 55000)
);
```

this operation:

```java
employees.parallelStream()
         .map(Employee::getName)
         .collect(Collectors.toList());
```

usually has little useful parallel work to do.

### Sources of Overhead

- Splitting the input into tasks.
- Fork/Join task scheduling.
- Synchronization and coordination.
- Combining partial results.
- Thread wake-up and execution costs.
- Extra memory/cache effects.

For small or very cheap operations, a sequential stream can be faster.

---

## 7. When Parallel Streams Can Be Useful

Parallel streams are more likely to help when the workload has most of these characteristics:

### 7.1 Large Dataset

There should be enough elements to keep multiple worker threads busy.

Examples:

- Hundreds of thousands of elements.
- Millions of numerical values.
- Large collections requiring substantial computation per element.

Dataset size alone is not enough, but a larger source gives the framework more opportunities to partition the work.

### 7.2 CPU-Bound Work

The operation should spend meaningful time computing rather than waiting for external resources.

Examples:

- Mathematical transformations.
- Parsing or CPU-heavy data processing.
- Image transformations.
- Expensive pure calculations.
- Independent numerical analysis.

### 7.3 Independent Operations

Each element should ideally be processed independently.

For example:

```java
numbers.parallelStream()
       .map(this::expensiveCalculation)
       .collect(Collectors.toList());
```

is a better parallel candidate than a pipeline where every element needs to modify shared mutable state.

---

## 8. CPU-Bound vs. I/O-Bound Work

Parallel streams are generally most suitable for **CPU-bound** work.

### CPU-Bound

The CPU is busy calculating the result.

```java
numbers.parallelStream()
       .map(this::heavyCalculation)
       .collect(Collectors.toList());
```

### I/O-Bound

The task spends significant time waiting for resources such as:

- Database responses.
- HTTP requests.
- File/network I/O.
- External services.

Using parallel streams for blocking I/O can be problematic because the common Fork/Join pool has a limited number of worker threads. Blocking those workers may reduce throughput and interfere with unrelated work using the same pool.

For large-scale I/O concurrency, explicit concurrency tools such as `ExecutorService`, `CompletableFuture`, reactive APIs, or dedicated application-level executors are often a better fit.

---

## 9. Example: Expensive CPU Operation

```java
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParallelBenchmark {

    private static int heavyOperation(int number) {
        int result = number;

        for (int i = 0; i < 1000; i++) {
            result = result * 31 + i;
        }

        return result;
    }

    public static void main(String[] args) {
        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < 1_000_000; i++) {
            numbers.add(i);
        }

        long startSequential = System.nanoTime();

        List<Integer> sequentialResult = numbers.stream()
                .map(ParallelBenchmark::heavyOperation)
                .collect(Collectors.toList());

        long endSequential = System.nanoTime();

        long startParallel = System.nanoTime();

        List<Integer> parallelResult = numbers.parallelStream()
                .map(ParallelBenchmark::heavyOperation)
                .collect(Collectors.toList());

        long endParallel = System.nanoTime();

        System.out.println("Sequential: "
                + (endSequential - startSequential) / 1_000_000
                + " ms");

        System.out.println("Parallel: "
                + (endParallel - startParallel) / 1_000_000
                + " ms");
    }
}
```

### Important Benchmarking Warning

A simple `System.currentTimeMillis()` benchmark is useful for a quick experiment but is not a reliable way to make serious performance claims.

For proper JVM benchmarking, use a tool such as **JMH (Java Microbenchmark Harness)**. JVM warm-up, JIT compilation, garbage collection, CPU frequency changes, allocation, and other system effects can significantly influence timing.

Also avoid drawing conclusions from one run. Benchmark multiple iterations and compare representative workloads.

---

## 10. Shared Mutable State: A Major Pitfall

Parallel streams make race conditions much easier to introduce when code mutates shared state.

### Bad Example

```java
List<Integer> numbers = new ArrayList<>();

for (int i = 1; i <= 1000; i++) {
    numbers.add(i);
}

List<Integer> squares = new ArrayList<>();

numbers.parallelStream()
       .map(n -> n * n)
       .forEach(squares::add);
```

The problem is that multiple threads can execute:

```java
squares.add(...);
```

concurrently.

`ArrayList` is not thread-safe for concurrent mutation. The result can therefore be incorrect or otherwise unsafe.

### Better Approach

Let the collector own the accumulation:

```java
List<Integer> squares = numbers.parallelStream()
        .map(n -> n * n)
        .collect(Collectors.toList());
```

The stream framework can use the collector's reduction machinery to combine partial results instead of having every worker mutate one shared list.

---

## 11. Prefer Stateless and Side-Effect-Free Operations

A parallel stream is easiest to reason about when each operation depends only on its input element and does not modify shared external state.

### Good

```java
List<Integer> result = numbers.parallelStream()
        .filter(n -> n % 2 == 0)
        .map(n -> n * n)
        .collect(Collectors.toList());
```

Each element can be processed independently.

### Risky

```java
int[] total = {0};

numbers.parallelStream()
       .forEach(n -> total[0] += n);
```

This has a race condition because multiple threads update the same mutable value.

For reductions, use stream reduction operations instead:

```java
int total = numbers.parallelStream()
        .mapToInt(Integer::intValue)
        .sum();
```

---

## 12. Reduction Must Be Associative

Parallel reduction relies on combining partial results. Therefore the reduction operation should be **associative**.

An operation is associative when:

```text
(a op b) op c == a op (b op c)
```

Examples of associative operations:

- Addition.
- Multiplication.
- Minimum.
- Maximum.

### Good Example

```java
int sum = numbers.parallelStream()
        .mapToInt(Integer::intValue)
        .sum();
```

### Dangerous Idea

A reduction that depends on evaluation order may produce unexpected results in parallel execution.

For example, subtraction is not associative:

```text
(10 - 5) - 2 = 3
10 - (5 - 2) = 7
```

Therefore arbitrary subtraction-based parallel reductions should not be treated like ordinary associative reductions.

---

## 13. The Fork/Join Pool

Java parallel streams are implemented using the **Fork/Join framework**.

By default, parallel stream operations use the common Fork/Join pool:

```java
ForkJoinPool.commonPool()
```

The pool is designed for tasks that can recursively split into smaller pieces and later combine their results.

### Inspect the Thread Name

```java
numbers.parallelStream()
       .forEach(number -> {
           System.out.println(
               number + " -> " + Thread.currentThread().getName()
           );
       });
```

You may see names such as:

```text
ForkJoinPool.commonPool-worker-1
ForkJoinPool.commonPool-worker-2
main
```

The exact scheduling and thread names depend on the runtime environment.

---

## 14. How Many Threads Are Used?

A common interview simplification is:

> The common Fork/Join pool's parallelism is typically related to the number of available processors, commonly `availableProcessors() - 1` for the default common-pool configuration.

However, avoid saying that a parallel stream is **always** exactly `CPU cores - 1` threads. The actual number of actively executing threads for a particular operation depends on the Fork/Join pool configuration, task structure, machine characteristics, and workload.

You can inspect the configured common-pool parallelism with:

```java
System.out.println(
    ForkJoinPool.commonPool().getParallelism()
);
```

---

## 15. Custom ForkJoinPool

Sometimes an application needs to isolate a parallel computation rather than relying on the global common pool.

A commonly used pattern is to submit the parallel-stream task to a custom `ForkJoinPool`:

```java
import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class CustomPoolExample {

    public static void main(String[] args) throws Exception {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

        ForkJoinPool customPool = new ForkJoinPool(4);

        try {
            customPool.submit(() -> {
                numbers.parallelStream()
                       .forEach(number ->
                           System.out.println(
                               number + " -> " +
                               Thread.currentThread().getName()
                           )
                       );
            }).get();
        } finally {
            customPool.shutdown();
        }
    }
}
```

### Important Caveat

This is useful when you need isolation or a different pool configuration, but custom pools should not be introduced casually. Explicit executors or application-managed concurrency are often clearer when the workload is fundamentally an asynchronous task system rather than a simple collection computation.

---

## 16. `parallel()` vs. `parallelStream()`

These are two different ways to obtain a parallel stream.

### From a Collection

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

numbers.parallelStream();
```

### Convert an Existing Stream

```java
numbers.stream()
       .parallel()
       .map(n -> n * n);
```

Likewise, you can switch a stream back to sequential mode:

```java
numbers.parallelStream()
       .sequential()
       .map(n -> n * n);
```

The last call to `parallel()` or `sequential()` determines the stream's mode.

---

## 17. Intermediate Operations in Parallel Streams

Common intermediate operations include:

```java
filter()
map()
flatMap()
distinct()
sorted()
limit()
skip()
```

Their performance characteristics can differ significantly in parallel mode.

For example:

```java
numbers.parallelStream()
       .filter(n -> n % 2 == 0)
       .map(n -> n * n)
       .collect(Collectors.toList());
```

`filter` and `map` are generally easy to apply independently.

Operations that require coordination or global knowledge, such as `sorted()` and some ordered operations, may have less impressive parallel scaling.

---

## 18. Why `sorted()` Can Be Expensive

Sorting is not simply an independent operation on each element. A global sorted result requires the framework to coordinate the partitions.

```java
List<Integer> sorted = numbers.parallelStream()
        .sorted()
        .collect(Collectors.toList());
```

Parallel execution may still help for sufficiently large workloads, but the scalability is often not as straightforward as a simple stateless `map`.

The same general principle applies to other operations that need substantial coordination or global ordering.

---

## 19. Primitive Streams

For numerical workloads, primitive streams such as `IntStream`, `LongStream`, and `DoubleStream` can avoid some boxing overhead.

Example:

```java
int sum = numbers.parallelStream()
        .mapToInt(Integer::intValue)
        .sum();
```

Or:

```java
int sum = IntStream.rangeClosed(1, 1_000_000)
        .parallel()
        .sum();
```

Primitive streams can be particularly useful for CPU-heavy numerical processing.

---

## 20. Parallel Stream Collectors

Collectors are designed to accumulate stream results, and the collector characteristics influence how efficiently a collector can work in parallel.

A familiar example is:

```java
List<Integer> result = numbers.parallelStream()
        .map(n -> n * n)
        .collect(Collectors.toList());
```

The important interview point is:

> Do not assume that manually mutating a shared collection is equivalent to using a collector.

The stream framework can accumulate partial results and combine them safely according to the collector's contract.

---

## 21. Side Effects Can Produce Logical Bugs Even with Thread-Safe Structures

Changing `ArrayList` to a concurrent collection does not automatically make a parallel-stream design good.

For example:

```java
ConcurrentLinkedQueue<Integer> queue = new ConcurrentLinkedQueue<>();

numbers.parallelStream()
       .map(n -> n * n)
       .forEach(queue::add);
```

This can be thread-safe, but it still introduces external mutation and often communicates a less declarative design than:

```java
List<Integer> result = numbers.parallelStream()
        .map(n -> n * n)
        .collect(Collectors.toList());
```

Thread safety and good parallel-stream design are related, but they are not the same thing.

---

## 22. Parallel Stream and `peek()`

`peek()` should not be used as a mechanism for performing important side effects, particularly in parallel pipelines.

Example:

```java
numbers.parallelStream()
       .peek(n -> System.out.println(
           Thread.currentThread().getName() + ": " + n
       ))
       .map(n -> n * n)
       .collect(Collectors.toList());
```

The output order is not guaranteed, and `peek()` is primarily intended for debugging or observing a pipeline, not for implementing business logic.

---

## 23. Common Cases Where Parallel Streams May Hurt Performance

Avoid assuming a speedup in situations such as:

### Small Input

```java
Arrays.asList(1, 2, 3, 4).parallelStream()
```

The parallelization overhead can dominate the work.

### Cheap Operations

```java
numbers.parallelStream()
       .map(n -> n + 1)
```

The actual computation is so cheap that the parallel overhead may cost more than the computation.

### Heavy Synchronization

If many threads contend for the same lock or shared resource, parallelism can make things slower.

### Blocking I/O

Worker threads can remain blocked while waiting for external resources.

### Order-Sensitive Work

Strong ordering requirements can reduce the advantage of parallel execution.

### Poorly Splittable Sources

Parallelism depends on efficient partitioning. Some data sources split much better than others.

---

## 24. Data Structure Matters

The stream source influences how effectively it can be partitioned.

An `ArrayList` is generally a good source for parallel processing because its elements are efficiently indexed and split.

```java
List<Integer> numbers = new ArrayList<>();

numbers.parallelStream()
       .map(n -> n * n)
       .collect(Collectors.toList());
```

A source with expensive or inefficient splitting may provide less benefit from parallel processing.

This is one reason benchmarking the actual workload matters more than relying on a fixed rule such as "large collection = parallel stream."

---

## 25. Ordering in Collectors

A parallel stream can still produce an ordered result when the operation and collector preserve ordering.

For example:

```java
List<Integer> result = numbers.parallelStream()
        .map(n -> n * 2)
        .collect(Collectors.toList());
```

For an ordered source, the resulting list commonly follows encounter order even though computation happened in parallel.

This does **not** mean that the mapping operations executed one by one in order. It means the reduction/collection can preserve the stream's encounter order.

That distinction is important in interviews.

---

## 26. `reduce()` in Parallel Streams

Consider:

```java
int sum = numbers.parallelStream()
        .reduce(0, Integer::sum);
```

Conceptually, the framework can compute partial sums in multiple partitions and then combine those partial sums.

For a custom reduction, the identity, accumulator, and combiner must obey the reduction contract.

Example:

```java
int sum = numbers.parallelStream()
        .reduce(
            0,
            (a, b) -> a + b,
            (a, b) -> a + b
        );
```

The combiner is particularly important for parallel execution because partial results need to be merged.

---

## 27. Interview Trap: "Parallel Means Faster"

### Incorrect Statement

> Parallel streams are always faster because they use all CPU cores.

### Correct Answer

Parallel streams can be faster when the workload is sufficiently large, CPU-bound, independently executable, and has enough work to amortize parallelization overhead. They can also be slower when the workload is small, cheap, blocking, synchronization-heavy, or order-sensitive.

---

## 28. Interview Trap: "Every Element Gets a Thread"

### Incorrect Statement

> Every element in a parallel stream runs on a separate thread.

### Correct Answer

No. The stream framework partitions the data into tasks, and a limited number of worker threads execute those tasks. Threads process multiple elements over time.

---

## 29. Interview Trap: "`forEachOrdered()` Makes Execution Sequential"

### More Accurate Answer

`forEachOrdered()` preserves encounter order for the terminal action, but it does not mean that the entire stream pipeline necessarily runs as a simple one-thread-at-a-time sequential pipeline.

However, ordering constraints can limit parallelism and reduce the performance benefit.

---

## 30. Interview Trap: "A Thread-Safe Collection Fixes Everything"

Using `ConcurrentHashMap`, `CopyOnWriteArrayList`, or another concurrent structure may prevent a data race, but it does not automatically make the overall algorithm efficient.

The preferred first question is:

> Can this stream be written as an independent, side-effect-free transformation or reduction?

Usually, that is easier to reason about and easier for the stream implementation to parallelize.

---

## 31. Example: Employee Processing

```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Employee {
    private final String name;
    private final String department;
    private final double salary;

    public Employee(String name, String department, double salary) {
        this.name = name;
        this.department = department;
        this.salary = salary;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public double getSalary() {
        return salary;
    }
}

public class EmployeeParallelExample {

    public static void main(String[] args) {
        List<Employee> employees = Arrays.asList(
            new Employee("Amit", "IT", 50000),
            new Employee("Rahul", "HR", 60000),
            new Employee("Priya", "Finance", 70000),
            new Employee("Anjali", "IT", 55000)
        );

        List<String> itEmployees = employees.parallelStream()
                .filter(employee -> "IT".equals(employee.getDepartment()))
                .map(Employee::getName)
                .collect(Collectors.toList());

        System.out.println(itEmployees);
    }
}
```

This is structurally better than manually adding names to a shared list because the pipeline is stateless and the collection step handles accumulation.

---

## 32. Parallel Streams in Real Applications

Be careful when using parallel streams inside server applications.

For example, if a Spring Boot application handles many incoming HTTP requests and each request launches a parallel stream using the common pool, unrelated requests may compete for the same common-pool worker threads.

This can create:

- Thread contention.
- Unpredictable latency.
- Resource competition.
- Reduced throughput under load.

For application services with explicit concurrency requirements, a dedicated executor can make resource ownership and capacity easier to control.

The right choice depends on the workload and the application's concurrency model.

---

## 33. Parallel Stream Best Practices

### Prefer Independent Operations

```java
numbers.parallelStream()
       .map(this::calculate)
       .filter(this::isValid)
       .collect(Collectors.toList());
```

### Avoid Shared Mutable State

Do not write multiple threads into an ordinary `ArrayList`, `HashMap`, or primitive variable wrapper unless the design explicitly guarantees safe coordination.

### Measure Before and After

Do not assume a parallel stream is faster. Benchmark the actual workload.

### Prefer CPU-Bound Work

Parallel streams are generally a better fit for computational work than blocking network/database operations.

### Understand Ordering Requirements

Only pay the cost of ordering when the application actually needs it.

### Consider the Common Pool

Remember that parallel streams normally use shared common-pool infrastructure, which may affect other code in the application.

---

## 34. A Practical Decision Checklist

Before replacing `.stream()` with `.parallelStream()`, ask:

1. Is the dataset large enough?
2. Is each element's computation expensive enough to amortize overhead?
3. Is the work CPU-bound?
4. Are operations independent?
5. Can I avoid shared mutable state?
6. Do I really need ordering?
7. Is the source efficiently splittable?
8. Could this compete with other code using the common Fork/Join pool?
9. Have I actually benchmarked both implementations?

If several answers are "no", sequential processing is often the safer choice.

---

## 35. Corrected Benchmarking Example with a Helper Method

For learning purposes, it is useful to isolate the two versions:

```java
private static List<Integer> sequential(List<Integer> numbers) {
    return numbers.stream()
            .map(ParallelBenchmark::heavyOperation)
            .collect(Collectors.toList());
}

private static List<Integer> parallel(List<Integer> numbers) {
    return numbers.parallelStream()
            .map(ParallelBenchmark::heavyOperation)
            .collect(Collectors.toList());
}
```

Then benchmark both methods over multiple iterations with a proper benchmark harness such as JMH for meaningful measurements.

---

## 36. Sequential vs. Parallel: Interview Summary

| Criterion | Sequential Stream | Parallel Stream |
|---|---|---|
| Thread usage | Usually calling thread | Multiple Fork/Join workers may participate |
| Creation | `stream()` | `parallelStream()` / `parallel()` |
| Overhead | Lower | Higher |
| Small datasets | Usually preferable | Often unnecessary or slower |
| Large CPU-heavy workloads | May leave cores unused | Can improve throughput |
| I/O-bound workloads | Often simpler | Can be problematic with blocking operations |
| Shared mutation | Easier to reason about | High risk if incorrectly designed |
| `forEach()` ordering | Encounter order is naturally observed in sequential execution | Not guaranteed to be observed in encounter order |
| `forEachOrdered()` | Ordered | Ordered, but may reduce parallel benefit |
| Default pool | N/A | `ForkJoinPool.commonPool()` |
| Best practice | Keep operations clear and side-effect-free | Keep operations clear, independent, and side-effect-free |

---

## 37. One-Minute Interview Answer

> **What is a parallel stream in Java 8?**
>
> A parallel stream is a Stream API pipeline that allows elements to be processed concurrently by partitioning the source and using the Fork/Join framework. In the default configuration it uses the common ForkJoinPool. Parallel streams can improve performance for sufficiently large, CPU-bound, independent workloads, but they introduce overhead from partitioning, scheduling, synchronization, and combining results. They are therefore not always faster. `forEach()` does not guarantee encounter order in a parallel stream, while `forEachOrdered()` preserves encounter order at the terminal action. Shared mutable state should be avoided, and proper benchmarking should be used before claiming a performance improvement.

---

## 38. Key Takeaways

- `parallelStream()` enables parallel stream processing.
- Parallel streams partition work rather than creating one thread per element.
- The default execution mechanism is the common Fork/Join pool.
- `forEach()` does not guarantee encounter order in a parallel stream.
- `forEachOrdered()` preserves encounter order but can reduce parallel efficiency.
- Parallelism has overhead; small or cheap workloads may become slower.
- Large, CPU-bound, independent computations are the strongest candidates.
- Avoid shared mutable state and side effects.
- Prefer `collect()` and proper reduction operations over manual mutation.
- Reduction functions should satisfy the required associativity/combination contract.
- Blocking I/O is generally not the ideal use case for the common Fork/Join pool.
- A parallel stream can still produce an ordered result even though computation occurred concurrently.
- Custom ForkJoinPool usage is possible, but application-managed executors may be clearer for explicit concurrency requirements.
- Use JMH for serious performance benchmarking.

---

## Quick Revision

```text
Parallel Stream
    |
    +-- Source
    |     |
    |     +-- Spliterator partitions data
    |
    +-- Fork/Join execution
    |     |
    |     +-- Worker threads process partitions
    |
    +-- Intermediate operations
    |     |
    |     +-- map / filter / flatMap / etc.
    |
    +-- Combine partial results
    |
    +-- Terminal operation
          |
          +-- collect / reduce / forEach / etc.
```

**Rule of thumb:** use parallel streams because measurement shows they help a real workload—not simply because the collection is large or the machine has multiple CPU cores.
# Java 8 Stream API — Comprehensive Study Notes

These notes are based on the provided source material, **“Top 30 Java 8 Stream API Interview Questions & Answers”** from *The Curious Coder*. fileciteturn0file0L2-L5

## 1. Fundamentals of the Stream API

### What is a Stream?

A **Stream** is a sequence of elements that supports functional-style operations for processing data concisely and declaratively.

Package:

```java
java.util.stream
```

Core interface:

```java
java.util.stream.Stream
```

### Key Characteristics

- **Not a data structure:** A stream does not store data in memory. It carries values from a source such as a collection, array, or I/O channel through a pipeline of computational steps.
- **Not part of the Collection hierarchy:** `Stream` does not extend `Collection`.
- **Single-use:** After a terminal operation is executed or the stream is closed, the stream cannot be reused. Reusing a consumed stream results in `IllegalStateException`.
- **Sequential or parallel:** Streams can execute sequentially or in parallel.

### `Stream` Interface vs `stream()` Method

| Concept | Meaning |
|---|---|
| `Stream` | The core interface in `java.util.stream` that provides functional data-processing operations. |
| `stream()` | A Java 8 default method added to `Collection` that converts a collection such as a `List` or `Set` into a sequential stream. |

## 2. Collections vs Streams

| Feature | Collection | Stream |
|---|---|---|
| Primary purpose | Stores and organizes data in memory | Processes data through functional operations |
| Storage | Holds data physically in memory | Does not store data |
| Modification | Elements can be added or removed | Does not modify the underlying data source |
| Iteration | External iteration using loops such as `for` and `while` | Internal iteration handled by the Stream API |
| Reusability | Can be traversed and reused multiple times | Single-use; consumed after a terminal operation |

## 3. Core Stream Operations & Functional Interfaces

Stream methods commonly accept functional interfaces as arguments.

| Stream Method | Functional Interface | Main Purpose |
|---|---|---|
| `filter()` | `Predicate<T>` | Selects elements satisfying a boolean condition |
| `map()` | `Function<T, R>` | Transforms each element into another value or type |
| `forEach()` | `Consumer<T>` | Performs an action on each element |

Functional-interface signatures from the source:

```java
Predicate<T>  -> boolean test(T t)
Function<T,R> -> R apply(T t)
Consumer<T>   -> void accept(T t)
```

### Practical Pipeline

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

numbers.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .forEach(System.out::println);
```

Processing:

```text
[1, 2, 3, 4, 5, 6]
        ↓ filter(even)
[2, 4, 6]
        ↓ map(square)
[4, 16, 36]
        ↓ forEach
prints 4, 16, 36
```

## 4. Intermediate vs Terminal Operations & Lazy Evaluation

### Intermediate Operations

Intermediate operations transform one stream into another stream. Examples include:

- `filter()`
- `map()`
- `sorted()`
- `distinct()`
- `limit()`

They can be chained to form a processing pipeline.

### Lazy Evaluation

Intermediate operations are **lazy**. Calling them does not immediately process elements; they only construct the execution pipeline.

For example, a pipeline containing only `filter()` and `map()` and no terminal operation performs no actual stream processing.

### Terminal Operations

Terminal operations trigger execution and produce a result or side effect. Examples include:

- `collect()`
- `forEach()`
- `reduce()`
- `count()`
- `findFirst()`

Once a terminal operation executes, the stream is consumed and can no longer be reused.

## 5. Accumulation, Reduction & Grouping with Collectors

### `collect()` vs `Collector` vs `Collectors`

| Term | Meaning |
|---|---|
| `collect()` | Terminal method of `Stream` that triggers reduction into a mutable container |
| `Collector` | Interface that defines the reduction/accumulation strategy |
| `Collectors` | Utility class providing factory methods for built-in collectors |

Common collector methods:

```java
Collectors.toList()
Collectors.toSet()
Collectors.toMap()
Collectors.joining()
```

### `Collectors.toList()`

Collects stream elements into a `List` while preserving insertion order.

### `Collectors.toSet()`

Collects elements into a `Set`, eliminating duplicate elements.

### `Collectors.toMap()`

Collects elements into key-value pairs.

If duplicate keys occur, `toMap()` throws `IllegalStateException` unless a merge function is supplied.

### `partitioningBy()` vs `groupingBy()`

#### `partitioningBy(Predicate)`

Splits elements into **exactly two groups**, represented by Boolean keys: `true` and `false`.

```java
Map<Boolean, List<Integer>> partitioned = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));
```

Example result:

```text
{true=[2, 4], false=[1, 3, 5]}
```

#### `groupingBy(Function)`

Groups elements into multiple categories based on a classification function.

```java
Map<Character, List<String>> grouped = names.stream()
    .collect(Collectors.groupingBy(name -> name.charAt(0)));
```

Example result:

```text
A = [Alice]
B = [Bob]
C = [Charlie]
```

## 6. Searching, Matching & Short-Circuiting

### Matching Operations

These operations return `boolean`:

- `anyMatch(Predicate)` — `true` if at least one element matches.
- `allMatch(Predicate)` — `true` if every element matches.
- `noneMatch(Predicate)` — `true` if no element matches.

### Finding Operations

- `findFirst()` returns an `Optional<T>` containing the first element according to encounter order.
- `findAny()` returns an `Optional<T>` containing any element. In parallel streams, it can provide better performance when encounter order is not important.

### Short-Circuiting

An operation is **short-circuiting** when stream processing can stop before evaluating all remaining elements because the final outcome is already known.

Examples:

```text
findFirst()
findAny()
anyMatch()
limit()
```

## 7. Sorting Operations

### `Stream.sorted()`

`sorted()` is an intermediate operation that produces a new stream ordered according to natural ordering. The elements must support `Comparable`.

### `Stream.sorted(Comparator)`

Accepts a custom `Comparator` for explicit ordering logic.

### `Stream.sorted()` vs `Collections.sort()`

| Feature | `Stream.sorted()` | `Collections.sort()` |
|---|---|---|
| Type | Intermediate Stream operation | Static utility method on lists |
| Original collection | Unmodified; returns a new stream | Mutates the original list in place |
| Execution | Lazy | Eager |

## 8. Primitive Streams

Java 8 provides specialized primitive stream interfaces to reduce performance overhead associated with boxing and unboxing between primitives and wrapper types.

Available primitive streams:

```java
IntStream
LongStream
DoubleStream
```

These provide direct primitive-oriented operations such as:

```java
.sum()
.average()
.min()
.max()
```

For integer data, `mapToInt()` can convert an object stream into an `IntStream`.

## 9. Parallel Streams & Concurrency

Parallel streams use the shared:

```java
ForkJoinPool.commonPool()
```

infrastructure under the hood.

### Encounter Order

Because parallel processing uses multiple threads, operations such as `forEach()` do not guarantee preservation of the original encounter order.

### `forEachOrdered()`

`forEachOrdered()` forces parallel stream iteration to respect the original encounter order, with a possible performance cost compared with unconstrained parallel processing.

## 10. Transformations & Reduction

### `map()` vs `flatMap()`

#### `map()` — 1-to-1 transformation

Each element is transformed into one output element.

```text
Stream<T> → Stream<R>
```

It preserves the stream structure.

#### `flatMap()` — flattening / 1-to-many transformation

Each element is transformed into a stream, and the resulting streams are flattened into one combined stream.

```text
Stream<List<T>> → Stream<T>
```

This is useful for nested collections such as `List<List<T>>`.

### `reduce()` vs `collect()`

| Operation | Purpose |
|---|---|
| `reduce()` | Immutably combines stream elements into one scalar result, such as sum, product, or maximum |
| `collect()` | Accumulates stream elements into a mutable container such as `List`, `Set`, or `Map` |

## 11. Ways to Sum Integers Using Streams

The source presents three primary techniques.

### 1. `reduce()`

```java
int sum = list.stream()
    .reduce(0, Integer::sum);
```

### 2. `Collectors.summingInt()`

```java
int sum = list.stream()
    .collect(Collectors.summingInt(Integer::intValue));
```

### 3. Primitive `IntStream` with `mapToInt()`

```java
int sum = list.stream()
    .mapToInt(Integer::intValue)
    .sum();
```

## Interview-Focused Quick Revision

### Stream fundamentals

```text
Stream = processing pipeline, not storage
Stream != Collection
Streams are single-use
Streams support sequential and parallel execution
```

### Pipeline model

```text
Source
  ↓
Intermediate operations
  ↓
Intermediate operations
  ↓
Terminal operation
```

### Common operation categories

```text
Filtering      → filter()
Transformation → map(), flatMap()
Side effects   → forEach()
Sorting        → sorted()
Deduplication  → distinct()
Limiting       → limit()
Reduction      → reduce()
Collection     → collect()
Matching       → anyMatch(), allMatch(), noneMatch()
Finding        → findFirst(), findAny()
```

### High-value distinctions

```text
map()       → one input → one output
flatMap()   → nested outputs → flattened output

reduce()    → single scalar result
collect()   → mutable result container

partitioningBy() → exactly two Boolean groups
groupingBy()     → multiple classification groups

findFirst() → respects encounter order
findAny()   → any matching/available element; useful for parallel streams

forEach()         → order not guaranteed in parallel streams
forEachOrdered()  → preserves encounter order
```

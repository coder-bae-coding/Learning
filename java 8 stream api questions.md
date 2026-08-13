# Java 8 Stream API — Detailed Interview & Practical Notes

> **Source:** Detailed notes from the supplied material, **“Java 8 Stream API Interview Questions | 20 Coding Problems Solved Live”** by The Curious Coder. The source covers 20 coding problems and a final Stream API cheat sheet across 8 pages. fileciteturn5file0L2-L10

---

# 1. Remove Duplicates & Sort in Descending Order

## Problem

Given an unsorted `List<Integer>` containing duplicate values, remove all duplicates and sort the remaining unique numbers in descending order.

## Input

```java
List<Integer> list = Arrays.asList(10, 5, 20, 5, 30, 10);
```

## Solution

```java
List<Integer> result = list.stream()
    .distinct() // Removes duplicate values
    .sorted(Comparator.reverseOrder()) // Sorts in descending order
    .collect(Collectors.toList());
```

## How it works

### `stream()`

Creates a sequential stream from the list.

### `distinct()`

Removes duplicate elements from the stream.

For the input:

```text
10, 5, 20, 5, 30, 10
```

After `distinct()`:

```text
10, 5, 20, 30
```

### `sorted(Comparator.reverseOrder())`

Sorts the numbers from largest to smallest.

Result after sorting:

```text
30, 20, 10, 5
```

### `collect(Collectors.toList())`

Collects the stream elements into a `List`.

## Key operations

- `distinct()` → removes duplicates
- `sorted()` → sorts elements
- `Comparator.reverseOrder()` → reverses natural ordering
- `collect(Collectors.toList())` → creates a list from the stream

The source explicitly identifies `distinct()`, `sorted()`, and `Comparator.reverseOrder()` as the key operations. fileciteturn5file0L7-L17

---

# 2. Filter Odd Numbers and Return Their Squares

## Problem

From a list of integers, filter out all even numbers and return a list containing the squares of only the odd numbers.

## Input

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
```

## Solution

```java
List<Integer> result = numbers.stream()
    .filter(n -> n % 2 != 0) // Keeps only odd numbers
    .map(n -> n * n) // Transforms each odd number to its square
    .collect(Collectors.toList());
```

## How it works

### `filter()`

`filter()` keeps only elements for which the predicate returns `true`.

```java
.filter(n -> n % 2 != 0)
```

The predicate is true for odd numbers, so:

```text
1, 3, 5
```

remain.

### `map()`

`map()` transforms every remaining element.

```java
.map(n -> n * n)
```

The values become:

```text
1, 9, 25
```

## Key operations

- `filter()` → selects elements
- `map()` → transforms elements
- `collect()` → gathers the resulting values

This is the source's filter → transform → collect pattern. fileciteturn5file0L18-L27

---

# 3. Get the 2nd and 3rd Elements from a List

## Problem

Given a list, skip the first element and extract only the next two elements.

## Input

```java
List<Integer> list = Arrays.asList(10, 20, 30, 40, 50);
```

## Solution

```java
List<Integer> result = list.stream()
    .skip(1) // Skips the first element
    .limit(2) // Limits stream to next 2 elements
    .collect(Collectors.toList());
```

## Output

```text
[20, 30]
```

## How it works

`skip(1)` removes the first stream element:

```text
10 | 20, 30, 40, 50
```

Then `limit(2)` takes the next two:

```text
20, 30
```

## Key operations

- `skip(n)` → skip the first `n` elements
- `limit(n)` → take at most the next `n` elements

The source explicitly uses `skip(1)` followed by `limit(2)`. fileciteturn5file0L28-L39

---

# 4. Find the Second Highest Number in a List

## Problem

Find the second-highest number in a list that may contain duplicate values.

## Important edge cases

Duplicates must be removed first. Otherwise, duplicate occurrences of the maximum value can incorrectly become the second item after sorting.

The source also emphasizes handling the `Optional` result safely when fewer than two distinct values exist. fileciteturn5file0L40-L54

## Input

```java
List<Integer> numbers = Arrays.asList(10, 45, 45, 30, 20);
```

## Solution

```java
Optional<Integer> secondHighest = numbers.stream()
    .distinct() // Ensures unique values
    .sorted(Comparator.reverseOrder()) // Sorts highest to lowest
    .skip(1) // Skips the highest element
    .findFirst(); // Picks the next element

if (secondHighest.isPresent()) {
    System.out.println(secondHighest.get());
}
```

## Step-by-step pipeline

### Step 1 — `distinct()`

```text
10, 45, 45, 30, 20
```

becomes:

```text
10, 45, 30, 20
```

### Step 2 — descending sort

```text
45, 30, 20, 10
```

### Step 3 — `skip(1)`

Removes the highest value:

```text
30, 20, 10
```

### Step 4 — `findFirst()`

Returns `30` wrapped in:

```java
Optional<Integer>
```

## Why `Optional` matters

`findFirst()` may not find an element. Calling `get()` blindly can produce `NoSuchElementException`.

The source therefore demonstrates checking `isPresent()` before calling `get()`. fileciteturn5file0L45-L54

---

# 5. Partition Numbers into Even and Odd

## Problem

Divide a list of integers into two groups: even numbers and odd numbers.

## Input

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);
```

## Solution

```java
Map<Boolean, List<Integer>> partitioned = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));
```

## Result interpretation

```java
partitioned.get(true);  // Even numbers
partitioned.get(false); // Odd numbers
```

For the supplied input:

```text
true  -> [2, 4, 6]
false -> [1, 3, 5]
```

## Why `partitioningBy()`?

`Collectors.partitioningBy()` splits stream elements into two groups according to a boolean predicate.

- `true` → elements satisfying the predicate
- `false` → elements not satisfying the predicate

The source explicitly describes this boolean-map partitioning behavior. fileciteturn5file0L56-L65

---

# 6. Find the Longest String in a List

## Problem

Find the string with the maximum character length from a list of strings.

## Example

```java
List<String> names = Arrays.asList("Java", "SpringBoot", "Microservices");
```

## Solution

```java
Optional<String> longestString = names.stream()
    .max(Comparator.comparing(String::length));

longestString.ifPresent(System.out::println);
```

## How it works

`String::length` is used as the comparison key.

Conceptually:

```text
Java          -> 4
SpringBoot    -> 10
Microservices -> 13
```

Therefore the maximum is `Microservices`.

`max()` returns an `Optional<String>` because a stream may be empty. fileciteturn5file0L66-L74

---

# 7. Find the First Employee with Salary Greater Than 50,000

## Problem

Given a list of custom `Employee` objects, find the first employee whose salary is greater than `50,000`.

## Solution

```java
List<Employee> employees = getEmployeeList();

Optional<Employee> result = employees.stream()
    .filter(emp -> emp.getSalary() > 50000)
    .findFirst();

result.ifPresent(emp -> System.out.println(emp.getName()));
```

## Important concepts

### `filter()`

Keeps employees whose salary satisfies:

```java
emp.getSalary() > 50000
```

### `findFirst()`

Returns the first matching employee and short-circuits once a match is found.

### `Optional<Employee>`

The result is optional because no employee may satisfy the condition.

The source identifies custom filtering and `findFirst()` as the key operations. fileciteturn5file0L75-L85

---

# 8. Find the Top 2 Highest-Paid Employees

## Problem

Retrieve the top two highest-earning employees.

## Solution

```java
List<Employee> top2Paid = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    // Descending by salary
    .limit(2)
    // Take top 2
    .collect(Collectors.toList());
```

## How it works

### `Comparator.comparingInt(Employee::getSalary)`

Builds a comparator from the integer salary value.

### `reversed()`

Turns the salary ordering into descending order.

### `limit(2)`

Keeps only the first two employees after sorting.

The source uses this exact `sorted(...reversed()).limit(2)` approach. fileciteturn5file0L86-L95

---

# 9. Sort Employees by Salary, then by Name

## Problem

Sort employees primarily by salary in ascending order. If two employees have the same salary, sort those employees by name alphabetically.

## Option A — Comparator Chaining

```java
List<Employee> sortedEmployees = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary)
        .thenComparing(Employee::getName))
    .collect(Collectors.toList());
```

## How comparator chaining works

The first comparison is salary:

```java
Employee::getSalary
```

When the salaries are equal, the secondary comparator is applied:

```java
.thenComparing(Employee::getName)
```

This gives a clean multi-level ordering.

## Option B — Custom Comparator Logic

The source also gives the explicit comparator form:

```java
List<Employee> customSorted = employees.stream()
    .sorted((e1, e2) -> {
        if (e1.getSalary() != e2.getSalary()) {
            return Integer.compare(e1.getSalary(), e2.getSalary());
        } else {
            return e1.getName().compareTo(e2.getName());
        }
    })
    .collect(Collectors.toList());
```

Both versions implement the same salary-first, name-second ordering. fileciteturn5file0L96-L114

---

# 10. Find Frequency of Each Element in a List

## Problem

Given a list containing duplicates, count how many times each element occurs.

## Input

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 3, 4, 1, 1);
```

## Solution

```java
Map<Integer, Long> frequencyMap = numbers.stream()
    .collect(Collectors.groupingBy(
        element -> element, // Key selector: element itself
        Collectors.counting() // Counts elements in each group
    ));
```

## How it works

`groupingBy()` groups values according to a classifier.

Here the classifier is the element itself:

```java
element -> element
```

Then the downstream collector:

```java
Collectors.counting()
```

counts the elements in every group.

Expected result type:

```java
Map<Integer, Long>
```

For the supplied input, the logical result is:

```text
1 -> 3
2 -> 1
3 -> 2
4 -> 1
```

The source specifically combines `groupingBy()` and `counting()` for this problem. fileciteturn5file0L115-L126

---

# 11. Count Employees Present in Each Department

## Problem

Given a list of employees, find the total number of employees belonging to each department.

## Solution

```java
Map<String, Long> employeeCountPerDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment, // Group by department name
        Collectors.counting() // Count employees in each group
    ));
```

## Key idea

This uses the same grouping-and-counting pattern as a frequency map, but the grouping key is the department:

```java
Employee::getDepartment
```

Expected result type:

```java
Map<String, Long>
```

Each department name maps to its employee count. fileciteturn5file0L127-L134

---

# 12. Find Total Transaction Amount per Category

## Problem

Given `Transaction` objects containing a category and amount, calculate the total transaction amount for each category.

## Solution

```java
Map<String, Integer> totalPerCategory = transactions.stream()
    .collect(Collectors.groupingBy(
        Transaction::getCategory,
        Collectors.summingInt(Transaction::getAmount)
    ));
```

## How it works

First, transactions are classified by:

```java
Transaction::getCategory
```

Then the transaction amounts within each category are added using:

```java
Collectors.summingInt(Transaction::getAmount)
```

The source also calls out `Collectors.summingDouble()` for double-valued amounts. fileciteturn5file0L135-L143

---

# 13. Find Average Salary of Employees in Each Department

## Problem

Compute the average salary for employees in each department.

## Solution

```java
Map<String, Double> avgSalaryPerDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingInt(Employee::getSalary)
    ));
```

## Important point

`Collectors.averagingInt()` returns a `Double` average.

Therefore:

```java
Map<String, Double>
```

is the expected result shape.

The source explicitly highlights the `Double` return type for `averagingInt()`. fileciteturn5file0L145-L153

---

# 14. Find the Highest-Paid Employee in Each Department

## Problem

Identify the employee who earns the maximum salary within each department.

## Solution

```java
Map<String, Optional<Employee>> highestPaidPerDept =
    employees.stream()
        .collect(Collectors.groupingBy(
            Employee::getDepartment,
            Collectors.maxBy(Comparator.comparingInt(Employee::getSalary))
        ));
```

## Step-by-step

First, employees are grouped by:

```java
Employee::getDepartment
```

Then each group is reduced with:

```java
Collectors.maxBy(
    Comparator.comparingInt(Employee::getSalary)
)
```

The result shape is:

```java
Map<String, Optional<Employee>>
```

The source identifies `groupingBy()`, `maxBy()`, and `Comparator.comparingInt()` as the key pieces. fileciteturn5file0L154-L163

---

# 15. Convert Employee Names into a Comma-Separated String

## Problem

Extract all employee names and format them into a single comma-separated string.

## Solution

```java
String namesCsv = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.joining(", "));
```

## How it works

`map(Employee::getName)` changes the stream from `Employee` objects to names.

Then:

```java
Collectors.joining(", ")
```

combines the names into one string using `, ` as the delimiter.

Example shape:

```text
Alice, Bob, Charlie
```

The source specifically identifies `Collectors.joining()` as the key collector. fileciteturn5file0L164-L171

---

# 16. Find Common Elements Between Two Lists

## Problem

Identify numbers present in both `list1` and `list2`.

## Input

```java
List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
List<Integer> list2 = Arrays.asList(3, 4, 5, 6);
```

## Naive Approach — O(N × M)

```java
List<Integer> commonElements = list2.stream()
    .filter(list1::contains) // list1.contains is O(N) lookup
    .collect(Collectors.toList());
```

The source describes this approach as `O(N × M)` because `List.contains()` is an O(N) lookup and it is performed while processing the other list. fileciteturn5file0L172-L181

## Optimized Approach — O(N + M)

Convert the first list to a set first:

```java
Set<Integer> set1 = new HashSet<>(list1); // O(1) average lookup
```

Then:

```java
List<Integer> optimizedCommon = list2.stream()
    .filter(set1::contains) // O(1) average lookup
    .collect(Collectors.toList());
```

The source describes the optimized approach as `O(N + M)` using average O(1) `HashSet` lookups. fileciteturn5file0L182-L186

## Interview lesson

The Stream pipeline may look almost identical, but choosing the right backing data structure changes the performance significantly.

---

# 17. Flatten a List of Lists & Remove Duplicates

## Problem

Given a nested `List<List<Integer>>`, flatten it into a single list containing only unique elements.

## Input

```java
List<List<Integer>> listOfLists = Arrays.asList(
    Arrays.asList(1, 2, 3),
    Arrays.asList(3, 4, 5),
    Arrays.asList(5, 6)
);
```

## Solution

```java
List<Integer> flatUniqueList = listOfLists.stream()
    .flatMap(List::stream) // Flattens inner lists into a single stream
    .distinct() // Removes duplicate entries
    .collect(Collectors.toList());
```

## Why `flatMap()`?

Before flattening, the elements are themselves lists:

```text
[List(1,2,3), List(3,4,5), List(5,6)]
```

`flatMap(List::stream)` turns each nested list into a stream and combines the nested streams into one stream:

```text
1, 2, 3, 3, 4, 5, 5, 6
```

Then:

```java
.distinct()
```

produces:

```text
1, 2, 3, 4, 5, 6
```

The source explicitly describes `flatMap()` as the key operation for flattening nested lists. fileciteturn5file0L187-L201

---

# 18. Process a Stream in Parallel — Unordered & Fast

## Problem

Print all employee names as quickly as possible when output ordering does not matter.

## Solution

```java
employees.parallelStream()
    .map(Employee::getName)
    .forEach(System.out::println);
```

## Key concept: `parallelStream()`

The source explains that `parallelStream()` splits execution across multiple threads using the `ForkJoinPool`.

Because execution is concurrent, the ordering of output from `forEach()` is non-deterministic. fileciteturn5file0L202-L210

## Important interview point

Do not expect:

```java
parallelStream().forEach(...)
```

to preserve the input encounter order. This example is specifically for situations where output order is not important.

---

# 19. Parallel Stream Pitfall — Non-Thread-Safe Collections

## Question

What is wrong with this code?

```java
List<Integer> list = new ArrayList<>();

IntStream.rangeClosed(1, 1000)
    .parallel()
    .forEach(i -> list.add(i)); // WRONG!
```

## Why it is wrong

`ArrayList` is not thread-safe. With a parallel stream, several worker threads can attempt to call `add()` on the same list concurrently.

The source identifies possible consequences including:

- race conditions
- lost updates
- `ArrayIndexOutOfBoundsException`

fileciteturn5file0L211-L220

## Safer approach from the source

Avoid the external mutable side effect and collect the stream result:

```java
List<Integer> safeList = IntStream.rangeClosed(1, 1000)
    .parallel()
    .boxed()
    .collect(Collectors.toList());
```

The source recommends the functional collection approach instead of mutating the shared `ArrayList`. fileciteturn5file0L221-L226

## Core lesson

When processing in parallel, prefer Stream operations and collectors that manage the aggregation rather than manually mutating shared state from `forEach()`.

---

# 20. Total Combined Salary Using Parallel Reduction

## Problem

Compute the total salary of all employees concurrently using parallel stream processing.

## Solution

```java
int totalCombinedSalary = employees.parallelStream()
    .map(Employee::getSalary)
    .reduce(0, (a, b) -> a + b);
```

## How it works

### `parallelStream()`

Enables parallel processing of the employee stream.

### `map(Employee::getSalary)`

Transforms each employee into their salary value.

### `reduce(0, (a, b) -> a + b)`

Combines the salary values into a single integer.

The `0` is the identity value and `(a, b) -> a + b` is the accumulation operation.

The source describes this as a pure, stateless reduction suitable for parallel stream processing when the identity and accumulator are used correctly. fileciteturn5file0L227-L236

---

# Detailed Interview Patterns

## Pattern 1 — Filter → Transform → Collect

```java
stream()
    .filter(...)
    .map(...)
    .collect(...);
```

Used directly in the odd-number-and-squares problem.

## Pattern 2 — Distinct → Sort → Skip/Limit

```java
stream()
    .distinct()
    .sorted(...)
    .skip(...)
    .limit(...);
```

Useful for ranked and positional problems.

## Pattern 3 — Group → Aggregate

```java
Collectors.groupingBy(
    keyExtractor,
    downstreamCollector
)
```

The source uses this pattern for:

```text
groupingBy() + counting()
groupingBy() + summingInt()
groupingBy() + averagingInt()
groupingBy() + maxBy()
```

## Pattern 4 — Flatten → Deduplicate

```java
flatMap(...)
    .distinct();
```

Used for nested list data.

## Pattern 5 — Parallel → Reduction

```java
parallelStream()
    .map(...)
    .reduce(...);
```

Used by the source for total employee salary.

---

# Rapid-Fire Interview Questions

1. Which Stream operation removes duplicate elements?
2. What is the difference between `filter()` and `map()`?
3. When would you use `flatMap()` instead of `map()`?
4. How do `skip()` and `limit()` work together to extract a range?
5. Why should `distinct()` be used before finding the second-highest value when duplicates are possible?
6. Why does `findFirst()` return an `Optional`?
7. What does `Collectors.partitioningBy()` return?
8. How is `groupingBy()` + `counting()` used for frequency problems?
9. How can `thenComparing()` implement secondary sorting?
10. Why can a `HashSet` make the common-elements solution faster than repeated `List.contains()` calls?
11. Why is `parallelStream()` output order non-deterministic with `forEach()`?
12. Why are side effects such as `list.add()` dangerous inside parallel stream operations?
13. Which collector is useful for concatenating strings with a delimiter?
14. Which collectors are used in the source for summation and averaging?
15. When is `reduce()` useful for combining stream values into a single result?

---

# Stream API Cheat Sheet

The source's final cheat sheet summarizes the most-used Stream API methods and collectors. fileciteturn5file0L237-L252

| Requirement | Stream Method / Collector |
|---|---|
| Filter data | `filter(Predicate)` |
| Transform data | `map(Function)` |
| Flatten nested structures | `flatMap(Function)` |
| Skip first N elements | `skip(n)` |
| Take top N elements | `limit(n)` |
| Unique elements | `distinct()` |
| Grouping & aggregations | `Collectors.groupingBy()` |
| Partition into True/False groups | `Collectors.partitioningBy()` |
| String concatenation | `Collectors.joining(delimiter)` |
| Reduction / summation | `reduce()`, `Collectors.summingInt()`, `Collectors.averagingInt()` |

---

# Quick Mental Models

### `filter()`

**Question answered:** “Which elements should stay?”

```java
.filter(x -> condition)
```

### `map()`

**Question answered:** “What should each element become?”

```java
.map(x -> transformedValue)
```

### `flatMap()`

**Question answered:** “How do I flatten nested structures?”

```java
.flatMap(collection -> collection.stream())
```

### `distinct()`

**Question answered:** “How do I remove duplicate values?”

```java
.distinct()
```

### `sorted()`

**Question answered:** “How should the elements be ordered?”

```java
.sorted(comparator)
```

### `skip()` + `limit()`

**Question answered:** “Which section of the stream do I want?”

```java
.skip(n)
.limit(n)
```

### `groupingBy()`

**Question answered:** “Which key should each element belong to?”

```java
.collect(Collectors.groupingBy(...))
```

### `partitioningBy()`

**Question answered:** “Which elements satisfy this boolean predicate?”

```java
.collect(Collectors.partitioningBy(...))
```

### `joining()`

**Question answered:** “How do I combine elements into one string?”

```java
.collect(Collectors.joining(", "))
```

### `reduce()`

**Question answered:** “How do I combine multiple values into one result?”

```java
.reduce(identity, accumulator)
```

---

# Source Coverage

This document preserves the full set of **20 coding problems** from the supplied material, including the original problem statements, complete code examples, key Stream methods, edge-case handling, grouping and collector patterns, the common-elements complexity comparison, parallel-stream caveats, and the final Stream API cheat sheet. The source covers the problems across pages 1–8. fileciteturn5file0L2-L10

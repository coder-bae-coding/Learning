# Java 8 Stream API — Interview & Coding Questions

These 20 problems are based on the supplied **Java 8 Stream API Complete Interview & Practical Guide**. They are organized as practice questions first, followed by concise solution approaches and the key Stream API operations involved.

## 1. Remove Duplicates & Sort in Descending Order

**Question:** Given an unsorted list of integers containing duplicates, remove duplicate values and sort the remaining unique numbers in descending order.

**Practice with:** `distinct()`, `sorted()`, `Comparator.reverseOrder()`

---

## 2. Filter Odd Numbers and Return Their Squares

**Question:** From a list of integers, filter out all even numbers and return a list containing the squares of only the odd numbers.

**Practice with:** `filter()`, `map()`

---

## 3. Get the 2nd and 3rd Elements from a List

**Question:** Given a list, skip the first element and extract only the next two elements into a new list.

**Practice with:** `skip()`, `limit()`

---

## 4. Find the Second Highest Number in a List

**Question:** Find the second-highest number in a list that may contain duplicate values.

**Important:** Remove duplicates first and handle the `Optional` result safely when fewer than two distinct values exist.

**Practice with:** `distinct()`, `sorted(Comparator.reverseOrder())`, `skip()`, `findFirst()`

---

## 5. Partition Numbers into Even and Odd

**Question:** Divide a list of integers into two groups: even numbers and odd numbers.

**Practice with:** `Collectors.partitioningBy()`

`true` → even numbers  
`false` → odd numbers

---

## 6. Find the Longest String in a List

**Question:** Find the string with the maximum number of characters from a list of strings.

**Practice with:** `max()`, `Comparator.comparing()`

---

## 7. Find the First Employee with Salary Greater Than 50,000

**Question:** Given a list of `Employee` objects, find the first employee whose salary is greater than `50_000`.

**Practice with:** `filter()`, `findFirst()`

---

## 8. Find the Top 2 Highest-Paid Employees

**Question:** Retrieve the two employees with the highest salaries.

**Practice with:** `sorted()`, `Comparator.comparingInt()`, `reversed()`, `limit()`

---

## 9. Sort Employees by Salary, then by Name

**Question:** Sort employees by salary in ascending order. When salaries are equal, sort by name alphabetically.

**Practice with:** `Comparator.comparingInt()`, `thenComparing()`

**Alternative:** Implement the same ordering using custom comparator logic.

---

## 10. Find Frequency of Each Element in a List

**Question:** Given a list of integers containing duplicates, count how many times each value occurs.

**Practice with:** `Collectors.groupingBy()`, `Collectors.counting()`

Expected result type:

```java
Map<Integer, Long>
```

---

## 11. Count Employees Present in Each Department

**Question:** Given a list of employees, find the total number of employees belonging to each department.

**Practice with:** `Collectors.groupingBy()`, `Collectors.counting()`

Expected result type:

```java
Map<String, Long>
```

---

## 12. Find Total Transaction Amount per Category

**Question:** Given transactions containing a category and amount, calculate the total transaction amount for each category.

**Practice with:** `Collectors.groupingBy()`, `Collectors.summingInt()` / `Collectors.summingDouble()`

---

## 13. Find Average Salary of Employees in Each Department

**Question:** Compute the average salary for employees working in each department.

**Practice with:** `Collectors.groupingBy()`, `Collectors.averagingInt()`

**Note:** `averagingInt()` returns `Double` values.

---

## 14. Find the Highest-Paid Employee in Each Department

**Question:** Identify the employee earning the maximum salary in every department.

**Practice with:** `Collectors.groupingBy()`, `Collectors.maxBy()`, `Comparator.comparingInt()`

Expected result shape:

```java
Map<String, Optional<Employee>>
```

---

## 15. Convert Employee Names into a Comma-Separated String

**Question:** Extract all employee names and combine them into one comma-separated string.

**Practice with:** `map()`, `Collectors.joining(", ")`

---

## 16. Find Common Elements Between Two Lists

**Question:** Identify the numbers present in both `list1` and `list2`.

### Naive approach

Use `list1.contains()` while streaming `list2`. The supplied notes identify this as **O(N × M)** because `List.contains()` is an O(N) lookup.

### Optimized approach

Convert the first list to a `HashSet` and stream the second list against it. The supplied notes describe this as **O(N + M)** with average O(1) set lookups.

**Practice with:** `filter()`, `contains()`

---

## 17. Flatten a List of Lists & Remove Duplicates

**Question:** Given `List<List<Integer>>`, flatten it into a single list containing only unique elements.

**Practice with:** `flatMap()`, `distinct()`

---

## 18. Process a Stream in Parallel (Unordered & Fast)

**Question:** Print all employee names as quickly as possible when output ordering does not matter.

**Practice with:** `parallelStream()`, `map()`, `forEach()`

**Important:** Parallel execution uses multiple threads and the order of `forEach()` output is non-deterministic.

---

## 19. Parallel Stream Pitfall: Non-Thread-Safe Collection

**Question:** What is wrong with this code?

```java
List<Integer> list = new ArrayList<>();

IntStream.rangeClosed(1, 1000)
    .parallel()
    .forEach(i -> list.add(i));
```

**Expected discussion:** `ArrayList` is not thread-safe. Concurrent writes can cause race conditions, lost updates, or `ArrayIndexOutOfBoundsException`.

**Safer approach:** Avoid side effects and collect the stream instead.

```java
List<Integer> safeList = IntStream.rangeClosed(1, 1000)
    .parallel()
    .boxed()
    .collect(Collectors.toList());
```

---

## 20. Total Combined Salary Using Parallel Reduction

**Question:** Compute the total salary of all employees using parallel stream processing.

**Practice with:** `parallelStream()`, `map()`, `reduce()`

Example pattern:

```java
int totalCombinedSalary = employees.parallelStream()
    .map(Employee::getSalary)
    .reduce(0, (a, b) -> a + b);
```

**Key idea:** The supplied notes describe `reduce()` here as a pure, stateless reduction suitable for parallel processing when the identity and accumulator are used correctly.

---

# Rapid-Fire Revision Questions

1. Which Stream operation removes duplicate elements?
2. Which operation transforms each element into another value?
3. When would you use `flatMap()` instead of `map()`?
4. How do `skip()` and `limit()` work together to extract a range?
5. Why should `distinct()` be used before finding the second-highest value when duplicates are possible?
6. Why does `findFirst()` return an `Optional`?
7. What does `Collectors.partitioningBy()` return?
8. How is `groupingBy()` + `counting()` used for frequency problems?
9. How can `thenComparing()` implement secondary sorting?
10. What is the difference between a naive list lookup and a `HashSet` lookup in the common-elements problem?
11. Why is `parallelStream()` output order non-deterministic with `forEach()`?
12. Why are side effects such as `list.add()` dangerous inside parallel stream operations?
13. Which collector is useful for concatenating strings with a delimiter?
14. Which collectors are useful for summation and averaging?
15. When is `reduce()` a better fit than mutating an external collection?

# Stream API Cheat Sheet

| Requirement | Stream Method / Collector |
|---|---|
| Filter data | `filter(Predicate)` |
| Transform data | `map(Function)` |
| Flatten nested structures | `flatMap(Function)` |
| Skip first N elements | `skip(n)` |
| Take top N elements | `limit(n)` |
| Unique elements | `distinct()` |
| Grouping & aggregations | `Collectors.groupingBy()` |
| Partition into true/false groups | `Collectors.partitioningBy()` |
| String concatenation | `Collectors.joining(delimiter)` |
| Reduction / summation | `reduce()`, `Collectors.summingInt()`, `Collectors.averagingInt()` |

# Spring Bean Scopes — Singleton vs Prototype

> Study notes extracted from the provided **The Curious Coder** material on Java Spring Boot Bean Scopes.

## 1. What is a Bean Scope?

A **bean scope** defines the lifecycle and visibility of a bean instance managed by the Spring IoC container.

It determines:
- when the bean is instantiated,
- how long the bean lives, and
- whether a new instance is created for each injection/request or a shared instance is reused.

## 2. Singleton Scope (Default)

### Definition

Only **one shared instance** of a bean is created per Spring IoC container.

### Creation time

By default, a singleton bean is **eagerly instantiated during application startup**.

### Behavior

Every class/component that injects the singleton bean receives a reference to the **same object in memory**. Therefore, the injected references have the same identity/hash code in the demonstration.

### Configuration

Singleton is the default when no `@Scope` is specified.

It can also be declared explicitly:

```java
@Scope("singleton")
```

or:

```java
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
```

## 3. Prototype Scope

### Definition

A **new bean instance** is created each time the bean is requested or injected from the container.

### Creation time

Prototype beans are **lazily instantiated on demand**, rather than eagerly created at application startup.

### Behavior

Different injection points/requests receive **independent instances**, which have different memory references and therefore different identity/hash codes in the demonstration.

### Configuration

```java
@Scope("prototype")
```

or:

```java
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
```

### Important caveat

Using prototype scope for many short-lived objects can significantly increase memory usage because new instances are created repeatedly.

## 4. Demonstration from the Material

The example uses a `Student` component and injects it three times into `StudentController`.

### Student bean

```java
package com.example.demo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
// Default: @Scope("singleton")
// Change to @Scope("prototype") to test prototype behavior.
public class Student {

    public Student() {
        System.out.println("Student bean created! HashCode: "
                + System.identityHashCode(this));
    }

    public String getHashCode() {
        return "Student instance: " + System.identityHashCode(this);
    }
}
```

### Controller with multiple injections

```java
package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private Student student1;

    @Autowired
    private Student student2;

    @Autowired
    private Student student3;

    @GetMapping("/student1")
    public String getStudent1() {
        return student1.getHashCode();
    }

    @GetMapping("/student2")
    public String getStudent2() {
        return student2.getHashCode();
    }

    @GetMapping("/student3")
    public String getStudent3() {
        return student3.getHashCode();
    }
}
```

## 5. Behavior Comparison

| Property | Singleton | Prototype |
|---|---|---|
| Instances | One shared instance per Spring IoC container | New instance each time requested/injected from the container |
| Default? | Yes | No |
| Creation | Eager by default at startup | Lazy/on demand |
| Injection behavior | All three injection points share the same instance | Injection points receive distinct instances |
| Hash code in example | Same for all three endpoints | Different for each endpoint |
| Memory consideration | Reuses one instance | Repeated creation can increase memory usage |

## 6. Interview-Level Mental Model

Think of the two scopes as:

**Singleton:**

```text
Spring Container
      |
      v
  Student #1
   /  |  \\
  /   |   \\
v    v    v
C1   C2   C3
(same Student object)
```

**Prototype:**

```text
Spring Container
   |     |     |
   v     v     v
Student Student Student
  #1      #2      #3
(each is a different object)
```

## 7. What to Remember

1. **Singleton is Spring's default bean scope.**
2. A singleton means **one shared instance per Spring IoC container**.
3. **Prototype creates a new instance when the container provides the bean.**
4. Singleton beans are **eagerly created by default**, while prototype beans are **created on demand**.
5. In the example, the easiest way to observe the difference is to compare `System.identityHashCode(this)` across the injected objects.
6. Prototype scope can cause more object creation and consequently higher memory usage when used extensively for short-lived beans.

## Source

Knowledge in this file is extracted from the provided PDF/video material titled **“Understanding Bean Scopes: Singleton vs Prototype”** by **The Curious Coder**. The PDF explicitly defines bean scope and the singleton/prototype behaviors used above. fileciteturn0file0L5-L30

The code example and controller demonstration are based on the material's `Student` and `StudentController` example. fileciteturn0file0L31-L76

The stated comparison of constructor-call frequency and endpoint hash codes comes from the source material's behavior comparison. fileciteturn0file0L79-L89

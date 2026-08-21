# IoC and Dependency Injection in Spring Boot

## Overview

This example demonstrates the core concepts of **Inversion of Control (IoC)** and **Dependency Injection (DI)** using three Spring-managed classes:

- `Student` — a Spring component
- `StudentService` — a service that depends on `Student`
- `StudentController` — the REST entry point that depends on `StudentService`

Spring creates and manages these objects inside the **IoC Container**, while `@Autowired` tells Spring to inject the required dependency.

---

## 1. Student Class — The Component

```java
import org.springframework.stereotype.Component;

@Component
public class Student {

    public Student() {
        System.out.println("Student object created at " + this.hashCode());
    }
}
```

### What happens here?

`@Component` tells Spring that `Student` is a Spring-managed bean.

When the application starts, Spring detects the class during component scanning and creates a `Student` object. That object is stored and managed by the **IoC Container**.

The constructor prints the object's `hashCode()` so that the same Spring-managed instance can be observed later.

---

## 2. StudentService Class — The Dependency Consumer

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    @Autowired
    private Student student;

    public Student getStudent() {
        return student;
    }
}
```

### What happens here?

`@Service` tells Spring that `StudentService` is also a Spring-managed bean.

The `StudentService` class needs a `Student` object. Instead of creating it manually using:

```java
Student student = new Student();
```

Spring injects the already-managed `Student` bean into the `student` field because of `@Autowired`.

This is **Dependency Injection**: the class receives the object it depends on instead of creating that object itself.

---

## 3. StudentController Class — The Entry Point

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/student")
    public int getStudentAddress() {
        Student s1 = studentService.getStudent();
        return s1.hashCode();
    }
}
```

### What happens here?

`@RestController` makes `StudentController` a Spring-managed REST controller.

Spring injects the `StudentService` bean into the controller because of `@Autowired`.

When a request is made to:

```text
GET /student
```

the controller obtains the `Student` object from `StudentService` and returns its `hashCode()`.

---

## How IoC and Dependency Injection Work Together

The dependency flow is:

```text
Spring IoC Container
        │
        ├── creates Student
        │
        ├── creates StudentService
        │       └── injects Student
        │
        └── creates StudentController
                └── injects StudentService
```

The request flow is:

```text
Client
  │
  │ GET /student
  ▼
StudentController
  │
  │ uses
  ▼
StudentService
  │
  │ returns
  ▼
Student
  │
  ▼
hashCode()
```

---

## IoC — Inversion of Control

Normally, a class controls the creation of its dependencies:

```java
Student student = new Student();
```

With Spring IoC, the control is inverted. **Spring creates, configures, and manages the objects instead of the application classes doing it themselves.**

That is why Spring is called an **IoC Container**.

---

## Dependency Injection

Dependency Injection is the mechanism through which Spring supplies one bean to another bean.

In this example:

```java
@Autowired
private Student student;
```

Spring injects the `Student` bean into `StudentService`.

Similarly:

```java
@Autowired
private StudentService studentService;
```

Spring injects the `StudentService` bean into `StudentController`.

---

## Key Annotations

| Annotation | Purpose |
|---|---|
| `@Component` | Registers a general-purpose class as a Spring bean |
| `@Service` | Registers a service/business-logic class as a Spring bean |
| `@RestController` | Registers a REST controller as a Spring bean and handles HTTP requests |
| `@Autowired` | Tells Spring to inject a matching dependency |
| `@GetMapping` | Maps an HTTP GET request to a controller method |

---

## Important Concept

The most important idea to remember is:

> **IoC = Spring controls object creation and lifecycle.**
>
> **DI = Spring supplies the dependencies required by an object.**

In this example, neither `StudentService` nor `StudentController` needs to manually create its dependency with `new`. Spring creates and wires the objects for them.

---

## Expected Behavior

When the application starts, Spring creates the `Student` bean and the constructor prints something similar to:

```text
Student object created at 123456789
```

When `/student` is requested, the controller returns the same bean's `hashCode()` (assuming the bean remains the same singleton instance, which is Spring's default bean scope).

This demonstrates that the `Student` object is being managed by Spring rather than being manually created each time it is needed.

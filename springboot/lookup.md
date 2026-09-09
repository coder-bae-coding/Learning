# Spring `@Lookup` Annotation

Here is a detailed summary and the complete Java code implementation from **Spring `@Lookup` Annotation | How to Inject Prototype Bean into Singleton Bean Correctly** by *The Curious Coder*.

## 1. High-Level Concept & Problem Statement

This video tackles a classic Spring Boot interview puzzle: Injecting a Prototype-scoped Bean into a Singleton-scoped Bean.

### The Problem

- **Singleton scope (Default):** Created once by the Spring IoC container when the application starts up.
- **Prototype scope (`@Scope("prototype")`):** A new instance is created every time the bean is requested from the container.
- **The Conflict:** If you inject a Prototype bean into a Singleton bean via standard `@Autowired`:
  - The Singleton bean is only initialized once during startup.
  - Therefore, dependency injection happens only once.
  - The Prototype bean injected into the Singleton becomes stale/frozen, and every request to the Singleton reuses the exact same Prototype instance, completely defeating the purpose of prototype scoping.

### The Solution: Method Injection using `@Lookup`

Spring provides the `@Lookup` annotation for Method Injection. Under the hood, Spring uses CGLIB bytecode generation to dynamically override the annotated method at runtime and perform a fresh lookup in the ApplicationContext (`getBean(Class)`) every time that method is called.

---

## 2. The Prototype Bean (`Student.java`)

To verify whether a new object instance is being created, a constructor log prints the memory address/hash of the newly constructed bean:

```java
package com.example.demo;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Student {

    public Student() {
        System.out.println("Student bean created at: " + this);
    }

    public int getHashCode() {
        return this.hashCode();
    }
}
```

---

## 3. The Broken Approach: Standard `@Autowired` in Singleton

When you simply autowire the prototype bean into a singleton controller:

```java
package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Singleton by default
public class StudentControllerBroken {

    @Autowired
    private Student student; // Injected only once at application startup!

    @GetMapping("/hashcode-broken")
    public int getStudentHashCode() {
        // Every API call returns the EXACT same hashcode!
        return student.getHashCode();
    }
}
```

---

## 4. The Correct Approaches Using `@Lookup`

### Approach A: Stub Method Returning `null`

Spring’s CGLIB proxy intercepts calls to `getStudent()` and fetches a fresh prototype bean directly from the container, completely ignoring the `return null;` body:

```java
package com.example.demo;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StudentController {

    public StudentController() {
        System.out.println("StudentController (Singleton) bean created at: " + this);
    }

    // @Lookup tells Spring to dynamically generate the implementation
    @Lookup
    public Student getStudent() {
        // Body is bypassed and overridden by CGLIB proxy at runtime
        return null;
    }

    @GetMapping("/student/hashcode")
    public int getStudentHashCode() {
        // Calls getStudent() -> Spring intercepts and fetches a brand new Student instance
        Student student = getStudent();
        return student.getHashCode();
    }
}
```

### Approach B: Clean Abstract Class & Method

To eliminate dummy `return null;` boilerplate, you can declare the method and class as abstract. Spring will generate the subclass and provide the concrete implementation:

```java
package com.example.demo;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class StudentControllerAbstract {

    public StudentControllerAbstract() {
        System.out.println("StudentControllerAbstract bean created at: " + this);
    }

    // Abstract method: Spring creates the implementation dynamically via CGLIB
    @Lookup
    public abstract Student getStudent();

    @GetMapping("/student-abstract/hashcode")
    public int getStudentHashCode() {
        Student student = getStudent();
        return student.getHashCode();
    }
}
```

---

## 5. Summary Table for OneNote

| Strategy | Singleton Injected with Prototype | Behavior on API Call | Verdict |
| --- | --- | --- | --- |
| **Standard `@Autowired`** | Bound once during application startup | Same prototype instance reused indefinitely | ❌ Fails (Prototype acts like a Singleton) |
| **`ApplicationContext.getBean()`** | Injects container and queries it manually | Returns a new instance every time | ⚠️ Works, but tightly couples code to Spring API |
| **`ObjectProvider<Student>`** | Injects `ObjectProvider.getObject()` | Returns a new instance every time | ✅ Modern alternative |
| **`@Lookup` (Video Solution)** | CGLIB dynamically generates method body | Returns a new instance on every invocation | ✅ Clean, decoupled, and standard Spring solution |

---

YouTube video views will be stored in your YouTube History, and your data will be stored and used by YouTube according to its Terms of Service.

# Spring Bean Scopes — Request vs Session

> Study notes extracted from the provided material **“Understanding Bean Scopes: Request vs Session : Java Spring Boot Interview Question 11”** by The Curious Coder.

## 1. Web-Aware Bean Scopes

`request` and `session` are **web-aware bean scopes**. Unlike singleton and prototype, they are designed for a web-enabled Spring `ApplicationContext`, such as Spring MVC / REST applications.

---

## 2. Request Scope

### Definition

A request-scoped bean has **one instance per HTTP request**.

```java
@RequestScope
```

or the longer form:

```java
@Scope("request")
```

### Lifecycle

1. An HTTP request arrives.
2. Spring creates the request-scoped bean instance.
3. If multiple controllers/services use that bean during the same request, they share the **same instance**.
4. When the HTTP response finishes, that bean instance is destroyed.

### Mental model

```text
HTTP Request #1
      |
      +---- Controller ----+
      |                    |
      |                 Student #1
      |                    |
      +---- Service -------+
           (same instance)

HTTP Request #2
      |
   Student #2
   (different from #1)
```

The important rule is:

> **Same request = same request-scoped bean instance.**

---

## 3. Session Scope

### Definition

A session-scoped bean has **one instance per HTTP session**.

```java
@SessionScope
```

or:

```java
@Scope("session")
```

### Lifecycle

1. A client/browser establishes an HTTP session, commonly associated with a session cookie such as `JSESSIONID`.
2. Spring creates the session-scoped bean for that session.
3. Multiple HTTP requests from the same session reuse the **same instance**.
4. When the session expires or is invalidated, the bean instance is destroyed.

### Mental model

```text
Session A
   |
   +--> Request 1 ----+
   |                   |
   +--> Request 2 --> Student #1
   |                   |
   +--> Request 3 ----+
      (same instance)

Session B
   |
   +--> Request 1 --> Student #2
                    (different session)
```

The important rule is:

> **Same session across requests = same session-scoped bean instance.**

---

## 4. Prototype vs Request Scope — Common Interview Question

Both scopes can result in multiple objects, but they have **different creation and sharing rules**.

| Feature | Prototype Scope | Request Scope |
|---|---|---|
| Creation trigger | Every time the bean is referenced/requested/injected from the container | Once per incoming HTTP request |
| Sharing inside one request | Different injection points can receive different instances | Injection points share the same instance within that request |
| Lifecycle | Spring instantiates it; cleanup is left to Java GC | Managed as part of the web request lifecycle and destroyed after the response |
| Web-aware? | No | Yes |

### The key distinction

Suppose `Controller A` and `Service B` both depend on `Student`:

```text
Prototype:
Controller A ---> Student #1
Service B    ---> Student #2
```

But with request scope:

```text
One HTTP Request:
Controller A ---> Student #1 <--- Service B
```

So request scope is **not simply “prototype for web requests.”** It also provides **sharing within the same request**.

---

## 5. The Scoped Proxy Problem

A very important interview concept is what happens when a **short-lived bean** is injected into a **long-lived singleton bean**.

For example:

```text
Singleton Controller
        |
        v
Request-scoped Student
```

The default singleton controller is created during application startup. At startup, there is normally **no active HTTP request or session**.

Without special handling, Spring can fail with an error such as:

```text
Scope 'request' is not active for the current thread
```

or a related `IllegalStateException` / `BeanCreationException`.

### Why?

The singleton exists before the request-scoped object has a valid request context.

---

## 6. Scoped Proxy — The Solution

Spring can inject a **scoped proxy** instead of trying to inject the actual request/session bean immediately.

Full configuration:

```java
@Scope(
    value = WebApplicationContext.SCOPE_REQUEST,
    proxyMode = ScopedProxyMode.TARGET_CLASS
)
```

For request scope, the shorthand is:

```java
@RequestScope
```

For session scope:

```java
@SessionScope
```

### What does the proxy do?

The injected dependency is a lightweight **CGLIB proxy** acting as a stand-in for the real object.

At runtime, when a method is called on the proxy, Spring dynamically locates the actual bean instance associated with the **current request/session** and delegates the call to it.

Conceptually:

```text
Singleton Controller
        |
        v
   Scoped Proxy
        |
        v
Actual bean for current request/session
```

This allows a singleton bean to safely depend on a shorter-lived web-scoped bean.

---

## 7. Example from the Material

### Student bean

```java
package com.example.demo.model;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.annotation.SessionScope;

@Component
@Scope(
    value = WebApplicationContext.SCOPE_REQUEST,
    proxyMode = ScopedProxyMode.TARGET_CLASS
)
// Alternatively use @RequestScope or @SessionScope.
public class Student {

    public Student() {
        System.out.println("Student Bean Created! HashCode: " + this.hashCode());
    }

    public int getBeanHashCode() {
        return this.hashCode();
    }
}
```

The example uses the bean's hash code to make instance identity visible while testing the scope behavior.

---

## 8. Service Layer

```java
@Service
public class StudentService {

    @Autowired
    private Student student;

    public int getStudentHashCode() {
        return student.getBeanHashCode();
    }
}
```

The service also accesses the same `Student` dependency.

---

## 9. Controller Layer

```java
@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private Student student;

    @Autowired
    private StudentService studentService;

    @GetMapping("/check-scope")
    public Map<String, Integer> checkScope() {
        Map<String, Integer> response = new HashMap<>();

        response.put(
            "controllerStudentHashCode",
            student.getBeanHashCode()
        );

        response.put(
            "serviceStudentHashCode",
            studentService.getStudentHashCode()
        );

        return response;
    }
}
```

This endpoint exposes the hash code obtained through the controller and through the service so that the sharing behavior can be observed directly.

---

## 10. Observed Runtime Behavior

### With `@RequestScope`

For one request to:

```text
GET /api/students/check-scope
```

the two values are identical:

```text
controllerStudentHashCode = 12345
serviceStudentHashCode    = 12345
```

This demonstrates that the controller and service share the **same bean instance during that request**.

On a second HTTP request, the hash code changes:

```text
Request #1 -> 12345
Request #2 -> 67890
```

But within each request, controller and service still see the same value.

### With `@SessionScope`

Requests from the **same browser/client session** continue to produce the same hash code.

```text
Session A
Request 1 -> 12345
Request 2 -> 12345
Request 3 -> 12345
```

Starting a new session, such as from a different browser/private session, produces a different bean instance/hash code.

---

## 11. Request vs Session vs Prototype — Quick Revision

| Scope | Instance boundary | Shared where? | Destroyed when? |
|---|---|---|---|
| Prototype | Bean request/injection | Not shared between separate prototype creations | Source material states Java GC handles cleanup |
| Request | HTTP request | Shared across injections during one HTTP request | After the HTTP response/request lifecycle ends |
| Session | HTTP session | Shared across requests belonging to the same session | Session expiry/invalidation |

### Easy memory trick

```text
Prototype -> new object per container request
Request   -> one object per HTTP request
Session   -> one object per HTTP session
```

---

## 12. Interview Questions You Should Be Able to Answer

### Q1. What is request scope?

A bean scope where Spring creates one bean instance for each HTTP request and shares it across components participating in that request.

### Q2. What is session scope?

A bean scope where Spring creates one bean instance per HTTP session and reuses it across requests from that session.

### Q3. What is the difference between prototype and request scope?

Prototype creates a new instance whenever the bean is requested/injected from the container, while request scope creates one instance per HTTP request and shares that instance throughout the request.

### Q4. Why is `proxyMode` needed?

A singleton may be initialized when there is no active request/session. A scoped proxy acts as a stand-in and resolves the real request/session-scoped object when the dependency is actually used within an active web context.

### Q5. What happens after a request ends?

The request-scoped bean instance is destroyed.

### Q6. What happens when a session ends?

The session-scoped bean instance is destroyed when the session expires or is invalidated.

---

## 13. One-Page Mental Model

```text
                    SPRING WEB APP
                          |
            +-------------+-------------+
            |             |             |
        Prototype       Request       Session
            |             |             |
       New object      One object     One object
       per container  per HTTP req.  per HTTP session
       request        |             |
                      +--> shared   +--> reused across
                           across        requests in
                           controller    same session
                           + service
```

The most important interview distinction is:

> **Prototype is about container-level object creation. Request and Session are about web lifecycle boundaries.**

---

## Source

This note is based on the provided PDF/video material **“Understanding Bean Scopes: Request vs Session : Java Spring Boot Interview Question 11”** by The Curious Coder.

The source defines request and session as web-aware scopes, explains their lifecycle and sharing behavior, and contrasts request scope with prototype scope. fileciteturn7file0L5-L41

The scoped-proxy explanation and `proxyMode` configuration come from the source material's discussion of injecting short-lived web-scoped beans into singleton beans. fileciteturn7file0L42-L60

The `Student`, service, and controller examples are based directly on the provided code examples. fileciteturn7file0L61-L84 fileciteturn7file0L88-L128

The runtime observations for request and session scopes are based on the material's demonstrated behavior. fileciteturn7file0L131-L143

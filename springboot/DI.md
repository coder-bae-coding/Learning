Here is a detailed summary and the complete Java code implementation from Mastering
Dependency Injection in Spring Boot | Constructor, Setter, Field by The Curious Coder.

## 1. High-Level Summary & Core Concepts
The video explains Dependency Injection (DI) in Spring Boot—where the Spring IoC (Inversion
of Control) container manages object creation and lifecycle rather than the developer manually
doing new ClassName().
It compares the three main ways to perform DI:
1. Field Injection: Injected directly into the class variable using @Autowired.
2. Setter Injection: Injected via a public setter method annotated with @Autowired.
3. Constructor Injection: Injected through the class constructor (using final fields).
Key Trade-offs Discussed:
● Immutability: Constructor injection allows fields to be marked final, ensuring thread
safety and preventing reassignment after creation. Field and Setter injection leave fields
mutable.
● Optional Dependencies: Both Field and Setter injection easily support
@Autowired(required = false) for non-mandatory beans.
● Testing & Best Practices: Constructor injection is the Spring-recommended approach
because dependencies cannot be left uninitialized, and objects can be instantiated easily
in unit tests without reflection or Spring test runners.
2. Base Model Bean (Employee.java)
package com.example.demo.model;
import org.springframework.stereotype.Component;
@Component
public class Employee {
private Integer id;
private String name;
public Employee() {}
public Employee(Integer id, String name) {
this.id = id;
this.name = name;
}
public Integer getId() {
return id;
}
public void setId(Integer id) {
this.id = id;
}
public String getName() {
return name;
}
public void setName(String name) {
this.name = name;
}
}
3. The Three DI Implementation Approaches
Approach 1: Field Injection
Annotating the private field directly with @Autowired.
package com.example.demo.controller;
import com.example.demo.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class FieldInjectionController {
// 1. Direct field injection
// Can also mark optional: @Autowired(required = false)
@Autowired
private Employee employee;
@GetMapping("/field/employee")
public Employee getEmployee() {
return employee;
}
}
● Pros: Compact, minimal boilerplate.
● Cons: Cannot mark fields final; violates encapsulation; hides dependencies; hard to
unit-test without reflection.
Approach 2: Setter Injection
Annotating the setter method with @Autowired.
package com.example.demo.controller;
import com.example.demo.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class SetterInjectionController {
private Employee employee;
// 2. Setter injection
// Supports optional injection via required = false
@Autowired(required = false)
public void setEmployee(Employee employee) {
this.employee = employee;
}
@GetMapping("/setter/employee")
public Employee getEmployee() {
return employee;
}
}
● Pros: Ideal for optional or reconfigurable dependencies that can be modified or injected at
any point after bean creation.
● Cons: Cannot mark fields final; leaves the object in a potentially partially-constructed
state before setter invocation.
Approach 3: Constructor Injection (Industry Recommended)
Injecting dependencies through the class constructor.
package com.example.demo.controller;
import com.example.demo.model.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class ConstructorInjectionController {
// 3. Immutability achieved using final
private final Employee employee;
// @Autowired is optional in modern Spring Boot if there is only
// one constructor
@Autowired
public ConstructorInjectionController(Employee employee) {
this.employee = employee;
}
@GetMapping("/constructor/employee")
public Employee getEmployee() {
return employee;
}
}
● Pros:
○ Immutability: Guarantees all required dependencies are initialized at object
creation time (final keyword).
○ Clean Unit Testing: You can simply use new
ConstructorInjectionController(mockEmployee) without Spring context overhead.
○ Explicit Contracts: Clear visibility of all mandatory dependencies required by the
class.
● Cons: Less flexible for optional dependencies; constructors with too many arguments
become messy (an indicator of a class doing too much—violating Single Responsibility).
4. Quick Comparison Table (For OneNote)
Feature Field Injection Setter Injection Constructor Injection
Annotation PlacementDirectly on variable On setter method On constructor
Supports final
(Immutability)
❌ No ❌ No ✅ Yes
Optional
Dependencies
✅
@Autowired(required =
false)
✅
@Autowired(required =
false)
⚠️ Harder (requires
@Nullable or
Optional<T>)
Unit Testing
Friendliness
❌ Poor (needs
reflection/Mockito
runner)
⚠️ Moderate ✅ Best (plain Java
new keyword)
Industry
Recommendation
Disfavored in modern
Spring
Only for optional
dependencies
Primary
Recommended
Standard
YouTube video views will be stored in your YouTube History, and your data will be stored and
used by YouTube according to its Terms of Service
Here is a detailed summary and the complete Java code implementation from @Qualifier vs
@Primary in Spring Boot | Dependency Injection Simplified by The Curious Coder.

## 1. High-Level Summary & Problem Statement

In Spring Boot, dependency injection relies heavily on type-based autowiring (@Autowired).
When more than one bean of the same interface or class type exists in the application context,
Spring cannot decide which bean to inject and throws the classic ambiguity error:
Field required a single bean, but 2 were found: [...]
NoUniqueBeanDefinitionException
The video explains how to solve this bean ambiguity problem in two distinct scenarios using:
1. @Qualifier: Explicitly selects a specific bean by its bean name at the injection site.
2. @Primary: Gives higher preference (a default choice) to one bean when multiple
candidates are available and no specific qualifier is defined.
2. Scenario 1: Interface with Multiple @Component
Implementations
A. Interface (Vehicle.java)
package com.example.demo;
public interface Vehicle {
int numberOfWheels();
}
B. Implementations (TwoWheeler.java and FourWheeler.java)
Spring assigns default bean names using camelCase of the class name (i.e., "twoWheeler" and
"fourWheeler"), or you can provide a custom name like @Component("twoWheelerBean").
package com.example.demo;
import org.springframework.stereotype.Component;
@Component
public class TwoWheeler implements Vehicle {
@Override
public int numberOfWheels() {
return 2;
}
}
package com.example.demo;
import org.springframework.stereotype.Component;
@Component
public class FourWheeler implements Vehicle {
@Override
public int numberOfWheels() {
return 4;
}
}
C. Injection with @Qualifier (VehicleController.java)
Without @Qualifier, Spring fails on startup because both TwoWheeler and FourWheeler match
the Vehicle type.
package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class VehicleController {
@Autowired
@Qualifier("fourWheeler") // Matches default bean name in camelCase
private Vehicle vehicle;
@GetMapping("/number-of-wheels")
public int getNumberOfWheels() {
return vehicle.numberOfWheels();
}
}
3. Scenario 2: Multiple @Bean Definitions in
@Configuration & @Primary
The second demonstration shows how bean ambiguity occurs with standard POJO factories
defined inside @Configuration classes, and how @Primary acts as a default fallback.
A. POJO (Employee.java)
package com.example.demo;
public class Employee {
private int id;
private String name;
public Employee() {}
public Employee(int id, String name) {
this.id = id;
this.name = name;
}
public int getId() {
return id;
}
public void setId(int id) {
this.id = id;
}
public String getName() {
return name;
}
public void setName(String name) {
this.name = name;
}
}
B. Configuration with Multiple Beans & @Primary
(EmployeeConfig.java)
Both methods return the Employee type. @Primary marks secondEmployee as the default bean
to inject whenever no explicit qualifier is specified.
package com.example.demo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
@Configuration
public class EmployeeConfig {
@Bean("firstEmployee")
public Employee getEmployee() {
return new Employee(1, "Shivam");
}
@Bean("secondEmployee")
@Primary // Injected by default if no @Qualifier is present
public Employee getEmployeeTwo() {
return new Employee(2, "Aman");
}
}
C. Injection Demonstration (EmployeeController.java)
package com.example.demo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class EmployeeController {
// Case 1: Injects "secondEmployee" automatically due to @Primary
@Autowired
private Employee defaultEmployee;
// Case 2: Injects "firstEmployee" explicitly overriding @Primary
@Autowired
@Qualifier("firstEmployee")
private Employee explicitEmployee;
@GetMapping("/employee-id")
public int getEmployeeId() {
// Returns 2 (from @Primary)
return defaultEmployee.getId();
}
}
4. Key Differences for Interview Prep
Feature @Primary @Qualifier
Purpose Defines a default fallback bean
when ambiguity exists
Explicitly targets a specific
bean by its name
Annotation Level Applied at the definition site
(@Component, @Bean)
Applied at the injection site
(@Autowired field, setter, or
constructor parameter)
Priority Lower precedence (overridden
by @Qualifier)
Highest precedence (takes
precedence over @Primary)
Feature @Primary @Qualifier
Flexibility Useful when one primary
implementation is used 90% of
the time
Useful when fine-grained
control is required across
different consumers
YouTube video views will be stored in your YouTube History, and your data will be stored and
used by YouTube according to its Terms of Service
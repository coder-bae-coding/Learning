Here is a detailed summary and the complete Java code implementation from [**What's Behind @Transactional in Spring Boot Transaction Management?**](https://www.youtube.com/watch?v=1twyRtpqlbY) by *The Curious Coder*.

## 1. High-Level Concept & Problem Statement

The video explains database transaction management using the classic **Bank Account Transfer** example:

- **Scenario:** Alex transfers ₹100 to Jack.

- **Operations:**

  1. Debit ₹100 from Account A.

  2. Credit ₹100 to Account B.

- **The Failure Scenario:** If an exception occurs after Step 1 but before Step 2 completes:

  - **Without** **`@Transactional`****:** Each repository call (like `save()` or SQL update) runs in its own auto-commit transaction. Account A loses ₹100, but Account B never receives it, leading to dirty/inconsistent data (violating Atomicity).

  - **With** **`@Transactional`****:** The entire `transferMoney()` method executes within a single database transaction boundary. If an unhandled runtime exception occurs, Spring rolls back all modifications done within that boundary, restoring Account A’s balance.


## 2. Complete Code Implementation (Ready for OneNote)

### A. Entity Class (`Account.java`)

Java

```
package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    private Long id;
    private String name;
    private Double balance;

    public Account() {
    }

    public Account(Long id, String name, Double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}

```

### B. Spring Data Repository (`AccountRepository.java`)

Java

```
package com.example.demo.repository;

import com.example.demo.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}

```

### C. Service Layer with `@Transactional` (`BankService.java`)

The video demonstrates the difference between having and omitting `@Transactional` when an unexpected runtime exception is thrown midway:

Java

```
package com.example.demo.service;

import com.example.demo.entity.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BankService {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * @Transactional creates a single proxy-managed transaction boundary.
     * If an uncaught RuntimeException occurs, everything rolled back.
     */
    @Transactional
    public void transferMoney(Long fromAccountId, Long toAccountId, Double amount) {
        // Step 1: Debit from Account A
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        accountRepository.save(fromAccount);

        // Simulated crash/failure midway (e.g. server crash, network failure, bug)
        if (toAccountId == 99L) { // Test trigger for simulated error
            throw new RuntimeException("Simulated unexpected network/system failure!");
        }

        // Step 2: Credit to Account B
        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));
        
        toAccount.setBalance(toAccount.getBalance() + amount);
        accountRepository.save(toAccount);
    }
}

```

### D. Rest Controller (`BankController.java`)

Java

```
package com.example.demo.controller;

import com.example.demo.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private BankService bankService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestParam Long fromId,
            @RequestParam Long toId,
            @RequestParam Double amount) {
        
        bankService.transferMoney(fromId, toId, amount);
        return ResponseEntity.ok("Transfer completed successfully!");
    }
}

```

## 3. How Spring Handles It Internally (Interview Highlights)

1. **AOP Proxy Mechanism:**


   - Spring creates a dynamic proxy around `@Transactional` beans (`PlatformTransactionManager`).


   - When `transferMoney()` is called from outside the class:


     - **Before invocation:** The proxy calls `transactionManager.getTransaction(...)` and begins a DB transaction.


     - **On success:** Calls `transactionManager.commit()`.


     - **On unhandled RuntimeException/Error:** Calls `transactionManager.rollback()`.


2. **Default Rollback Rules:**


   - Rolls back automatically on **`RuntimeException`** (unchecked exceptions) and **`Error`**.

   - By default, it **does not** roll back on checked exceptions (`Exception`) unless explicitly configured:



     Java
     ```
     @Transactional(rollbackFor = Exception.class)

     ```
3. **Transaction Propagation Types Overview:**


   - **`REQUIRED`** **(Default):** Joins the existing transaction if one exists; creates a new one if not.

   - **`REQUIRES_NEW`****:** Suspends any outer transaction and always starts a separate, independent transaction.

   - **`SUPPORTS`****:** Executes within a transaction if one exists, otherwise runs non-transactionally.

   - **`MANDATORY`****:** Requires an existing transaction; throws an exception if none is found.

   - **`NOT_SUPPORTED`****:** Suspends any active transaction and executes non-transactionally.

   - **`NEVER`****:** Throws an exception if an active transaction exists.
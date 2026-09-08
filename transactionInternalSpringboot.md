Here is a detailed summary and the complete Java code implementation from [**How Propagation Works in Spring Boot Transactions? | @Transactional Deep Dive**](https://www.youtube.com/watch?v=K0c7kzBRPUw) by *The Curious Coder*.

## 1. Video Overview & Architecture

This video continues from Part 1 by dissecting how Spring handles nested method calls under different transaction boundaries using **Transaction Propagation**.

The demo refactors the transfer logic across two services to showcase how transactions propagate across Spring proxies:

- **`WalletService`** **(Outer Service):** Coordinates the overall workflow via `transfer(...)`.

- **`UserService`** **(Inner Service):** Executes individual balance modifications via `debit(...)` and `credit(...)`.

- **Custom** **`TransactionManager`** **Logging:** Subclasses `JpaTransactionManager` to log when transactions start, join, commit, or roll back.

## 2. Complete Code Implementation (Ready for OneNote)

### A. Entity (`UserWallet.java`)

Java

```
package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_wallet")
public class UserWallet {

    @Id
    private Long id;
    private String name;
    private Double balance;

    public UserWallet() {}

    public UserWallet(Long id, String name, Double balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getBalance() { return balance; }
    public void setBalance(Double balance) { this.balance = balance; }
}

```

### B. Repository (`UserWalletRepository.java`)

Java

```
package com.example.demo.repository;

import com.example.demo.entity.UserWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWalletRepository extends JpaRepository<UserWallet, Long> {
}

```

### C. Inner Service (`UserService.java`)

Contains the individual transaction-aware business units (`debit` and `credit`):

Java

```
package com.example.demo.service;

import com.example.demo.entity.UserWallet;
import com.example.demo.repository.UserWalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserWalletRepository repository;

    /**
     * Can be configured with different propagations:
     * Propagation.REQUIRED (Default)
     * Propagation.REQUIRES_NEW
     * Propagation.MANDATORY, etc.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void debit(Long userId, Double amount) {
        UserWallet user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setBalance(user.getBalance() - amount);
        repository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void credit(Long userId, Double amount) {
        UserWallet user = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setBalance(user.getBalance() + amount);
        repository.save(user);

        // Simulated exception to demonstrate rollback isolation
        if (userId == 99L) {
            throw new RuntimeException("Simulated error in credit operation!");
        }
    }
}

```

### D. Outer Service (`WalletService.java`)

Coordinates `debit` and `credit` from `UserService`:

Java

```
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    @Autowired
    private UserService userService;

    @Transactional
    public void transfer(Long senderId, Long receiverId, Double amount) {
        // Step 1: Debit sender
        userService.debit(senderId, amount);

        // Step 2: Credit receiver
        userService.credit(receiverId, amount);
    }
}

```

### E. Rest Controller (`WalletController.java`)

Java

```
package com.example.demo.controller;

import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestParam Long senderId,
            @RequestParam Long receiverId,
            @RequestParam Double amount) {

        walletService.transfer(senderId, receiverId, amount);
        return ResponseEntity.ok("Transfer completed");
    }
}

```

## 3. Deep-Dive: Propagation Behaviors & Isolation

| **Propagation Type** | **If Outer Transaction Exists** | **If No Outer Transaction Exists** | **Rollback Behavior** |
| -------------------------- | ------------------------------------------------------------------------------------ | -------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **`REQUIRED`** *(Default)* | Joins the outer transaction. | Starts a new transaction. | Any inner exception marks the shared physical transaction as `rollback-only`. The whole unit rolls back. |
| **`REQUIRES_NEW`** | Suspends outer transaction, opens a completely new independent physical transaction. | Starts a new transaction. | If the inner transaction fails, only the inner transaction rolls back. If the outer method handles the exception (e.g. `try-catch`), previously committed independent transactions are **not** rolled back. |
| **`SUPPORTS`** | Joins the outer transaction. | Runs non-transactionally. | Depends on whether an outer transaction was present. |
| **`NOT_SUPPORTED`** | Suspends outer transaction and runs non-transactionally. | Runs non-transactionally. | Executes without transactional rollback support. |
| **`MANDATORY`** | Joins the outer transaction. | **Throws** **`IllegalTransactionStateException`**. | Requires caller to have initiated a transaction. |
| **`NEVER`** | **Throws** **`IllegalTransactionStateException`**. | Runs non-transactionally. | Strict prohibition of running inside any transaction. |
| **`NESTED`** | Creates a JDBC **savepoint** within the existing transaction. | Starts a new transaction (like `REQUIRED`). | Rollback only reverts back to the savepoint without affecting the outer transaction's prior work. |

### Key Takeaways for Interviews

1. **The Double-Debit Trap with** **`REQUIRES_NEW`****:** If `debit()` runs in `REQUIRES_NEW` and commits, but `credit()` subsequently throws an exception, the debit remains committed in the database unless compensating logic or a single joint transaction boundary (`REQUIRED`) is used.

2. **Spring AOP Proxy Rule:** Propagation settings are honored **only** when calls pass through the Spring proxy (i.e. between different Spring beans like `WalletService` -> `UserService`). Calling a `@Transactional(propagation = REQUIRES_NEW)` method from within the same class bypasses the proxy and will not create a new transaction.
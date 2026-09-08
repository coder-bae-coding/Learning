# Spring Boot Transactions — Interview Notes

## 1. What is a Transaction?

A **transaction** is a group of database operations that should be treated as **one unit of work**.

> **All operations succeed → COMMIT**  
> **Any failure → ROLLBACK**

### Bank transfer example

```java
public void transferMoney() {
    debitAccountA();
    creditAccountB();
}
```

Suppose A has ₹1000 and B has ₹500:

```text
A: ₹1000 → ₹900
B: ₹500  → ₹600
```

If debit succeeds but credit fails, we don't want:

```text
A: ₹900   ❌
B: ₹500   ❌
```

We want the entire operation rolled back:

```text
A: ₹1000   ✅
B: ₹500    ✅
```

---

## 2. `@Transactional`

In Spring, `@Transactional` defines a **transaction boundary** around a method.

```java
@Service
public class BankService {

    @Transactional
    public void transferMoney(Long fromId, Long toId, Double amount) {
        debit(fromId, amount);
        credit(toId, amount);
    }
}
```

Think:

```text
@Transactional
     ↓
START TRANSACTION
     ↓
execute method
     ↓
 ┌───────────────┐
 │ success       │ → COMMIT
 │ exception     │ → ROLLBACK
 └───────────────┘
```

### Easy interview definition

> `@Transactional` makes multiple database operations execute within a transaction so that they are committed together or rolled back together when a rollback-triggering exception occurs.

---

## 3. Without `@Transactional`

Imagine:

```java
public void transferMoney() {
    debit();
    credit();
}
```

If operations are committed independently, this can happen:

```text
Debit  → COMMIT ✅
Credit → EXCEPTION ❌
```

Result: money is deducted but not credited.

With `@Transactional`:

```text
Debit  → pending
Credit → EXCEPTION
        ↓
     ROLLBACK
```

Both changes are undone.

---

## 4. What Spring Does Internally

Spring usually applies `@Transactional` through an **AOP proxy**.

When another Spring bean calls:

```java
bankService.transferMoney();
```

conceptually the proxy does something like:

```java
startTransaction();

try {
    transferMoney();
    commit();
} catch (RuntimeException | Error e) {
    rollback();
    throw e;
}
```

You normally do **not** write this transaction-management code yourself.

### Important interview terms

```text
@Transactional
      ↓
Spring AOP Proxy
      ↓
Transaction Manager
      ↓
Database transaction
```

For JPA, Spring commonly works with a transaction manager backed by the JPA provider/database setup.

---

## 5. Rollback Rules

By default, Spring rolls back for:

- `RuntimeException`
- `Error`

Example:

```java
@Transactional
public void transferMoney() {
    debit();
    throw new RuntimeException("Transfer failed");
    // credit() is never reached
}
```

Result:

```text
ROLLBACK ✅
```

### Checked exception

A checked exception does **not** trigger rollback by default.

```java
@Transactional
public void transferMoney() throws Exception {
    debit();
    throw new Exception("Checked exception");
}
```

To explicitly rollback for checked exceptions:

```java
@Transactional(rollbackFor = Exception.class)
```

---

## 6. Propagation

Propagation answers:

> **What should happen when a `@Transactional` method is called while another transaction already exists?**

### REQUIRED — Default

```java
@Transactional
public void methodA() {
    methodB();
}

@Transactional(propagation = Propagation.REQUIRED)
public void methodB() {
}
```

If a transaction already exists, `methodB()` joins it.
If none exists, Spring creates one.

```text
Tx1
 ├── methodA()
 └── methodB()
```

**Default propagation = `REQUIRED`**

---

### REQUIRES_NEW

Always uses a **new transaction**.

```java
@Transactional
public void methodA() {
    methodB();
}

@Transactional(propagation = Propagation.REQUIRES_NEW)
public void methodB() {
}
```

Conceptually:

```text
Tx1 starts
   ↓
Tx1 suspended
   ↓
Tx2 starts for methodB()
   ↓
Tx2 commits/rolls back
   ↓
Tx1 resumes
```

Useful when the inner work must have its own transaction, e.g. some audit/history use cases.

---

### SUPPORTS

```java
@Transactional(propagation = Propagation.SUPPORTS)
```

- Existing transaction → join it
- No transaction → run without a transaction

---

### MANDATORY

```java
@Transactional(propagation = Propagation.MANDATORY)
```

Requires an existing transaction.

```text
Transaction exists? → JOIN
No transaction?     → Exception
```

---

### NOT_SUPPORTED

```java
@Transactional(propagation = Propagation.NOT_SUPPORTED)
```

Runs **without** a transaction and suspends an active transaction if one exists.

---

### NEVER

```java
@Transactional(propagation = Propagation.NEVER)
```

Must run without a transaction.
If an active transaction exists, Spring throws an exception.

---

## 7. Most Important Propagation Table

| Propagation | Existing Tx? | New Tx? |
|---|---|---|
| `REQUIRED` | Join | Create if none |
| `REQUIRES_NEW` | Suspend outer | Always create |
| `SUPPORTS` | Join | No |
| `MANDATORY` | Must join | No |
| `NOT_SUPPORTED` | Suspend | No |
| `NEVER` | Error | No |

---

## 8. Very Important: Self-Invocation

This is a common Spring interview question.

Consider:

```java
@Service
public class BankService {

    public void methodA() {
        methodB();
    }

    @Transactional
    public void methodB() {
        // database work
    }
}
```

Calling `methodB()` directly from `methodA()` **inside the same object** bypasses the Spring proxy.

So the transactional interception may not happen as expected.

```text
External caller
     ↓
Spring Proxy
     ↓
methodA()
     ↓
methodB()   ← direct internal call, proxy bypassed
```

Better approach: move the transactional method to another Spring bean when appropriate.

---

## 9. `@Transactional` Is Usually Put on the Service Layer

Typical structure:

```text
Controller
    ↓
Service  ← @Transactional
    ↓
Repository
    ↓
Database
```

Example:

```java
@RestController
public class BankController {

    @PostMapping("/transfer")
    public void transfer() {
        bankService.transferMoney(1L, 2L, 100.0);
    }
}
```

```java
@Service
public class BankService {

    @Transactional
    public void transferMoney(Long fromId, Long toId, Double amount) {
        // debit
        // credit
    }
}
```

---

## 10. Interview Questions — Quick Revision

### Q1. What does `@Transactional` do?

It defines a transaction boundary so that related database operations can be committed or rolled back as one unit.

### Q2. How does Spring implement it?

Using **AOP proxy-based transaction interception** and a transaction manager.

### Q3. What is the default propagation?

```java
Propagation.REQUIRED
```

### Q4. What causes rollback by default?

```text
RuntimeException
Error
```

### Q5. Does a checked exception rollback by default?

No. Use:

```java
@Transactional(rollbackFor = Exception.class)
```

### Q6. `REQUIRED` vs `REQUIRES_NEW`?

```text
REQUIRED      → join existing or create new
REQUIRES_NEW  → suspend existing and always create new
```

### Q7. Why can `@Transactional` fail on self-invocation?

Because the internal method call does not pass through the Spring proxy.

### Q8. Where is `@Transactional` commonly placed?

Usually on the **service layer**, where the business operation spans multiple repository/database calls.

---

## 11. One-Line Memory Trick

```text
@Transactional = START → WORK → COMMIT
                         ↓
                      FAILURE
                         ↓
                      ROLLBACK
```

And remember:

```text
REQUIRED      = JOIN or CREATE
REQUIRES_NEW  = SUSPEND + NEW
SUPPORTS      = JOIN if available
MANDATORY     = MUST HAVE Tx
NOT_SUPPORTED = NO Tx
NEVER         = Tx NOT ALLOWED
```

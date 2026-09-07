# Strategy Design Pattern

## 1. What is the Strategy Pattern?

**Strategy is a behavioural design pattern** that lets you define a family of algorithms, encapsulate each algorithm in a separate class, and make them interchangeable at runtime.

### Simple idea

```text
                 Context
                    |
          ----------------------
          |                    |
   EncryptionStrategy    CompressionStrategy
          |                    |
     --------------       -------------
     |            |       |           |
    AES          RSA      ZIP         GZIP
```

The main benefit is that the **context depends on abstractions**, not on concrete algorithm implementations.

---

## 2. Why do we need it?

Imagine a notification system that can:

- encrypt a notification using AES or RSA
- compress it using ZIP or GZIP
- send it as an Email

A naive implementation could put all of the choices inside `Notification`:

```java
if (encryptionType == AES) {
    // AES logic
} else if (encryptionType == RSA) {
    // RSA logic
}

if (compressionType == ZIP) {
    // ZIP logic
} else if (compressionType == GZIP) {
    // GZIP logic
}
```

As more algorithms are added, this class becomes difficult to maintain and violates the idea of keeping each responsibility isolated.

The Strategy Pattern moves each algorithm into its own class.

---

# 3. Components in this example

| Component | Class / Interface | Responsibility |
|---|---|---|
| Context | `Notification` | Uses the selected strategies |
| Concrete Context | `Email` | Sends an email |
| Strategy | `EncryptionStrategy` | Common encryption contract |
| Concrete Strategies | `AESEncryption`, `RSAEncryption` | Implement encryption algorithms |
| Strategy | `CompressionStrategy` | Common compression contract |
| Concrete Strategies | `ZIP`, `GZIP` | Implement compression algorithms |
| Client | `Main` | Chooses the strategies and creates the context |

---

# 4. Encryption Strategy

The common interface defines what every encryption algorithm must provide.

```java
public interface EncryptionStrategy {
    void encrypt();
}
```

The `Notification` class does **not** need to know how AES or RSA works. It only knows that an encryption strategy can `encrypt()`.

---

# 5. Concrete Encryption Strategies

## AES

```java
public class AESEncryption implements EncryptionStrategy {

    @Override
    public void encrypt() {
        System.out.println("AES Encryption");
    }
}
```

## RSA

```java
public class RSAEncryption implements EncryptionStrategy {

    @Override
    public void encrypt() {
        System.out.println("RSA Encryption");
    }
}
```

Both classes follow the same contract but implement different algorithms.

---

# 6. Compression Strategy

```java
public interface CompressionStrategy {
    void compress();
}
```

Concrete implementations:

## ZIP

```java
public class ZIP implements CompressionStrategy {

    @Override
    public void compress() {
        System.out.println("ZIP Encrypt");
    }
}
```

> In a real application this message would normally describe compression rather than encryption. The screenshot's code prints `ZIP Encrypt`; the important Strategy Pattern concept is the interchangeable `compress()` implementation.

## GZIP

```java
public class GZIP implements CompressionStrategy {

    @Override
    public void compress() {
        System.out.println("GZIP");
    }
}
```

---

# 7. Context — Notification

`Notification` stores references to the strategy interfaces.

```java
public abstract class Notification {

    EncryptionStrategy encryptionStrategy;
    CompressionStrategy compressionStrategy;

    public Notification(
            EncryptionStrategy encryptionStrategy,
            CompressionStrategy compressionStrategy) {
        this.encryptionStrategy = encryptionStrategy;
        this.compressionStrategy = compressionStrategy;
    }

    void encrypt() {
        encryptionStrategy.encrypt();
    }

    void compress() {
        compressionStrategy.compress();
    }

    abstract void send();
}
```

### Important point

`Notification` is the **context**.

It delegates the algorithm-specific work to the objects stored in:

```java
EncryptionStrategy encryptionStrategy;
CompressionStrategy compressionStrategy;
```

It does not contain AES, RSA, ZIP, or GZIP implementation logic.

---

# 8. Concrete Context — Email

```java
public class Email extends Notification {

    public Email(
            EncryptionStrategy encryptionStrategy,
            CompressionStrategy compressionStrategy) {
        super(encryptionStrategy, compressionStrategy);
    }

    @Override
    void send() {
        System.out.println("Email Sended");
    }
}
```

`Email` is responsible for the **email-specific behaviour** (`send()`), while the encryption and compression algorithms remain separate strategies.

---

# 9. Selecting Strategies at Runtime

The client chooses which implementations to use:

```java
public class Main {

    public static void main(String[] args) {
        Notification email =
                new Email(new AESEncryption(), new ZIP());

        email.send();
        email.encrypt();
        email.compress();
    }
}
```

The important line is:

```java
new Email(new AESEncryption(), new ZIP());
```

We have selected:

```text
Encryption → AES
Compression → ZIP
```

We could instead select:

```java
Notification email =
        new Email(new RSAEncryption(), new GZIP());
```

Now the same `Email` context uses:

```text
Encryption → RSA
Compression → GZIP
```

The context code does not change.

---

# 10. What does "selected at runtime" mean?

The **strategy objects are supplied when the context is created**, rather than the algorithm being hard-coded inside the context.

```text
                    Client
                      |
          chooses concrete strategies
                      |
          ---------------------------
          |                         |
      AES/RSA                    ZIP/GZIP
          \                         /
           \                       /
               Email (Context)
```

Because the fields are typed as interfaces, the same context can work with different implementations.

---

# 11. Why this is Strategy Pattern

The key characteristics visible in the code are:

### Family of algorithms

```text
AES / RSA
ZIP / GZIP
```

### Each algorithm is encapsulated

```text
AESEncryption
RSAEncryption
ZIP
GZIP
```

### Common interfaces

```text
EncryptionStrategy
CompressionStrategy
```

### Context uses the abstraction

```java
EncryptionStrategy encryptionStrategy;
CompressionStrategy compressionStrategy;
```

### Algorithms can be changed without changing the context

```java
new Email(new AESEncryption(), new ZIP());
```

can become:

```java
new Email(new RSAEncryption(), new GZIP());
```

---

# 12. Strategy vs putting everything in one class

### Without Strategy

```text
Notification
   |
   +-- if AES
   +-- if RSA
   +-- if ZIP
   +-- if GZIP
   +-- more conditions...
```

The class keeps growing whenever a new algorithm is introduced.

### With Strategy

```text
Notification
   |
   +-- EncryptionStrategy --> AES / RSA / ...
   |
   +-- CompressionStrategy -> ZIP / GZIP / ...
```

Adding a new algorithm normally means adding a new strategy class rather than modifying the context's algorithm-selection logic.

---

# 13. When should you use Strategy Pattern?

Use it when:

1. You have multiple ways of performing the same operation.
2. The algorithms are likely to change or grow over time.
3. You want to select an algorithm dynamically.
4. A class contains many `if/else` or `switch` branches for choosing behaviour.
5. You want to isolate algorithm-specific code and keep the main class simple.

### Common real-world examples

```text
Payment
 ├── CreditCardPayment
 ├── UpiPayment
 └── NetBankingPayment

Sorting
 ├── QuickSort
 ├── MergeSort
 └── HeapSort

Pricing
 ├── NormalPricing
 ├── DiscountPricing
 └── FestivalPricing

Routing
 ├── CarRoute
 ├── BikeRoute
 └── WalkingRoute
```

---

# 14. Advantages

- **Open/Closed Principle:** new strategies can be added with minimal changes to existing context code.
- **Less conditional logic:** algorithm selection is separated from algorithm implementation.
- **Better maintainability:** each algorithm has its own class.
- **Easy testing:** strategies can be tested independently.
- **Runtime flexibility:** different strategies can be supplied to the same context.

# 15. Disadvantages

- Adds more classes/interfaces.
- The client must understand which strategy to choose.
- For a very small piece of behaviour, Strategy can sometimes be unnecessary complexity.

---

# 16. Interview-ready definition

> **Strategy Pattern is a behavioural design pattern that defines a family of interchangeable algorithms, encapsulates each algorithm behind a common interface, and allows the client/context to choose or change the algorithm without modifying the context.**

A very easy way to remember it:

```text
Same job + different ways to do it
             ↓
        Strategy Pattern
```

---

# 17. One-line mapping of the screenshot

```text
Notification = Context
Email        = Concrete Context
EncryptionStrategy = Strategy
AESEncryption/RSAEncryption = Concrete Strategies
CompressionStrategy = Strategy
ZIP/GZIP = Concrete Strategies
Main = Client that selects the strategies
```

---

# 18. Key takeaway

The most important idea is **composition over hard-coded behaviour**.

Instead of making `Email` know every possible encryption and compression algorithm:

```java
Email -> knows AES, RSA, ZIP, GZIP ...
```

we make it depend on abstractions:

```java
Email -> EncryptionStrategy
Email -> CompressionStrategy
```

That is what makes the behaviour interchangeable.

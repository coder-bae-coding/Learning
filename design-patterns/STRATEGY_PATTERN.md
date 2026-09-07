# Strategy Design Pattern

### Definition
**Behavioral design pattern** that defines a family of algorithms, encapsulates each in a separate class, and makes them interchangeable.

### Core Idea
```text
Context
  ↓
Strategy Interface
  ↓
Concrete Strategies
```

### Your Example
```text
Notification (Context)
│
├── EncryptionStrategy
│   ├── AESEncryption
│   └── RSAEncryption
│
└── CompressionStrategy
    ├── ZIP
    └── GZIP
```

### Key Code
```java
public interface EncryptionStrategy {
    void encrypt();
}

public interface CompressionStrategy {
    void compress();
}
```

Context stores the abstractions:
```java
EncryptionStrategy encryptionStrategy;
CompressionStrategy compressionStrategy;
```

Strategies are selected when creating the context:
```java
Notification email =
    new Email(new AESEncryption(), new ZIP());
```

Can switch without changing `Email`:
```java
new Email(new RSAEncryption(), new GZIP());
```

### Why Use It?
Use Strategy when:
- Multiple ways/algorithms perform the same task
- Behavior needs to be interchangeable
- You want to avoid large `if/else` or `switch` blocks
- New algorithms may be added later

### Benefits
✅ Follows Open/Closed Principle  
✅ Less conditional logic  
✅ Easy to add/test algorithms  
✅ Flexible behavior

### Drawback
❌ More classes and interfaces  

### Interview One-Liner
> **Strategy Pattern encapsulates interchangeable algorithms behind a common interface and allows choosing the required behavior without changing the context.**

### Remember
```text
Same job + different ways to do it
            ↓
       Strategy Pattern
```

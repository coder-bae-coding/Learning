# Adapter Design Pattern

## 1. What is Adapter Pattern?

**Adapter is a structural design pattern** that allows classes with **incompatible interfaces** to work together.

### Simple idea

```text
Client
  ↓
Target Interface
  ↓
Adapter
  ↓
Third-Party Class
```

The client uses the interface it already understands, while the Adapter converts that call into the format required by the third-party class.

---

## 2. Problem in this example

Our application wants one common payment API:

```java
public interface PaymentService {
    String pay(int amount, int customerId);
}
```

But different payment gateways provide different APIs:

```text
PayPal   → doTransaction(double amount, int cId) → int
Razorpay → performTransaction(double amount, double cId) → boolean
```

Their method names, parameter types and return types are different.

Instead of changing third-party code or making the client understand every gateway, we use **Adapters**.

---

# 3. Components

| Component | Class | Responsibility |
|---|---|---|
| Target | `PaymentService` | Interface expected by the application |
| Adaptee | `PayPalGateway` | Existing third-party API |
| Adaptee | `RazorPayGateway` | Existing third-party API |
| Adapter | `PayPalAdapter` | Converts `PaymentService` → PayPal API |
| Adapter | `RazorPayAdapter` | Converts `PaymentService` → Razorpay API |
| Client | `PaymentClient` | Uses only `PaymentService` |
| Entry point | `Main` | Creates and wires objects |

---

# 4. Target Interface

```java
public interface PaymentService {
    String pay(int amount, int customerId);
}
```

This is the **common interface** expected by our application.

The client does not need to know anything about PayPal or Razorpay.

---

# 5. Third-Party Classes (Adaptees)

## PayPalGateway

```java
public class PayPalGateway {
    public int doTransaction(double amount, int cId) {
        // PayPal payment logic
        return 1; // 1 = success
    }
}
```

PayPal uses:

```text\doTransaction()
amount → double
customerId → int
result → int
```

Our application, however, expects:

```text
pay()
amount → int
customerId → int
result → String
```

So PayPal cannot directly implement `PaymentService` without changing its API.

---

## RazorPayGateway

```java
public class RazorPayGateway {
    public boolean performTransaction(double amount, double cId) {
        // Razorpay payment logic
        return true; // true = success
    }
}
```

Razorpay uses:

```text
performTransaction()
amount → double
customerId → double
result → boolean
```

Again, this does not match `PaymentService`.

---

# 6. PayPal Adapter

```java
public class PayPalAdapter implements PaymentService {
    private final PayPalGateway paypalGateway;

    public PayPalAdapter(PayPalGateway paypalGateway) {
        this.paypalGateway = paypalGateway;
    }

    @Override
    public String pay(int amount, int customerId) {
        double doubleAmount = Double.valueOf(amount);
        int result = paypalGateway.doTransaction(doubleAmount, customerId);

        if (result == 1) {
            return "SUCCESS";
        } else {
            return "FAILED";
        }
    }
}
```

### What the adapter does

```text
PaymentService.pay(int, int)
            ↓
       PayPalAdapter
            ↓
 convert int → double
            ↓
paypalGateway.doTransaction(double, int)
            ↓
      int result
            ↓
  1 → SUCCESS
  else → FAILED
```

The Adapter performs **interface/parameter/return-value conversion**.

---

# 7. RazorPay Adapter

```java
public class RazorPayAdapter implements PaymentService {
    private final RazorPayGateway razorPayGateway;

    public RazorPayAdapter(RazorPayGateway razorPayGateway) {
        this.razorPayGateway = razorPayGateway;
    }

    @Override
    public String pay(int amount, int customerId) {
        double doubleAmount = Double.valueOf(amount);
        double doubleCustomerId = Double.valueOf(customerId);

        boolean result = razorPayGateway.performTransaction(
            doubleAmount,
            doubleCustomerId
        );

        if (result) {
            return "SUCCESS";
        } else {
            return "FAILED";
        }
    }
}
```

### What this adapter converts

```text
int amount      → double amount
int customerId  → double customerId
boolean result  → "SUCCESS" / "FAILED"
```

It hides all Razorpay-specific details from the application.

---

# 8. Client

```java
public class PaymentClient {
    private final PaymentService paymentService;

    public PaymentClient(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void processPayment(int amount, int customerId) {
        String result = paymentService.pay(amount, customerId);

        if ("SUCCESS".equalsIgnoreCase(result)) {
            System.out.println("Payment done successfully");
        } else {
            System.out.println("Payment failed");
        }
    }
}
```

### Important point

`PaymentClient` knows only:

```java
PaymentService
```

It does **not** know:

```java
PayPalGateway
RazorPayGateway
```

This makes the client independent of the third-party libraries.

---

# 9. Main / Wiring

```java
public class Main {
    public static void main(String[] args) {

        // Razorpay
        RazorPayGateway razorPayGateway = new RazorPayGateway();
        PaymentService razorPayAdapter =
                new RazorPayAdapter(razorPayGateway);

        PaymentClient client1 =
                new PaymentClient(razorPayAdapter);

        client1.processPayment(100, 101);

        // PayPal
        PayPalGateway payPalGateway = new PayPalGateway();
        PaymentService payPalAdapter =
                new PayPalAdapter(payPalGateway);

        PaymentClient client2 =
                new PaymentClient(payPalAdapter);

        client2.processPayment(200, 102);
    }
}
```

### Flow

For Razorpay:

```text
Main
 ↓
RazorPayGateway
 ↓
RazorPayAdapter
 ↓
PaymentService reference
 ↓
PaymentClient
```

For PayPal:

```text
Main
 ↓
PayPalGateway
 ↓
PayPalAdapter
 ↓
PaymentService reference
 ↓
PaymentClient
```

---

# 10. Why Adapter is useful

Without Adapter, `PaymentClient` would need different code for every gateway:

```text
if PayPal → call doTransaction()
if Razorpay → call performTransaction()
if ...
```

With Adapter:

```java
PaymentService paymentService;
paymentService.pay(amount, customerId);
```

The client always uses the same API.

---

# 11. Key Benefits

- **Compatibility:** makes incompatible interfaces work together.
- **Loose coupling:** client depends on `PaymentService`, not concrete gateways.
- **Third-party integration:** useful when external classes cannot be modified.
- **Easy replacement:** switch from PayPal to Razorpay by changing the Adapter supplied to the client.
- **Single Responsibility:** conversion logic stays inside the Adapter.

---

# 12. Adapter Pattern in one line

> **Adapter converts the interface of an existing class into the interface expected by the client.**

---

# 13. Interview Example

### Question: Why do we need Adapter here?

**Answer:** PayPal and Razorpay expose different APIs, but our application wants one common `PaymentService` interface. The Adapter translates the common `pay()` call into the specific third-party method and converts the result back into the application's expected format.

### What is the Adaptee?

`PayPalGateway` and `RazorPayGateway`.

### What is the Target?

`PaymentService`.

### What is the Adapter?

`PayPalAdapter` and `RazorPayAdapter`.

### What is the Client?

`PaymentClient`.

---

# 14. Remember the structure

```text
             PaymentService
             (Target)
                  ↑
          implements
          /          \
         /            \
PayPalAdapter     RazorPayAdapter
     ↓                  ↓
PayPalGateway      RazorPayGateway
  (Adaptee)            (Adaptee)

                  ↑
                  |
            PaymentClient
               (Client)
```

### Memory trick

```text
Client wants Target
        ↓
Adapter translates
        ↓
Adaptee does the real work
```

---

# 15. Important distinction

**Adapter does not change the third-party class.**

It wraps the existing class and translates between interfaces.

In this example:

```java
private final PayPalGateway paypalGateway;
```

and

```java
private final RazorPayGateway razorPayGateway;
```

show that the Adapters **contain** the third-party objects and delegate the actual payment operation to them.

---

# Quick Revision

```text
Adapter = Structural Pattern

Target      → PaymentService
Adapter     → PayPalAdapter / RazorPayAdapter
Adaptee     → PayPalGateway / RazorPayGateway
Client      → PaymentClient

Purpose:
Make incompatible interfaces work together.

Main idea:
Client calls pay()
        ↓
Adapter converts
        ↓
Third-party gateway executes
        ↓
Adapter converts result
        ↓
Client receives common result
```

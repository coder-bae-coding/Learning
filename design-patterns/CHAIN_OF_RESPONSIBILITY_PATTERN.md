# Chain of Responsibility Design Pattern

## Definition
**Chain of Responsibility is a behavioral design pattern** where a request is passed through a chain of handlers until one handler can process it.

### Simple idea
```text
Client
  ↓
Level1Handler → Level2Handler → ManagerHandler
``` 

The sender does **not need to know which handler** will process the request.

---

## Example from the code
A support ticket has a `severity`:

```text
severity = 1 → Support 1
severity = 2 → Support 2
severity = 3 → Manager
```

The chain is created in `Main`:

```java
SupportHandler level1 = new Level1Handler();
SupportHandler level2 = new Level2Handler();
SupportHandler manager = new ManagerHandler();

level1.setNextHandler(level2);
level2.setNextHandler(manager);

Ticket ticket = new Ticket(30);
level1.handle(ticket);
```

**Important:** the screenshot shows `Ticket(30)`, while the handlers shown only explicitly handle severities `1`, `2`, and `3`. So the exact runtime result for severity `30` is not supported by the screenshots; the important pattern is the forwarding chain.

---

## Components

| Component | Example | Role |
|---|---|---|
| Handler | `SupportHandler` | Common handler abstraction |
| Concrete Handlers | `Level1Handler`, `Level2Handler`, `ManagerHandler` | Try to handle the request |
| Request | `Ticket` | Contains `severity` |
| Client | `Main` | Builds chain and starts request |

---

## Handler
`SupportHandler` keeps a reference to the next handler:

```java
protected SupportHandler nextHandler;

public void setNextHandler(SupportHandler nextHandler) {
    this.nextHandler = nextHandler;
}

public abstract void handle(Ticket ticket);
```

This `nextHandler` reference is what creates the chain.

---

## How a handler works
Example from `Level1Handler`:

```java
public void handle(Ticket ticket) {
    if (ticket.getSeverity() == 1) {
        System.out.println("Handled by Support 1");
    } else if (nextHandler != null) {
        nextHandler.handle(ticket);
    }
}
```

`Level2Handler` follows the same idea for severity `2`, and `ManagerHandler` handles severity `3`.

### Flow
```text
Ticket
  ↓
Level 1
  ├─ severity 1 → handle
  └─ otherwise → Level 2
                    ├─ severity 2 → handle
                    └─ otherwise → Manager
                                      └─ severity 3 → handle
```

---

## Why use it?
Without it, the client may need a large chain of conditions:

```java
if (severity == 1) ...
else if (severity == 2) ...
else if (severity == 3) ...
```

With Chain of Responsibility, each handler owns its own decision and forwards the request when it cannot handle it.

---

## When to use
- Multiple handlers may process the same request.
- The correct handler should be decided dynamically.
- You want to avoid a large `if/else` or `switch` in the client.
- You want to add/reorder handlers without changing the client.

### Common examples
```text
Support ticket escalation
Approval workflows
Logging chains
Authentication/authorization filters
Servlet / HTTP request filters
```

---

## Advantages
✅ Decouples sender from receiver  
✅ Easy to add/reorder handlers  
✅ Removes large conditional logic from client  

## Disadvantages
❌ Request may pass through many handlers  
❌ Request may remain unhandled if the chain has no suitable handler  
❌ Debugging the chain can be harder

---

## Interview one-liner
> **Chain of Responsibility passes a request through a chain of handlers, giving each handler a chance to process it or forward it to the next handler.**

### Memory trick
```text
Request → Try → Can't handle? → Next → Try → ...
```

### Your code mapping
```text
SupportHandler = Handler
Level1/Level2/Manager = Concrete Handlers
Ticket = Request
Main = Client
nextHandler = Chain link
```
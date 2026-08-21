# Prototype Design Pattern — Detailed Notes

## 1. What is the Prototype Pattern?

The **Prototype Design Pattern** is a **creational design pattern** used when we want to create a new object by **copying/cloning an existing object**, instead of repeatedly constructing the object from scratch.

### Core idea

```text
Create an expensive/configured object once
            ↓
      Keep it as prototype
            ↓
 Clone it whenever a similar object is required
```

This can be useful when object creation is expensive because it may involve configuration, database access, I/O, network calls, parsing, loading resources, or other costly setup work.

### Why use it?

- Avoid repeating expensive initialization.
- Create many similar objects quickly.
- Keep object creation logic inside the object being cloned.
- Reduce coupling between client code and complicated construction logic.
- Allow a cloned object to be customized after cloning.

The screenshots specifically illustrate the idea with a `GameBot` object that performs simulated expensive initialization (`Thread.sleep(2000)`) and is then cloned multiple times.

---

## 2. Prototype Pattern in the screenshots

The notes in the screenshots summarize the pattern as:

> Create an object once and clone it when needed to save time.

The implementation uses a **copy constructor** and a `clone()`-style method.

The important flow is:

```text
new GameBot(...)      ← expensive
        ↓
      gB1
        ↓
 gB1.clone()
        ↓
 gB2 / gB3 / gB4 ...
```

Instead of doing this repeatedly:

```java
new GameBot(...); // expensive every time
new GameBot(...);
new GameBot(...);
new GameBot(...);
```

the program creates one prototype and clones it:

```java
GameBot gB1 = new GameBot(...);

GameBot gB2 = gB1.cloneable();
GameBot gB3 = gB1.cloneable();
GameBot gB4 = gB1.cloneable();
```

---

## 3. Typical structure

A simple Prototype implementation usually contains:

```text
Prototype
   │
   ├── clone()
   │
   └── copy constructor / copy logic
          ↑
          │
   Concrete Prototype
          ↑
          │
        Client
```

In the screenshot's example:

- `GameBot` = concrete prototype.
- `GameBot(GameBot g1)` = copy constructor.
- `cloneable()` = clone operation.
- `PrototypeDesignPatternLearningMain` = client.

---

# 4. GameBot example from the screenshots

## Class definition

The screenshots show a `GameBot` with these main fields:

```java
public class GameBot {
    public String name;
    public int health;
    public List<String> weapons;
}
```

Conceptually, the object contains:

```text
GameBot
 ├── name
 ├── health
 └── weapons
```

The `weapons` field is especially important because it is a **mutable reference type** (`List<String>`), which is why the notes discuss **shallow copy vs deep copy**.

---

## 5. Expensive constructor

The constructor shown in the screenshot performs simulated expensive initialization:

```java
public GameBot(String name, int health, int attackPower, List<String> weapons) {
    System.out.println("Loading Game");
    System.out.println("Loading Game Animation");
    System.out.println("Loading Game Sound");

    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        System.out.println("Interrupted in thread");
    }

    this.name = name;
    this.health = health;
    this.weapons = weapons;
}
```

### Important observation

The screenshot's constructor accepts an `attackPower` parameter, but the displayed fields do not contain an `attackPower` field and the constructor does not assign it to an instance field.

So, based strictly on the shown code, `attackPower` is effectively unused after construction.

---

# 6. Copy constructor

The important Prototype mechanism is the private copy constructor:

```java
private GameBot(GameBot g1) {
    this.name = g1.name;
    this.health = g1.health;
    this.weapons = new ArrayList<>(g1.weapons);
}
```

This constructor takes an existing `GameBot` and creates another `GameBot` from its state.

Notice the difference between the fields:

```java
this.name = g1.name;
this.health = g1.health;
```

These are simple assignments.

For the list:

```java
this.weapons = new ArrayList<>(g1.weapons);
```

a **new list object** is created containing the same string elements.

That means changes to the cloned object's list do not change the original object's list structure.

---

# 7. Clone method

The screenshot shows:

```java
public GameBot cloneable() {
    return new GameBot(this);
}
```

This is the method clients use to clone the object.

### Important point

`cloneable()` is a custom method name in the screenshot. It is **not** the same thing as Java's `Cloneable` marker interface.

The implementation is simply:

```text
cloneable()
    ↓
new GameBot(this)
    ↓
copy constructor
    ↓
new object
```

This is often clearer than relying on Java's built-in `Object.clone()` mechanism.

---

# 8. Why the copy constructor matters

The copy constructor centralizes the copying rules.

Without a copy constructor, the client might have to know every internal field:

```java
GameBot gB2 = new GameBot(...);
gB2.setName(gB1.getName());
gB2.setHealth(gB1.getHealth());
gB2.setWeapons(...);
```

That is undesirable because the client becomes responsible for object-copying details.

With Prototype:

```java
GameBot gB2 = gB1.cloneable();
```

The `GameBot` class itself controls how a copy is created.

---

# 9. Shallow Copy vs Deep Copy

The notes in the screenshot contrast **Shallow Copy** and **Deep Copy**.

This distinction is crucial in Prototype implementations.

## Shallow copy

A shallow copy creates a new outer object, but referenced objects can still be shared.

Example conceptually:

```text
Original GameBot
      │
      └── weapons ──→ List A

Cloned GameBot
      │
      └── weapons ──→ List A
```

Both objects point to the same list.

If one object modifies the list, the other object may observe that modification.

For example:

```java
this.weapons = g1.weapons;
```

would share the same `List` instance.

---

## Deep copy

A deep copy creates independent nested mutable objects.

Conceptually:

```text
Original GameBot
      │
      └── weapons ──→ List A

Cloned GameBot
      │
      └── weapons ──→ List B
```

The two lists are different objects.

In the screenshot, the copy constructor does this:

```java
this.weapons = new ArrayList<>(g1.weapons);
```

So the list container is copied rather than shared.

For a `List<String>`, this is usually enough for independent list structure because `String` is immutable.

---

# 10. Why String is special

`String` in Java is immutable.

Therefore, copying references to existing `String` values is generally safe because the referenced string cannot be modified in-place.

For example:

```java
String s1 = "AK";
String s2 = s1;
```

Even though `s1` and `s2` refer to the same string object, nobody can mutate that string object.

So a list such as:

```java
List<String> weapons;
```

is significantly easier to copy safely than a list of mutable custom objects.

### Important interview distinction

```text
String           → immutable
ArrayList        → mutable
Custom object    → may be mutable
```

The immutability of the elements matters just as much as the copying of the collection itself.

---

# 11. The screenshot's weapon modification test

The client code creates several clones:

```java
GameBot gB1 = new GameBot(
    "BOT1",
    100,
    100,
    new ArrayList<>(List.of("Sniper", "AK"))
);

GameBot gB2 = gB1.cloneable();
gB2.setName("B2");

GameBot gB3 = gB1.cloneable();
gB3.setName("B3");

GameBot gB4 = gB1.cloneable();
gB4.setName("B4");

GameBot gB5 = gB1.cloneable();
gB5.setName("B5");

GameBot gB6 = gB1.cloneable();
gB6.setName("B6");

List<String> w = gB6.getWeapons();
w.add("Pistol");
```

This is a useful demonstration of why the copy constructor uses:

```java
new ArrayList<>(g1.weapons)
```

rather than:

```java
g1.weapons
```

Because the list is independently copied, adding `"Pistol"` to `gB6`'s list does not add it to the prototype's list.

---

# 12. Getters and setters

The screenshot also shows standard getters and setters:

```java
public int getHealth() {
    return health;
}

public void setHealth(int health) {
    this.health = health;
}

public String getName() {
    return name;
}

public void setName(String name) {
    this.name = name;
}

public List<String> getWeapons() {
    return weapons;
}

public void setWeapons(List<String> weapons) {
    this.weapons = weapons;
}
```

These methods allow the client to customize a cloned object after cloning.

For example:

```java
gameBot2.setName("B2");
```

The clone keeps the original object's other state but can be modified independently.

---

# 13. toString()

The screenshot includes an overridden `toString()`:

```java
@Override
public String toString() {
    return "GameBot{" +
            "name='" + name + '\'' +
            ", health=" + health +
            ", weapons=" + weapons +
            '}';
}
```

This allows statements such as:

```java
System.out.println(gB1);
System.out.println(gB2);
```

to print the object's state rather than the default class-name/hash representation.

---

# 14. Client program and performance demonstration

The main class shown is:

```java
public class PrototypeDesignPatternLearningMain {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        // create prototype
        GameBot gB1 = new GameBot(...);

        // clone it many times
        GameBot gB2 = gB1.cloneable();
        GameBot gB3 = gB1.cloneable();
        GameBot gB4 = gB1.cloneable();
        GameBot gB5 = gB1.cloneable();
        GameBot gB6 = gB1.cloneable();

        // print objects
        System.out.println(gB1);
        System.out.println(gB2);
        System.out.println(gB3);
        System.out.println(gB4);
        System.out.println(gB5);
        System.out.println(gB6);

        long end = System.currentTimeMillis();
        System.out.println("Time Required " + (end - start));
    }
}
```

The exact argument list in the photograph is partially obscured, but the important behavior is clear: the constructor performs the expensive `Thread.sleep(2000)`, while cloning uses the lightweight copy constructor.

---

# 15. Why the example is faster

Suppose construction takes roughly 2 seconds:

```java
Thread.sleep(2000);
```

If we construct six objects independently, the simulated initialization could take roughly:

```text
6 × 2 seconds = 12 seconds
```

With Prototype:

```text
1 expensive construction + 5 cheap copies
≈ 2 seconds + copy overhead
```

This is the main reason Prototype can improve performance when object initialization is expensive.

> Note: `Thread.sleep()` is only being used in the example to simulate expensive initialization. Real applications may spend time loading files, querying data, building complex state, initializing libraries, etc.

---

# 16. Prototype vs normal object creation

## Without Prototype

```java
GameBot b1 = new GameBot(...);
GameBot b2 = new GameBot(...);
GameBot b3 = new GameBot(...);
GameBot b4 = new GameBot(...);
```

Every constructor call performs initialization again.

## With Prototype

```java
GameBot prototype = new GameBot(...);

GameBot b1 = prototype.cloneable();
GameBot b2 = prototype.cloneable();
GameBot b3 = prototype.cloneable();
GameBot b4 = prototype.cloneable();
```

Only the prototype pays the expensive initialization cost.

---

# 17. When Prototype is useful

Prototype is a good choice when:

1. Object creation is expensive.
2. Many objects share almost the same initial configuration.
3. The application needs many variants of a base object.
4. Construction requires complex setup.
5. The exact concrete type of the object may vary at runtime.
6. We want to avoid putting complicated creation logic into client code.

### Example use cases

- Game characters / game bots.
- UI components with many similar configurations.
- Document templates.
- Complex configuration objects.
- Preconfigured report objects.
- Cached objects that can be copied.
- Graphics objects or shapes.
- Machine-learning model configurations.
- Request templates.

---

# 18. Advantages

### 1. Faster object creation

Cloning can be much cheaper than repeated construction when initialization is expensive.

### 2. Avoids repeated initialization

The expensive initialization occurs once for the prototype.

### 3. Easy customization

A clone can be modified after copying:

```java
GameBot clone = prototype.cloneable();
clone.setName("B2");
```

### 4. Encapsulates copy logic

The prototype class owns the rules for making copies.

### 5. Reduces dependency on concrete constructors

Client code does not need to know all constructor parameters every time.

---

# 19. Disadvantages / things to watch out for

### 1. Copying nested objects can be tricky

The biggest issue is deciding whether to make a shallow copy or deep copy.

### 2. Mutable references can cause bugs

If a mutable field is accidentally shared, changing one clone can affect another.

### 3. Deep cloning may be expensive

A true deep copy can itself become costly when object graphs are large.

### 4. Cyclic references complicate cloning

Objects that reference each other can make deep copying more difficult.

### 5. Copy semantics must be well-defined

Every field should have a clear answer to:

```text
Should this value be copied?
Should this reference be shared?
Should a new nested object be created?
```

---

# 20. Shallow vs deep copy — interview-ready table

| Aspect | Shallow Copy | Deep Copy |
|---|---|---|
| Outer object | New | New |
| Nested mutable objects | Often shared | Independently copied |
| Memory usage | Lower | Higher |
| Copy speed | Faster | Slower |
| Isolation between objects | Lower | Higher |
| Risk of accidental shared state | Higher | Lower |
| Example for list field | `this.weapons = g1.weapons` | `this.weapons = new ArrayList<>(g1.weapons)` |

### One-line interview answer

**Shallow copy creates a new outer object but may share nested references; deep copy recursively creates independent copies of mutable nested state.**

---

# 21. Important nuance: the screenshot's copy is not fully recursive deep copy

It is worth being precise here.

This line:

```java
this.weapons = new ArrayList<>(g1.weapons);
```

creates a new list, but it does **not** clone every element in the list.

For this example that is fine because the elements are `String`, and `String` is immutable.

If the field were instead:

```java
List<Weapon> weapons;
```

and `Weapon` were mutable, then this:

```java
new ArrayList<>(g1.weapons)
```

would still share the same `Weapon` objects.

A stronger deep-copy implementation would need to create new `Weapon` objects as well.

---

# 22. Cloneable interface vs Prototype Pattern

Do not confuse these concepts.

### Java `Cloneable`

`Cloneable` is a marker interface used with Java's `Object.clone()` mechanism.

### Prototype Pattern

Prototype is a **design pattern / object-creation strategy**. It does not require Java's `Cloneable` interface.

The screenshot actually demonstrates a cleaner custom approach:

```java
public GameBot cloneable() {
    return new GameBot(this);
}
```

Therefore:

```text
Prototype Pattern ≠ Cloneable interface
```

You can implement the Prototype pattern using:

- Copy constructors.
- Factory methods.
- A custom `copy()` / `clone()` method.
- `Object.clone()` in some designs.

---

# 23. Better naming in production code

The screenshot uses:

```java
cloneable()
```

For production code, a clearer name would often be:

```java
copy()
```

or:

```java
clone()
```

or a copy constructor directly:

```java
new GameBot(existingBot)
```

For example:

```java
public GameBot copy() {
    return new GameBot(this);
}
```

This communicates intent more clearly.

---

# 24. A cleaner interview-friendly implementation

```java
import java.util.ArrayList;
import java.util.List;

public class GameBot {
    private String name;
    private int health;
    private List<String> weapons;

    public GameBot(String name, int health, List<String> weapons) {
        this.name = name;
        this.health = health;
        this.weapons = new ArrayList<>(weapons);
    }

    // Copy constructor
    private GameBot(GameBot other) {
        this.name = other.name;
        this.health = other.health;
        this.weapons = new ArrayList<>(other.weapons);
    }

    // Prototype operation
    public GameBot copy() {
        return new GameBot(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public List<String> getWeapons() {
        return weapons;
    }

    @Override
    public String toString() {
        return "GameBot{" +
                "name='" + name + '\'' +
                ", health=" + health +
                ", weapons=" + weapons +
                '}';
    }
}
```

Usage:

```java
public class Main {
    public static void main(String[] args) {
        GameBot prototype = new GameBot(
                "BOT1",
                100,
                List.of("Sniper", "AK")
        );

        GameBot bot2 = prototype.copy();
        bot2.setName("BOT2");

        GameBot bot3 = prototype.copy();
        bot3.setName("BOT3");

        bot2.getWeapons().add("Pistol");

        System.out.println(prototype);
        System.out.println(bot2);
        System.out.println(bot3);
    }
}
```

---

# 25. Prototype pattern vs Factory pattern

Both are creational patterns, but they solve different problems.

### Factory

Factory answers:

> **Which kind of object should I create?**

Example:

```java
GameBot bot = GameBotFactory.create("SNIPER");
```

### Prototype

Prototype answers:

> **I already have a configured object. Can I copy it?**

Example:

```java
GameBot bot = prototype.copy();
```

### Easy way to remember

```text
Factory   → create a new object from creation rules
Prototype → create a new object from an existing object
```

---

# 26. Prototype pattern vs Builder pattern

### Builder

Useful when constructing a complex object step-by-step:

```java
new GameBotBuilder()
    .name("BOT1")
    .health(100)
    .build();
```

### Prototype

Useful when a configured object already exists and similar objects can be copied:

```java
GameBot bot2 = bot1.copy();
```

### Memory trick

```text
Builder   → build a complex object
Prototype → duplicate an existing object
```

---

# 27. Prototype pattern checklist

When implementing Prototype, think through these questions:

```text
1. What object is expensive to construct?
2. What state should be copied?
3. Which fields are immutable?
4. Which fields are mutable?
5. Which mutable fields must be duplicated?
6. Is shallow copy sufficient?
7. Is deep copy required?
8. How will the client request a copy?
```

---

# 28. Interview questions and answers

## Q1. What is the Prototype Design Pattern?

**Answer:** Prototype is a creational design pattern in which new objects are created by copying an existing prototype object instead of constructing them from scratch.

## Q2. Why use Prototype?

**Answer:** It is useful when object creation is expensive or complex and many objects need similar initial state.

## Q3. How is Prototype implemented in Java?

**Answer:** Common approaches include copy constructors, custom `copy()`/`clone()` methods, or Java's `Object.clone()` mechanism.

## Q4. What is shallow copy?

**Answer:** A shallow copy creates a new outer object but may keep references to the same nested objects.

## Q5. What is deep copy?

**Answer:** A deep copy creates independent nested objects so modifications to the clone do not affect the original object's nested mutable state.

## Q6. Why is `new ArrayList<>(other.weapons)` used?

**Answer:** It creates a separate list object, preventing the original and cloned `GameBot` from sharing the same list structure.

## Q7. Is `new ArrayList<>(other.weapons)` a complete deep copy?

**Answer:** Not necessarily. It copies the list container but not mutable element objects. For `String` elements, this is generally sufficient because `String` is immutable.

## Q8. Does Prototype require `Cloneable`?

**Answer:** No. Prototype is a design pattern. The implementation can use a copy constructor or custom copy method without implementing Java's `Cloneable` interface.

## Q9. What is the biggest issue with Prototype?

**Answer:** Correctly copying object state, especially nested mutable objects, can be difficult.

## Q10. Why does cloning improve performance in the example?

**Answer:** The constructor simulates expensive initialization with `Thread.sleep(2000)`. The prototype pays that cost once; clones use a much cheaper copy constructor.

---

# 29. Key code to remember

### Prototype creation

```java
GameBot prototype = new GameBot(...);
```

### Copy constructor

```java
private GameBot(GameBot other) {
    this.name = other.name;
    this.health = other.health;
    this.weapons = new ArrayList<>(other.weapons);
}
```

### Clone/copy method

```java
public GameBot copy() {
    return new GameBot(this);
}
```

### Client usage

```java
GameBot bot2 = prototype.copy();
bot2.setName("BOT2");
```

---

# 30. Final mental model

```text
                 PROTOTYPE
                     │
          ┌──────────┴──────────┐
          │                     │
     Expensive object       Similar objects
          │                     │
          ▼                     ▼
       prototype ────────→ clone/copy
                               │
                    ┌──────────┼──────────┐
                    ▼          ▼          ▼
                  Bot2       Bot3       Bot4

Copy strategy:

Immutable fields  → reference/value copy is usually fine
Mutable fields    → decide whether to share or duplicate

Shallow copy      → nested references may be shared
Deep copy         → nested mutable state is independently copied
```

## One-line summary for interviews

> **Prototype is a creational design pattern that creates new objects by copying a preconfigured existing object, making it especially useful when object construction is expensive or complex.**

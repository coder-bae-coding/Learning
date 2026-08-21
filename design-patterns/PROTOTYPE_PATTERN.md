# Prototype Design Pattern

## What?
A **creational pattern** where a new object is created by **copying an existing object**.

```text
Create once → Clone → Customize
```

Use it when object creation is expensive and many similar objects are needed.

## GameBot Example

```java
public class GameBot {
    public String name;
    public int health;
    public List<String> weapons;
}
```

The constructor simulates expensive work with:

```java
Thread.sleep(2000);
```

Create one prototype:

```java
GameBot gB1 = new GameBot(...);
```

Then clone it:

```java
GameBot gB2 = gB1.cloneable();
GameBot gB3 = gB1.cloneable();
```

## Copy Constructor

```java
private GameBot(GameBot g1) {
    this.name = g1.name;
    this.health = g1.health;
    this.weapons = new ArrayList<>(g1.weapons);
}
```

## Clone Method

```java
public GameBot cloneable() {
    return new GameBot(this);
}
```

Flow:

```text
cloneable()
   ↓
new GameBot(this)
   ↓
copy constructor
   ↓
new object
```

> `cloneable()` is a custom method name here. It is not Java's `Cloneable` interface.

## Shallow vs Deep Copy

### Shallow Copy

```java
this.weapons = g1.weapons;
```

The same list is shared:

```text
Bot A ──→ List ←── Bot B
```

Changing the list can affect both bots.

### Independent Copy

```java
this.weapons = new ArrayList<>(g1.weapons);
```

Now the lists are different:

```text
Bot A ──→ List A
Bot B ──→ List B
```

Because the example uses `List<String>`, this is sufficient for the list structure because `String` is immutable.

**Important:** `new ArrayList<>(...)` copies the list, but does not recursively clone arbitrary mutable elements inside it.

## Example from Screenshot

```java
GameBot gB6 = gB1.cloneable();
gB6.setName("B6");

List<String> w = gB6.getWeapons();
w.add("Pistol");
```

Since each clone gets its own list, adding `Pistol` to `gB6` does not change `gB1`'s list.

## Why use Prototype?

If construction takes about 2 seconds:

```text
6 normal constructions  ≈ 12 sec
1 construction + clones ≈ 2 sec + copy time
```

The `Thread.sleep(2000)` only simulates expensive initialization.

## Advantages

- Avoids repeated expensive construction.
- Quickly creates similar objects.
- Copy logic stays inside the class.
- Clones can be customized.

## Main Risk

Be careful with **mutable reference fields**. Decide whether each field should be shared or copied.

## Interview Answer

**Prototype Pattern creates objects by cloning an existing object. It is useful when object creation is expensive and many similar objects are required. A copy constructor is a simple way to implement it in Java.**

### Remember

```text
Prototype = Existing object → Copy → Modify
Shallow   = references may be shared
Deep      = nested mutable state is copied independently
```
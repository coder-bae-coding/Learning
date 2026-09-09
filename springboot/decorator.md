# Decorator Design Pattern in Java

> Detailed summary and complete Java code implementation from **Decorator Design Pattern in Java | Real World Examples + Interview Questions** by *The Curious Coder*.

## 1. Core Problem & Concept Overview

### What is the Decorator Pattern?

The Decorator Pattern is a structural design pattern that allows you to attach new behaviors and responsibilities to an object dynamically at runtime without modifying the underlying class or using static inheritance.

### The Class Explosion Problem

Consider a pizza shop or hotel booking system:

- You start with a base `MargheritaPizza`.
- Customers want custom combinations: extra cheese, mushrooms, olives, cheese + mushrooms, cheese + olives + mushrooms, etc.
- **Naive inheritance:** Creating concrete classes for every permutation (`MargheritaWithCheese`, `MargheritaWithCheeseAndMushrooms`, etc.) causes an exponential class explosion (`2^n` classes for `n` toppings), making maintenance impossible.
- **Decorator solution:** Wrap the original object dynamically in layers. Each wrapper (decorator) conforms to the same interface and adds its own behavior before or after delegating to the wrapped object.

### The Golden Rule of Decorator

The abstract decorator class exhibits both:

1. **IS-A relationship:** It implements the component interface, so it can substitute the component anywhere.
2. **HAS-A relationship:** It holds a reference to a component instance of the same interface, which it wraps/decorates.

---

## 2. Implementation 1: Pizza Shop Example

### A. Component Interface — `Pizza.java`

```java
package com.example.decorator.pizza;

public interface Pizza {
    String getDescription();
    int getCost();
}
```

### B. Concrete Component — `MargheritaPizza.java`

```java
package com.example.decorator.pizza;

public class MargheritaPizza implements Pizza {

    @Override
    public String getDescription() {
        return "Margherita Pizza";
    }

    @Override
    public int getCost() {
        return 200;
    }
}
```

### C. Abstract Decorator — `PizzaDecorator.java`

The decorator has both the **IS-A** and **HAS-A** relationships:

```java
package com.example.decorator.pizza;

// "IS-A" Pizza (implements Pizza)
// AND "HAS-A" Pizza (contains Pizza reference)
public abstract class PizzaDecorator implements Pizza {

    protected Pizza pizza;

    public PizzaDecorator(Pizza pizza) {
        this.pizza = pizza;
    }
}
```

### D. Concrete Decorators — Toppings

#### `CheeseDecorator.java`

```java
package com.example.decorator.pizza;

public class CheeseDecorator extends PizzaDecorator {

    public CheeseDecorator(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Extra Cheese";
    }

    @Override
    public int getCost() {
        return pizza.getCost() + 50;
    }
}
```

#### `MushroomDecorator.java`

```java
package com.example.decorator.pizza;

public class MushroomDecorator extends PizzaDecorator {

    public MushroomDecorator(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Mushrooms";
    }

    @Override
    public int getCost() {
        return pizza.getCost() + 80;
    }
}
```

#### `OlivesDecorator.java`

```java
package com.example.decorator.pizza;

public class OlivesDecorator extends PizzaDecorator {

    public OlivesDecorator(Pizza pizza) {
        super(pizza);
    }

    @Override
    public String getDescription() {
        return pizza.getDescription() + ", Olives";
    }

    @Override
    public int getCost() {
        return pizza.getCost() + 100;
    }
}
```

### E. Client Execution — `PizzaClient.java`

```java
package com.example.decorator.pizza;

public class PizzaClient {

    public static void main(String[] args) {

        // Step 1: Base Pizza
        Pizza pizza = new MargheritaPizza();

        // Step 2: Layer decorators dynamically
        pizza = new CheeseDecorator(pizza);
        pizza = new MushroomDecorator(pizza);
        pizza = new OlivesDecorator(pizza);

        // Nested equivalent:
        // Pizza myPizza = new OlivesDecorator(
        //     new MushroomDecorator(
        //         new CheeseDecorator(new MargheritaPizza())
        //     )
        // );

        System.out.println("Description: " + pizza.getDescription());
        System.out.println("Total Cost: " + pizza.getCost());
    }
}
```

### Output

```text
Description: Margherita Pizza, Extra Cheese, Mushrooms, Olives
Total Cost: 430
```

---

## 3. Implementation 2: Hotel Room Add-ons Example

The video also demonstrates a secondary domain example modeling add-on packages to a base hotel room.

### A. Component Interface — `Room.java`

```java
package com.example.decorator.hotel;

public interface Room {
    String getDescription();
    int getCost();
}
```

### B. Base Room — `BasicRoom.java`

```java
package com.example.decorator.hotel;

public class BasicRoom implements Room {

    @Override
    public String getDescription() {
        return "Basic Hotel Room";
    }

    @Override
    public int getCost() {
        return 3000;
    }
}
```

### C. Abstract Room Decorator — `RoomDecorator.java`

```java
package com.example.decorator.hotel;

public abstract class RoomDecorator implements Room {

    protected Room room;

    public RoomDecorator(Room room) {
        this.room = room;
    }
}
```

### D. Concrete Decorators — Room Amenities

#### `BreakfastDecorator.java`

```java
package com.example.decorator.hotel;

public class BreakfastDecorator extends RoomDecorator {

    public BreakfastDecorator(Room room) {
        super(room);
    }

    @Override
    public String getDescription() {
        return room.getDescription() + " with Breakfast";
    }

    @Override
    public int getCost() {
        return room.getCost() + 500;
    }
}
```

#### `ExtraBedDecorator.java`

```java
package com.example.decorator.hotel;

public class ExtraBedDecorator extends RoomDecorator {

    public ExtraBedDecorator(Room room) {
        super(room);
    }

    @Override
    public String getDescription() {
        return room.getDescription() + " with Extra Bed";
    }

    @Override
    public int getCost() {
        return room.getCost() + 800;
    }
}
```

#### `MiniBarDecorator.java`

```java
package com.example.decorator.hotel;

public class MiniBarDecorator extends RoomDecorator {

    public MiniBarDecorator(Room room) {
        super(room);
    }

    @Override
    public String getDescription() {
        return room.getDescription() + " with Mini Bar";
    }

    @Override
    public int getCost() {
        return room.getCost() + 1000;
    }
}
```

### E. Execution — `HotelClient.java`

```java
package com.example.decorator.hotel;

public class HotelClient {

    public static void main(String[] args) {

        Room bookedRoom = new BasicRoom();

        bookedRoom = new BreakfastDecorator(bookedRoom);
        bookedRoom = new ExtraBedDecorator(bookedRoom);
        bookedRoom = new MiniBarDecorator(bookedRoom);

        System.out.println("Booking: " + bookedRoom.getDescription());
        System.out.println("Price: INR " + bookedRoom.getCost());
    }
}
```

### Output

```text
Booking: Basic Hotel Room with Breakfast with Extra Bed with Mini Bar
Price: INR 5300
```

---

## 4. Key Takeaways for Interviews & OneNote

| Point | Description |
|---|---|
| **Primary Principle** | **Open/Closed Principle (OCP):** Classes are open for extension (adding new decorators) but closed for modification. |
| **Real-world Java SDK example** | Standard Java I/O Streams (`new BufferedReader(new InputStreamReader(new FileInputStream("file.txt")))`) is the classic implementation of the Decorator pattern. |
| **Decorator vs Inheritance** | Inheritance adds behavior statically at compile time for the entire class; Decorators add behavior dynamically at runtime per object instance. |
| **Decorator vs Proxy** | A Decorator enhances or modifies behavior and descriptions; a Proxy typically controls access, caching, or lazy initialization without changing the core business result. |

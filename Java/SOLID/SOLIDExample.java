package SOLIDLearning;

import java.util.List;

/* ------------------------------------------------------------------
   S - Single Responsibility Principle
   Each class has exactly one reason to change.
   ------------------------------------------------------------------ */

// Toy only knows how to "start". Nothing else.
abstract class Toy {
    private final String name;

    protected Toy(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // public, so subclasses CAN'T narrow visibility (fixes your L issue)
    public abstract void start();
}

/* ------------------------------------------------------------------
   I - Interface Segregation Principle
   Small, focused interfaces instead of one fat "ToyFeatures" interface.
   A toy only implements what it actually needs.
   ------------------------------------------------------------------ */
interface Motorized {
    void motorStarts();
}

interface Soundable {
    void speakerStarts();
}

/* ------------------------------------------------------------------
   O - Open/Closed Principle
   ToyCar/ToyPlane extend behaviour by creating NEW classes, not by
   editing Toy.java or ToyCar.java. Adding a ToyRobot never touches
   existing tested code.
   ------------------------------------------------------------------ */

// L - Liskov Substitution Principle
// Any Toy reference can be replaced by ToyCar/ToyPlane without
// surprises: start() still means "the toy becomes active", the
// contract (public, no exceptions thrown, no missing behaviour)
// is preserved exactly as the parent promised.
class ToyCar extends Toy implements Motorized, Soundable {

    public ToyCar(String name) {
        super(name);
    }

    @Override
    public void start() {
        System.out.println(getName() + ": Car Starts");
    }

    @Override
    public void motorStarts() {
        System.out.println(getName() + ": Motor Starts");
    }

    @Override
    public void speakerStarts() {
        System.out.println(getName() + ": Speaker Starts");
    }
}

// A new toy type added WITHOUT modifying Toy, ToyCar, or the interfaces.
class ToyPlane extends Toy implements Motorized {

    public ToyPlane(String name) {
        super(name);
    }

    @Override
    public void start() {
        System.out.println(getName() + ": Plane Starts");
    }

    @Override
    public void motorStarts() {
        System.out.println(getName() + ": Propeller Spins");
    }
}

// A toy with no motor/sound at all - ISP means it's not forced
// to implement methods it doesn't need.
class ToyBlock extends Toy {

    public ToyBlock(String name) {
        super(name);
    }

    @Override
    public void start() {
        System.out.println(getName() + ": nothing happens, it's a block");
    }
}

/* ------------------------------------------------------------------
   D - Dependency Inversion Principle
   High-level module (ToyBox) depends on abstractions (Toy,
   Motorized, Soundable) - never on concrete classes like ToyCar.
   You can feed it any current or future Toy without changing ToyBox.
   ------------------------------------------------------------------ */
class ToyBox {

    private final List<Toy> toys;

    public ToyBox(List<Toy> toys) {
        this.toys = toys;
    }

    public void startAll() {
        for (Toy toy : toys) {
            toy.start();

            // depends on the Motorized abstraction, not on ToyCar
            if (toy instanceof Motorized motorized) {
                motorized.motorStarts();
            }

            // depends on the Soundable abstraction, not on ToyCar
            if (toy instanceof Soundable soundable) {
                soundable.speakerStarts();
            }
        }
    }
}

public class SOLIDExample {
    public static void main(String[] args) {
        ToyBox box = new ToyBox(List.of(
                new ToyCar("Racer"),
                new ToyPlane("Jet"),
                new ToyBlock("Cube")
        ));

        box.startAll();
    }
}

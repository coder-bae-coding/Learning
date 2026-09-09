# Observer Design Pattern

Here is the detailed summary and complete Java code implementation from **Observer Design Pattern in Java Explained | Complete Hands-On Implementation** by The Curious Coder.

## 1. Core Concept & Problem Overview

### What is the Observer Pattern?

The Observer Pattern is a behavioral design pattern that defines a **one-to-many dependency** between objects. When one object (the **Subject / Publisher**) changes state or triggers an event, all its dependent objects (the **Observers / Subscribers**) are notified and updated automatically.

### Real-World Analogy

- **Subject (Publisher):** The Curious Coder YouTube Channel
- **Observers (Subscribers):** People subscribed to the channel
- **Event:** Uploading a new video triggers an automatic notification to every subscriber in the list.

### Key Structural Rules Covered in the Video

1. **One-to-Many Relationship:** The subject keeps a dynamic collection (for example, `List<Subscriber>`) to register (subscribe) and remove (unsubscribe) observers.

2. **Push vs. Pull Model (Passing Subject Reference):** Instead of passing dozens of individual parameters (for example, title, URL, file name) inside the `update()` method, the subject passes `this` (itself). The observers then query (pull) whatever specific data fields they need.

3. **Loose Coupling via Interfaces:** Both subjects and observers should rely on abstract interfaces (`Subscriber`, `Channel`) so new subscriber types (for example, `TelegramSubscriber`, `EmailSubscriber`) can be added without modifying existing code (OCP).

---

## 2. Complete Code Implementation

### A. Observer Interface — `Subscriber.java`

```java
package com.example.observer;

public interface Subscriber {

    // Passes the subject reference so observers can extract what they need
    void update(TheCuriousCoderChannel channel);
}
```

### B. Concrete Observer — `YouTubeSubscriber.java`

```java
package com.example.observer;

public class YouTubeSubscriber implements Subscriber {

    private int id;
    private String name;

    public YouTubeSubscriber(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public void update(TheCuriousCoderChannel channel) {
        System.out.println(
                "Hello " + name + " (YouTube), new video uploaded: "
                        + channel.getVideoTitle()
                        + " [" + channel.getVideoFileName() + "]"
        );
    }
}
```

### C. Concrete Observer — `TelegramSubscriber.java`

```java
package com.example.observer;

// Demonstrating flexibility: another subscriber channel without changing the subject
public class TelegramSubscriber implements Subscriber {

    private String username;

    public TelegramSubscriber(String username) {
        this.username = username;
    }

    @Override
    public void update(TheCuriousCoderChannel channel) {
        System.out.println(
                "Push to Telegram user @" + username
                        + ": Watch " + channel.getVideoTitle()
        );
    }
}
```

### D. Subject — `TheCuriousCoderChannel.java`

```java
package com.example.observer;

import java.util.ArrayList;
import java.util.List;

public class TheCuriousCoderChannel {

    // 1-to-many relationship: Subject holds a list of generic Observers
    private List<Subscriber> subscribers = new ArrayList<>();

    // Subject state
    private String videoTitle;
    private String videoFileName;

    // Register observer
    public void subscribe(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    // Unregister observer
    public void unsubscribe(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    // Notify all registered observers
    public void notifySubscribers() {
        for (Subscriber subscriber : subscribers) {
            subscriber.update(this); // Passes 'this' so observers can query state
        }
    }

    // Business action that alters state and triggers notification
    public void uploadVideo(String videoTitle, String videoFileName) {
        this.videoTitle = videoTitle;
        this.videoFileName = videoFileName;

        System.out.println("\n--- Channel uploaded video: " + videoTitle + " ---");

        // Broadcast the update to all subscribers
        notifySubscribers();
    }

    // Getters for observers to pull data
    public String getVideoTitle() {
        return videoTitle;
    }

    public String getVideoFileName() {
        return videoFileName;
    }
}
```

### E. Main Execution / Client — `ObserverDemo.java`

```java
package com.example.observer;

public class ObserverDemo {

    public static void main(String[] args) {

        // 1. Create the Subject
        TheCuriousCoderChannel channel = new TheCuriousCoderChannel();

        // 2. Create Observers
        Subscriber sub1 = new YouTubeSubscriber(1, "Naman");
        Subscriber sub2 = new YouTubeSubscriber(2, "Harshit");
        Subscriber sub3 = new YouTubeSubscriber(3, "Manvi");
        Subscriber sub4 = new TelegramSubscriber("tech_fanatic");

        // 3. Register subscribers (establish 1-to-many relationship)
        channel.subscribe(sub1);
        channel.subscribe(sub2);
        channel.subscribe(sub3);
        channel.subscribe(sub4);

        // 4. Trigger event -> Automatically broadcasts to all 4 observers
        channel.uploadVideo(
                "Observer Pattern in Java",
                "observer_pattern.mp4"
        );

        // 5. Unsubscribe a user and trigger again
        channel.unsubscribe(sub2);
        channel.uploadVideo(
                "Decorator Pattern Deep Dive",
                "decorator_pattern.mp4"
        );
    }
}
```

### Output

```text
--- Channel uploaded video: Observer Pattern in Java ---
Hello Naman (YouTube), new video uploaded: Observer Pattern in Java
[observer_pattern.mp4]
Hello Harshit (YouTube), new video uploaded: Observer Pattern in Java
[observer_pattern.mp4]
Hello Manvi (YouTube), new video uploaded: Observer Pattern in Java
[observer_pattern.mp4]
Push to Telegram user @tech_fanatic: Watch Observer Pattern in Java

--- Channel uploaded video: Decorator Pattern Deep Dive ---
Hello Naman (YouTube), new video uploaded: Decorator Pattern Deep Dive
[decorator_pattern.mp4]
Hello Manvi (YouTube), new video uploaded: Decorator Pattern Deep Dive
[decorator_pattern.mp4]
Push to Telegram user @tech_fanatic: Watch Decorator Pattern Deep Dive
```

---

## 3. Quick Reference for Interviews & Notes

| Feature | Details |
|---|---|
| **Pattern Category** | Behavioral Design Pattern |
| **Core Intent** | Broadcast state changes from one publisher to multiple subscribers without tightly coupling them |
| **Key Relationships** | **1-to-Many:** Subject holds `List<Observer>`. **Observer Callback:** `observer.update(subject)` |
| **Real-world Java/Spring Examples** | **Spring Framework:** `ApplicationEventPublisher` and `@EventListener`  \n**Java Reactive:** RxJava / Project Reactor (`Flux` / `Mono`)  \n**GUI / AWT:** `ActionListener` on buttons (`addActionListener(...)`) |
| **Push vs Pull Trade-off** | **Push:** Passes all data in method arguments (inflexible if observers need different details).  \n**Pull:** Passes the subject reference; each observer extracts only what it needs (more decoupled & scalable). |

### Interview Mental Model

```text
Subject / Publisher
       |
       | notify()
       v
+---------------------+
|  Subscriber 1       |
|  Subscriber 2       |
|  Subscriber 3       |
|  Subscriber 4       |
+---------------------+
       |
       v
   update(subject)
```

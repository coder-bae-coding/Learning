# Proxy Design Pattern

## What is Proxy?

A **Proxy** is a structural design pattern where a substitute object controls access to a real object.

```text
Client → Proxy → Real Object
```

The Proxy and Real Object implement the **same interface**, so the client does not need to know what is happening behind the Proxy.

The Proxy can add logic such as:
- Lazy loading
- Access control
- Network communication

---

## 1. Virtual Proxy — Lazy Loading

### Purpose
Delay creation of an expensive object until it is actually needed.

### Problem
Creating a heavy object during application startup can be slow even when the object is never used.

### Flow

```text
Client
  ↓
MovieProxy          ← created immediately
  ↓ play()
Movies              ← created only on first play()
  ↓
Video loads + plays
```

### Code

```java
public interface Video {
    void play() throws InterruptedException;
}
```

```java
public class Movies implements Video {
    private String fileName;

    public Movies(String fileName) throws InterruptedException {
        this.fileName = fileName;
        loadVideo();
    }

    private void loadVideo() throws InterruptedException {
        System.out.println("Loading video from server: " + fileName);
        Thread.sleep(4000); // Simulates expensive loading
    }

    @Override
    public void play() {
        System.out.println("Playing video: " + fileName);
    }
}
```

```java
public class MovieProxy implements Video {
    private String fileName;
    private Video video;

    public MovieProxy(String fileName) {
        this.fileName = fileName;
        System.out.println("Proxy object created instantly for: " + fileName);
    }

    @Override
    public void play() throws InterruptedException {
        if (video == null) {
            video = new Movies(fileName); // Lazy initialization
        }

        video.play();
    }
}
```

### Client

```java
public class VirtualProxyDemo {
    public static void main(String[] args) throws InterruptedException {
        Video video1 = new MovieProxy("ShawshankRedemption.mp4");
        Video video2 = new MovieProxy("HeraPheri.mp4");
        Video video3 = new MovieProxy("Prestige.mp4");

        System.out.println("\n--- Playing Videos for the First Time ---");
        video1.play();
        video2.play();

        System.out.println("\n--- Playing Video Again ---");
        video1.play(); // No second loading
    }
}
```

### Key point

```java
if (video == null) {
    video = new Movies(fileName);
}
```

The heavy object is created **only once and only when required**.

---

## 2. Protection Proxy — Access Control

### Purpose
Control access to a sensitive operation based on permissions or roles.

### Problem
Unauthorized users should not be able to execute operations such as database deletion.

### Flow

```text
Client → DatabaseProxy → check role → MySQLDatabase
                         ↓
                   ADMIN? → allow
                   Other  → deny
```

### Code

```java
public interface Database {
    void delete();
}
```

```java
public class MySQLDatabase implements Database {
    @Override
    public void delete() {
        System.out.println("User record has been successfully deleted from MySQL Database.");
    }
}
```

```java
public class DatabaseProxy implements Database {
    private Database db;
    private String role;

    public DatabaseProxy(String role) {
        this.role = role;
        this.db = new MySQLDatabase();
    }

    @Override
    public void delete() {
        if ("ADMIN".equalsIgnoreCase(role)) {
            db.delete();
        } else {
            System.out.println("Access Denied! Role '" + role
                    + "' does not have permission to delete.");
        }
    }
}
```

### Client

```java
public class ProtectionProxyDemo {
    public static void main(String[] args) {
        System.out.println("--- Executing as ADMIN ---");
        Database adminProxy = new DatabaseProxy("ADMIN");
        adminProxy.delete(); // Allowed

        System.out.println("\n--- Executing as MANAGER ---");
        Database managerProxy = new DatabaseProxy("MANAGER");
        managerProxy.delete(); // Denied
    }
}
```

### Key point

The client calls the same `delete()` method, but the Proxy decides whether the real operation is allowed.

---

## 3. Remote Proxy — Network Abstraction

### Purpose
Provide a local representative for an object that exists on a remote server or external system.

### Problem
The client should not have to deal with authentication, connections, serialization, HTTP/RPC calls, etc.

### Flow

```text
Client
  ↓
WeatherProxy (local)
  ↓
Authenticate
  ↓
Open connection
  ↓
Serialize request
  ↓
HTTP/RPC call
  ↓
Remote WeatherService
```

### Remote side

```java
public interface Weather {
    String getWeather();
}
```

```java
public class WeatherService implements Weather {
    @Override
    public String getWeather() {
        return "Temperature: 28°C, Condition: Sunny";
    }
}
```

### Local Proxy

```java
public class WeatherProxy implements Weather {
    private Weather weather;

    public WeatherProxy() {
        this.weather = new WeatherService();
    }

    private void authenticate() {
        System.out.println("Step 1: Authenticating client credentials...");
    }

    private void openConnection() {
        System.out.println("Step 2: Opening secure connection to remote server...");
    }

    private void serializeRequest() {
        System.out.println("Step 3: Serializing request payload...");
    }

    private String sendHttpRequest() {
        System.out.println("Step 4: Dispatching HTTP request and receiving response...");
        return weather.getWeather();
    }

    @Override
    public String getWeather() {
        authenticate();
        openConnection();
        serializeRequest();
        return sendHttpRequest();
    }
}
```

### Client

```java
public class RemoteProxyDemo {
    public static void main(String[] args) {
        System.out.println("--- Fetching Weather via Remote Proxy ---");

        Weather weatherProxy = new WeatherProxy();
        String result = weatherProxy.getWeather();

        System.out.println("Response Received -> " + result);
    }
}
```

### Key point

The client only sees:

```java
weatherProxy.getWeather();
```

All networking-related steps are hidden inside the Proxy.

---

## Three Types — Quick Revision

| Type | Main Goal | Proxy Logic |
|---|---|---|
| **Virtual Proxy** | Performance | Delays expensive object creation |
| **Protection Proxy** | Security | Checks roles/permissions |
| **Remote Proxy** | Network abstraction | Handles communication with remote object |

## Easy way to remember

```text
Virtual    → WHEN to create?
Protection → WHO can access?
Remote     → HOW to communicate?
```

## Interview Definition

**Proxy Design Pattern provides a substitute object with the same interface as the real object and uses that substitute to control access, add behavior, or hide complexity before delegating to the real object.**

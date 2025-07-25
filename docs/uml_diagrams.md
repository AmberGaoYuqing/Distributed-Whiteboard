# Distributed Whiteboard System - UML Diagrams

## Class Diagram

```mermaid
classDiagram
    class ManagerLogin {
        -JFrame frame
        -JTextField usernameInputField
        +static Manager managerBoard
        +static String address
        +static int port
        +static String username
        +initialize()
        +main(String[] args)
        -parseArguments(String[] args)
    }

    class UserLogin {
        -JFrame frame
        -JTextField usernameInputField
        +static Connection connection
        +static Guest guestBoard
        +static Socket socket
        +static String address
        +static int port
        +static String username
        +initialize()
        +main(String[] args)
        -parseArguments(String[] args)
        -handleLogin()
        -waitForStatusUpdate()
        -handleStatusResponse()
    }

    class WhiteboardServer {
        -ServerSocket serverSocket
        -ArrayList<Connection> connections
        -String managerName
        +static void launch(int port, String username)
        -acceptConnections()
        -broadcastMessage(String message)
    }

    class Connection {
        -Socket socket
        -DataInputStream inputStream
        -DataOutputStream outputStream
        -String currentStatus
        +launch()
        +getCurrentStatus()
        +resetStatus()
    }

    class Manager {
        -Connection connection
        -String username
        -GraphicsView graphicsView
        +initialize()
        +handleMessage(String message)
    }

    class Guest {
        -Connection connection
        -String username
        -GraphicsView graphicsView
        +initialize()
        +handleMessage(String message)
    }

    class GraphicsView {
        -ArrayList<Shape> shapes
        -Color currentColor
        -float currentStroke
        +paintComponent(Graphics g)
        +addShape(Shape shape)
        +clear()
    }

    class Shape {
        <<interface>>
        +draw(Graphics2D g2d)
    }

    ManagerLogin --> Manager : creates
    UserLogin --> Guest : creates
    Manager --> Connection : uses
    Guest --> Connection : uses
    Manager --> GraphicsView : contains
    Guest --> GraphicsView : contains
    GraphicsView --> Shape : contains
    WhiteboardServer --> Connection : manages
```

## Sequence Diagrams

### Manager Login Flow

```mermaid
sequenceDiagram
    participant ML as ManagerLogin
    participant WS as WhiteboardServer
    participant M as Manager
    participant GV as GraphicsView

    ML->>ML: parseArguments()
    ML->>ML: initialize()
    ML->>ML: display login GUI
    ML->>WS: launch(port, username)
    WS->>WS: start server
    ML->>M: create Manager instance
    M->>GV: initialize GraphicsView
    M->>WS: establish connection
```

### User Login Flow

```mermaid
sequenceDiagram
    participant UL as UserLogin
    participant WS as WhiteboardServer
    participant G as Guest
    participant GV as GraphicsView

    UL->>UL: parseArguments()
    UL->>WS: connect to server
    WS-->>UL: connection established
    UL->>UL: initialize()
    UL->>UL: display login GUI
    UL->>WS: send join request
    WS-->>UL: send status
    alt Status = YES
        UL->>G: create Guest instance
        G->>GV: initialize GraphicsView
    else Status = NO
        UL->>UL: show error message
    else Status = REJECTED
        UL->>UL: show rejection message
    end
```

### Whiteboard Operation Flow

```mermaid
sequenceDiagram
    participant M as Manager
    participant WS as WhiteboardServer
    participant G as Guest
    participant GV as GraphicsView

    M->>GV: draw shape
    GV->>WS: broadcast shape data
    WS->>G: forward shape data
    G->>GV: update display
    
    G->>GV: draw shape
    GV->>WS: send shape data
    WS->>M: forward shape data
    M->>GV: update display
```

## Component Diagram

```mermaid
graph TB
    subgraph Client
        ML[ManagerLogin]
        UL[UserLogin]
        M[Manager]
        G[Guest]
        GV[GraphicsView]
    end

    subgraph Server
        WS[WhiteboardServer]
        C[Connection]
    end

    ML --> WS
    UL --> WS
    M --> C
    G --> C
    WS --> C
```

## Notes

1. **Class Relationships**:
   - ManagerLogin and UserLogin are entry points for manager and guest users
   - WhiteboardServer manages all connections and message broadcasting
   - Connection handles socket communication
   - Manager and Guest share similar structure but with different permissions
   - GraphicsView handles the drawing interface
   - Shape interface defines drawing behavior

2. **Key Interactions**:
   - Manager must start the server before guests can connect
   - All drawing operations are broadcasted to all connected clients
   - Server maintains the list of connected users
   - Connection status is managed through the Connection class

3. **Design Patterns Used**:
   - Observer Pattern: For handling drawing updates
   - Singleton Pattern: For server instance
   - Factory Pattern: For creating shapes
   - MVC Pattern: For separating UI and logic 
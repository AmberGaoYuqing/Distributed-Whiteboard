# Whiteboard System Demo Script

## 1. System Overview
"Good morning/afternoon, today I'll be demonstrating our distributed whiteboard system. This is a client-server application that enables real-time collaborative drawing. Let me walk you through its key components and features."

### 1.1 Architecture
"The system consists of three main components:
1. Server (WhiteboardServer.java)
   - Manages all client connections
   - Handles message broadcasting
   - Maintains user list

2. Manager (Manager.java)
   - Has full control over the whiteboard
   - Can manage users and files
   - Initiates the server

3. Guest (Guest.java)
   - Regular user interface
   - Can draw and chat
   - Synchronizes with other users"

## 2. Key Features Demonstration

### 2.1 User Management
"Let's start with user management. The system supports multiple users collaborating simultaneously."

#### 2.1.1 User Connection
```java
// WhiteboardServer.java
public static List<ClientConnection> clientConnections = new ArrayList<>();
public static List<String> usernames = new ArrayList<>();
```

"When a new user wants to join:
1. They send a request through Connection.java
2. The manager receives the request in ClientConnection.java
3. The manager can approve or reject the request
4. If approved, the user is added to the user list"

#### 2.1.2 User List Synchronization
```java
// ConnectionManager.java
public static void broadcastUserList() {
    String userList = "userJList:" + String.join(",", WhiteboardServer.usernames);
    broadcast(userList);
}
```

"The user list is automatically updated for all clients whenever:
- A new user joins
- A user leaves
- A user is kicked by the manager"

### 2.2 Drawing Functionality
"Now, let's look at the drawing system. We support various drawing tools:"

#### 2.2.1 Drawing Tools
- Line
- Rectangle
- Oval
- Triangle
- Free Draw
- Text
- Eraser (with multiple sizes)

#### 2.2.2 Drawing Implementation
```java
// Listener.java
public void mouseReleased(MouseEvent e) {
    endX = e.getX();
    endY = e.getY();
    prepareGraphics();
    
    if (tool == ToolType.ERASE) {
        handleEraseOperation();
    } else {
        record = buildRecordBasedOnTool(tool, startX, startY, endX, endY);
    }
    
    broadcastDrawingOperation();
}
```

### 2.3 File Management
"The manager has special file management capabilities:"

#### 2.3.1 File Operations
```java
// Manager.java
private void handleMenuAction(String cmd) {
    switch (cmd) {
        case "New":
            artCanvas.removeAll();
            listenerRecord.clearRecord();
            ConnectionManager.broadcast("new_canvas");
            break;
        case "Save": 
            saveFile(); 
            break;
        case "Open": 
            openFile(); 
            break;
    }
}
```

## 3. Technical Implementation

### 3.1 Message Broadcasting
"The core of our system is the broadcasting mechanism:"

#### 3.1.1 Broadcast Process
1. When a user draws:
   - Listener captures the event
   - Generates a drawing command
   - Calls ConnectionManager.broadcast()

2. The broadcast process:
   - Server receives the command
   - Broadcasts to all connected clients
   - Each client updates their canvas

#### 3.1.2 Broadcast Implementation
```java
// ConnectionManager.java
public static void broadcast(String message) {
    for (ClientConnection client : WhiteboardServer.clientConnections) {
        client.outputStream.writeUTF(message);
    }
}
```

### 3.2 Real-time Synchronization
"The system ensures real-time synchronization through:"

1. Drawing synchronization:
   - Each drawing command is broadcast immediately
   - All clients receive and execute the command
   - The canvas stays synchronized

2. User list synchronization:
   - When users join or leave
   - When a user is kicked
   - All clients see the updated list

3. Chat synchronization:
   - Messages are broadcast to all clients
   - Chat history is maintained

## 4. Common Questions and Answers

### 4.1 Design Decisions
Q: "Why did you choose this broadcasting mechanism?"
A: "We chose this mechanism because:
1. It ensures consistency across all clients
2. It's simple to implement and maintain
3. It provides reliable message delivery
4. It's suitable for real-time collaboration"

### 4.2 Technical Questions
Q: "How do you handle network issues?"
A: "We handle network issues through:
1. TCP protocol for reliable delivery
2. Error handling in broadcast methods
3. Connection status monitoring
4. Graceful disconnection handling"

Q: "Why are there broadcast methods in both Listener and ConnectionManager?"
A: "The Listener class contains broadcast-related methods to handle drawing-specific broadcast logic, while ConnectionManager handles the core broadcasting mechanism. This separation allows for better organization of drawing-specific broadcast logic while maintaining the central broadcasting functionality in ConnectionManager."

### 4.3 Performance Questions
Q: "How does the system handle multiple simultaneous users?"
A: "The system handles multiple users through:
1. Multi-threaded client connections
2. Efficient message broadcasting
3. Optimized drawing operations
4. Proper resource management"

## 5. Conclusion
"In conclusion, our system provides:
1. Real-time collaboration
2. Reliable message delivery
3. User management
4. File operations
5. Drawing tools

While there's room for optimization, the current implementation meets all requirements and provides a stable platform for collaborative drawing."

## 6. Demo Flow
1. Start the server and manager
2. Demonstrate user connection
3. Show drawing tools and synchronization
4. Demonstrate file operations
5. Show chat functionality
6. Demonstrate user management
7. Handle any questions

## 7. Troubleshooting Guide
1. If a client can't connect:
   - Check server is running
   - Verify port number
   - Check network connection

2. If drawing isn't synchronized:
   - Check client connection
   - Verify broadcast messages
   - Check for error messages

3. If file operations fail:
   - Check file permissions
   - Verify file path
   - Check for error messages 
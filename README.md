# IK1203 — Networking and Communication (Socket Programming)

This repository contains the programming assignments for the **IK1203 Networks and Communication** course at **KTH Royal Institute of Technology**.  
The project focuses on implementing **TCP** and **HTTP** communication using **Java sockets**.

---

## Tasks Overview

### Task 1 — TCPAsk
Implements a simple **TCP client** that connects to a server, sends a message, and prints the response.  
**Files:** `TCPClient.java`, `TCPAsk.java`

### Task 2 — Concurrent TCPAsk
Extends the client to handle **multiple concurrent connections** using threads.  
**Files:** `TCPClient.java`, `MyRunnable.java`

### Task 3 — HTTPAsk
Implements a basic **HTTP server** that interprets HTTP GET requests and returns responses.  
**Files:** `HTTPAsk.java`

### Task 4 — Concurrent HTTPAsk
Improves the HTTP server to support **parallel client handling** and **better error management**.  
**Files:** `ConcHTTPAsk.java`, `MyRunnable.java`

---

## How to Run

Example for Task 1:
```bash
javac TCPClient.java TCPAsk.java
java TCPAsk <host> <port> <message>
```
---

## 🧑‍💻 Author
**Aleena Amir**

KTH Royal Institute of Technology

📧 aaamir@kth.se

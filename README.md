# 📅 Task Scheduler — Java

A deadline-based task scheduling application built in Java with a Swing GUI. Tasks are automatically prioritized using a dynamic scoring algorithm that factors in both **priority level** and **time urgency**.

## ✨ Features

- **Deadline-aware prioritization** — tasks are ranked using a weighted score of priority × urgency multiplier
- **Four priority levels** — Low, Medium, High, Critical
- **Three task statuses** — Pending, In Progress, Completed
- **Urgency labels** — Overdue, < 1 Hour, < 6 Hours, < 24 Hours, < 3 Days, On Track
- **Persistent storage** — tasks are saved to disk (`tasks.dat`) and reloaded on restart
- **Swing GUI** — full graphical user interface with add, edit, delete, and status-toggle actions
- **Color-coded table** — rows highlighted by urgency and overdue state

## 🗂️ Project Structure

```
Task Scheduler/
├── src/
│   ├── App.java                  # Entry point — launches the Swing UI
│   ├── model/
│   │   ├── Task.java             # Task entity with scoring & urgency logic
│   │   ├── Priority.java         # Priority enum (LOW → CRITICAL)
│   │   └── Status.java           # Status enum (PENDING, IN_PROGRESS, COMPLETED)
│   ├── scheduler/
│   │   └── TaskScheduler.java    # Priority-queue scheduler + file persistence
│   └── ui/
│       ├── TaskSchedulerUI.java  # Main Swing window
│       ├── TaskDialog.java       # Add / Edit task dialog
│       └── RoundedBorder.java    # Custom UI border component
├── tasks.dat                     # Serialized task storage (auto-generated)
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java 11+
- VS Code with the [Java Extension Pack](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)

### Running the Application

1. Open the project folder in VS Code
2. Press **F5** or click **Run → Run Without Debugging**
3. The GUI will launch automatically

### Building Manually

```bash
javac -d bin src/**/*.java src/App.java
java -cp bin App
```

## 🧠 How Prioritization Works

Each task gets an **effective score** = `priority score × urgency multiplier`

| Urgency Window | Multiplier |
|----------------|-----------|
| Overdue        | ×10       |
| < 1 Hour       | ×8        |
| < 6 Hours      | ×5        |
| < 24 Hours     | ×3        |
| < 3 Days       | ×2        |
| On Track       | ×1        |

Tasks with the highest score float to the top of the queue.

## 💾 Persistence

Tasks are serialized using Java's built-in `ObjectOutputStream` and stored in `tasks.dat` in the project root. On startup, the file is deserialized and the task counter is set to avoid ID collisions.

## 📄 License

MIT

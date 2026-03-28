# 📅 Task Scheduler — Java

A deadline-based task scheduling application built in Java with a Swing GUI.  
Tasks are automatically prioritized using a dynamic scoring algorithm that factors in both **priority level** and **time urgency** — so the most critical, time-sensitive task is always at the top.

---

## 📸 What It Looks Like

The application opens a graphical window showing all your tasks sorted by urgency. Rows are color-coded — red for overdue, orange for tasks due soon, green for completed.

---

## ✨ Features

- **Deadline-aware prioritization** — tasks are ranked using a weighted score of `priority × urgency multiplier`
- **Four priority levels** — Low, Medium, High, Critical
- **Three task statuses** — Pending, In Progress, Completed
- **Urgency labels** — Overdue, < 1 Hour, < 6 Hours, < 24 Hours, < 3 Days, On Track
- **Persistent storage** — tasks are saved to disk (`tasks.dat`) and reloaded automatically on restart
- **Swing GUI** — full graphical interface with Add, Edit, Delete, and Toggle Status actions
- **Color-coded table** — rows highlighted by urgency and completion state

---

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
├── tasks.dat                     # Serialized task storage (auto-generated at runtime)
└── README.md
```

---

## 🚀 Getting Started

Follow these steps exactly — no prior setup is assumed.

### Step 1 — Install Java

1. Go to: https://www.oracle.com/java/technologies/downloads/
2. Download the **JDK 17** (or any version 11+) installer for your operating system
3. Run the installer and follow the prompts
4. Verify the installation by opening a terminal and running:
   ```
   java -version
   ```
   You should see something like `java version "17.x.x"`.

### Step 2 — Install VS Code (recommended)

1. Download VS Code from: https://code.visualstudio.com/
2. Install the **Java Extension Pack** inside VS Code:
   - Open VS Code → press `Ctrl+Shift+X`
   - Search for `Java Extension Pack` by Microsoft
   - Click Install

### Step 3 — Get the Code

Open a terminal (Command Prompt or PowerShell on Windows) and run:

```bash
git clone https://github.com/Hardik115/Java-Project.git
cd "Java-Project/Task Scheduler"
```

> If you don't have Git installed, download it from: https://git-scm.com/download/win

### Step 4 — Run the Application

**Option A — Using VS Code (easiest):**
1. Open VS Code
2. Click `File → Open Folder` and select the `Task Scheduler` folder
3. Press **F5** or go to `Run → Run Without Debugging`
4. The GUI window will launch

**Option B — Using the terminal (Windows):**
```cmd
cd "Task Scheduler"
mkdir bin
javac -d bin src\App.java src\model\*.java src\scheduler\*.java src\ui\*.java
java -cp bin App
```

**Option B — Using the terminal (macOS / Linux):**
```bash
mkdir -p bin
javac -d bin src/App.java src/model/*.java src/scheduler/*.java src/ui/*.java
java -cp bin App
```

---

## 🖥️ How to Use the Application

Once the app launches, you'll see the main task dashboard.

### Adding a Task
1. Click the **➕ Add Task** button in the toolbar
2. Fill in:
   - **Name** — a short title for the task
   - **Description** — optional details
   - **Deadline** — pick a date and time using the date/time spinners
   - **Priority** — choose Low, Medium, High, or Critical
3. Click **Save** — the task appears in the list, sorted by urgency

### Editing a Task
1. Click on a task row to select it
2. Click the **✏️ Edit** button
3. Update any fields and click **Save**

### Deleting a Task
1. Select a task row
2. Click the **🗑️ Delete** button and confirm

### Changing Task Status
1. Select a task row
2. Click **Toggle Status** to cycle through: `Pending → In Progress → Completed`

### Understanding the Colors
| Row Color | Meaning |
|-----------|---------|
| 🔴 Red | Task is **overdue** |
| 🟠 Orange/Yellow | Due within **6 hours** |
| 🟢 Green | Task is **completed** |
| White/Default | Normal, on track |

The list **auto-refreshes every minute** so urgency labels and row colors stay up to date without any action from you.

---

## 🧠 How Prioritization Works

Each task gets an **effective score** = `priority score × urgency multiplier`

| Priority | Score |
|----------|-------|
| Low      | 1     |
| Medium   | 2     |
| High     | 3     |
| Critical | 4     |

| Urgency Window | Multiplier |
|----------------|-----------|
| Overdue        | ×10       |
| < 1 Hour       | ×8        |
| < 6 Hours      | ×5        |
| < 24 Hours     | ×3        |
| < 3 Days       | ×2        |
| On Track       | ×1        |

**Example:** A `HIGH` priority task due in 30 minutes scores `3 × 8 = 24`, outranking a `CRITICAL` task due in 2 days which scores `4 × 2 = 8`.

Tasks are always shown highest score first.

---

## 💾 Data Persistence

Your tasks are automatically saved every time you add, edit, delete, or change the status of a task. The data is stored in a file called `tasks.dat` in the project root using Java serialization.

- **No database required** — everything is stored locally in one file
- On next launch, tasks are restored exactly as you left them
- If `tasks.dat` is deleted or missing, the app starts with an empty task list (no crash)

---

## 🛠️ Troubleshooting

| Problem | Solution |
|---------|----------|
| `javac` not found | Java is not installed or not on your PATH — redo Step 1 |
| `git` not found | Install Git from https://git-scm.com/download/win |
| App window doesn't open | Make sure you compiled with all source files listed in Step 4 |
| Tasks not saving | Ensure the app has write permission in the project folder |

---

## 📄 License

MIT — free to use, modify, and distribute.

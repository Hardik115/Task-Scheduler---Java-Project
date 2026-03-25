package ui;

import model.Priority;
import model.Status;
import model.Task;
import scheduler.TaskScheduler;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

public class TaskSchedulerUI extends JFrame {

    // ── Palette ──────────────────────────────────────────────────────────────
    static final Color BG_PRIMARY   = new Color(12, 12, 26);
    static final Color BG_SECONDARY = new Color(20, 20, 40);
    static final Color BG_CARD      = new Color(28, 28, 55);
    static final Color BG_ROW_EVEN  = new Color(22, 22, 45);
    static final Color BG_ROW_ODD   = new Color(18, 18, 38);
    static final Color BG_HEADER    = new Color(15, 15, 35);
    static final Color ACCENT       = new Color(124, 58, 237);
    static final Color ACCENT_LIGHT = new Color(167, 118, 255);
    static final Color ACCENT_HOVER = new Color(109, 40, 217);
    static final Color TEXT_PRI     = new Color(226, 232, 240);
    static final Color TEXT_SEC     = new Color(148, 163, 184);
    static final Color TEXT_DIM     = new Color(100, 116, 139);
    static final Color BORDER_CLR   = new Color(55, 55, 85);
    static final Color CLR_CRITICAL = new Color(239, 68, 68);
    static final Color CLR_HIGH     = new Color(249, 115, 22);
    static final Color CLR_MEDIUM   = new Color(234, 179, 8);
    static final Color CLR_LOW      = new Color(34, 197, 94);
    static final Color CLR_OVERDUE  = new Color(220, 38, 38);
    static final Color CLR_PROGRESS = new Color(59, 130, 246);
    static final Color CLR_DONE     = new Color(107, 114, 128);
    static final Color CLR_PENDING  = new Color(251, 191, 36);
    static final Color CLR_SUCCESS  = new Color(16, 185, 129);

    static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    static final String SAVE_FILE = System.getProperty("user.home") + java.io.File.separator + "task_scheduler_data.dat";

    // ── State ────────────────────────────────────────────────────────────────
    private final TaskScheduler scheduler = new TaskScheduler();
    private TaskTableModel tableModel;
    private JTable taskTable;
    private JLabel lblTotal, lblPending, lblInProg, lblCompleted, lblOverdue;
    private JComboBox<String> filterCombo;
    private JTextField searchField;

    // ── Constructor ──────────────────────────────────────────────────────────
    public TaskSchedulerUI() {
        setTitle("⚡ Task Scheduler — Deadline Priority Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 800);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_PRIMARY);
        setContentPane(root);

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCenter(),  BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);

        loadOrDemo();
        refreshTable();
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(45, 20, 95), getWidth(), 0, new Color(20, 20, 60)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_CLR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        hdr.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 3));
        left.setOpaque(false);
        JLabel title = new JLabel("⚡  Task Scheduler");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(TEXT_PRI);
        JLabel sub = new JLabel("Deadline-driven priority management");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(ACCENT_LIGHT);
        left.add(title); left.add(sub);

        JButton nextBtn = styledButton("🎯  Next Priority Task", ACCENT, ACCENT_HOVER, TEXT_PRI);
        nextBtn.addActionListener(e -> showNextTask());

        hdr.add(left, BorderLayout.WEST);
        hdr.add(nextBtn, BorderLayout.EAST);
        return hdr;
    }

    // ── Center ───────────────────────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel c = new JPanel(new BorderLayout(0, 14));
        c.setBackground(BG_PRIMARY);
        c.setBorder(BorderFactory.createEmptyBorder(18, 22, 10, 22));
        c.add(buildStats(),     BorderLayout.NORTH);
        c.add(buildTableArea(), BorderLayout.CENTER);
        return c;
    }

    // ── Stats Cards ──────────────────────────────────────────────────────────
    private JPanel buildStats() {
        JPanel p = new JPanel(new GridLayout(1, 5, 14, 0));
        p.setOpaque(false);
        lblTotal     = statCard(p, "Total Tasks",   "0", TEXT_PRI);
        lblPending   = statCard(p, "Pending",        "0", CLR_PENDING);
        lblInProg    = statCard(p, "In Progress",    "0", CLR_PROGRESS);
        lblCompleted = statCard(p, "Completed",      "0", CLR_SUCCESS);
        lblOverdue   = statCard(p, "Overdue ⚠",     "0", CLR_OVERDUE);
        return p;
    }

    private JLabel statCard(JPanel parent, String title, String val, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(0, 0, 5, getHeight(), 5, 5));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 14));

        JLabel tl = new JLabel(title);
        tl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tl.setForeground(TEXT_SEC);

        JLabel vl = new JLabel(val);
        vl.setFont(new Font("Segoe UI", Font.BOLD, 30));
        vl.setForeground(accent);

        card.add(tl, BorderLayout.NORTH);
        card.add(vl, BorderLayout.CENTER);
        parent.add(card);
        return vl;
    }

    // ── Table Area ───────────────────────────────────────────────────────────
    private JPanel buildTableArea() {
        JPanel area = new JPanel(new BorderLayout(0, 12));
        area.setOpaque(false);
        area.add(buildToolbar(),  BorderLayout.NORTH);
        area.add(buildTable(),    BorderLayout.CENTER);
        area.add(buildActions(),  BorderLayout.SOUTH);
        return area;
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        filterCombo = new JComboBox<>(new String[]{"All Tasks", "Pending", "In Progress", "Completed"});
        styleCombo(filterCombo);
        filterCombo.setPreferredSize(new Dimension(160, 38));
        filterCombo.addActionListener(e -> refreshTable());

        searchField = darkTextField("🔍  Search tasks by name or description...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { refreshTable(); }
            public void removeUpdate(DocumentEvent e)  { refreshTable(); }
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(filterCombo);

        bar.add(left, BorderLayout.WEST);
        bar.add(searchField, BorderLayout.CENTER);
        return bar;
    }

    private JScrollPane buildTable() {
        tableModel = new TaskTableModel();
        taskTable  = new JTable(tableModel);

        taskTable.setBackground(BG_ROW_EVEN);
        taskTable.setForeground(TEXT_PRI);
        taskTable.setSelectionBackground(new Color(124, 58, 237, 70));
        taskTable.setSelectionForeground(TEXT_PRI);
        taskTable.setGridColor(new Color(40, 40, 65));
        taskTable.setRowHeight(48);
        taskTable.setShowVerticalLines(false);
        taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taskTable.setFillsViewportHeight(true);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader th = taskTable.getTableHeader();
        th.setBackground(BG_HEADER);
        th.setForeground(TEXT_SEC);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_CLR));
        th.setPreferredSize(new Dimension(0, 44));
        th.setReorderingAllowed(false);

        int[] widths = {50, 210, 270, 100, 170, 120, 120};
        for (int i = 0; i < widths.length; i++)
            taskTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        taskTable.setDefaultRenderer(Object.class, new RowRenderer());
        taskTable.getColumnModel().getColumn(3).setCellRenderer(new PriorityRenderer());
        taskTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        taskTable.getColumnModel().getColumn(6).setCellRenderer(new UrgencyRenderer());

        taskTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showEditDialog();
            }
        });

        JScrollPane sp = new JScrollPane(taskTable);
        sp.setBorder(new RoundedBorder(12, BORDER_CLR));
        sp.getViewport().setBackground(BG_ROW_EVEN);
        return sp;
    }

    private JPanel buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        p.setOpaque(false);

        JButton add   = styledButton("➕  Add Task",       CLR_SUCCESS,           new Color(5, 150, 105),  TEXT_PRI);
        JButton start = styledButton("▶  Start Task",      CLR_PROGRESS,          new Color(37, 99, 235),  TEXT_PRI);
        JButton edit  = styledButton("✏  Edit Task",       new Color(60, 60, 100), new Color(45, 45, 80),  TEXT_PRI);
        JButton done  = styledButton("✔  Mark Complete",   new Color(20, 80, 55),  new Color(10, 60, 40),  CLR_SUCCESS);
        JButton del   = styledButton("🗑  Delete Task",     new Color(80, 20, 20),  new Color(60, 10, 10),  CLR_CRITICAL);

        add.addActionListener(e   -> showAddDialog());
        start.addActionListener(e -> updateStatus(Status.IN_PROGRESS));
        edit.addActionListener(e  -> showEditDialog());
        done.addActionListener(e  -> updateStatus(Status.COMPLETED));
        del.addActionListener(e   -> deleteSelected());

        p.add(add); p.add(start); p.add(edit); p.add(done); p.add(del);
        return p;
    }

    // ── Footer ───────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel f = new JPanel(new BorderLayout());
        f.setBackground(BG_SECONDARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 22, 8, 22)));

        JLabel info = new JLabel("Tasks ranked by: Priority Score × Urgency Multiplier (overdue=10×, <1h=8×, <6h=5×, <24h=3×, <3d=2×)");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        info.setForeground(TEXT_DIM);

        JLabel ver = new JLabel("Task Scheduler v1.0");
        ver.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ver.setForeground(TEXT_DIM);

        f.add(info, BorderLayout.WEST);
        f.add(ver,  BorderLayout.EAST);
        return f;
    }

    // ── Actions ──────────────────────────────────────────────────────────────
    private void refreshTable() {
        String filter = (String) filterCombo.getSelectedItem();
        String kw = searchField.getText().trim();

        List<Task> tasks;
        if (!kw.isEmpty() && !kw.startsWith("🔍")) {
            tasks = scheduler.search(kw);
        } else {
            switch (filter == null ? "All Tasks" : filter) {
                case "Pending":     tasks = scheduler.getTasksByStatus(Status.PENDING);     break;
                case "In Progress": tasks = scheduler.getTasksByStatus(Status.IN_PROGRESS); break;
                case "Completed":   tasks = scheduler.getTasksByStatus(Status.COMPLETED);   break;
                default:            tasks = scheduler.getAllTasksSorted();
            }
        }
        tableModel.setTasks(tasks);
        updateStats();
    }

    private void updateStats() {
        lblTotal.setText(String.valueOf(scheduler.getTotalCount()));
        lblPending.setText(String.valueOf(scheduler.getPendingCount()));
        lblInProg.setText(String.valueOf(scheduler.getInProgressCount()));
        lblCompleted.setText(String.valueOf(scheduler.getCompletedCount()));
        lblOverdue.setText(String.valueOf(scheduler.getOverdueCount()));
    }

    private void showNextTask() {
        java.util.Optional<Task> opt = scheduler.getNextTask();
        if (opt.isPresent()) {
            Task t = opt.get();
            JOptionPane.showMessageDialog(this, buildDetailPanel(t),
                    "🎯  Next Priority Task", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No active tasks in the queue!",
                    "Queue Empty", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAddDialog() {
        TaskDialog d = new TaskDialog(this, "Add New Task", null);
        d.setVisible(true);
        if (d.isConfirmed()) {
            scheduler.addTask(d.getTaskName(), d.getDesc(), d.getDeadline(), d.getPriority());
            save();
            refreshTable();
        }
    }

    private void showEditDialog() {
        int row = taskTable.getSelectedRow();
        if (row < 0) { warn("Please select a task to edit."); return; }
        Task t = tableModel.getTaskAt(row);
        TaskDialog d = new TaskDialog(this, "Edit Task", t);
        d.setVisible(true);
        if (d.isConfirmed()) {
            scheduler.updateTask(t.getId(), d.getTaskName(), d.getDesc(), d.getDeadline(), d.getPriority());
            save();
            refreshTable();
        }
    }

    private void updateStatus(Status s) {
        int row = taskTable.getSelectedRow();
        if (row < 0) { warn("Please select a task first."); return; }
        scheduler.setStatus(tableModel.getTaskAt(row).getId(), s);
        save();
        refreshTable();
    }

    private void deleteSelected() {
        int row = taskTable.getSelectedRow();
        if (row < 0) { warn("Please select a task to delete."); return; }
        Task t = tableModel.getTaskAt(row);
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete \"" + t.getName() + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok == JOptionPane.YES_OPTION) { scheduler.removeTask(t.getId()); save(); refreshTable(); }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    // ── Detail Panel ─────────────────────────────────────────────────────────
    private JPanel buildDetailPanel(Task t) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        p.setPreferredSize(new Dimension(400, 230));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 4, 5, 4);
        g.anchor = GridBagConstraints.WEST;

        addRow(p, g, 0, "Task",        t.getName(),                TEXT_PRI,                   Font.BOLD);
        addRow(p, g, 1, "Priority",    t.getPriority().toString(), priorityColor(t.getPriority()), Font.BOLD);
        addRow(p, g, 2, "Deadline",    t.getFormattedDeadline(),   t.isOverdue() ? CLR_OVERDUE : TEXT_PRI, Font.PLAIN);
        addRow(p, g, 3, "Urgency",     t.getUrgencyLabel(),        urgencyColor(t.getUrgencyLabel()), Font.BOLD);
        addRow(p, g, 4, "Status",      t.getStatus().toString(),   statusColor(t.getStatus()),  Font.PLAIN);
        if (!t.getDescription().isEmpty())
            addRow(p, g, 5, "Notes",   t.getDescription(),         TEXT_SEC,                   Font.PLAIN);
        return p;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String lbl, String val, Color c, int style) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(lbl + ":  ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_SEC);
        p.add(l, g);
        g.gridx = 1; g.weightx = 1;
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", style, 13));
        v.setForeground(c);
        p.add(v, g);
    }

    // ── Persistence ────────────────────────────────────────────────────────────
    private void save() {
        scheduler.save(SAVE_FILE);
    }

    private void loadOrDemo() {
        boolean loaded = scheduler.load(SAVE_FILE);
        if (!loaded) loadDemoTasks(); // first run: seed with sample data
    }

    // ── Demo Data ────────────────────────────────────────────────────────────
    private void loadDemoTasks() {
        LocalDateTime now = LocalDateTime.now();
        scheduler.addTask("Fix critical login bug",    "Users can't login with special chars in password", now.plusHours(2),   Priority.CRITICAL);
        scheduler.addTask("Write unit tests",          "Cover payment module with JUnit 5",                now.plusHours(18),  Priority.HIGH);
        scheduler.addTask("Code review PR #47",        "Review cart feature pull request",                 now.plusHours(5),   Priority.MEDIUM);
        scheduler.addTask("Deploy to staging",         "Deploy latest build to staging environment",       now.plusDays(1),    Priority.HIGH);
        scheduler.addTask("DB schema migration",       "Run v2.0 migration scripts on production",         now.minusHours(1),  Priority.CRITICAL);
        scheduler.addTask("Update README docs",        "Add installation and usage instructions",          now.plusDays(5),    Priority.LOW);
    }

    // ── Color Helpers ────────────────────────────────────────────────────────
    static Color priorityColor(Priority p) {
        switch (p) {
            case CRITICAL: return CLR_CRITICAL;
            case HIGH:     return CLR_HIGH;
            case MEDIUM:   return CLR_MEDIUM;
            default:       return CLR_LOW;
        }
    }

    static Color statusColor(Status s) {
        switch (s) {
            case PENDING:     return CLR_PENDING;
            case IN_PROGRESS: return CLR_PROGRESS;
            default:          return CLR_DONE;
        }
    }

    static Color urgencyColor(String u) {
        switch (u) {
            case "OVERDUE":    return CLR_OVERDUE;
            case "< 1 Hour":  return CLR_CRITICAL;
            case "< 6 Hours": return CLR_HIGH;
            case "< 24 Hours":return CLR_MEDIUM;
            case "< 3 Days":  return CLR_PROGRESS;
            case "Done":      return CLR_DONE;
            default:          return CLR_LOW;
        }
    }

    // ── UI Helpers ───────────────────────────────────────────────────────────
    private JButton styledButton(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(BG_CARD);
        cb.setForeground(TEXT_PRI);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(new RoundedBorder(8, BORDER_CLR));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setBackground(s ? ACCENT : BG_CARD);
                setForeground(TEXT_PRI);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    private JTextField darkTextField(String placeholder) {
        JTextField tf = new JTextField(placeholder) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setOpaque(false);
        tf.setForeground(TEXT_SEC);
        tf.setCaretColor(TEXT_PRI);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(10, BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (tf.getText().startsWith("🔍")) { tf.setText(""); tf.setForeground(TEXT_PRI); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (tf.getText().trim().isEmpty()) { tf.setText(placeholder); tf.setForeground(TEXT_SEC); }
            }
        });
        return tf;
    }

    // ══ Table Model ══════════════════════════════════════════════════════════
    static class TaskTableModel extends AbstractTableModel {
        private final String[] COLS = {"#", "Task Name", "Description", "Priority", "Deadline", "Status", "Urgency"};
        private List<Task> tasks = new ArrayList<>();

        void setTasks(List<Task> list) { this.tasks = new ArrayList<>(list); fireTableDataChanged(); }
        Task getTaskAt(int row)        { return tasks.get(row); }

        @Override public int getRowCount()               { return tasks.size(); }
        @Override public int getColumnCount()            { return COLS.length; }
        @Override public String getColumnName(int c)     { return COLS[c]; }

        @Override public Object getValueAt(int row, int col) {
            Task t = tasks.get(row);
            switch (col) {
                case 0: return t.getId();
                case 1: return t.getName();
                case 2: return t.getDescription();
                case 3: return t.getPriority();
                case 4: return t.getFormattedDeadline();
                case 5: return t.getStatus();
                case 6: return t.getUrgencyLabel();
                default: return "";
            }
        }
    }

    // ══ Cell Renderers ═══════════════════════════════════════════════════════
    class RowRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(sel ? new Color(124, 58, 237, 60) : (row % 2 == 0 ? BG_ROW_EVEN : BG_ROW_ODD));
            setForeground(TEXT_PRI);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            return this;
        }
    }

    class PriorityRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(sel ? new Color(124, 58, 237, 60) : (row % 2 == 0 ? BG_ROW_EVEN : BG_ROW_ODD));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            if (v instanceof Priority) setForeground(priorityColor((Priority) v));
            return this;
        }
    }

    class StatusRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(sel ? new Color(124, 58, 237, 60) : (row % 2 == 0 ? BG_ROW_EVEN : BG_ROW_ODD));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            if (v instanceof Status) setForeground(statusColor((Status) v));
            return this;
        }
    }

    class UrgencyRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, foc, row, col);
            setBackground(sel ? new Color(124, 58, 237, 60) : (row % 2 == 0 ? BG_ROW_EVEN : BG_ROW_ODD));
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            String s = v == null ? "" : v.toString();
            setForeground(urgencyColor(s));
            return this;
        }
    }
}

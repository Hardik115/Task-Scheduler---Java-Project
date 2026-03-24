package ui;

import model.Priority;
import model.Task;

import javax.swing.*;
import java.awt.*;

import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static ui.TaskSchedulerUI.*;

public class TaskDialog extends JDialog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private boolean confirmed = false;
    private JTextField nameField;
    private JTextArea  descArea;
    private JTextField deadlineField;
    private JComboBox<Priority> priorityCombo;

    public TaskDialog(JFrame parent, String title, Task existingTask) {
        super(parent, title, true);
        setSize(520, 480);
        setResizable(false);
        setLocationRelativeTo(parent);
        setUndecorated(true);
        getRootPane().setBorder(new RoundedBorder(14, BORDER_CLR));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
            }
        };
        root.setOpaque(false);
        setContentPane(root);

        root.add(buildTitleBar(title), BorderLayout.NORTH);
        root.add(buildForm(existingTask), BorderLayout.CENTER);
        root.add(buildButtons(), BorderLayout.SOUTH);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────
    private JPanel buildTitleBar(String title) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0, new Color(40,20,90), getWidth(),0, new Color(20,20,60)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(BORDER_CLR);
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        p.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 16));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setForeground(TEXT_PRI);

        JButton close = new JButton("✕");
        close.setForeground(TEXT_SEC);
        close.setFont(new Font("Segoe UI", Font.BOLD, 14));
        close.setOpaque(false);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dispose());

        p.add(lbl, BorderLayout.WEST);
        p.add(close, BorderLayout.EAST);
        return p;
    }

    // ── Form ──────────────────────────────────────────────────────────────────
    private JPanel buildForm(Task t) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 0, 6, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        // Task Name
        g.gridx = 0; g.gridy = 0; g.gridwidth = 1;
        p.add(formLabel("Task Name *"), g);
        nameField = darkField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g.gridy = 1;
        p.add(nameField, g);

        // Description
        g.gridy = 2;
        p.add(formLabel("Description"), g);
        descArea = new JTextArea(3, 30);
        descArea.setBackground(BG_CARD);
        descArea.setForeground(TEXT_PRI);
        descArea.setCaretColor(TEXT_PRI);
        descArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        JScrollPane sc = new JScrollPane(descArea);
        sc.setBorder(BorderFactory.createEmptyBorder());
        sc.getViewport().setBackground(BG_CARD);
        g.gridy = 3;
        p.add(sc, g);

        // Deadline
        g.gridy = 4;
        p.add(formLabel("Deadline  (yyyy-MM-dd HH:mm) *"), g);
        deadlineField = darkField();
        g.gridy = 5;
        p.add(deadlineField, g);

        // Priority
        g.gridy = 6;
        p.add(formLabel("Priority *"), g);
        priorityCombo = new JComboBox<>(Priority.values());
        styleCombo(priorityCombo);
        g.gridy = 7;
        p.add(priorityCombo, g);

        // Pre-fill if editing
        if (t != null) {
            nameField.setText(t.getName());
            descArea.setText(t.getDescription());
            deadlineField.setText(t.getDeadline().format(FMT));
            priorityCombo.setSelectedItem(t.getPriority());
        }
        return p;
    }

    // ── Buttons ───────────────────────────────────────────────────────────────
    private JPanel buildButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CLR));

        JButton cancel = dialogButton("Cancel", BG_CARD, new Color(45, 45, 80), TEXT_SEC);
        cancel.addActionListener(e -> dispose());

        JButton save = dialogButton("  Save Task  ", ACCENT, ACCENT_HOVER, TEXT_PRI);
        save.addActionListener(e -> onSave());

        p.add(cancel);
        p.add(save);
        return p;
    }

    private void onSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { warn("Task name cannot be empty."); return; }

        String dlStr = deadlineField.getText().trim();
        try {
            LocalDateTime.parse(dlStr, FMT); // validate format
        } catch (DateTimeParseException ex) {
            warn("Invalid deadline format. Use: yyyy-MM-dd HH:mm\nExample: 2026-04-01 14:30");
            return;
        }

        confirmed = true;
        dispose();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public boolean isConfirmed()    { return confirmed; }
    public String getTaskName()     { return nameField.getText().trim(); }
    public String getDesc()         { return descArea.getText().trim(); }
    public Priority getPriority()   { return (Priority) priorityCombo.getSelectedItem(); }
    public LocalDateTime getDeadline() {
        try { return LocalDateTime.parse(deadlineField.getText().trim(), FMT); }
        catch (DateTimeParseException e) { return LocalDateTime.now().plusDays(1); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(TEXT_SEC);
        return l;
    }

    private JTextField darkField() {
        JTextField tf = new JTextField();
        tf.setBackground(BG_CARD);
        tf.setForeground(TEXT_PRI);
        tf.setCaretColor(TEXT_PRI);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER_CLR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return tf;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(BG_CARD);
        cb.setForeground(TEXT_PRI);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(new RoundedBorder(8, BORDER_CLR));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> l, Object v, int i, boolean s, boolean f) {
                super.getListCellRendererComponent(l, v, i, s, f);
                setBackground(s ? ACCENT : BG_CARD);
                setForeground(TEXT_PRI);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    private JButton dialogButton(String text, Color bg, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? hover : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
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
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        return btn;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }
}

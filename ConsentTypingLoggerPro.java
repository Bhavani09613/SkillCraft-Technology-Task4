import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class ConsentTypingLoggerPro {
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss");
    private JFrame frame;
    private JTextArea textArea;
    private JButton startBtn, stopBtn, viewLogBtn, clearBtn, themeBtn;
    private JLabel statusLabel, wordCountLabel, timerLabel;
    private BufferedWriter writer;
    private boolean logging = false;
    private long startTime;
    private String currentFilename;
    private boolean darkMode = false;
    private Timer timer;
    public ConsentTypingLoggerPro() {
        frame = new JFrame("✍️ Consent Typing Logger Pro - Educational Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);
        frame.setLocationRelativeTo(null);
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (logging && writer != null) {
                    try {
                        String ts = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME);
                        writer.write(ts + " : " + e.getKeyChar());
                        writer.newLine();
                        writer.flush();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        startBtn = new JButton("▶ Start Logging (Consent)");
        stopBtn = new JButton("⏹ Stop Logging");
        viewLogBtn = new JButton("📂 View Log File");
        clearBtn = new JButton("🧹 Clear Text");
        themeBtn = new JButton("🌗 Toggle Theme");
        stopBtn.setEnabled(false);
        viewLogBtn.setEnabled(false);
        statusLabel = new JLabel("Not logging. Press Start to begin with consent.");
        statusLabel.setForeground(Color.RED);
        wordCountLabel = new JLabel("Words: 0 | Characters: 0");
        timerLabel = new JLabel("Time: 00:00");
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void update() {
                String text = textArea.getText();
                int words = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
                int chars = text.length();
                wordCountLabel.setText("Words: " + words + " | Characters: " + chars);
            }
            public void insertUpdate(DocumentEvent e) { update(); }
            public void removeUpdate(DocumentEvent e) { update(); }
            public void changedUpdate(DocumentEvent e) { update(); }
        });
        startBtn.addActionListener(ae -> startLogging());
        stopBtn.addActionListener(ae -> stopLogging());
        viewLogBtn.addActionListener(ae -> openLogFile());
        clearBtn.addActionListener(ae -> textArea.setText(""));
        themeBtn.addActionListener(ae -> toggleTheme());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(startBtn);
        top.add(stopBtn);
        top.add(viewLogBtn);
        top.add(clearBtn);
        top.add(themeBtn);
        JPanel bottom = new JPanel(new BorderLayout());
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stats.add(wordCountLabel);
        stats.add(new JLabel(" | "));
        stats.add(timerLabel);
        bottom.add(stats, BorderLayout.WEST);
        bottom.add(statusLabel, BorderLayout.CENTER);
        frame.getContentPane().add(top, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.getContentPane().add(bottom, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
    private void startLogging() {
        int confirm = JOptionPane.showConfirmDialog(frame,
                "This educational demo records your typed keys (with your consent) for analysis.\n"
                        + "No data is sent anywhere — it’s saved only on your machine.\n\n"
                        + "Do you consent to start logging?",
                "User Consent Required", JOptionPane.YES_NO_OPTION);
       if (confirm != JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(frame, "Logging cancelled. No data recorded.");
            return;
        }
        try {
            currentFilename = "typing_log_" + LocalDateTime.now().format(TF) + ".txt";
            writer = new BufferedWriter(new FileWriter(currentFilename, true));
            writer.write("=== Logging started at " + LocalDateTime.now() + " (user consent given) ===");
            writer.newLine();
            writer.flush();
            logging = true;
            startTime = System.currentTimeMillis();
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
            viewLogBtn.setEnabled(false);
            statusLabel.setText("Logging to file: " + currentFilename);
            statusLabel.setForeground(new Color(0, 128, 0));
            timer = new Timer(1000, e -> updateTimer());
            timer.start();
            System.out.println("✅ Logging started!");
            System.out.println("📝 File created: " + currentFilename);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Failed to open log file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private void stopLogging() {
        if (!logging) return;
        logging = false;
        startBtn.setEnabled(true);
        stopBtn.setEnabled(false);
        viewLogBtn.setEnabled(true);
        statusLabel.setText("Not logging.");
        statusLabel.setForeground(Color.RED);
        if (timer != null) timer.stop();
        try {
            if (writer != null) {
                writer.write("=== Logging stopped at " + LocalDateTime.now() + " ===");
                writer.newLine();
                writer.close();
                writer = null;
            }
            long duration = (System.currentTimeMillis() - startTime) / 1000;
            int chars = textArea.getText().length();
            double minutes = duration / 60.0;
            double cpm = minutes > 0 ? chars / minutes : chars;
            String message;
            if (chars > 300) message = "🔥 Great session! You typed a lot!";
            else if (chars > 100) message = "👏 Nice work! Keep practicing!";
            else message = "👍 Good start! Type more to improve.";
            JOptionPane.showMessageDialog(frame,
                    "🛑 Logging stopped.\n\n"
                    + "Duration: " + duration + " seconds\n"
                    + "Characters typed: " + chars + "\n"
                    + String.format("Typing speed: %.1f chars/min\n\n", cpm)
                    + message + "\n\nLog file saved: " + currentFilename,
                    "Session Summary", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("🛑 Logging stopped.");
            System.out.println("✅ Log file saved successfully!");
            } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private void openLogFile() {
        try {
            if (currentFilename != null) {
                Desktop.getDesktop().open(new File(currentFilename));
            } else {
                JOptionPane.showMessageDialog(frame, "No log file available.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Could not open log file.");
        }
    }
    private void updateTimer() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        long mins = elapsed / 60;
        long secs = elapsed % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", mins, secs));
    }
    private void toggleTheme() {
        Color bg, fg;
        if (!darkMode) {
            bg = new Color(30, 30, 30);
            fg = Color.WHITE;
            darkMode = true;
            themeBtn.setText("☀ Light Mode");
        } else {
            bg = Color.WHITE;
            fg = Color.BLACK;
            darkMode = false;
            themeBtn.setText("🌗 Dark Mode");
        }
        textArea.setBackground(bg);
        textArea.setForeground(fg);
        frame.getContentPane().setBackground(bg);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConsentTypingLoggerPro::new);
        System.out.println("📘 Consent Typing Logger Pro started. Waiting for user to begin logging...");
    }
}

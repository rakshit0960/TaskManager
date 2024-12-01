import javax.swing.*;
import java.awt.*;

public class TaskManagerFrame extends JFrame {
    private ProcessTableModel tableModel;
    private JTable processTable;
    private Timer updateTimer;

    @SuppressWarnings("unused")
    public TaskManagerFrame() {
        setTitle("Java Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Initialize components
        tableModel = new ProcessTableModel();
        processTable = new JTable(tableModel);

        // Add components to frame
        JScrollPane scrollPane = new JScrollPane(processTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add toolbar with buttons
        JToolBar toolBar = new JToolBar();
        JButton refreshButton = new JButton("Refresh");
        JButton endTaskButton = new JButton("End Task");

        refreshButton.addActionListener(e -> refreshProcessList());
        endTaskButton.addActionListener(e -> endSelectedProcess());

        toolBar.add(refreshButton);
        toolBar.add(endTaskButton);
        add(toolBar, BorderLayout.NORTH);

        // Set up auto-refresh timer (updates every 5 seconds)
        updateTimer = new Timer(5000, e -> refreshProcessList());
        updateTimer.start();
    }

    private void refreshProcessList() {
        tableModel.refreshProcesses();
    }

    private void endSelectedProcess() {
        int selectedRow = processTable.getSelectedRow();
        if (selectedRow != -1) {
            ProcessInfo process = tableModel.getProcessAt(selectedRow);
            try {
                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    new ProcessBuilder("taskkill", "/PID", String.valueOf(process.getPid())).start();
                } else {
                    // Linux/Unix command to kill process
                    new ProcessBuilder("kill", String.valueOf(process.getPid())).start();
                }
                refreshProcessList();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to terminate process: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
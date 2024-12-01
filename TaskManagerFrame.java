import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
        add(toolBar, BorderLayout.SOUTH);

        // Set up auto-refresh timer (updates every 5 seconds)
        updateTimer = new Timer(5000, e -> refreshProcessList());
        updateTimer.start();

        // Inside TaskManagerFrame constructor, after creating the table
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processTable.setAutoCreateRowSorter(true);  // Enable sorting
        processTable.getTableHeader().setReorderingAllowed(false);  // Prevent column reordering

        // Customize column widths
        processTable.getColumnModel().getColumn(0).setPreferredWidth(70);   // PID
        processTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // Name
        processTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Memory
        processTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // CPU

        // Add right-click context menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem killItem = new JMenuItem("End Process");
        killItem.addActionListener(e -> endSelectedProcess());
        contextMenu.add(killItem);

        processTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = processTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        processTable.setRowSelectionInterval(row, row);
                        contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        // Add search/filter feature
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        searchField.setToolTipText("Search processes");
        searchPanel.add(new JLabel("Filter: "));
        searchPanel.add(searchField);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void search() {
                String text = searchField.getText().toLowerCase();
                tableModel.filterProcesses(text);
            }

            @Override
            public void insertUpdate(DocumentEvent e) { search(); }
            @Override
            public void removeUpdate(DocumentEvent e) { search(); }
            @Override
            public void changedUpdate(DocumentEvent e) { search(); }
        });

        add(searchPanel, BorderLayout.NORTH);
        add(toolBar, BorderLayout.SOUTH);  // Move toolbar to bottom
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
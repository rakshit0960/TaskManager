import javax.swing.table.AbstractTableModel;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessTableModel extends AbstractTableModel {
    private List<ProcessInfo> processes;
    private final String[] columnNames = {"PID", "Process Name", "Memory Usage (MB)", "CPU Usage (%)"};
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public ProcessTableModel() {
        processes = new ArrayList<>();
        refreshProcesses();
    }

    @Override
    public int getRowCount() {
        return processes.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ProcessInfo process = processes.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> process.getPid();
            case 1 -> process.getName();
            case 2 -> process.getMemoryUsage() / (1024 * 1024); // Convert to MB
            case 3 -> String.format("%.1f", process.getCpuUsage());
            default -> null;
        };
    }

    public ProcessInfo getProcessAt(int row) {
        return processes.get(row);
    }

    public void refreshProcesses() {
        processes.clear();
        try {
            Process process;
            if (IS_WINDOWS) {
                process = new ProcessBuilder("tasklist", "/FO", "CSV", "/NH").start();
            } else {
                // Linux/Unix command
                process = new ProcessBuilder("ps", "-e", "-o", "pid,comm,rss").start();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (IS_WINDOWS) {
                    parseWindowsProcess(line);
                } else {
                    parseUnixProcess(line);
                }
            }
            fireTableDataChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseWindowsProcess(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 2) {
            String name = parts[0].replaceAll("\"", "");
            long pid = Long.parseLong(parts[1].replaceAll("\"", ""));
            long memory = 0;
            if (parts.length >= 5) {
                String memStr = parts[4].replaceAll("\"", "").replaceAll("[^0-9]", "");
                memory = Long.parseLong(memStr) * 1024; // Convert KB to bytes
            }
            processes.add(new ProcessInfo(pid, name, memory, 0.0));
        }
    }

    private void parseUnixProcess(String line) {
        // Skip header line
        if (line.trim().startsWith("PID")) return;

        String[] parts = line.trim().split("\\s+");
        if (parts.length >= 3) {
            try {
                long pid = Long.parseLong(parts[0]);
                String name = parts[1];
                // RSS is in KB, convert to bytes
                long memory = Long.parseLong(parts[2]) * 1024;
                processes.add(new ProcessInfo(pid, name, memory, 0.0));
            } catch (NumberFormatException ignored) {
                // Skip lines that can't be parsed
            }
        }
    }
}
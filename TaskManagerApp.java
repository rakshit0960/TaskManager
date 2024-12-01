import javax.swing.*;

public class TaskManagerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TaskManagerFrame frame = new TaskManagerFrame();
            frame.setVisible(true);
        });
    }
}
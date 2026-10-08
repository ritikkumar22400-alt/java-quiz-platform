package quizplatform;

import quizplatform.service.AppContext;
import quizplatform.storage.Repository;
import quizplatform.ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            Repository repo = new Repository(Path.of("data"));
            try {
                repo.load();
            } catch (Exception e) {
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Failed to load data: " + e.getMessage(), "Startup Error",
                        javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            AppContext ctx = new AppContext(repo);
            new LoginFrame(ctx).setVisible(true);
        });
    }
}

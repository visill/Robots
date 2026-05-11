package visill.robot;

import visill.robot.l10n.Locales;
import visill.robot.l10n.Localization;
import visill.robot.controller.MainController;

import javax.swing.*;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }
    public static void main(String[] args) {
        try {
//        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
          UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Localization.ChangeLanguage(Locales.Russian);

        SwingUtilities.invokeLater(MainController::run);
    }
}

package visill.robot.l10n;

import javax.swing.*;

public class Russia {
    public static void ChangeLanguage() {
        UIManager.put("OptionPane.yesButtonText", "Да");
        UIManager.put("OptionPane.noButtonText", "Нет");
        UIManager.put("OptionPane.cancelButtonText", "Отмена");
        UIManager.put("OptionPane.titleText", "Выберите опцию");
        UIManager.put("QuitButton.quitText", "Вы точно хотите выйти?");

    }
}

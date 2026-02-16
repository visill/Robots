package visill.robot;

import org.junit.Test;
import visill.robot.l10n.Locales;
import visill.robot.l10n.Localization;
import visill.robot.l10n.Russia;

import javax.swing.*;

import static org.junit.Assert.*;

public class LocalizationTest {
    @Test public void russianWorks() {
        Russia.ChangeLanguage();
        assertEquals(UIManager.get("OptionPane.yesButtonText"), "Да");
        assertEquals(UIManager.get("OptionPane.noButtonText"), "Нет");
    }
    @Test public void l10nRussianWorks() {
        Localization.ChangeLanguage(Locales.Russian);
        assertEquals(UIManager.get("OptionPane.yesButtonText"), "Да");
        assertEquals(UIManager.get("OptionPane.noButtonText"), "Нет");
    }
}

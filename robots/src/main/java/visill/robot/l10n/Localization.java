package visill.robot.l10n;

public class Localization {
    public static void ChangeLanguage(Locales locale) {
        switch (locale) {
            case Russian -> Russia.ChangeLanguage();
        }
    }
}

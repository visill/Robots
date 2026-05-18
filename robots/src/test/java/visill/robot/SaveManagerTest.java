package visill.robot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import visill.robot.save.SaveManager;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SaveManagerTest {

    private SaveManager saveManager;
    private String tempFilePath;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        File tempFile = tempDir.resolve("window_settings.json").toFile();
        tempFilePath = tempFile.getAbsolutePath();
        saveManager = new SaveManager(tempFilePath);
    }

    @Test
    void testSaveAndLoadWindowPositionAndSize() {
        // 1. Создаем тестовое окно с начальными параметрами
        JFrame originalWindow = new JFrame();
        originalWindow.setBounds(100, 150, 400, 300);

        // 2. Сохраняем его состояние
        String windowName = "mainWindow";
        saveManager.saveWindow(originalWindow, windowName);

        // 3. Создаем новое "чистое" менеджер-хранилище (имитируем перезапуск программы)
        SaveManager newSaveManager = new SaveManager(tempFilePath);

        // 4. Создаем другое окно, куда будем загружать параметры
        JFrame targetWindow = new JFrame();
        targetWindow.setBounds(0, 0, 10, 10); // Сброшенные координаты

        // 5. Загружаем данные
        boolean isLoaded = newSaveManager.loadWindow(targetWindow, windowName);

        // 6. Проверяем результаты
        assertTrue(isLoaded, "Окно должно успешно загрузиться");
        assertEquals(100, targetWindow.getX(), "Координата X должна быть 100");
        assertEquals(150, targetWindow.getY(), "Координата Y должна быть 150");
        assertEquals(400, targetWindow.getWidth(), "Ширина должна быть 400");
        assertEquals(300, targetWindow.getHeight(), "Высота должна быть 300");
    }

    @Test
    void testLoadNonExistentWindow() {
        // Проверяем, что загрузка неизвестного окна возвращает false
        JFrame window = new JFrame();
        boolean isLoaded = saveManager.loadWindow(window, "unknown_window");

        assertFalse(isLoaded, "Загрузка несуществующего окна должна вернуть false");
    }

    @Test
    void testSaveAndLoadMaximizedState() throws InterruptedException {
        // 1. Создаем окно и разворачиваем его во весь экран
        JFrame maximizedWindow = new JFrame();
        maximizedWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 2. Сохраняем состояние
        String windowName = "maxWindow";
        saveManager.saveWindow(maximizedWindow, windowName);

        // 3. Создаем окно для загрузки
        JFrame targetWindow = new JFrame();

        // 4. Загружаем состояние
        SaveManager newSaveManager = new SaveManager(tempFilePath);
        newSaveManager.loadWindow(targetWindow, windowName);

        // Так как в WindowStatus применяется SwingUtilities.invokeLater,
        // даем небольшую паузу (50-100мс), чтобы поток EDT успел выполнить код
        Thread.sleep(100);

        // 5. Проверяем, что окно развернуто
        assertEquals(JFrame.MAXIMIZED_BOTH, targetWindow.getExtendedState(), "Окно должно быть развернуто");
    }
}
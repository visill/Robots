package visill.robot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import visill.robot.model.log.LogChangeListener;
import visill.robot.model.log.LogEntry;
import visill.robot.model.log.LogLevel;
import visill.robot.view.LogWindowSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class LogSourceTest{

    private LogWindowSource logSource;
    private final int QUEUE_LENGTH = 5;

    @BeforeEach
    void setUp() {
        logSource = new LogWindowSource(QUEUE_LENGTH);
    }

    // ==========================================
    // 1. ФУНКЦИОНАЛЬНЫЕ ТЕСТЫ (ОДНОПОТОЧНЫЕ)
    // ==========================================

    @Test
    @DisplayName("Инициализация с некорректным размером должна бросать исключение")
    void testInvalidConstructorArgument() {
        assertThrows(IllegalArgumentException.class, () -> new LogWindowSource(0));
        assertThrows(IllegalArgumentException.class, () -> new LogWindowSource(-10));
    }

    @Test
    @DisplayName("Метод size() должен корректно расти до лимита очереди")
    void testSizeGrowthAndLimit() {
        assertEquals(0, logSource.size());

        logSource.append(LogLevel.Info, "Msg 1");
        assertEquals(1, logSource.size());

        // Заполняем полностью
        for (int i = 2; i <= QUEUE_LENGTH; i++) {
            logSource.append(LogLevel.Info, "Msg " + i);
        }
        assertEquals(QUEUE_LENGTH, logSource.size());

        // Добавляем сверху лимита
        logSource.append(LogLevel.Info, "Msg Overflow");
        assertEquals(QUEUE_LENGTH, logSource.size(), "Размер не должен превышать лимит очереди");
    }

    @Test
    @DisplayName("Метод all() должен возвращать только последние N элементов при перезаписи")
    void testCircularBufferOverwriting() {
        // Пишем 7 логов в очередь размером 5
        for (int i = 1; i <= 7; i++) {
            logSource.append(LogLevel.Info, "Message " + i);
        }

        List<LogEntry> logs = new ArrayList<>();
        logSource.all().forEach(logs::add);

        assertEquals(5, logs.size());
        // Первые два (Message 1 и 2) должны быть затерты
        assertEquals("Message 3", logs.get(0).getMessage());
        assertEquals("Message 7", logs.get(4).getMessage());
    }

    @Test
    @DisplayName("Метод lastN() должен отдавать ровно N или меньше элементов")
    void testLastN() {
        logSource.append(LogLevel.Debug, "1");
        logSource.append(LogLevel.Debug, "2");

        // Запросили больше, чем есть
        List<LogEntry> logs3 = new ArrayList<>();
        logSource.lastN(3).forEach(logs3::add);
        assertEquals(2, logs3.size());

        // Запросили ровно 1
        List<LogEntry> logs1 = new ArrayList<>();
        logSource.lastN(1).forEach(logs1::add);
        assertEquals(1, logs1.size());
        assertEquals("2", logs1.get(0).getMessage());

        // Некорректный аргумент
        assertThrows(IllegalArgumentException.class, () -> logSource.lastN(0));
    }

    // ==========================================
    // 2. ТЕСТЫ СЛУШАТЕЛЕЙ (OBSERVER PATTERN)
    // ==========================================

    @Test
    @DisplayName("Слушатели должны получать уведомления при добавлении лога")
    void testListenerNotification() {
        AtomicInteger notificationCount = new AtomicInteger(0);
        LogChangeListener listener = notificationCount::incrementAndGet;

        logSource.registerListener(listener);
        logSource.append(LogLevel.Info, "Test");

        assertEquals(1, notificationCount.get());

        logSource.unregisterListener(listener);
        logSource.append(LogLevel.Info, "Test 2");

        assertEquals(1, notificationCount.get(), "После отписка уведомления идти не должны");
    }

    // ==========================================
    // 3. МНОГОПОТОЧНЫЕ ТЕСТЫ (STRESS TESTS)
    // ==========================================

    @Test
    @DisplayName("Конкурентная запись не должна ломать структуру данных и вызывать Exception")
    void testConcurrentAppend() throws InterruptedException {
        int threadCount = 10;
        int iterationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < iterationsPerThread; j++) {
                        logSource.append(LogLevel.Info, "Thread " + threadId + " log " + j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Ждем завершения всех потоков (максимум 5 секунд)
        boolean finishedCleanly = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finishedCleanly, "Потоки зависли (возможен Deadlock)");
        assertEquals(QUEUE_LENGTH, logSource.size(), "Буфер должен остаться заполненным");

        // Проверяем, что range() работает во время/после конкурентной записи и не отдает null
        assertDoesNotThrow(() -> {
            for (LogEntry entry : logSource.all()) {
                assertNotNull(entry);
                assertNotNull(entry.getMessage());
            }
        });
    }
}
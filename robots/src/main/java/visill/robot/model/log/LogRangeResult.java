package visill.robot.model.log;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LogRangeResult implements Iterable<LogEntry> {
    private final LogNode startNode;
    private final int count;

    public LogRangeResult(LogNode startNode, int count) {
        this.startNode = startNode;
        this.count = count;
    }

    public LogNode getNode() {
        return startNode;
    }

    public int getCount() {
        return count;
    }

    @Override
    public Iterator<LogEntry> iterator() {
        return new Iterator<LogEntry>() {
            private LogNode current = startNode;
            private int remaining = count;

            @Override
            public boolean hasNext() {
                // Итерация продолжается, пока не кончился счетчик и есть следующий узел
                return remaining > 0 && current != null;
            }

            @Override
            public LogEntry next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                LogEntry entry = current.getEntry();
                current = current.getNext(); // Переходим к следующему узлу односвязного списка
                remaining--;
                return entry;
            }
        };
    }
}

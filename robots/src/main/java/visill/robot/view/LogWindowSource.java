package visill.robot.view;

import visill.robot.model.log.*;

import java.util.*;

/**
 * Что починить:
 * 1. Этот класс порождает утечку ресурсов (связанные слушатели оказываются
 * удерживаемыми в памяти)
 * 2. Этот класс хранит активные сообщения лога, но в такой реализации он 
 * их лишь накапливает. Надо же, чтобы количество сообщений в логе было ограничено 
 * величиной m_iQueueLength (т.е. реально нужна очередь сообщений 
 * ограниченного размера) 
 */

public class LogWindowSource
{
    private final int m_iQueueLength;
    private int m_size = 0;

    private final ArrayList<LogChangeListener> m_listeners;
    private volatile LogChangeListener[] m_activeListeners;
    private LogNode head = null;
    private LogNode tail = null;
    long tailIndex = 0;
    private final Map<Long, LogNode> nodeMap = new HashMap<>();
    public LogWindowSource(int iQueueLength) 
    {
        if (iQueueLength <= 0) {
            throw new IllegalArgumentException("Length must be > 0");
        }
        m_iQueueLength = iQueueLength;

        m_listeners = new ArrayList<LogChangeListener>();
    }
    
    public void registerListener(LogChangeListener listener)
    {
        synchronized(m_listeners)
        {
            m_listeners.add(listener);
            m_activeListeners = null;
        }
    }
    
    public void unregisterListener(LogChangeListener listener)
    {
        synchronized(m_listeners)
        {
            m_listeners.remove(listener);
            m_activeListeners = null;
        }
    }
    
    public void append(LogLevel logLevel, String strMessage)
    {
        LogEntry entry = new LogEntry(logLevel, strMessage);
        long currentIdx = tailIndex++;
        LogNode newNode = new LogNode(entry, currentIdx);
        nodeMap.put(currentIdx, newNode);

        synchronized (this) {
            if (head == null) {
                head = newNode;
                tail = newNode;
                m_size = 1;
            } else {
                // Добавляем в конец односвязного списка
                tail.next = newNode;
                tail = newNode;
                m_size++;

                if (m_size > m_iQueueLength) {
                    LogNode oldHead = head;
                    head = head.next;
                    nodeMap.remove(oldHead.getGlobalIndex());

                    oldHead.next = null;
                    m_size--;
                }
            }
        }
        LogChangeListener [] activeListeners = m_activeListeners;
        if (activeListeners == null)
        {
            synchronized (m_listeners)
            {
                if (m_activeListeners == null)
                {
                    activeListeners = m_listeners.toArray(new LogChangeListener [0]);
                    m_activeListeners = activeListeners;
                }
            }
        }
        for (LogChangeListener listener : activeListeners)
        {
            listener.onLogChanged();
        }
    }
    
    public synchronized int size()
    {
        return m_size;
    }

    public synchronized Iterable<LogEntry> range(long startFromGlobalIdx, int count)
    {
        LogNode startNode = nodeMap.get(startFromGlobalIdx);
        count = Math.min(count,m_size);
        return new LogRangeResult(startNode, count);
    }
    public synchronized Iterable<LogEntry> lastN(int n) {
        if (n <=0) {
            throw new IllegalArgumentException("N must be > 0");
        }
        int max_n = Math.min(n, m_size);
        if (tail == null) {
            return new ArrayList<LogEntry>();
        }
        long startIdx = tail.getGlobalIndex() - max_n + 1;

        return range(startIdx, max_n);
    }
    public synchronized  Iterable<LogEntry> all()
    {
        int len = size();
        return range(head.getGlobalIndex(),len);

    }
}

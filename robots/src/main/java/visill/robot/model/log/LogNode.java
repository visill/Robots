package visill.robot.model.log;

public class LogNode {
    private final LogEntry entry;
    private final long globalIndex;
    public LogNode next;

    public LogNode(LogEntry entry, long globalIndex) {
        this.entry = entry;
        this.globalIndex = globalIndex;
    }

    public LogEntry getEntry() { return entry; }
    public LogNode getNext() { return next; }
    public long getGlobalIndex() { return globalIndex; }
}
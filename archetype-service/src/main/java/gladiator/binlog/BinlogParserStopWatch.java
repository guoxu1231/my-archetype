package gladiator.binlog;

import org.springframework.util.StopWatch;


public class BinlogParserStopWatch extends StopWatch {

    private int taskCount;

    public void countEvent(int events) {
        taskCount += events;
    }

    @Override
    public void start(String taskName) throws IllegalStateException {
        super.start(taskName);
        this.taskCount = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" [Binlog Event]=").append(taskCount);
        return sb.toString();
    }
}

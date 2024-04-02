package taskmanager;

import task.Task;
import taskmanager.HistoryManager;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();


    @Override
    public void add(Task task) {
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        while (history.size() > 10) {
            history.removeFirst();
        }
        return history;
    }
}

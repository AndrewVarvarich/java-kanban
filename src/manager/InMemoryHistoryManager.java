package manager;

import task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();
    private int numberElements = 10;

    @Override
    public void add(Task task) {
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        while (history.size() > numberElements) {
            history.removeFirst();
        }
        return history;
    }
}

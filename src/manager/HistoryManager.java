package manager;

import task.Task;

import java.util.List;
import java.util.Map;

public interface HistoryManager {

    void add(Task task);

    void remove(int id);

    Map<Integer, InMemoryHistoryManager.Node> getHistory();

    List<Task> getTasksHistory();

}

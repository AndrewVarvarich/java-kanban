package Task;

import TaskManager.*;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public void addSubTask(Integer subTaskId) {
        if (!subtaskIds.contains(subTaskId) || subtaskIds.isEmpty()) {
            subtaskIds.add(subTaskId);
        }
    }

    public void removeSubTask(int id) {
        subtaskIds.remove(id);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }
}

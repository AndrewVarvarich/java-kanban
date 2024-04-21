package task;

import manager.*;
import java.util.ArrayList;
import java.util.Iterator;
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
        List<Integer> intList = subtaskIds;
        Iterator<Integer> iterator = intList.iterator();
        while (iterator.hasNext()) {
            int i = iterator.next();
            if (i == id) {
                iterator.remove();
            }
        }
        /*for (Integer ids : subtaskIds) {
            if (ids == id) {
                subtaskIds.remove(ids);
            }
        }*/
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "taskId'" + taskId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}

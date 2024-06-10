package task;

import com.google.gson.annotations.SerializedName;
import manager.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds;
    @SerializedName("epicEndTime")
    protected LocalDateTime endTime;

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
        this.subtaskIds = new ArrayList<>();
    }

    public void addSubTask(Integer subTaskId) {
        if (!subtaskIds.contains(subTaskId) || subtaskIds.isEmpty()) {
            subtaskIds.add(subTaskId);
        }
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void removeSubTask(int id) {
        List<Integer> intList = subtaskIds;
        intList.removeIf(i -> i == id);
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

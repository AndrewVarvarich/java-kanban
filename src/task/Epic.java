package task;

import manager.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds = new ArrayList<>();
    protected LocalDateTime endTime;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Epic(String name, String description, TaskStatus status) {
        super(name, description, status);
    }

    public void addSubTask(Integer subTaskId) {
        if (!subtaskIds.contains(subTaskId) || subtaskIds.isEmpty()) {
            subtaskIds.add(subTaskId);
        }
    }

    @Override
    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
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
    public LocalDateTime getStartTime() {
        return startTime;
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

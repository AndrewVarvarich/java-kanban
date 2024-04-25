package task;

import manager.*;

import java.util.Objects;

public class Task {

    protected String name;
    protected String description;
    protected TaskStatus status;
    protected int taskId;

    public Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setTaskId(int id) {
        this.taskId = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Task task = (Task) o;
        return Objects.equals(name, task.name) && Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, status, taskId);
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId'" + taskId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}

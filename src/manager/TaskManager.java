package manager;

import task.Epic;
import task.SubTask;
import task.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    List<Task> getTasks();

    List<SubTask> getSubTasks();

    List<Epic> getEpics();

    void clearTasks();

    void clearSubTasks();

    void clearEpics();

    Task getTaskById(int id);

    SubTask getSubTaskById(int id);

    Epic getEpicById(int id);


    void addTask(Task task);

    void addSubTask(SubTask subTask);

    void addEpic(Epic epic);

    void removeTaskById(int id);

    void removeSubTaskById(int id);

    void removeEpicById(int id);

    void updateTask(Task task, int id);

    void updateSubTask(SubTask subTask, int id);

    List<SubTask> getAllSubtasksForEpic(int epicId);


    void add(Task task);

    List<Task> getHistory();

    Set<Task> getPrioritizedTasks();

    LocalDateTime getEpicEndTime(int epicId);
}

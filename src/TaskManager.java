import java.util.*;
import java.util.stream.Collectors;

public class TaskManager {

    private static int id = 1;

    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, SubTask> subTasks;
    private final HashMap<Integer, Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    public ArrayList<String> getTasks() {
        ArrayList<String> tasksList = new ArrayList<>();
        for (Task task : tasks.values()) {
            tasksList.add(task.toString());
        }
        return tasksList;
    }

    public ArrayList<String> getSubTasks() {
        ArrayList<String> subTasksList = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            subTasksList.add(subTask.toString());
        }
        return subTasksList;
    }

    public ArrayList<String> getEpic() {
        ArrayList<String> epicsList = new ArrayList<>();
        for (Epic epic : epics.values()) {
            epicsList.add(epic.toString());
        }
        return epicsList;
    }

    public void clearTask() {
        tasks.clear();
    }

    public void clearSubTask() {
        subTasks.clear();
    }

    public void clearEpic() {
        epics.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public SubTask getSubTaskById(int id) {
        return subTasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }
//
    public void addTask(Task task) {
        int taskId = id;
        if (tasks.equals(null) || !tasks.containsValue(task.getName())) {
            task.setId(taskId);
            tasks.put(taskId, task);
            id++;
        } else {
            System.out.println("Такая задача уже есть");
        }

    }

    public void addSubTask(SubTask subTask) {
        int taskId = id;
        if (subTasks.equals(null) || !subTasks.equals(subTask.getName())) {
            subTask.setId(taskId);
            subTasks.put(taskId, subTask);
            id++;
        } else {
            System.out.println("Такая задача уже есть");
        }
    }

    public void addEpic(Epic epic) {
        int taskId = id;
        if (epics.equals(null) || !epics.equals(epic.getName())) {
            epic.setId(taskId);
            epics.put(taskId, epic);
            id++;
        } else {
            System.out.println("Такая задача уже есть");
        }
    }

    public void removeTaskById(int id) {
        if (tasks.containsKey(id)) {
            tasks.remove(id);
        }
    }

    public void removeSubTaskById(int id) {
        if (subTasks.containsKey(id)) {
            subTasks.remove(id);
        }
    }

    public void removeEpicById(int id) {
        if (epics.containsKey(id)) {
            epics.remove(id);
        }
    }

    public void updateTask(Task task) {
        int requiredId;
        for (Map.Entry<Integer, Task> entry : tasks.entrySet())
        if (entry.getValue().equals(task)) {
            requiredId = entry.getKey();
            task.setId(requiredId);
            tasks.put(requiredId, task);
        }
    }

    public void updateSubTask(SubTask subTask) {
        int requiredId;
        for (Map.Entry<Integer, SubTask> entry : subTasks.entrySet())
            if (entry.getValue().equals(subTask)) {
                requiredId = entry.getKey();
                subTask.setId(requiredId);
                subTask.epicId = subTasks.get(requiredId).epicId;
                subTasks.put(requiredId, subTask);
            }
    }

    public void addSubTaskToEpic(int subtaskId, int epicId) {
        SubTask subtask = subTasks.get(subtaskId);

        subtask.epicId = epicId;
    }

    public ArrayList<SubTask> getAllSubtasksForEpic(int epicId) {
        ArrayList<SubTask> subtasksForEpic = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            if (subTask.epicId == epicId) {
                subtasksForEpic.add(subTask);
            }
        }
        return subtasksForEpic;
    }

    public boolean areSubtasksEqualForStatus(ArrayList<SubTask> subtasks, TaskStatus taskStatus) {
        boolean allEqualForStatus = true;

        for (SubTask subTask : subtasks) {
            if (subTask.getStatus() != taskStatus) {
                allEqualForStatus = false;
                break;
            }
        }
        return allEqualForStatus;
    }

    public void updateEpicStatus(int epicId) {
        ArrayList<SubTask> subTasks = getAllSubtasksForEpic(epicId);


        if (subTasks.isEmpty()) {
            epics.get(epicId).setStatus(TaskStatus.NEW);
            return;
        } else if (areSubtasksEqualForStatus(subTasks, TaskStatus.NEW)) {
            epics.get(epicId).setStatus(TaskStatus.NEW);
            return;
        } else if (areSubtasksEqualForStatus(subTasks, TaskStatus.DONE)) {
            epics.get(epicId).setStatus(TaskStatus.DONE);
            return;
        } else {
            epics.get(epicId).setStatus(TaskStatus.IN_PROGRESS);
        }
    }






}

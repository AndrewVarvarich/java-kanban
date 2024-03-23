package taskManager;

import task.*;

import java.util.*;

public class TaskManager {


    private int nextTaskId = 1;

    private final Map<Integer, Task> tasks;
    private final Map<Integer, SubTask> subTasks;
    private final Map<Integer, Epic> epics;

    public TaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
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
        if (!tasks.containsValue(task)) {
            setTaskId(task);
            tasks.put(task.getTaskId(), task);
        } else {
            System.out.println("Такая задача уже есть");
        }

    }

    public void addSubTask(SubTask subTask) {

        if (!subTasks.containsValue(subTask)) {
            setSubTaskId(subTask);
            subTasks.put(subTask.getTaskId(), subTask);
            addSubTaskToEpic(subTask);
            updateEpicStatus(subTask.getEpicId());
        } else {
            System.out.println("Такая задача уже есть");
        }
    }

    public void addEpic(Epic epic) {
        if (!epics.containsValue(epic)) {
            setEpicId(epic);
            epics.put(epic.getTaskId(), epic);
        } else {
            System.out.println("Такая задача уже есть");
        }
    }

    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    public void removeSubTaskById(int id, boolean updateEpic) {
        final SubTask removableSubTask = subTasks.get(id);
        subTasks.remove(id);

        int specificEpicId = removableSubTask.getEpicId();
        final Epic specificEpic = epics.get(specificEpicId);

        if (updateEpic) {
            specificEpic.removeSubTask(id);
            updateEpicStatus(specificEpicId);
        }

    }

    public void removeSubTaskById(int id) {
        removeSubTaskById(id, true);
    }

    public void removeEpicById(int id) {
        final Epic removableEpic = epics.get(id);
        for (Integer subTaskId : removableEpic.getSubtaskIds()) {
            removeSubTaskById(subTaskId, false);
        }

        epics.remove(id);

    }

    public void updateTask(Task task, int id) {
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        task.setTaskId(id);
        tasks.put(id, task);
    }

    public void updateSubTask(SubTask subTask, int id) {
        final SubTask savedTask = subTasks.get(id);
        if (savedTask == null) {
            return;
        }
        subTask.setTaskId(id);
        subTasks.put(id, subTask);
        updateEpicStatus(subTask.getEpicId());
    }

    private void addSubTaskToEpic(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubTask(subTask.getTaskId());
        }
    }

    public List<SubTask> getAllSubtasksForEpic(int epicId) {
        List<SubTask> subtasksForEpic = new ArrayList<>();
        final Epic givenEpic = epics.get(epicId);
        List<Integer> subTasksId = givenEpic.getSubtaskIds();
        for (int subTask : subTasksId) {
            subtasksForEpic.add(subTasks.get(subTask));
        }
        return subtasksForEpic;
    }

    private boolean areSubtasksEqualForStatus(List<SubTask> subtasks, TaskStatus taskStatus) {
        boolean allEqualForStatus = true;

        for (SubTask subTask : subtasks) {
            if (subTask.getStatus() != taskStatus) {
                allEqualForStatus = false;
                break;
            }
        }
        return allEqualForStatus;
    }

    private void updateEpicStatus(int epicId) {
        List<SubTask> subTasks = getAllSubtasksForEpic(epicId);

        if (subTasks.isEmpty()) {
            epics.get(epicId).setStatus(TaskStatus.NEW);
        } else if (areSubtasksEqualForStatus(subTasks, TaskStatus.NEW)) {
            epics.get(epicId).setStatus(TaskStatus.NEW);
        } else if (areSubtasksEqualForStatus(subTasks, TaskStatus.DONE)) {
            epics.get(epicId).setStatus(TaskStatus.DONE);
        } else {
            epics.get(epicId).setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void setTaskId(Task task) {
        task.setTaskId(getNextTaskId());
    }

    private void setSubTaskId(SubTask subTask) {
        subTask.setTaskId(getNextTaskId());
    }

    private void setEpicId(Epic epic) {
        epic.setTaskId(getNextTaskId());
    }

    public int getNextTaskId() {
        nextTaskId += nextTaskId;
        return nextTaskId;
    }

}


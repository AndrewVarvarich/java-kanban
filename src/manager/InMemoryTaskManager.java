package manager;

import task.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {


    private int nextTaskId = 1;

    private final Map<Integer, Task> tasks;
    private final Map<Integer, SubTask> subTasks;
    private final Map<Integer, Epic> epics;

    HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void clearTask() {
        tasks.clear();
    }

    @Override
    public void clearSubTask() {
        subTasks.clear();
    }

    @Override
    public void clearEpic() {
        epics.clear();
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    //
    @Override
    public void addTask(Task task) {
        if (!tasks.containsValue(task)) {
            setTaskId(task);
            tasks.put(task.getTaskId(), task);
        } else {
            System.out.println("Такая задача уже есть");
        }

    }

    @Override
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

    @Override
    public void addEpic(Epic epic) {
        if (!epics.containsValue(epic)) {
            setEpicId(epic);
            epics.put(epic.getTaskId(), epic);
        } else {
            System.out.println("Такая задача уже есть");
        }
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubTaskById(int id, boolean updateEpic) {
        SubTask removableSubTask = subTasks.get(id);
        int specificEpicId = removableSubTask.getEpicId();
        subTasks.remove(id);
        historyManager.remove(id);

        final Epic specificEpic = epics.get(specificEpicId);

        if (updateEpic) {
            int removableSubTaskId = removableSubTask.getTaskId();
            specificEpic.removeSubTask(removableSubTaskId);
            updateEpicStatus(specificEpicId);
        }
    }

    @Override
    public void removeSubTaskById(int id) {
        removeSubTaskById(id, true);

    }

    @Override
    public void removeEpicById(int id) {
        final Epic removableEpic = epics.get(id);
        for (Integer subTaskId : removableEpic.getSubtaskIds()) {
            removeSubTaskById(subTaskId, false);
        }

        epics.remove(id);
        historyManager.remove(id);

    }

    @Override
    public void updateTask(Task task, int id) {
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        task.setTaskId(id);
        tasks.put(id, task);
    }

    @Override
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

    @Override
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
            if (subTask != null && subTask.getStatus() != taskStatus) {
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

    private int getNextTaskId() {
        nextTaskId += nextTaskId;
        return nextTaskId;
    }

    @Override
    public void add(Task task) {
        historyManager.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}


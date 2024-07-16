package manager;

import exceptions.NotFoundException;
import task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {


    private int nextTaskId = 1;

    private final Map<Integer, Task> tasks;
    private final Map<Integer, SubTask> subTasks;
    private final Map<Integer, Epic> epics;

    private final Set<Task> prioritizedTasks;

    HistoryManager historyManager;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.subTasks = new HashMap<>();
        this.epics = new HashMap<>();
        prioritizedTasks = new TreeSet<>(Comparator
                .comparing(Task::getStartTime));
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
    public void clearTasks() {
        List<Task> removeList = getTasks();
        for (Task task : removeList) {
            historyManager.remove(task.getTaskId());
        }
        tasks.clear();
    }

    @Override
    public void clearSubTasks() {
        List<SubTask> removeList = getSubTasks();
        for (Task task : removeList) {
            historyManager.remove(task.getTaskId());
        }
        subTasks.clear();
    }

    @Override
    public void clearEpics() {
        List<Epic> removeList = getEpics();
        for (Task task : removeList) {
            historyManager.remove(task.getTaskId());
        }
        epics.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Задача не найдена");
        }
        historyManager.add(task);
        return tasks.get(id);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = subTasks.get(id);
        if (subTask == null) {
            throw new NotFoundException("Подзадача не найдена");
        }
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпик не найден");
        }
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public void addTask(Task task) {
        if (!tasks.containsValue(task)) {
            boolean isOverlap = tasks.values().stream()
                    .anyMatch(task1 -> areTheTaskCross(task));
            if (isOverlap) {
                throw new IllegalArgumentException("Задачи пересекаются");
            } else {
                setTaskId(task);
                tasks.put(task.getTaskId(), task);
                prioritizedTasks.add(task);

            }
        }
    }

    @Override
    public void addSubTask(SubTask subTask) {
        if (!subTasks.containsValue(subTask)) {
            boolean isOverlap = subTasks.values().stream()
                    .anyMatch(subTask1 -> areTheTaskCross(subTask));
            if (isOverlap) {
                throw new IllegalArgumentException("Подзадачи пересекаются");
            } else {
                setSubTaskId(subTask);
                subTasks.put(subTask.getTaskId(), subTask);
                addSubTaskToEpic(subTask);
                updateEpicStatus(subTask.getEpicId());
                getEpicEndTime(subTask.getEpicId());
                prioritizedTasks.add(subTask);

            }
        }
    }

    @Override
    public void addEpic(Epic epic) {
        if (!epics.containsValue(epic)) {
            setEpicId(epic);
            epics.put(epic.getTaskId(), epic);
        }
    }

    @Override
    public void removeTaskById(int id) {
        prioritizedTasks.removeIf(task -> task.getTaskId() == id);
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubTaskById(int id) {
        SubTask removableSubTask = subTasks.get(id);

        if (removableSubTask == null) {
            throw new IllegalArgumentException("Подзадачи c id: " + id + " не существует.");
        }

        int specificEpicId = removableSubTask.getEpicId();
        removableSubTask.removeEpicId();
        subTasks.remove(id);
        historyManager.remove(id);

        Epic specificEpic = epics.get(specificEpicId);
        if (specificEpic != null) {
            int removableSubTaskId = removableSubTask.getTaskId();
            specificEpic.removeSubTask(removableSubTaskId);
            updateEpicStatus(specificEpicId);
        }
        prioritizedTasks.removeIf(task -> task.getTaskId() == id);
    }

    public void removeEpicById(int id) {
        Epic removableEpic = epics.get(id);

        if (removableEpic == null) {
            throw new IllegalArgumentException("Эпика с id: " + id + " не существует.");
        }

        for (Integer subTaskId : removableEpic.getSubtaskIds()) {
            removeSubTaskById(subTaskId);
        }

        epics.remove(id);
        historyManager.remove(id);
        prioritizedTasks.removeIf(task -> task.getTaskId() == id);
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
            getEpicEndTime(subTask.getEpicId());
    }

    @Override
    public List<SubTask> getAllSubtasksForEpic(int epicId) {
        return subTasks.values().stream()
                .filter(subTask -> subTask.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    @Override
    public void add(Task task) {
        historyManager.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public LocalDateTime getEpicEndTime(int epicId) {
        Epic currentEpic = epics.get(epicId);
        if (currentEpic != null) {
            Duration epicDuration = Duration.ZERO;
            Optional<LocalDateTime> startTimeOpt = getAllSubtasksForEpic(epicId).stream()
                    .map(SubTask::getStartTime)
                    .min(LocalDateTime::compareTo);
            if (startTimeOpt.isPresent()) {
                LocalDateTime startTime = startTimeOpt.get();
                for (SubTask subTask : getAllSubtasksForEpic(epicId)) {
                    epicDuration = epicDuration.plus(subTask.getDuration());
                }
                currentEpic.setDuration(epicDuration);
                currentEpic.setStartTime(startTime);
                LocalDateTime endTime = startTime.plus(epicDuration);
                currentEpic.setEndTime(endTime);
                return endTime;
            } else {
                throw new RuntimeException("Нет подзадач для данного эпика");
            }
        } else {
            return null;
        }
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        if (!prioritizedTasks.isEmpty()) {
            List<Task> tasksWithStartTime = tasks.values().stream()
                            .filter(task -> task.getStartTime() != null)
                            .toList();

            List<SubTask> subTasksWithStartTime = subTasks.values().stream()
                            .filter(subTask -> subTask.getStartTime() != null)
                            .toList();

            List<Epic> epicsWithStartTime = epics.values().stream()
                            .filter(epic -> epic.getStartTime() != null)
                            .toList();

            prioritizedTasks.addAll(tasksWithStartTime);
            prioritizedTasks.addAll(subTasksWithStartTime);
            prioritizedTasks.addAll(epicsWithStartTime);

            return prioritizedTasks;
        } else {
            return new TreeSet<>();
        }
    }

    private boolean areTheTaskCross(Task task) {
        LocalDateTime newTaskStart = task.getStartTime();
        LocalDateTime newTaskEnd = newTaskStart.plus(task.getDuration());
        for (Task task1 : getPrioritizedTasks()) {
            LocalDateTime existingTaskStart = task1.getStartTime();
            LocalDateTime existingTaskEnd = existingTaskStart.plus(task1.getDuration());

            // Проверяем, что идентификаторы задач не совпадают
            if (task.getTaskId() != task1.getTaskId() &&
                    newTaskStart.isBefore(existingTaskEnd) &&
                    newTaskEnd.isAfter(existingTaskStart)) {
                return true;
            }
        }
        return false;
    }
    /*private boolean areTheTaskCross(Task task) {
        LocalDateTime newTaskStart = task.getStartTime();
        LocalDateTime newTaskEnd = newTaskStart.plus(task.getDuration());
        for (Task task1 : getPrioritizedTasks()) {
            LocalDateTime existingTaskStart = task1.getStartTime();
            LocalDateTime existingTaskEnd = existingTaskStart.plus(task1.getDuration());

            if (newTaskStart.isBefore(existingTaskEnd) && newTaskEnd.isAfter(existingTaskStart)) {
                return true;
            }
        }
        return false;
    }*/

    private void addSubTaskToEpic(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            epic.addSubTask(subTask.getTaskId());
        }
    }

    private boolean areSubtasksEqualForStatus(List<SubTask> subtasks, TaskStatus taskStatus) {
        return !subtasks.stream()
                .anyMatch(subTask -> subTask != null && subTask.getStatus() != taskStatus);
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
        nextTaskId += 1;
        return nextTaskId;
    }
}


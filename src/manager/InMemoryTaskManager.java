package manager;

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

    @Override
    public void addTask(Task task) {
        if (!tasks.containsValue(task)) {
            boolean isOverlap = tasks.values().stream()
                    .anyMatch(task1 -> areTheTaskCross(task));
            if (!isOverlap) {
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
                    .anyMatch(this::areTheTaskCross);
            if (!isOverlap) {
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
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void removeSubTaskById(int id) {
        SubTask removableSubTask = subTasks.get(id);
        int specificEpicId = removableSubTask.getEpicId();

        removableSubTask.removeEpicId();
        subTasks.remove(id);
        historyManager.remove(id);

        final Epic specificEpic = epics.get(specificEpicId);
        int removableSubTaskId = removableSubTask.getTaskId();

        specificEpic.removeSubTask(removableSubTaskId);
        updateEpicStatus(specificEpicId);
    }

    @Override
    public void removeEpicById(int id) {
        final Epic removableEpic = epics.get(id);
        for (Integer subTaskId : removableEpic.getSubtaskIds()) {
            removeSubTaskById(subTaskId);
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
    public Map<Integer, InMemoryHistoryManager.Node> getHistory() {
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
            prioritizedTasks.addAll(
                    tasks.values().stream()
                            .filter(task -> task.getStartTime() != null)
                            .toList()
            );

            prioritizedTasks.addAll(
                    subTasks.values().stream()
                            .filter(subTask -> subTask.getStartTime() != null)
                            .toList()
            );

            prioritizedTasks.addAll(
                    epics.values().stream()
                            .filter(epic -> epic.getStartTime() != null)
                            .toList()
            );
            return prioritizedTasks;
        } else {
            return new TreeSet<>();
        }

    }

    private boolean areTheTaskCross(Task task) {
        for (Task task1 : getPrioritizedTasks()) {
            if (task.getStartTime().isBefore(task1.getEndTime()) && task.getEndTime().isAfter(task1.getStartTime()));
            return false;
        }
        return true;
    }

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


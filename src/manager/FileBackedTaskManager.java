package manager;

import exceptions.ManagerSaveException;
import task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path file;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");

    public FileBackedTaskManager(Path file) {
        super();
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        try (FileReader fr = new FileReader(fileBackedTaskManager.file.toFile());
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                Task task = parseToObject(line);
                if (task == null) {
                    continue;
                } else {
                    if (task instanceof Epic) {
                        fileBackedTaskManager.addEpic((Epic) task);
                    } else if (task instanceof SubTask) {
                        fileBackedTaskManager.addSubTask((SubTask) task);
                    } else {
                        fileBackedTaskManager.addTask(task);
                    }
                }
            }
            return fileBackedTaskManager;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла");
        }
    }

    private static TaskStatus getStatusFromTask(String str) {
        TaskStatus status;
        if (str.equals("NEW")) {
            status = TaskStatus.NEW;
        } else if (str.equals("DONE")) {
            status = TaskStatus.DONE;
        } else {
            status = TaskStatus.IN_PROGRESS;
        }
        return status;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addSubTask(SubTask subTask) {
        super.addSubTask(subTask);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeSubTaskById(int id) {
        super.removeSubTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    private static Task parseToObject(String line) {
        String[] str = line.split(",");
        Task task = null;
        String firstElement = str[0];
        String typeTask = str[1];
        String nameTask = str[2];
        String taskStatus = str[3];
        String taskDescription = str[4];
        String taskDuration = null;
        String taskStartTime = null;
        String epicIdForSubTask = null;

        if (firstElement.equals("id")) {
            return null;
        }
        if (str.length > 6 && !str[5].isEmpty()) {
            taskDuration = str[5];
        }
        if (str.length >= 7 && !str[6].isEmpty()) {
            taskStartTime = str[6];
        }
        if (str.length == 8) {
            epicIdForSubTask = str[7];
        }
        switch (typeTask) {
            case "TASK":
                task = new Task(nameTask, taskDescription, getStatusFromTask(taskStatus));
                Duration durationT = Duration.parse(taskDuration);
                task.setDuration(durationT);
                LocalDateTime startTimeTask = LocalDateTime.parse(taskStartTime, FORMATTER);
                task.setStartTime(startTimeTask);
                break;
            case "SUBTASK":
                task = new SubTask(nameTask, taskDescription, getStatusFromTask(taskStatus),
                    Integer.parseInt(epicIdForSubTask));
                Duration durationS = Duration.parse(taskDuration);
                task.setDuration(durationS);
                LocalDateTime startTimeSubTask = LocalDateTime.parse(taskStartTime, FORMATTER);
                task.setStartTime(startTimeSubTask);
                break;
            case "EPIC":
                task = new Epic(nameTask, taskDescription, getStatusFromTask(taskStatus));
                break;
            default:
                break;
        }
        return task;
    }

    private String convertTaskToString(Task task) {
        String taskType;
        if (this.getTasks().contains(task)) {
            taskType = TaskType.TASK.toString();
        } else if (this.getSubTasks().contains(task)) {
            taskType = TaskType.SUBTASK.toString();
        } else {
            taskType = TaskType.EPIC.toString();
        }
        String str;
        if (this.getEpics().contains(task)) {
            str = String.format("%s,%s,%s,%s,%s", task.getTaskId(), taskType.toUpperCase(), task.getName(),
                    task.getStatus().toString(), task.getDescription());
        } else {
            str = String.format("%s,%s,%s,%s,%s,%s,%s", task.getTaskId(), taskType.toUpperCase(), task.getName(),
                    task.getStatus().toString(), task.getDescription(), task.getDuration(),
                    task.getStartTime().format(FORMATTER));

            if (this.getSubTasks().contains(task)) {
                SubTask subTask = this.getSubTaskById(task.getTaskId());
                str += "," + subTask.getEpicId();
            }
        }
        return str;
    }

    private void save() {
        try (Writer fileWriter = new FileWriter(file.toFile(), StandardCharsets.UTF_8, false);
             BufferedWriter bw = new BufferedWriter(fileWriter); PrintWriter out = new PrintWriter(bw)) {
            out.println("id,type,name,status,description,duration,startTime,epic");
            for (Task task : this.getTasks()) {
                out.println(convertTaskToString(task));
            }
            for (Task task : this.getEpics()) {
                out.println(convertTaskToString(task));
            }
            for (Task task : this.getSubTasks()) {
                out.println(convertTaskToString(task));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи файла");
        }
    }
}

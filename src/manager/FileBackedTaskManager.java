package manager;

import task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final Path file;

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
        String[] str = line.split(",");
        Task task = null;
        if (str[0].equals("id")) {
            return null;
        }
        switch (str[1]) {
            case "TASK":
                task = new Task(str[2], str[4], getStatusFromTask(str[3]));
                Duration durationT = Duration.parse(str[5]);
                task.setDuration(durationT);
                LocalDateTime startTimeTask = LocalDateTime.parse(str[6], formatter);
                task.setStartTime(startTimeTask);
                break;
            case "SUBTASK":
                task = new SubTask(str[2], str[4], getStatusFromTask(str[3]),
                    Integer.parseInt(str[7]));
                Duration durationS = Duration.parse(str[5]);
                task.setDuration(durationS);
                LocalDateTime startTimeSubTask = LocalDateTime.parse(str[6], formatter);
                task.setStartTime(startTimeSubTask);
                break;
            case "EPIC":
                task = new Epic(str[2], str[4], getStatusFromTask(str[3]));
                break;
            default:
                break;
        }
        return task;
    }

    private String parseTask(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
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
                    task.getStartTime().format(formatter));

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
                out.println(parseTask(task));
            }
            for (Task task : this.getEpics()) {
                out.println(parseTask(task));
            }
            for (Task task : this.getSubTasks()) {
                out.println(parseTask(task));
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи файла");
        }
    }
}

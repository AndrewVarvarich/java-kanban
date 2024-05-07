package manager;

import task.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private Path file;

    public FileBackedTaskManager(Path file) {
        super();
        this.file = file;
    }

    public static void main(String[] args) throws IOException {
        Path dir = Files.createDirectories(Paths.get("C://Intel/Java My Projects/java-kanban"));
        Path file = dir.resolve("data.csv");
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
        fileBackedTaskManager.addTask(task1);
        Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
        fileBackedTaskManager.addTask(task2);
        Epic epic1 = new Epic("Собрать пазл", "Выделить 2 часа на это", TaskStatus.NEW);
        fileBackedTaskManager.addEpic(epic1);
        Epic epic2 = new Epic("Собрать компьютер", "Нужна помощь друзей", TaskStatus.NEW);
        fileBackedTaskManager.addEpic(epic2);
        SubTask subTask1 = new SubTask("Разложить все кусочки пазла на столе рубашкой вверх", "Я не " +
                "люблю это занятие", TaskStatus.NEW, epic1.getTaskId());
        fileBackedTaskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Соединить все кусочки в картинку", "Не сломать кусочки",
                TaskStatus.NEW, epic1.getTaskId());
        fileBackedTaskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask("Позвать друзей и собрать пазл", "Не забыть про напитки",
                TaskStatus.NEW, epic1.getTaskId());
        fileBackedTaskManager.addSubTask(subTask3);
        FileBackedTaskManager fileBackedTaskManager1 = loadFromFile(file);
        List<Task> list1 = new ArrayList<>(fileBackedTaskManager.getTasks());
        List<Task> list2 = new ArrayList<>(fileBackedTaskManager1.getTasks());
        if (list1.equals(list2)) {
            System.out.println("Переменные объектов равны, следовательно они идентичны");
        }
    }

    public static FileBackedTaskManager loadFromFile(Path file) {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
        try (FileReader fr = new FileReader(fileBackedTaskManager.file.toFile());
             BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                Task task = parseTask(line);
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

    private void save() {
        try (Writer fileWriter = new FileWriter(file.toFile(), StandardCharsets.UTF_8, false);
             BufferedWriter bw = new BufferedWriter(fileWriter); PrintWriter out = new PrintWriter(bw)) {
            out.println("id,type,name,status,description,epic");
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

    public String parseTask(Task task) {
        String taskType;
        if (this.getTasks().contains(task)) {
            taskType = TaskType.TASK.toString();
        } else if (this.getSubTasks().contains(task)) {
            taskType = TaskType.SUBTASK.toString();
        } else {
            taskType = TaskType.EPIC.toString();
        }

        String str = String.format("%s,%s,%s,%s,%s", task.getTaskId(), taskType.toUpperCase(), task.getName(),
                task.getStatus().toString(), task.getDescription());

        if (this.getSubTasks().contains(task)) {
            SubTask subTask = this.getSubTaskById(task.getTaskId());
            str += "," + subTask.getEpicId();
        }
        return str;
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
    public void removeSubTaskById(int id, boolean updateEpic) {
        super.removeSubTaskById(id, updateEpic);
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
    public void clearTask() {
        super.clearTask();
        save();
    }

    @Override
    public void clearSubTask() {
        super.clearSubTask();
        save();
    }

    @Override
    public void clearEpic() {
        super.clearEpic();
        save();
    }

    private static Task parseTask(String line) {
        String[] str = line.split(",");
        Task task = null;
        if (str[0].equals("id")) {
            return null;
        }
        task = switch (str[1]) {
            case "TASK" -> new Task(str[2], str[4], getStatusFromTask(str[3]));
            case "SUBTASK" -> new SubTask(str[2], str[4], getStatusFromTask(str[3]),
                    Integer.parseInt(str[5]));
            case "EPIC" -> new Epic(str[2], str[4], getStatusFromTask(str[3]));
            default -> task;
        };
        return task;
    }
}

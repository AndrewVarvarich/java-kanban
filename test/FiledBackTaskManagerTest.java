import manager.*;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FiledBackTaskManagerTest {

    @Test
    void testNoExceptionsThrown() {
        try {
            Path tempFile = Files.createTempFile("dataTest", ".csv");
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
            FileBackedTaskManager fileBackedTaskManager1 = FileBackedTaskManager.loadFromFile(tempFile);
            assert fileBackedTaskManager1 != null : "fileBackedTaskManager1 не должен быть null";
        } catch (IOException e) {
            System.out.println("Ошибка восстановления или записи файла");
        }
    }

    @Test
    void savingMultipleTasks() {
        try {
            Path tempFile = Files.createTempFile("dataTest", ".csv");
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
            Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
            fileBackedTaskManager.addTask(task1);
            Epic epic1 = new Epic("Посетить врача", "Нужны деньги", TaskStatus.NEW);
            fileBackedTaskManager.addEpic(epic1);
            SubTask subTask1 = new SubTask("Записаться ко врачу", "Узнать телефон знакомого врача",
                    TaskStatus.NEW, epic1.getTaskId());
            fileBackedTaskManager.addSubTask(subTask1);
            FileBackedTaskManager fileBackedTaskManager1 = FileBackedTaskManager.loadFromFile(tempFile);
            assertNotNull(fileBackedTaskManager.getTasks());
            assertNotNull(fileBackedTaskManager.getSubTasks());
            assertNotNull(fileBackedTaskManager.getEpics());
        } catch (IOException e) {
            System.out.println("Ошибка восстановления или записи файла");
        }
    }

    @Test
    void loadingMultipleTasks() {
        try {
            Path tempFile = Files.createTempFile("dataTest", ".csv");
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
            Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
            fileBackedTaskManager.addTask(task1);
            Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
            fileBackedTaskManager.addTask(task2);
            FileBackedTaskManager fileBackedTaskManager1 = FileBackedTaskManager.loadFromFile(tempFile);
            assertNotNull(fileBackedTaskManager1);
        } catch (IOException e) {
            System.out.println("Ошибка восстановления или записи файла");
        }
    }
}

import manager.*;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FiledBackTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @Test
    void savingMultipleTasks() {
        try {
            Path tempFile = Files.createTempFile("dataTest", ".csv");
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
            Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
            task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task1.setStartTime(LocalDateTime.of(2042, 1,23,15,1));
            fileBackedTaskManager.addTask(task1);
            Epic epic1 = new Epic("Посетить врача", "Нужны деньги", TaskStatus.NEW);
            fileBackedTaskManager.addEpic(epic1);
            SubTask subTask1 = new SubTask("Записаться ко врачу", "Узнать телефон знакомого врача",
                    TaskStatus.NEW, epic1.getTaskId());
            subTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            subTask1.setStartTime(LocalDateTime.of(2043, 1,26,15,1));
            fileBackedTaskManager.addSubTask(subTask1);
            FileBackedTaskManager fileBackedTaskManager1 = FileBackedTaskManager.loadFromFile(tempFile);
            assertNotNull(fileBackedTaskManager.getTasks());
            assertNotNull(fileBackedTaskManager.getSubTasks());
            assertNotNull(fileBackedTaskManager.getEpics());
        } catch (IOException e) {
            System.out.println("Ошибка восстановления или записи файла");
        }
    }
}

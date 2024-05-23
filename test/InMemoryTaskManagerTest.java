import manager.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import task.Epic;
import task.SubTask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @BeforeAll
    public static void setUp() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void shouldBePositiveIfEpicExistInSubTask() {
        Epic epic1 = new Epic("Сделать яичницу", "15 минут", TaskStatus.NEW);
        taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Купить яйца", "Потратить не более 100р", TaskStatus.NEW,
                epic1.getTaskId());
        subTask1.setStartTime(LocalDateTime.of(2000, 1, 6, 1, 1));
        subTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addSubTask(subTask1);
        assertNotNull(subTask1.getEpicId());
    }

    @Test
    public void areTheTaskCrossTest() {
        Task task1 = new Task("Выйти поиграть с друзьями", "Взять воды", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2000, 1, 6, 1, 1));
        task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addTask(task1);
        Task task2 = new Task("Запланировать отпуск", "Узнать стоимость билета", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2000, 1, 6, 1, 1));
        task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.add(task2);
        assertEquals(1, taskManager.getTasks().size());
    }
}
import manager.*;
import org.junit.Test;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private static final HistoryManager historyManager = Managers.getDefaultHistory();
    private static final TaskManager taskManager = Managers.getDefault();

    @Test
    public void shouldBePassIfHistoryIsEmpty() {
        for (Task task : historyManager.getTasksHistory()) {
            historyManager.remove(task.getTaskId());
        }
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    public void shouldBePositiveIfThereIsNoDuplication() {
        for (Task task : historyManager.getTasksHistory()) {
            historyManager.remove(task.getTaskId());
        }
        Task task1 = new Task("Выйти поиграть с друзьями", "Взять воды", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2015, 5, 6, 1, 1));
        task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addTask(task1);
        Task task2 = new Task("Запланировать отпуск", "Узнать стоимость билета", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2015, 4, 7, 1, 1));
        task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addTask(task2);
        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        taskManager.getTaskById(task1.getTaskId());
        assertEquals(2L, historyManager.getHistory().values().size());
    }

    @Test
    public void shouldRemoveFromHistory() {
        taskManager.clearTasks();
        Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
        task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task1.setStartTime(LocalDateTime.of(1999, 1, 1, 10, 0));
        taskManager.addTask(task1);
        Task task2 = new Task("Сходить в кофешоп", "Купить кофе", TaskStatus.NEW);
        task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task2.setStartTime(LocalDateTime.of(1999, 2, 1, 10, 0));
        taskManager.addTask(task2);
        Task task3 = new Task("Сходить в спортзал", "Попить воду", TaskStatus.NEW);
        task3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task3.setStartTime(LocalDateTime.of(1999, 3, 1, 10, 0));
        taskManager.addTask(task3);

        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        taskManager.getTaskById(task3.getTaskId());

        taskManager.removeTaskById(task1.getTaskId());
        assertNull(historyManager.getHistory().get(task1.getTaskId()));

        taskManager.removeTaskById(task2.getTaskId());
        assertNull(historyManager.getHistory().get(task2.getTaskId()));

        taskManager.removeTaskById(task3.getTaskId());
        assertNull(historyManager.getHistory().get(task3.getTaskId()));
    }
}

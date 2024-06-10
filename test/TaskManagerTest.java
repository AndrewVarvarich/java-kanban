import manager.*;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    static TaskManager taskManager = Managers.getDefault();
    static HistoryManager historyManager = Managers.getDefaultHistory();

    @BeforeEach
    void beforeEach() {
        taskManager.clearTasks();
        taskManager.clearSubTasks();
        taskManager.clearEpics();
        for (Task task : historyManager.getTasksHistory()) {
            historyManager.remove(task.getTaskId());
        }
    }

    @Test
    void shouldBePositiveIfTaskIdAreEqual() {
        for (Task task : taskManager.getTasks()) {
            assertEquals(task, taskManager.getTaskById(task.getTaskId()));
        }
    }

    @Test
    void shouldBePositiveIfSubtaskIdAreEqual() {
        for (SubTask subTask : taskManager.getSubTasks()) {
            assertEquals(subTask, taskManager.getSubTaskById(subTask.getTaskId()));
        }
    }

    @Test
    void shouldBePositiveIfEpicIdAreEqual() {
        for (Epic epic : taskManager.getEpics()) {
            assertEquals(epic, taskManager.getEpicById(epic.getTaskId()));
        }
    }

    @Test
    void shouldBePassIfUtilityClassCreateAnObject() {
        assertNotNull(taskManager);
    }

    @Test
    void shouldBePositiveIfAddingOverlappingTasksThrowsException() {
        Task firstTask = new Task("Первая задача", "Описание", TaskStatus.NEW);
        firstTask.setStartTime(LocalDateTime.of(2001, 2, 2, 2, 2));
        firstTask.setDuration(Duration.of(1, ChronoUnit.HOURS));
        taskManager.addTask(firstTask);

        Task overlappingTask = new Task("Вторая задача", "Описание", TaskStatus.NEW);
        overlappingTask.setStartTime(LocalDateTime.of(2001, 2, 2, 2, 30));
        overlappingTask.setDuration(Duration.of(1, ChronoUnit.HOURS));

        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(overlappingTask));
    }

        @Test
        void shouldBePositiveIfTheObjectHasNoChange() {
            Task task5 = new Task("Установить дверь", "Посмотреть крепеж", TaskStatus.NEW);
            Duration duration = Duration.of(10, ChronoUnit.MINUTES);
            task5.setDuration(duration);
            task5.setStartTime(LocalDateTime.of(2003, 5, 1, 1, 1));
            taskManager.addTask(task5);
            assertEquals(task5.getTaskId(), taskManager.getTaskById(task5.getTaskId()).getTaskId());
            assertEquals(task5.getName(), taskManager.getTaskById(task5.getTaskId()).getName());
            assertEquals(task5.getStatus(), taskManager.getTaskById(task5.getTaskId()).getStatus());
        }

        @Test
        void shouldBePositiveIfObjectsAreDifferent() {
            Task task6 = new Task("Купить кровать", "Посмотреть бельё", TaskStatus.NEW);
            task6.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task6.setStartTime(LocalDateTime.of(2004, 5, 1, 13, 1));
            taskManager.addTask(task6);
            taskManager.getTaskById(task6.getTaskId());
            Task task7 = new Task("Купить кровать", "Посмотреть бельё", TaskStatus.IN_PROGRESS);
            taskManager.updateTask(task7, task6.getTaskId());
            taskManager.getTaskById(task7.getTaskId());
            List<Task> historyTasks = historyManager.getTasksHistory();
            assertNotSame(task6, historyTasks.getFirst());
        }

        @Test
        void shouldBePositiveIfManagerWorksCorrectly() {
            Task task8 = new Task("Заказать самокат", "Купить воду", TaskStatus.DONE);
            task8.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task8.setStartTime(LocalDateTime.of(2005, 5, 1, 1, 1));
            taskManager.addTask(task8);
            Epic epic2 = new Epic("Позаниматься английским", "Выделить 2 часа на это", TaskStatus.DONE);
            taskManager.addEpic(epic2);
            SubTask subTask2 = new SubTask("Открыть новый учебник и включить изложение", "Я не " +
                    "люблю это занятие", TaskStatus.DONE, epic2.getTaskId());
            subTask2.setStartTime(LocalDateTime.of(2006, 1, 1, 1, 1));
            subTask2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            taskManager.addSubTask(subTask2);

            assertNotNull(taskManager.getTasks());
            assertNotNull(taskManager.getSubTasks());
            assertNotNull(taskManager.getEpics());
            assertNotNull(taskManager.getTaskById(task8.getTaskId()));
            assertNotNull(taskManager.getSubTaskById(subTask2.getTaskId()));
            assertNotNull(taskManager.getEpicById(epic2.getTaskId()));
        }

        @Test
        void testSelfAdditionShouldBePassIfAnExceptionWillBeThrown() {
            Epic epic3 = new Epic("Придумать стих", "Выделить 2 часа на это", TaskStatus.DONE);
            taskManager.addEpic(epic3);
            SubTask subTask3 = new SubTask("Изучить виды рифмовки", "Открыть интернет и попробовать",
                    TaskStatus.NEW, epic3.getTaskId());
            subTask3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            subTask3.setStartTime(LocalDateTime.of(2007, 1, 2, 1, 1));
            taskManager.addSubTask(subTask3);
            SubTask subTask4 = new SubTask("Попить водички", "Открыть бутылку",
                    TaskStatus.NEW, subTask3.getTaskId());
            assertThrows(NullPointerException.class, () -> taskManager.addSubTask(subTask4));
        }

        @Test
        void shouldSetEpicIdTo0WhenSubtaskRemove() {
            taskManager.clearSubTasks();
            Epic epic1 = new Epic("Сделать яичницу", "15 минут", TaskStatus.NEW);
            taskManager.addEpic(epic1);
            SubTask subTask1 = new SubTask("Купить яйца", "Потратить не более 100р", TaskStatus.NEW,
                    epic1.getTaskId());
            subTask1.setStartTime(LocalDateTime.of(2019, 1, 6, 16, 1));
            subTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            taskManager.addSubTask(subTask1);
            taskManager.removeSubTaskById(subTask1.getTaskId());
            assertEquals(0, taskManager.getSubTasks().size());
        }

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
        void loadingMultipleTasks() {
            try {
                Path tempFile = Files.createTempFile("dataTest", ".csv");
                FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
                Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
                task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
                task1.setStartTime(LocalDateTime.of(2032, 1,20,15,1));
                fileBackedTaskManager.addTask(task1);
                Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
                task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
                task2.setStartTime(LocalDateTime.of(2033, 1,21,15,1));
                fileBackedTaskManager.addTask(task2);
                FileBackedTaskManager fileBackedTaskManager1 = FileBackedTaskManager.loadFromFile(tempFile);
                assertNotNull(fileBackedTaskManager1);
            } catch (IOException e) {
                System.out.println("Ошибка восстановления или записи файла");
            }
        }
}

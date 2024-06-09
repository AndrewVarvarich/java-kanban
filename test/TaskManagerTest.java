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
        firstTask.setStartTime(LocalDateTime.of(2222, 2, 2, 2, 2));
        firstTask.setDuration(Duration.of(1, ChronoUnit.HOURS));
        taskManager.addTask(firstTask);

        // Попытка добавить задачу, которая пересекается с первой
        Task overlappingTask = new Task("Вторая задача", "Описание", TaskStatus.NEW);
        overlappingTask.setStartTime(LocalDateTime.of(2222, 2, 2, 2, 30));
        overlappingTask.setDuration(Duration.of(1, ChronoUnit.HOURS));

        // Проверка, что выбрасывается исключение при попытке добавления
        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(overlappingTask));
    }

        @Test
        void shouldBePositiveIfTheObjectHasNoChange() {
            Task task5 = new Task("Установить дверь", "Посмотреть крепеж", TaskStatus.NEW);
            Duration duration = Duration.of(10, ChronoUnit.MINUTES);
            task5.setDuration(duration);
            task5.setStartTime(LocalDateTime.of(2020, 5, 1, 1, 1));
            taskManager.addTask(task5);
            assertEquals(task5.getTaskId(), taskManager.getTaskById(task5.getTaskId()).getTaskId());
            assertEquals(task5.getName(), taskManager.getTaskById(task5.getTaskId()).getName());
            assertEquals(task5.getStatus(), taskManager.getTaskById(task5.getTaskId()).getStatus());
        }

        @Test
        void shouldBePositiveIfObjectsAreDifferent() {
            Task task6 = new Task("Купить кровать", "Посмотреть бельё", TaskStatus.NEW);
            task6.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task6.setStartTime(LocalDateTime.of(2020, 5, 1, 13, 1));
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
            task8.setStartTime(LocalDateTime.of(2017, 5, 1, 1, 1));
            taskManager.addTask(task8);
            Epic epic2 = new Epic("Позаниматься английским", "Выделить 2 часа на это", TaskStatus.DONE);
            taskManager.addEpic(epic2);
            SubTask subTask2 = new SubTask("Открыть новый учебник и включить изложение", "Я не " +
                    "люблю это занятие", TaskStatus.DONE, epic2.getTaskId());
            subTask2.setStartTime(LocalDateTime.of(2000, 1, 1, 1, 1));
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
            subTask3.setStartTime(LocalDateTime.of(2000, 1, 2, 1, 1));
            taskManager.addSubTask(subTask3);
            SubTask subTask4 = new SubTask("Попить водички", "Открыть бутылку",
                    TaskStatus.NEW, subTask3.getTaskId());
            assertThrows(NullPointerException.class, () -> taskManager.addSubTask(subTask4));
        }

        // Метод для проверки того, что встроенный связный список корректно работает при операциях удаления
        @Test
        void shouldBePositiveIfRemoveIsWorkingCorrectly() {
            Task task1 = new Task("Сходить в ресторан", "Покушать салатик", TaskStatus.NEW);
            task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task1.setStartTime(LocalDateTime.of(1999, 5, 1, 1, 1));
            taskManager.addTask(task1);
            Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
            task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task2.setStartTime(LocalDateTime.of(1999, 6, 1, 1, 1));
            taskManager.addTask(task2);
            taskManager.getTaskById(task1.getTaskId());
            taskManager.getTaskById(task2.getTaskId());
            List<Task> list1 = historyManager.getTasksHistory();
            taskManager.removeTaskById(task1.getTaskId());
            List<Task> list2 = historyManager.getTasksHistory();
            assertNotEquals(list1, list2);
        }

        // Метод для проверки того, что встроенный связный список корректно работает при операциях добавления
        @Test
        void shouldBePositiveIfAdditionIsWorkingCorrectly() {
            taskManager.clearTasks();
            for (Task task : historyManager.getTasksHistory()) {
                historyManager.remove(task.getTaskId());
            }
            Task task1 = new Task("Выйти поиграть с друзьями", "Взять воды", TaskStatus.NEW);
            task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task1.setStartTime(LocalDateTime.of(2008, 5, 1, 1, 1));
            taskManager.addTask(task1);
            Task task2 = new Task("Запланировать отпуск", "Узнать стоимость билета", TaskStatus.NEW);
            task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task2.setStartTime(LocalDateTime.of(2008, 6, 1, 1, 1));
            taskManager.addTask(task2);
            taskManager.getTaskById(task1.getTaskId());
            taskManager.getTaskById(task2.getTaskId());
            List<Task> list1 = historyManager.getTasksHistory();
            Task task3 = new Task("Выгулять собаку", "Не забыть взять с собой игрушки", TaskStatus.NEW);
            task3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task3.setStartTime(LocalDateTime.of(208, 4, 1, 1, 1));
            taskManager.addTask(task3);
            taskManager.getTaskById(task3.getTaskId());
            List<Task> list2 = historyManager.getTasksHistory();
            assertNotEquals(list1, list2);
        }

        @Test
        void shouldBePositiveIfEpicDoesntHaveIrrelevantSubtaskIds() {
            Epic epic1 = new Epic("Приготовить романтический ужин", "Выделить 2 часа на это",
                    TaskStatus.NEW);
            taskManager.addEpic(epic1);
            SubTask subTask1 = new SubTask("Купить продуктов на ужин", "Составить список",
                    TaskStatus.NEW, epic1.getTaskId());
            subTask1.setStartTime(LocalDateTime.of(2000, 1, 3, 1, 1));
            subTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            taskManager.addSubTask(subTask1);
            SubTask subTask2 = new SubTask("Выложить все продукты на столе и подготовить к приготовлению",
                    "Мыть руки после каждого прикасновения к чему-либо", TaskStatus.NEW, epic1.getTaskId());
            subTask2.setStartTime(LocalDateTime.of(2000, 1, 4, 1, 1));
            subTask2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            taskManager.addSubTask(subTask2);
            SubTask subTask3 = new SubTask("Накрыть на стол", "Не забыть про вино!",
                    TaskStatus.NEW, epic1.getTaskId());
            subTask3.setStartTime(LocalDateTime.of(2000, 1, 5, 1, 1));
            subTask3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            taskManager.addSubTask(subTask3);
            taskManager.removeSubTaskById(subTask1.getTaskId());
            List<Integer> arr1 = epic1.getSubtaskIds();
            assertEquals(2, arr1.size());
        }

        @Test
        void shouldBePositiveIfTasksHistoryWorkCorrectly() {
            List<Task> list2 = historyManager.getTasksHistory();
            for (Task task : list2) {
                historyManager.remove(task.getTaskId());
            }
            taskManager.clearTasks();
            taskManager.clearSubTasks();
            taskManager.clearEpics();
            Task task1 = new Task("Поиграть в доту", "Получить жетоны чтобы пройти дальше",
                    TaskStatus.NEW);
            task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task1.setStartTime(LocalDateTime.of(2006, 5, 1, 1, 1));
            taskManager.addTask(task1);
            Task task2 = new Task("Поиграть в валорант с другом", "Апнуть звание", TaskStatus.NEW);
            task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task2.setStartTime(LocalDateTime.of(2006, 1, 1, 1, 1));
            taskManager.addTask(task2);
            Task task3 = new Task("Приготоваить покушать", "Купить курицу", TaskStatus.NEW);
            task3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            task3.setStartTime(LocalDateTime.of(2006, 3, 1, 1, 1));
            taskManager.addTask(task3);
            taskManager.getTaskById(task1.getTaskId());
            taskManager.getTaskById(task2.getTaskId());
            taskManager.getTaskById(task3.getTaskId());
            List<Task> list1 = historyManager.getTasksHistory();
            assertEquals(3, list1.size());
        }

        @Test
        void shouldSetEpicIdTo0WhenSubtaskRemove() {
            taskManager.clearSubTasks();
            Epic epic1 = new Epic("Сделать яичницу", "15 минут", TaskStatus.NEW);
            taskManager.addEpic(epic1);
            SubTask subTask1 = new SubTask("Купить яйца", "Потратить не более 100р", TaskStatus.NEW,
                    epic1.getTaskId());
            subTask1.setStartTime(LocalDateTime.of(2000, 1, 6, 16, 1));
            subTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
            taskManager.addSubTask(subTask1);
            taskManager.removeSubTaskById(subTask1.getTaskId());
            assertEquals(0, taskManager.getSubTasks().size());
        }

        @Test
        void shouldBePositiveIfEpicStatusNEW() {
            taskManager.clearTasks();
            taskManager.clearSubTasks();
            taskManager.clearEpics();
            Duration duration = Duration.of(1, ChronoUnit.HOURS);
            LocalDateTime date1 = LocalDateTime.of(2024, 5, 15, 9, 0);
            LocalDateTime date2 = LocalDateTime.of(2024, 5, 15, 11, 0);
            LocalDateTime date3 = LocalDateTime.of(2024, 5, 15, 13, 0);
            Epic epic1 = new Epic("Сдать проект", "Убедиться что материал подготовлен", TaskStatus.NEW);
            taskManager.addEpic(epic1);
            SubTask sTask1 = new SubTask("Провести исследование", "Собрать ископаемые", TaskStatus.NEW,
                    epic1.getTaskId());
            sTask1.setDuration(duration);
            sTask1.setStartTime(date1);
            taskManager.addSubTask(sTask1);
            SubTask sTask2 = new SubTask("Записать результаты исследования", "Детально описать ископаемые",
                    TaskStatus.NEW, epic1.getTaskId());
            sTask2.setDuration(duration);
            sTask2.setStartTime(date2);
            taskManager.addSubTask(sTask2);
            SubTask sTask3 = new SubTask("Подготовить доклад и презентацию", "Распределить задачи по " +
                    "группе", TaskStatus.NEW, epic1.getTaskId());
            sTask3.setDuration(duration);
            sTask3.setStartTime(date3);
            taskManager.addSubTask(sTask3);
            assertEquals(epic1.getStatus(), TaskStatus.NEW);
        }

        @Test
        void shouldBePositiveIfEpicStatusDONE() {
            taskManager.clearTasks();
            taskManager.clearSubTasks();
            taskManager.clearEpics();
            Duration duration = Duration.of(1, ChronoUnit.HOURS);
            LocalDateTime date1 = LocalDateTime.of(2024, 5, 16, 9, 0);
            LocalDateTime date2 = LocalDateTime.of(2024, 5, 17, 11, 0);
            LocalDateTime date3 = LocalDateTime.of(2024, 5, 18, 13, 0);
            Epic epic1 = new Epic("Сдать проект", "Убедиться что материал подготовлен", TaskStatus.NEW);
            taskManager.addEpic(epic1);
            SubTask sTask1 = new SubTask("Провести исследование", "Собрать ископаемые", TaskStatus.DONE,
                    epic1.getTaskId());
            sTask1.setDuration(duration);
            sTask1.setStartTime(date1);
            taskManager.addSubTask(sTask1);
            SubTask sTask2 = new SubTask("Записать результаты исследования", "Детально описать ископаемые",
                    TaskStatus.DONE, epic1.getTaskId());
            sTask2.setDuration(duration);
            sTask2.setStartTime(date2);
            taskManager.addSubTask(sTask2);
            SubTask sTask3 = new SubTask("Подготовить доклад и презентацию", "Распределить задачи по " +
                    "группе", TaskStatus.DONE, epic1.getTaskId());
            sTask3.setDuration(duration);
            sTask3.setStartTime(date3);
            taskManager.addSubTask(sTask3);
            assertEquals(epic1.getStatus(), TaskStatus.DONE);
        }

        @Test
        void shouldBePositiveIfEpicStatusIN_PROGRESS() {
            taskManager.clearTasks();
            taskManager.clearSubTasks();
            taskManager.clearEpics();
            Duration duration = Duration.of(1, ChronoUnit.MINUTES);
            LocalDateTime date1 = LocalDateTime.of(2024, 5, 19, 9, 0);
            LocalDateTime date2 = LocalDateTime.of(2023, 5, 20, 11, 0);
            LocalDateTime date3 = LocalDateTime.of(2022, 5, 21, 13, 0);
            Epic epic1 = new Epic("Сдать проект", "Убедиться что материал подготовлен", TaskStatus.NEW);
            taskManager.addEpic(epic1);
            SubTask sTask1 = new SubTask("Провести исследование", "Собрать ископаемые", TaskStatus.DONE,
                    epic1.getTaskId());
            sTask1.setDuration(duration);
            sTask1.setStartTime(date3);
            taskManager.addSubTask(sTask1);
            SubTask sTask2 = new SubTask("Записать результаты исследования", "Детально описать ископаемые",
                    TaskStatus.NEW, epic1.getTaskId());
            sTask2.setDuration(duration);
            sTask2.setStartTime(date2);
            taskManager.addSubTask(sTask2);
            SubTask sTask3 = new SubTask("Подготовить доклад и презентацию", "Распределить задачи по " +
                    "группе", TaskStatus.DONE, epic1.getTaskId());
            sTask3.setDuration(duration);
            sTask3.setStartTime(date1);
            taskManager.addSubTask(sTask3);
            assertEquals(epic1.getStatus(), TaskStatus.IN_PROGRESS);
        }

        @Test
        void shouldBePositiveIfEpicStatusIN_PROGRESSWhenSubTaskIN_PROGRESS() {
            taskManager.clearTasks();
            taskManager.clearSubTasks();
            taskManager.clearEpics();
            Duration duration = Duration.of(1, ChronoUnit.HOURS);
            LocalDateTime date1 = LocalDateTime.of(2024, 5, 22, 9, 0);
            LocalDateTime date2 = LocalDateTime.of(2024, 5, 23, 11, 0);
            LocalDateTime date3 = LocalDateTime.of(2024, 5, 24, 13, 0);
            Epic epic1 = new Epic("Сдать проект", "Убедиться что материал подготовлен", TaskStatus.NEW);
            taskManager.addEpic(epic1);
            SubTask sTask1 = new SubTask("Провести исследование", "Собрать ископаемые",
                    TaskStatus.IN_PROGRESS, epic1.getTaskId());
            sTask1.setDuration(duration);
            sTask1.setStartTime(date1);
            taskManager.addSubTask(sTask1);
            SubTask sTask2 = new SubTask("Записать результаты исследования", "Детально описать ископаемые",
                    TaskStatus.IN_PROGRESS, epic1.getTaskId());
            sTask2.setDuration(duration);
            sTask2.setStartTime(date2);
            taskManager.addSubTask(sTask2);
            SubTask sTask3 = new SubTask("Подготовить доклад и презентацию", "Распределить задачи по " +
                    "группе", TaskStatus.IN_PROGRESS, epic1.getTaskId());
            sTask3.setDuration(duration);
            sTask3.setStartTime(date3);
            taskManager.addSubTask(sTask3);
            assertEquals(epic1.getStatus(), TaskStatus.IN_PROGRESS);
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
                task1.setStartTime(LocalDateTime.of(2020, 1,20,15,1));
                fileBackedTaskManager.addTask(task1);
                Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
                task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
                task2.setStartTime(LocalDateTime.of(2020, 1,21,15,1));
                fileBackedTaskManager.addTask(task2);
                FileBackedTaskManager fileBackedTaskManager1 = FileBackedTaskManager.loadFromFile(tempFile);
                assertNotNull(fileBackedTaskManager1);
            } catch (IOException e) {
                System.out.println("Ошибка восстановления или записи файла");
            }
        }
}

import manager.*;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import task.Epic;
import task.SubTask;
import task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    public void setting() {
        clearHistoryManager();
        clearTaskManager();
    }

    @Test
    public void shouldBePositiveIfEpicExistInSubTask() {
        Epic epic1 = new Epic("Сделать яичницу", "15 минут", TaskStatus.NEW);
        taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Купить яйца", "Потратить не более 100р", TaskStatus.NEW,
                epic1.getTaskId());
        subTask1.setStartTime(LocalDateTime.of(2034, 1, 6, 1, 1));
        subTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addSubTask(subTask1);
        assertNotNull(subTask1.getEpicId());
    }

    @Test
    public void areTheTaskCrossTest() {
        clearTaskManager();
        Task task1 = new Task("Выйти поиграть с друзьями", "Взять воды", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2035, 1, 6, 1, 1));
        task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addTask(task1);
        Task task2 = new Task("Запланировать отпуск", "Узнать стоимость билета", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2036, 1, 6, 1, 1));
        task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.add(task2);
        assertEquals(1, taskManager.getTasks().size());
    }

    @Test
    public void shouldBePositiveIfEpicDoesntHaveIrrelevantSubtaskIds() {
        Epic epic1 = new Epic("Приготовить романтический ужин", "Выделить 2 часа на это",
                TaskStatus.NEW);
        taskManager.addEpic(epic1);

        SubTask subTask1 = new SubTask("Купить продуктов на ужин", "Составить список",
                TaskStatus.NEW, epic1.getTaskId());
        subTask1.setStartTime(LocalDateTime.of(2013, 1, 3, 1, 1));
        subTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addSubTask(subTask1);

        SubTask subTask2 = new SubTask("Выложить все продукты на столе и подготовить к приготовлению",
                "Мыть руки после каждого прикасновения к чему-либо", TaskStatus.NEW, epic1.getTaskId());
        subTask2.setStartTime(LocalDateTime.of(2014, 1, 4, 1, 1));
        subTask2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addSubTask(subTask2);

        SubTask subTask3 = new SubTask("Накрыть на стол", "Не забыть про вино!",
                TaskStatus.NEW, epic1.getTaskId());
        subTask3.setStartTime(LocalDateTime.of(2015, 1, 5, 1, 1));
        subTask3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        taskManager.addSubTask(subTask3);

        taskManager.removeSubTaskById(subTask1.getTaskId());

        List<Integer> arr1 = epic1.getSubtaskIds();

        assertEquals(2, arr1.size());
    }

    @Test
    public void shouldBePositiveIfEpicStatusNEW() {
        Duration duration = Duration.of(1, ChronoUnit.HOURS);
        LocalDateTime date1 = LocalDateTime.of(2020, 5, 15, 9, 0);
        LocalDateTime date2 = LocalDateTime.of(2021, 5, 15, 11, 0);
        LocalDateTime date3 = LocalDateTime.of(2022, 5, 15, 13, 0);
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

        assertEquals(TaskStatus.NEW, epic1.getStatus());
    }

    @Test
    public void shouldBePositiveIfEpicStatusIN_PROGRESSWhenSubTaskIN_PROGRESS() {
        clearTaskManager();
        Duration duration = Duration.of(1, ChronoUnit.HOURS);
        LocalDateTime date1 = LocalDateTime.of(2029, 5, 22, 9, 0);
        LocalDateTime date2 = LocalDateTime.of(2030, 6, 23, 11, 0);
        LocalDateTime date3 = LocalDateTime.of(2031, 7, 24, 13, 0);
        Epic epic1 = new Epic("Сдать проект", "Убедиться что материал подготовлен", TaskStatus.NEW);
        taskManager.addEpic(epic1);
        SubTask sTask1 = new SubTask("Провести исследование", "Собрать ископаемые",
                TaskStatus.IN_PROGRESS, epic1.getTaskId());
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

        assertEquals(TaskStatus.IN_PROGRESS, epic1.getStatus());
    }

    @Test
    public void shouldBePositiveIfEpicStatusIN_PROGRESS() {
        clearTaskManager();
        Epic epic1 = new Epic("Сдать проект", "Убедиться что материал подготовлен", TaskStatus.NEW);
        taskManager.addEpic(epic1);
        SubTask sTask1 = new SubTask("Провести исследование", "Собрать ископаемые", TaskStatus.DONE,
                epic1.getTaskId());
        sTask1.setDuration(Duration.ofMinutes(1));
        sTask1.setStartTime(LocalDateTime.of(2026, 5, 19, 9, 0));
        taskManager.addSubTask(sTask1);
        SubTask sTask2 = new SubTask("Записать результаты исследования", "Детально описать ископаемые",
                TaskStatus.NEW, epic1.getTaskId());
        sTask2.setDuration(Duration.ofMinutes(2));
        sTask2.setStartTime(LocalDateTime.of(2027, 5, 20, 11, 0));
        taskManager.addSubTask(sTask2);
        SubTask sTask3 = new SubTask("Подготовить доклад и презентацию", "Распределить задачи по " +
                "группе", TaskStatus.DONE, epic1.getTaskId());
        sTask3.setDuration(Duration.ofMinutes(3));
        sTask3.setStartTime(LocalDateTime.of(2028, 5, 21, 13, 0));
        taskManager.addSubTask(sTask3);
        assertEquals(epic1.getStatus(), TaskStatus.IN_PROGRESS);
    }

    @Test
    public void shouldBePositiveIfRemoveIsWorkingCorrectly() {
        clearTaskManager();
        clearHistoryManager();
        Task task1 = new Task("Сходить в ресторан", "Покушать салатик", TaskStatus.NEW);
        task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task1.setStartTime(LocalDateTime.of(2008, 5, 1, 1, 1));
        taskManager.addTask(task1);

        Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
        task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task2.setStartTime(LocalDateTime.of(2009, 5, 1, 2, 1));
        taskManager.addTask(task2);

        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());


        List<Task> list1 = historyManager.getTasksHistory();
        assertEquals(2, list1.size());

        taskManager.removeTaskById(task1.getTaskId());

        List<Task> list2 = historyManager.getTasksHistory();
        assertEquals(1, list2.size());
        assertFalse(list2.contains(task1));
    }

    @Test
    public void shouldBePositiveIfEpicStatusDONE() {
        clearTaskManager();
        LocalDateTime date1 = LocalDateTime.of(2023, 5, 16, 9, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 5, 17, 10, 0);
        LocalDateTime date3 = LocalDateTime.of(2025, 5, 18, 11, 0);

        Epic epic1 = new Epic("Сдать проект", "Убедиться что материал подготовлен", TaskStatus.NEW);
        taskManager.addEpic(epic1);

        SubTask sTask1 = new SubTask("Провести исследование", "Собрать ископаемые", TaskStatus.DONE,
                epic1.getTaskId());
        sTask1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        sTask1.setStartTime(date1);
        taskManager.addSubTask(sTask1);

        SubTask sTask2 = new SubTask("Записать результаты исследования", "Детально описать ископаемые",
                TaskStatus.DONE, epic1.getTaskId());
        sTask2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        sTask2.setStartTime(date2);
        taskManager.addSubTask(sTask2);

        SubTask sTask3 = new SubTask("Подготовить доклад и презентацию", "Распределить задачи по " +
                "группе", TaskStatus.DONE, epic1.getTaskId());
        sTask3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        sTask3.setStartTime(date3);
        taskManager.addSubTask(sTask3);

        assertEquals(epic1.getStatus(), TaskStatus.DONE);
    }

    @Test
    public void shouldBePositiveIfAdditionIsWorkingCorrectly() {
        clearTaskManager();
        Task task1 = new Task("Выйти поиграть с друзьями", "Взять воды", TaskStatus.NEW);
        task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task1.setStartTime(LocalDateTime.of(2010, 5, 1, 1, 1));
        taskManager.addTask(task1);

        Task task2 = new Task("Запланировать отпуск", "Узнать стоимость билета", TaskStatus.NEW);
        task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task2.setStartTime(LocalDateTime.of(2011, 5, 2, 1, 1));
        taskManager.addTask(task2);

        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());

        List<Task> list1 = historyManager.getTasksHistory();

        Task task3 = new Task("Выгулять собаку", "Не забыть взять с собой игрушки", TaskStatus.NEW);
        task3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task3.setStartTime(LocalDateTime.of(2012, 6, 1, 1, 1));
        taskManager.addTask(task3);
        taskManager.getTaskById(task3.getTaskId());

        List<Task> list2 = historyManager.getTasksHistory();

        assertNotEquals(list1, list2);
    }

    @Test
    public void shouldBePositiveIfTasksHistoryWorkCorrectly() {
        clearHistoryManager();
        Task task1 = new Task("Поиграть в доту", "Получить жетоны чтобы пройти дальше", TaskStatus.NEW);
        task1.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task1.setStartTime(LocalDateTime.of(2016, 5, 1, 1, 1));
        taskManager.addTask(task1);

        Task task2 = new Task("Поиграть в валорант с другом", "Апнуть звание", TaskStatus.NEW);
        task2.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task2.setStartTime(LocalDateTime.of(2017, 5, 1, 2, 1));
        taskManager.addTask(task2);

        Task task3 = new Task("Приготовить покушать", "Купить курицу", TaskStatus.NEW);
        task3.setDuration(Duration.of(1, ChronoUnit.MINUTES));
        task3.setStartTime(LocalDateTime.of(2018, 5, 1, 3, 1));
        taskManager.addTask(task3);

        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        taskManager.getTaskById(task3.getTaskId());

        List<Task> list1 = historyManager.getTasksHistory();

        assertEquals(3, list1.size());
    }

    private void clearTaskManager() {
        taskManager.clearTasks();
        taskManager.clearSubTasks();
        taskManager.clearEpics();
    }

    private void clearHistoryManager() {
        for (Task task : historyManager.getTasksHistory()) {
            historyManager.remove(task.getTaskId());
        }
    }
}
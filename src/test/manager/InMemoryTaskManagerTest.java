package manager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import task.Epic;
import task.SubTask;
import task.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    private final static TaskManager taskManager = Managers.getDefault();
    private final static HistoryManager historyManager = Managers.getDefaultHistory();

    @BeforeAll
    static void beforeAll() {
        Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
        taskManager.addTask(task2);
        Task task3 = new Task("Сходить в спортзал", "Купить воду", TaskStatus.NEW);
        taskManager.addTask(task3);
        Epic epic1 = new Epic("Собрать пазл", "Выделить 2 часа на это", TaskStatus.NEW);
        taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Разложить все кусочки пазла на столе рубашкой вверх", "Я не " +
                "люблю это занятие", TaskStatus.NEW, epic1.getTaskId());
        taskManager.addSubTask(subTask1);
    }

    @Test
    void shouldBePositiveIfTaskIdAreEqual() {
        for (Task task : taskManager.getTasks()) {
            assertEquals(taskManager.getTaskById(task.getTaskId()), (taskManager.getTaskById(task.getTaskId())),
                    "Экземпляры не равны");
        }
    }

    @Test
    void shouldBePositiveIfSubtaskIdAreEqual() {
        for (SubTask subTask : taskManager.getSubTasks()) {
            assertEquals(taskManager.getSubTaskById(subTask.getTaskId()),
                    (taskManager.getSubTaskById(subTask.getTaskId())));
        }
    }

    @Test
    void shouldBePositiveIfEpicIdAreEqual() {
        for (Epic epic : taskManager.getEpics()) {
            assertEquals(taskManager.getEpicById(epic.getTaskId()),
                    (taskManager.getEpicById(epic.getTaskId())));
        }
    }


    @Test
    void shouldBePassIfUtilityClassCreateAnObject() {
        assertNotNull(taskManager);

    }

    @Test
    void shouldBePositiveIfIdsAreDifferent() {
        List<Task> tasks = taskManager.getTasks();
        assertNotEquals(tasks.getFirst().getTaskId(), tasks.get(1).getTaskId());
    }

    @Test
    void shouldBePositiveIfTheObjectHasNoChange() {
        Task task5 = new Task("Установить дверь", "Посмотреть крепеж", TaskStatus.NEW);
        taskManager.addTask(task5);
        assertEquals(task5.getTaskId(), taskManager.getTaskById(task5.getTaskId()).getTaskId());
        assertEquals(task5.getName(), taskManager.getTaskById(task5.getTaskId()).getName());
        assertEquals(task5.getStatus(), taskManager.getTaskById(task5.getTaskId()).getStatus());
    }

    @Test
    void shouldBePositiveIfObjectsAreDifferent() {
        Task task6 = new Task("Купить кровать", "Посмотреть бельё", TaskStatus.NEW);
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
        taskManager.addTask(task8);
        Epic epic2 = new Epic("Позаниматься английским", "Выделить 2 часа на это", TaskStatus.DONE);
        taskManager.addEpic(epic2);
        SubTask subTask2 = new SubTask("Открыть новый учебник и включить изложение", "Я не " +
                "люблю это занятие", TaskStatus.DONE, epic2.getTaskId());
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
        taskManager.addSubTask(subTask3);
        SubTask subTask4 = new SubTask("Попить водички", "Открыть бутылку",
                TaskStatus.NEW, subTask3.getTaskId());
        assertThrows(NullPointerException.class, () -> taskManager.addSubTask(subTask4));
    }

    @Test
    void shouldBePositiveIfRemoveIsWorkingCorrectly() {
        Task task1 = new Task("Сходить в ресторан", "Покушать салатик", TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
        taskManager.addTask(task2);
        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        ArrayList<Task> arr1 = historyManager.getTasksHistory();
        taskManager.removeTaskById(task1.getTaskId());
        ArrayList<Task> arr2 = historyManager.getTasksHistory();
        assertNotEquals(arr1, arr2);
    }

    @Test
    void shouldBePositiveIfAdditionIsWorkingCorrectly() {
        Task task1 = new Task("Выйти поиграть с друзьями", "Взять воды", TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Запланировать отпуск", "Узнать стоимость билета", TaskStatus.NEW);
        taskManager.addTask(task2);
        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        ArrayList<Task> arr1 = historyManager.getTasksHistory();
        Task task3 = new Task("Выгулять собаку", "Не забыть взять с собой игрушки", TaskStatus.NEW);
        taskManager.addTask(task3);
        taskManager.getTaskById(task3.getTaskId());
        ArrayList<Task> arr2 = historyManager.getTasksHistory();
        assertNotEquals(arr1, arr2);
    }

    @Test
    void shouldBePositiveIfEpicDoesntHaveIrrelevantSubtaskIds() {
        Epic epic1 = new Epic("Приготовить романтический ужин", "Выделить 2 часа на это",
                TaskStatus.NEW);
        taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Купить продуктов на ужин", "Составить список",
                TaskStatus.NEW, epic1.getTaskId());
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Выложить все продукты на столе и подготовить к приготовлению",
                "Мыть руки после каждого прикасновения к чему-либо", TaskStatus.NEW, epic1.getTaskId());
        taskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask("Накрыть на стол", "Не забыть про вино!",
                TaskStatus.NEW, epic1.getTaskId());
        taskManager.addSubTask(subTask3);
        List<Integer> arr1 = epic1.getSubtaskIds();
        taskManager.removeSubTaskById(subTask1.getTaskId());
        List<Integer> arr2 = epic1.getSubtaskIds();
        assertEquals(2, arr2.size());
    }
}
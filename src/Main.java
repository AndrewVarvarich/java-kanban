import task.*;
import manager.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
        taskManager.addTask(task1);
        Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
        taskManager.addTask(task2);
        Epic epic1 = new Epic("Собрать пазл", "Выделить 2 часа на это", TaskStatus.NEW);
        taskManager.addEpic(epic1);
        Epic epic2 = new Epic("Собрать компьютер", "Нужна помощь друзей", TaskStatus.NEW);
        taskManager.addEpic(epic2);
        SubTask subTask1 = new SubTask("Разложить все кусочки пазла на столе рубашкой вверх", "Я не " +
                "люблю это занятие", TaskStatus.NEW, epic1.getTaskId());
        taskManager.addSubTask(subTask1);
        SubTask subTask2 = new SubTask("Соединить все кусочки в картинку", "Не сломать кусочки",
                TaskStatus.NEW, epic1.getTaskId());
        taskManager.addSubTask(subTask2);
        SubTask subTask3 = new SubTask("Позвать друзей и собрать пазл", "Не забыть про напитки",
                TaskStatus.NEW, epic1.getTaskId());
        taskManager.addSubTask(subTask3);
        taskManager.getEpicById(epic1.getTaskId());
        taskManager.getEpicById(epic2.getTaskId());
        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        taskManager.getSubTaskById(subTask1.getTaskId());
        taskManager.getSubTaskById(subTask2.getTaskId());
        taskManager.getSubTaskById(subTask3.getTaskId());
        printHistory();
        taskManager.getTaskById(task1.getTaskId());
        printHistory();
        taskManager.getEpicById(epic2.getTaskId());
        printHistory();
        taskManager.removeTaskById(task1.getTaskId());
        printHistory();
        taskManager.removeEpicById(epic1.getTaskId());
        printHistory();
    }

    private static void printHistory() {
        System.out.println("\n История:");
        HistoryManager historyManager = Managers.getDefaultHistory();
        List<Task> arr = historyManager.getTasksHistory();
        for (Task task : arr) {
            System.out.println(task.toString());
        }
    }
}

import task.*;
import manager.*;

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
        SubTask subTask3 = new SubTask("Позвать друзей и собрать компьютер", "Собрать их будет сложно",
                TaskStatus.NEW, epic2.getTaskId());
        Task task3 = new Task("Сходить в магазин", "Купить воду", TaskStatus.DONE);
        SubTask subTask4 = new SubTask("Разложить все кусочки пазла на столе рубашкой вверх", "Я не " +
                "люблю это занятие", TaskStatus.NEW, epic1.getTaskId());
        SubTask subTask5 = new SubTask("Соединить все кусочки в картинку", "Не сломать кусочки",
                TaskStatus.DONE, epic1.getTaskId());
        taskManager.updateTask(task3, task1.getTaskId());
        taskManager.updateSubTask(subTask4, subTask1.getTaskId());
        taskManager.updateSubTask(subTask5, subTask2.getTaskId());
        taskManager.getEpicById(epic1.getTaskId());
        taskManager.getEpicById(epic2.getTaskId());
        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        taskManager.getSubTaskById(subTask1.getTaskId());
        taskManager.getSubTaskById(subTask2.getTaskId());
        taskManager.getSubTaskById(subTask1.getTaskId());
        taskManager.getSubTaskById(subTask2.getTaskId());
        taskManager.getSubTaskById(subTask1.getTaskId());
        taskManager.getSubTaskById(subTask2.getTaskId());
        taskManager.getSubTaskById(subTask1.getTaskId());
        taskManager.getSubTaskById(epic1.getTaskId());
        taskManager.getSubTaskById(subTask2.getTaskId());
        taskManager.getEpicById(epic1.getTaskId());
        taskManager.getEpicById(epic2.getTaskId());
        taskManager.getTaskById(task1.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        System.out.println("\n" + taskManager.getHistory() + "\n");
        printAllTasks(taskManager);

    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("\n Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);

            for (Task task : manager.getAllSubtasksForEpic(epic.getTaskId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("\n Подзадачи:");
        for (Task subtask : manager.getSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("\n История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}

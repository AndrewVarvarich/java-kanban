import Task.*;
import TaskManager.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
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
        taskManager.addSubTask(subTask3);
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpic() + "\n");
        Task task3 = new Task("Сходить в магазин", "Купить воду", TaskStatus.DONE);
        SubTask subTask4 = new SubTask("Разложить все кусочки пазла на столе рубашкой вверх", "Я не " +
                "люблю это занятие", TaskStatus.NEW, epic1.getTaskId());
        SubTask subTask5 = new SubTask("Соединить все кусочки в картинку", "Не сломать кусочки",
                TaskStatus.DONE, epic1.getTaskId());
        taskManager.updateTask(task3, task1.getTaskId());
        taskManager.updateSubTask(subTask4, subTask1.getTaskId());
        taskManager.updateSubTask(subTask5, subTask2.getTaskId());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpic() + "\n");
        taskManager.removeTaskById(task3.getTaskId());
        taskManager.removeEpicById(epic1.getTaskId());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpic());
        System.out.println(taskManager.getSubTasks());
    }
}

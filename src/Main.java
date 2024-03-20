public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        Task task1 = new Task("Сходить в магазин", "Купить воду", TaskStatus.NEW);
        Task task2 = new Task("Посетить врача", "Удаление зуба", TaskStatus.NEW);
        Epic epic1 = new Epic("Собрать пазл", "Выделить 2 часа на это", TaskStatus.NEW);
        Epic epic2 = new Epic("Собрать компьютер", "Нужна помощь друзей", TaskStatus.NEW);
        SubTask subTask1 = new SubTask("Разложить все кусочки пазла на столе рубашкой вверх", "Я не " +
                "люблю это занятие", TaskStatus.NEW);
        SubTask subTask2 = new SubTask("Соединить все кусочки в картинку", "Не сломать кусочки",
                TaskStatus.NEW);
        SubTask subTask3 = new SubTask("Позвать друзей и собрать компьютер", "Собрать их будет сложно",
                TaskStatus.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);
        taskManager.addSubTask(subTask3);
        taskManager.addEpic(epic1);
        taskManager.addEpic(epic2);
        taskManager.addSubTaskToEpic(subTask1.getTaskId(), epic1.getTaskId());
        taskManager.addSubTaskToEpic(subTask2.getTaskId(), epic1.getTaskId());
        taskManager.addSubTaskToEpic(subTask3.getTaskId(), epic2.getTaskId());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpic());
        Task task3 = new Task("Сходить в магазин", "Купить воду", TaskStatus.DONE);
        SubTask subTask4 = new SubTask("Разложить все кусочки пазла на столе рубашкой вверх", "Я не " +
                "люблю это занятие", TaskStatus.NEW);
        SubTask subTask5 = new SubTask("Соединить все кусочки в картинку", "Не сломать кусочки",
                TaskStatus.DONE);
        taskManager.updateTask(task3);
        taskManager.updateSubTask(subTask4);
        taskManager.updateSubTask(subTask5);
        taskManager.updateEpicStatus(epic1.getTaskId());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getSubTasks());
        System.out.println(taskManager.getEpic());
        taskManager.removeTaskById(task3.getTaskId());
        taskManager.removeEpicById(epic1.getTaskId());
        System.out.println(taskManager.getTasks());
        System.out.println(taskManager.getEpic());
    }
}

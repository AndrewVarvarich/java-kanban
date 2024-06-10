import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import manager.Managers;
import manager.TaskManager;
import manager.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import task.*;
import webapi.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

public class HttpTaskServerTest {
    private TaskManager manager;
    private HttpTaskServer taskServer;
    private Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        manager = createTaskManager();
        taskServer = createHttpTaskServer(manager);
        gson = createGson();
        clearTaskManager();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTaskShouldBePassIfTaskAddedCorrectly() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.now().plusMinutes(1)); // Убедитесь, что время в будущем

        String taskJson = gson.toJson(task);
        System.out.println("Task JSON: " + taskJson); // Логируем JSON

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull("Задачи не возвращаются", tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Некорректное имя задачи", "Test 2", tasksFromManager.getFirst().getName());
    }

    @Test
    public void testAddTaskWithConflictShouldBePassIfTaskWasNotAdded() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "First Task", TaskStatus.NEW);
        task1.setDuration(Duration.ofMinutes(60));
        task1.setStartTime(LocalDateTime.now().plusHours(1));
        Task task2 = new Task("Task 2", "Second Task", TaskStatus.NEW);
        task2.setDuration(Duration.ofMinutes(60));
        task2.setStartTime(LocalDateTime.now().plusHours(1).plusMinutes(30));

        String taskJson1 = gson.toJson(task1);
        String taskJson2 = gson.toJson(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson1))
                .build();

        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode());

        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson2))
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response2.statusCode());
    }

    @Test
    public void testAddEpicShouldBePassIfEpicAddedCorrectly() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        epic.setDuration(Duration.ofMinutes(5));
        epic.setStartTime(LocalDateTime.now());
        epic.setEndTime(LocalDateTime.now().plusMinutes(5));
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();

        assertNotNull("Эпики не возвращаются", epicsFromManager);
        assertEquals(1, epicsFromManager.size());
        assertEquals("Некорректное имя задачи", "Epic 1", epicsFromManager.getFirst().getName());
    }

    @Test
    public void testAddSubTaskShouldBePassIfSubTaskAddedCorrectly() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        manager.addEpic(epic);
        HttpClient client = HttpClient.newHttpClient();
        SubTask subTask = new SubTask("SubTask 1", "SubTask description", TaskStatus.NEW,
                epic.getTaskId());
        subTask.setDuration(Duration.ofMinutes(5));
        subTask.setStartTime(LocalDateTime.now());
        String subTaskJson = gson.toJson(subTask);
        URI url2 = URI.create("http://localhost:8080/subtasks");
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(url2)
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();

        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response2.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();
        assertNotNull("Подзадачи не возвращаются", subTasksFromManager);
        assertEquals(1, subTasksFromManager.size());
        assertEquals("Некорректное имя задачи", "SubTask 1", subTasksFromManager.getFirst().getName());
    }

    @Test
    public void testGetAllTasksShouldBePassIfAllTasksHaveBeenReceived() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "First Task", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.now().plusHours(1));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();
        assertNotNull("Задачи не возвращаются", tasksFromManager);
        assertTrue(response.body().contains("Task 1"));
    }

    @Test
    public void testGetTaskByIdShouldBePassIfTaskHaveBeenReceived() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "First Task", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.now().plusHours(1));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getTaskId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(response.body().contains("Task 1"));
    }

    @Test
    public void testDeleteTaskByIdShouldBePositiveIfTheTaskWasDeleted() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "First Task", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.now().plusHours(1));
        manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getTaskId());
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    public void testUpdateTaskByIdShouldBePassIfTaskWasUpdated() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "First Task", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.now().plusHours(1));
        manager.addTask(task);

        task.setName("Updated Task 1");
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + task.getTaskId());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        Task updatedTask = manager.getTaskById(task.getTaskId());
        assertEquals("Updated Task 1", updatedTask.getName());
    }

    @Test
    public void testGetAllSubTasksShouldBePassIfAllSubTasksHaveBeenReceived() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        manager.addEpic(epic);
        SubTask subTask = new SubTask("SubTask 1", "SubTask description", TaskStatus.NEW,
                epic.getTaskId());
        subTask.setDuration(Duration.ofMinutes(60));
        subTask.setStartTime(LocalDateTime.now().plusHours(1));
        manager.addSubTask(subTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<SubTask> subTasksFromManager = manager.getSubTasks();
        assertNotNull("Подзадачи не возвращаются", subTasksFromManager);
        assertTrue(response.body().contains("SubTask 1"));
    }

    @Test
    public void testGetAllEpicsShouldBePassIfAllEpicsHaveBeenReceived() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description", TaskStatus.NEW);
        manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.getEpics();
        assertNotNull("Эпики не возвращаются", epicsFromManager);
        assertTrue(response.body().contains("Epic 1"));
    }

    @Test
    public void testGetHistoryShouldBePassIfYheCorrectHistoryWasReceived() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", TaskStatus.NEW);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.now());
        manager.addTask(task);
        manager.getTaskById(task.getTaskId());

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>() {}.getType());
        assertEquals(1, history.size());
        assertEquals(task.getTaskId(), history.getFirst().getTaskId());
    }

    @Test
    public void testDeleteNonExistingTasksShouldBePassIfTheServerReturnsCode404() throws IOException,
            InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/100");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testUnsupportedMethodShouldBePassIfTheServerReturnsCode405() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.noBody()).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }

    @Test
    public void testDeleteNonExistingSubTaskShouldBePassIfTheServerReturnsCode404() throws IOException,
            InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/100");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteNonExistingEpicShouldBePassIfTheServerReturnsCode404() throws IOException,
            InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/100");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    private TaskManager createTaskManager() {
        return Managers.getDefault();
    }

    private HttpTaskServer createHttpTaskServer(TaskManager manager) throws IOException {
        return new HttpTaskServer(manager);
    }

    private Gson createGson() {
        return HttpTaskServer.getGson();
    }

    private void clearTaskManager() {
        manager.clearTasks();
        manager.clearSubTasks();
        manager.clearEpics();
    }
}

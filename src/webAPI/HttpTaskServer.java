package webAPI;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.TaskManager;
import manager.TaskStatus;
import task.Epic;
import task.SubTask;
import task.Task;

import java.io.IOException;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import exceptions.*;


public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new TaskHandler.DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new TaskHandler.LocalDateTimeAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new TaskHandler());
    }

    public static Gson getGson() {
        return gson;
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {

        TaskManager taskManager1 = Managers.getDefault();

        HttpTaskServer server = new HttpTaskServer(taskManager1);

        server.start();

        System.out.println("Сервер запущен на порту " + PORT);

    }

    static class TaskHandler extends BaseHttpHandler implements HttpHandler {
        TaskManager taskManager = Managers.getDefault();

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Началась обработка запроса от клиента.");

            URI requestURI = httpExchange.getRequestURI();
            String path = requestURI.getPath();
            String[] requestsString = path.split("/");
            String method = httpExchange.getRequestMethod();
            switch (method.toUpperCase()) {
                case "GET":
                    handleGetRequest(httpExchange, requestsString);
                    break;
                case "POST":
                    handlePostRequest(httpExchange, requestsString);
                    break;
                case "DELETE":
                    handleDeleteRequest(httpExchange, requestsString);
                    break;
                default:
                    sendNotAllowed(httpExchange, "Метод не поддерживается сервером");
                    break;
            }
        }

        static class DurationAdapter extends TypeAdapter<Duration> {
            @Override
            public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
                if (duration != null) {
                    jsonWriter.value(duration.toString());
                } else {
                    jsonWriter.nullValue();
                }
            }

            @Override
            public Duration read(JsonReader jsonReader) throws IOException {
                return Duration.parse(jsonReader.nextString());
            }

        }

        // LocalDateTimeAdapter
        public static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
            private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            @Override
            public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
                if (localDateTime != null) {
                    jsonWriter.value(localDateTime.format(FORMATTER));
                } else {
                    jsonWriter.nullValue();
                }
            }

            @Override
            public LocalDateTime read(JsonReader jsonReader) throws IOException {
                return LocalDateTime.parse(jsonReader.nextString(), FORMATTER);
            }
        }

        private void handleGetRequest(HttpExchange httpExchange, String[] requestsString) throws IOException {
            String response;

            try {
                switch (requestsString[1]) {
                    case "tasks":
                        if (requestsString.length == 2) {
                            System.out.println("Получаем задачи: " + taskManager.getTasks());
                            Type listType = new TypeToken<List<Task>>() {
                            }.getType();
                            List<Task> tasks = taskManager.getTasks();
                            response = gson.toJson(tasks, listType);
                            System.out.println("Сериализованные задачи: " + response);
                            sendText(httpExchange, response);
                        } else if (requestsString.length == 3) {
                            handleGetTaskById(httpExchange, Integer.parseInt(requestsString[2]));
                        } else {
                            sendNotFound(httpExchange, "Такой задачи нет");
                        }
                        break;
                    case "subtasks":
                        if (requestsString.length == 2) {
                            System.out.println("Получаем подзадачи: " + taskManager.getSubTasks());
                            Type listType = new TypeToken<List<SubTask>>() {
                            }.getType();
                            List<SubTask> subTasks = taskManager.getSubTasks();
                            response = gson.toJson(subTasks, listType);
                            System.out.println("Сериализованные задачи: " + response);
                            sendText(httpExchange, response);
                        } else if (requestsString.length == 3) {
                            handleGetSubtaskById(httpExchange, Integer.parseInt(requestsString[2]));
                        } else {
                            sendNotFound(httpExchange, "Такой задачи нет");
                        }
                        break;
                    case "epics":
                        if (requestsString.length == 2) {
                            System.out.println("Получаем эпики: " + taskManager.getEpics());
                            Type listType = new TypeToken<List<Epic>>() {
                            }.getType();
                            List<Epic> epics = taskManager.getEpics();
                            response = gson.toJson(epics, listType);
                            System.out.println("Сериализованные задачи: " + response);
                            sendText(httpExchange, response);
                        } else if (requestsString.length == 3) {
                            handleGetEpicById(httpExchange, Integer.parseInt(requestsString[2]));
                        } else if (requestsString.length == 4 && "subtasks".equals(requestsString[3])) {
                            handleGetEpicSubtasks(httpExchange, Integer.parseInt(requestsString[2]));
                        } else {
                            sendNotFound(httpExchange, "Такой задачи нет");
                        }
                        break;
                    case "history":
                        response = gson.toJson(taskManager.getHistory());
                        sendText(httpExchange, response);
                        break;
                    case "prioritized":
                        response = gson.toJson(taskManager.getPrioritizedTasks());
                        sendText(httpExchange, response);
                        break;
                    default:
                        sendNotFound(httpExchange, "Такой задачи нет");
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalServerError(httpExchange, "Внутренняя ошибка сервера");
            }
        }

        private void handlePostRequest(HttpExchange httpExchange, String[] requestsString) throws IOException {
            InputStream inputStream = httpExchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            try {
                Task task = parseToObject(body);

                if (task == null) {
                    sendInvalidRequest(httpExchange, "Некорректный формат Json");
                    return;
                }

                switch (requestsString[1]) {
                    case "tasks":
                        if (requestsString.length == 2) {
                            System.out.println("Началось добавление таски в менеджер");
                            taskManager.addTask(task);
                            sendTextPost(httpExchange, "Задача успешно создана");
                        } else if (requestsString.length == 3) {
                            System.out.println("Началось обновление таски по id");
                            taskManager.updateTask(task, Integer.parseInt(requestsString[2]));
                            sendTextPost(httpExchange, "Задача обновлена");
                        } else {
                            sendInvalidRequest(httpExchange, "Некорректный формат Json");
                        }
                        break;
                    case "subtasks":
                        if (requestsString.length == 2 && task instanceof SubTask) {
                            taskManager.addSubTask((SubTask) task);
                            sendTextPost(httpExchange, "Подзадача добавлена");
                        } else if (requestsString.length == 3 && task instanceof SubTask) {
                            taskManager.updateSubTask((SubTask) task, Integer.parseInt(requestsString[2]));
                            sendTextPost(httpExchange, "Подзадача обновлена");
                        } else {
                            sendInvalidRequest(httpExchange, "Некорректный формат Json");
                        }
                        break;
                    case "epics":
                        if (requestsString.length == 2 && task instanceof Epic) {
                            taskManager.addEpic((Epic) task);
                            sendTextPost(httpExchange, "Эпик создан");
                        } else {
                            sendInvalidRequest(httpExchange, "Некорректный формат Json");
                        }
                        break;
                    default:
                        sendNotFound(httpExchange, "Эпик не найден");
                        break;
                }
            } catch (NumberFormatException e) {
                sendInvalidRequest(httpExchange, "Некорретный ID");
            } catch (IllegalArgumentException e) {
                sendHasInteractions(httpExchange, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalServerError(httpExchange, "Внутренняя ошибка сервера");
            }
        }

        private void handleDeleteRequest(HttpExchange httpExchange, String[] requestsString) throws IOException {
            if (requestsString.length < 3) {
                sendInvalidRequest(httpExchange, "Некорректный запрос");
                return;
            }

            int id = Integer.parseInt(requestsString[2]);

            try {
                switch (requestsString[1]) {
                    case "tasks":
                        handleDeleteTaskById(httpExchange, id);
                        break;
                    case "subtasks":
                        handleDeleteSubTaskById(httpExchange, id);
                        break;
                    case "epics":
                        handleDeleteEpicById(httpExchange, id);
                        break;
                    default:
                        sendNotFound(httpExchange, "Не найдено");
                        break;
                }
            } catch (IllegalArgumentException e) {
                sendNotFound(httpExchange, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalServerError(httpExchange, "Внутренняя ошибка сервера");
            }
        }

        private void handleDeleteTaskById(HttpExchange httpExchange, int id) throws IOException {
            try {
                Task task = taskManager.getTaskById(id);
                taskManager.removeTaskById(task.getTaskId());
                sendText(httpExchange, "Задача удалена");
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleDeleteSubTaskById(HttpExchange httpExchange, int id) throws IOException {
            try {
                SubTask subTask = taskManager.getSubTaskById(id);
                taskManager.removeSubTaskById(subTask.getTaskId());
                sendText(httpExchange, "Подзадача удалена");
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleDeleteEpicById(HttpExchange httpExchange, int id) throws IOException {
            try {
                Epic epic = taskManager.getEpicById(id);
                taskManager.removeEpicById(epic.getTaskId());
                sendText(httpExchange, "Эпик удален");
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetTaskById(HttpExchange httpExchange, int id) throws IOException {
            try {
                Task task = taskManager.getTaskById(id);
                sendText(httpExchange, gson.toJson(task));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetSubtaskById(HttpExchange httpExchange, int id) throws IOException {
            try {
                SubTask subTask = taskManager.getSubTaskById(id);
                sendText(httpExchange, gson.toJson(subTask));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetEpicById(HttpExchange httpExchange, int id) throws IOException {
            try {
                Epic epic = taskManager.getEpicById(id);
                sendText(httpExchange, gson.toJson(epic));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetEpicSubtasks(HttpExchange httpExchange, int id) throws IOException {
            try {
                Epic epic = taskManager.getEpicById(id);
                sendText(httpExchange, gson.toJson(taskManager.getAllSubtasksForEpic(id)));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private static Task parseToObject(String json) {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            Task task = null;
            if (jsonObject.has("epicId")) {
                return gson.fromJson(jsonObject, SubTask.class);
            } else if (jsonObject.has("subtaskIds")) {
                return gson.fromJson(jsonObject, Epic.class);
            } else {
                return gson.fromJson(jsonObject, Task.class);
            }
        }

        private static TaskStatus getStatusFromTask(String str) {
            TaskStatus status;
            if (str.equals("NEW")) {
                status = TaskStatus.NEW;
            } else if (str.equals("DONE")) {
                status = TaskStatus.DONE;
            } else {
                status = TaskStatus.IN_PROGRESS;
            }
            return status;
        }
    }
}

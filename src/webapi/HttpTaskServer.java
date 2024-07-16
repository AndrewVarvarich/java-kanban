package webapi;

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
    public static final String TASKS = "tasks";
    public static final String SUBTASKS = "subtasks";
    public static final String EPICS = "epics";
    public static final String INVALID_REQUEST_MESSAGE = "Некорректный запрос";
    public static final String NOT_FOUND_MESSAGE = "Не найдено";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "Внутренняя ошибка сервера";
    public static final String METHOD_NOT_SUPPORTED_BY_SERVER = "Метод не поддерживается сервером";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String DELETE = "DELETE";
    public static final String HISTORY = "history";
    public static final String PRIORITIZED = "prioritized";
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
                case GET:
                    handleGetRequest(httpExchange, requestsString);
                    break;
                case POST:
                    handlePostRequest(httpExchange, requestsString);
                    break;
                case DELETE:
                    handleDeleteRequest(httpExchange, requestsString);
                    break;
                default:
                    sendNotAllowed(httpExchange, METHOD_NOT_SUPPORTED_BY_SERVER);
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

        private void handleGetRequest(HttpExchange httpExchange, String[] requestsString) {
            String response;

            try {
                switch (requestsString[1]) {
                    case TASKS:
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
                            sendNotFound(httpExchange, NOT_FOUND_MESSAGE);
                        }
                        break;
                    case SUBTASKS:
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
                            sendNotFound(httpExchange, NOT_FOUND_MESSAGE);
                        }
                        break;
                    case EPICS:
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
                        } else if (requestsString.length == 4 && SUBTASKS.equals(requestsString[3])) {
                            handleGetEpicSubtasks(httpExchange, Integer.parseInt(requestsString[2]));
                        } else {
                            sendNotFound(httpExchange, NOT_FOUND_MESSAGE);
                        }
                        break;
                    case HISTORY:
                        response = gson.toJson(taskManager.getHistory());
                        sendText(httpExchange, response);
                        break;
                    case PRIORITIZED:
                        response = gson.toJson(taskManager.getPrioritizedTasks());
                        sendText(httpExchange, response);
                        break;
                    default:
                        sendNotFound(httpExchange, NOT_FOUND_MESSAGE);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalServerError(httpExchange, INTERNAL_SERVER_ERROR_MESSAGE);
            }
        }

        private void handlePostRequest(HttpExchange httpExchange, String[] requestsString) throws IOException {
            InputStream inputStream = httpExchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            try {
                Task task = parseToObject(body);

                if (task == null) {
                    sendInvalidRequest(httpExchange, INVALID_REQUEST_MESSAGE);
                    return;
                }

                switch (requestsString[1]) {
                    case TASKS:
                        if (requestsString.length == 2) {
                            System.out.println("Началось добавление таски в менеджер");
                            taskManager.addTask(task);
                            sendTextPost(httpExchange, "Задача успешно создана");
                        } else if (requestsString.length == 3) {
                            System.out.println("Началось обновление таски по id");
                            taskManager.updateTask(task, Integer.parseInt(requestsString[2]));
                            sendTextPost(httpExchange, "Задача обновлена");
                        } else {
                            sendInvalidRequest(httpExchange, INVALID_REQUEST_MESSAGE);
                        }
                        break;
                    case SUBTASKS:
                        if (requestsString.length == 2 && task instanceof SubTask) {
                            taskManager.addSubTask((SubTask) task);
                            sendTextPost(httpExchange, "Подзадача добавлена");
                        } else if (requestsString.length == 3 && task instanceof SubTask) {
                            taskManager.updateSubTask((SubTask) task, Integer.parseInt(requestsString[2]));
                            sendTextPost(httpExchange, "Подзадача обновлена");
                        } else {
                            sendInvalidRequest(httpExchange, INVALID_REQUEST_MESSAGE);
                        }
                        break;
                    case EPICS:
                        if (requestsString.length == 2 && task instanceof Epic) {
                            taskManager.addEpic((Epic) task);
                            sendTextPost(httpExchange, "Эпик создан");
                        } else {
                            sendInvalidRequest(httpExchange, INVALID_REQUEST_MESSAGE);
                        }
                        break;
                    default:
                        sendNotFound(httpExchange, NOT_FOUND_MESSAGE);
                        break;
                }
            } catch (NumberFormatException e) {
                sendInvalidRequest(httpExchange, "Некорретный ID");
            } catch (IllegalArgumentException e) {
                sendHasInteractions(httpExchange, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalServerError(httpExchange, INTERNAL_SERVER_ERROR_MESSAGE);
            }
        }

        private void handleDeleteRequest(HttpExchange httpExchange, String[] requestsString) {
            if (requestsString.length < 3) {
                sendInvalidRequest(httpExchange, INVALID_REQUEST_MESSAGE);
                return;
            }

            int id = Integer.parseInt(requestsString[2]);

            try {
                switch (requestsString[1]) {
                    case TASKS:
                        handleDeleteTaskById(httpExchange, id);
                        break;
                    case SUBTASKS:
                        handleDeleteSubTaskById(httpExchange, id);
                        break;
                    case EPICS:
                        handleDeleteEpicById(httpExchange, id);
                        break;
                    default:
                        sendNotFound(httpExchange, NOT_FOUND_MESSAGE);
                        break;
                }
            } catch (IllegalArgumentException e) {
                sendNotFound(httpExchange, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                sendInternalServerError(httpExchange, INTERNAL_SERVER_ERROR_MESSAGE);
            }
        }

        private void handleDeleteTaskById(HttpExchange httpExchange, int id) {
            try {
                Task task = taskManager.getTaskById(id);
                taskManager.removeTaskById(task.getTaskId());
                sendText(httpExchange, "Задача удалена");
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleDeleteSubTaskById(HttpExchange httpExchange, int id) {
            try {
                SubTask subTask = taskManager.getSubTaskById(id);
                taskManager.removeSubTaskById(subTask.getTaskId());
                sendText(httpExchange, "Подзадача удалена");
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleDeleteEpicById(HttpExchange httpExchange, int id) {
            try {
                Epic epic = taskManager.getEpicById(id);
                taskManager.removeEpicById(epic.getTaskId());
                sendText(httpExchange, "Эпик удален");
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetTaskById(HttpExchange httpExchange, int id) {
            try {
                Task task = taskManager.getTaskById(id);
                sendText(httpExchange, gson.toJson(task));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetSubtaskById(HttpExchange httpExchange, int id) {
            try {
                SubTask subTask = taskManager.getSubTaskById(id);
                sendText(httpExchange, gson.toJson(subTask));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetEpicById(HttpExchange httpExchange, int id) {
            try {
                Epic epic = taskManager.getEpicById(id);
                sendText(httpExchange, gson.toJson(epic));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private void handleGetEpicSubtasks(HttpExchange httpExchange, int id) {
            try {
                Epic epic = taskManager.getEpicById(id);
                sendText(httpExchange, gson.toJson(taskManager.getAllSubtasksForEpic(id)));
            } catch (NotFoundException e) {
                sendNotFound(httpExchange, e.getMessage());
            }
        }

        private Task parseToObject(String json) {
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

        private TaskStatus getStatusFromTask(String str) {
            TaskStatus status;
            switch (str) {
                case "NEW":
                    status = TaskStatus.NEW;
                    break;
                case "DONE":
                    status = TaskStatus.DONE;
                    break;
                default:
                    status = TaskStatus.IN_PROGRESS;
                    break;
            }
            return status;
        }
    }
}

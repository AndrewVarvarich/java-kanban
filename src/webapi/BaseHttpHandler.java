package webapi;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendResponse(h, 200, text);
    }

    protected void sendTextPost(HttpExchange h, String text) throws IOException {
        sendResponse(h, 201, text);
    }

    protected void sendInvalidRequest(HttpExchange h, String text) throws IOException {
        sendResponse(h, 400, text);
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendResponse(h, 404, text);
    }

    protected void sendNotAllowed(HttpExchange h, String text) throws IOException {
        sendResponse(h, 405, text);
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        sendResponse(h, 406, text);
    }

    protected void sendInternalServerError(HttpExchange h, String text) throws IOException {
        sendResponse(h, 500, text);
    }

    private void sendResponse(HttpExchange httpExchange, int statusCode, String response) throws IOException {
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}

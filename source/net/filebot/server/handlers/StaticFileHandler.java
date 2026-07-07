package net.filebot.server.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class StaticFileHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();

		// default to index.html
		if (path == null || path.equals("/")) {
			path = "/index.html";
		}

		String resourcePath = "/net/filebot/server/web" + path;
		InputStream resource = getClass().getResourceAsStream(resourcePath);

		if (resource == null) {
			// 404 - serve index.html for SPA routing
			resource = getClass().getResourceAsStream("/net/filebot/server/web/index.html");
			if (resource == null) {
				String msg = "404 Not Found";
				byte[] data = msg.getBytes(StandardCharsets.UTF_8);
				exchange.sendResponseHeaders(404, data.length);
				exchange.getResponseBody().write(data);
				exchange.getResponseBody().close();
				return;
			}
		}

		String contentType = getContentType(path);
		exchange.getResponseHeaders().add("Content-Type", contentType);
		exchange.sendResponseHeaders(200, 0);

		OutputStream os = exchange.getResponseBody();
		byte[] buf = new byte[4096];
		int n;
		while ((n = resource.read(buf)) != -1) {
			os.write(buf, 0, n);
		}
		resource.close();
		os.close();
	}

	private String getContentType(String path) {
		if (path.endsWith(".html")) return "text/html; charset=UTF-8";
		if (path.endsWith(".css")) return "text/css; charset=UTF-8";
		if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
		if (path.endsWith(".png")) return "image/png";
		if (path.endsWith(".svg")) return "image/svg+xml";
		if (path.endsWith(".ico")) return "image/x-icon";
		if (path.endsWith(".json")) return "application/json";
		return "application/octet-stream";
	}

}

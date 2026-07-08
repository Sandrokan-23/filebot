package net.filebot.server;

import static net.filebot.Logging.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import com.sun.net.httpserver.HttpServer;

import net.filebot.server.handlers.*;

public class FileBotServer {

	private final HttpServer server;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	private final LogCaptureHandler logCapture = new LogCaptureHandler();
	private final SettingsStore settings = new SettingsStore();

	public FileBotServer(int port) throws IOException {
		InetSocketAddress addr = new InetSocketAddress("0.0.0.0", port);
		server = HttpServer.create(addr, 0);

		logCapture.setLevel(Level.ALL);

		// API endpoints
		server.createContext("/api/status", new StatusHandler());
		server.createContext("/api/settings", new SettingsHandler(settings));
		server.createContext("/api/files", new FilesHandler(settings));
		server.createContext("/api/rename", new SerializedHandler(new RenameHandler(settings)));
		server.createContext("/api/subtitles", new SerializedHandler(new SubtitlesHandler(settings)));
		server.createContext("/api/extract", new SerializedHandler(new ExtractHandler(settings)));
		server.createContext("/api/mediainfo", new MediaInfoHandler(settings));
		server.createContext("/api/list", new ListHandler(settings));
		server.createContext("/api/check", new SerializedHandler(new CheckHandler(settings)));
		server.createContext("/api/revert", new SerializedHandler(new RevertHandler(settings)));
		server.createContext("/api/script", new SerializedHandler(new ScriptHandler()));
		server.createContext("/api/log", new LogHandler(logCapture));

		// Static files
		server.createContext("/", new StaticFileHandler());

		server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
	}

	public LogCaptureHandler getLogCapture() {
		return logCapture;
	}

	public SettingsStore getSettings() {
		return settings;
	}

	public void start() {
		server.start();
		log.info(String.format("FileBot server listening on http://%s:%d", server.getAddress().getHostString(), server.getAddress().getPort()));
	}

	public void stop(int delay) {
		server.stop(delay);
	}

	public void shutdown() {
		executor.shutdownNow();
		server.stop(0);
	}

	public int getPort() {
		return server.getAddress().getPort();
	}

	/**
	 * Handler wrapper that serializes all requests on a single-thread executor.
	 */
	private class SerializedHandler implements com.sun.net.httpserver.HttpHandler {
		private final com.sun.net.httpserver.HttpHandler delegate;

		SerializedHandler(com.sun.net.httpserver.HttpHandler delegate) {
			this.delegate = delegate;
		}

		@Override
		public void handle(com.sun.net.httpserver.HttpExchange exchange) {
			executor.submit(() -> {
				try {
					delegate.handle(exchange);
				} catch (Exception e) {
					debug.log(Level.WARNING, "Handler error", e);
					try {
						String msg = JsonResult.error(500, "Internal server error");
						byte[] data = msg.getBytes(java.nio.charset.StandardCharsets.UTF_8);
						exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
						exchange.sendResponseHeaders(500, data.length);
						exchange.getResponseBody().write(data);
					} catch (IOException ignored) {
					} finally {
						exchange.close();
					}
				}
			});
		}
	}

}

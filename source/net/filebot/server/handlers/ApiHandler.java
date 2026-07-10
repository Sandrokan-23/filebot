package net.filebot.server.handlers;

import static net.filebot.Logging.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import net.filebot.server.ApiException;
import net.filebot.server.JsonResult;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public abstract class ApiHandler implements HttpHandler {

	@Override
	public void handle(HttpExchange exchange) {
		String path = exchange.getRequestURI().getPath();
		String method = exchange.getRequestMethod().toUpperCase();
		try {
			log.fine(() -> method + " " + path);

			exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
				exchange.sendResponseHeaders(204, -1);
				return;
			}

			Map<String, Object> params;

			if ("GET".equals(method)) {
				params = parseQueryParams(exchange.getRequestURI().getRawQuery());
			} else if ("POST".equals(method)) {
				params = parseJsonBody(exchange.getRequestBody());
			} else {
				sendJson(exchange, 405, JsonResult.error(405, "Method not allowed"));
				return;
			}

			Object result = handle(params);
			sendJson(exchange, 200, JsonResult.ok(result));
			log.fine(() -> method + " " + path + " -> 200 OK");
			return;
		} catch (ApiException e) {
			log.warning(() -> method + " " + path + " -> " + e.getCode() + " " + e.getMessage());
			try { sendJson(exchange, e.getCode(), JsonResult.error(e.getCode(), e.getMessage())); } catch (Exception ignored) { exchange.close(); }
		} catch (Throwable e) {
			log.log(Level.WARNING, method + " " + path + " -> 500 " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
			try { sendJson(exchange, 500, JsonResult.error(500, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage())); } catch (Exception ignored) { exchange.close(); }
		}
	}

	protected abstract Object handle(Map<String, Object> params) throws Throwable;

	private Map<String, Object> parseQueryParams(String query) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		if (query != null && !query.isEmpty()) {
			for (String pair : query.split("&")) {
				int idx = pair.indexOf('=');
				if (idx > 0) {
					String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
					String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
					map.put(key, val);
				}
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> parseJsonBody(InputStream body) throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		byte[] tmp = new byte[4096];
		int n;
		while ((n = body.read(tmp)) != -1) {
			buf.write(tmp, 0, n);
		}
		String json = buf.toString("UTF-8");
		if (json.isEmpty()) {
			return new LinkedHashMap<String, Object>();
		}
		return (Map<String, Object>) JsonReader.jsonToJava(json);
	}

	private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
		byte[] data = json.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
		exchange.sendResponseHeaders(status, data.length);
		OutputStream os = exchange.getResponseBody();
		os.write(data);
		os.close();
	}

	protected String getString(Map<String, Object> params, String key) {
		Object v = params.get(key);
		return v == null ? null : v.toString();
	}

	protected String getString(Map<String, Object> params, String key, String defaultValue) {
		String v = getString(params, key);
		return v != null ? v : defaultValue;
	}

	protected int getInt(Map<String, Object> params, String key, int defaultValue) {
		Object v = params.get(key);
		if (v == null) return defaultValue;
		if (v instanceof Number) return ((Number) v).intValue();
		try { return Integer.parseInt(v.toString()); } catch (Exception e) { return defaultValue; }
	}

	protected boolean getBool(Map<String, Object> params, String key, boolean defaultValue) {
		Object v = params.get(key);
		if (v == null) return defaultValue;
		if (v instanceof Boolean) return ((Boolean) v);
		return "true".equalsIgnoreCase(v.toString()) || "1".equals(v.toString());
	}

	@SuppressWarnings("unchecked")
	protected List<File> toFileList(Object obj) {
		return toFileList(obj, null);
	}

	protected List<File> toFileList(Object obj, String fallbackDir) {
		List<File> files = new ArrayList<File>();
		if (obj instanceof List) {
			for (Object item : (List<Object>) obj) {
				files.add(new File(item.toString()));
			}
		} else if (obj instanceof Collection) {
			for (Object item : (Collection<Object>) obj) {
				files.add(new File(item.toString()));
			}
		} else if (obj instanceof String) {
			files.add(new File((String) obj));
		} else if (obj instanceof Object[]) {
			for (Object item : (Object[]) obj) {
				files.add(new File(item.toString()));
			}
		}
		if (files.isEmpty() && fallbackDir != null && !fallbackDir.isEmpty()) {
			File folder = new File(fallbackDir);
			File[] children = folder.listFiles();
			if (children != null) {
				for (File f : children) {
					if (f.isFile()) {
						files.add(f);
					}
				}
			}
		}
		return files;
	}

}

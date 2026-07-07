package net.filebot.server;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cedarsoftware.util.io.JsonWriter;

public class JsonResult {

	private static final Map<String, Object> JSON_ARGS = new LinkedHashMap<String, Object>();

	static {
		JSON_ARGS.put("TYPE", false);
	}

	public static String ok(Object data) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("status", "ok");
		result.put("data", data);
		return JsonWriter.objectToJson(result, JSON_ARGS);
	}

	public static String error(int code, String message) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("status", "error");
		result.put("code", code);
		result.put("message", message);
		return JsonWriter.objectToJson(result, JSON_ARGS);
	}

	public static String error(int code, String message, Object data) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("status", "error");
		result.put("code", code);
		result.put("message", message);
		result.put("data", data);
		return JsonWriter.objectToJson(result, JSON_ARGS);
	}

}

package net.filebot.server.handlers;

import static net.filebot.Settings.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class StatusHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		Map<String, Object> info = new LinkedHashMap<String, Object>();
		info.put("application", getApplicationIdentifier());
		info.put("java", getJavaRuntimeIdentifier());
		info.put("system", getSystemIdentifier());
		info.put("revision", getApplicationRevisionNumber());
		return info;
	}

}

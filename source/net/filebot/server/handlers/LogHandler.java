package net.filebot.server.handlers;

import java.util.Map;

import net.filebot.server.LogCaptureHandler;

public class LogHandler extends ApiHandler {

	private final LogCaptureHandler logCapture;

	public LogHandler(LogCaptureHandler logCapture) {
		this.logCapture = logCapture;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		String logs = logCapture.pollAll();
		return logs;
	}

}

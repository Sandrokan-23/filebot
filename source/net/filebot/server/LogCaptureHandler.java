package net.filebot.server;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class LogCaptureHandler extends Handler {

	private final Queue<String> buffer = new ConcurrentLinkedQueue<String>();

	@Override
	public void publish(LogRecord record) {
		if (isLoggable(record)) {
			String msg = record.getMessage();
			if (record.getParameters() != null) {
				try {
					msg = java.text.MessageFormat.format(msg, record.getParameters());
				} catch (Exception e) {
					// ignore
				}
			}
			buffer.add(String.format("[%s] %s", record.getLevel().getName(), msg));
		}
	}

	public String pollAll() {
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = buffer.poll()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
		buffer.clear();
	}

}

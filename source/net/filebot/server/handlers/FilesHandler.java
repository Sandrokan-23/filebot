package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.filebot.server.SettingsStore;

public class FilesHandler extends ApiHandler {

	private final SettingsStore settings;

	public FilesHandler(SettingsStore settings) {
		this.settings = settings;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		String path = getString(params, "path");
		if (path == null || path.isEmpty()) {
			path = settings.getWorkingDirectory();
		}
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("No path specified and no working directory configured");
		}

		File dir = new File(path);
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + path);
		}

		File[] children = dir.listFiles();
		List<Object> entries = new ArrayList<Object>();
		if (children != null) {
			// directories first, then files, alphabetically
			java.util.Arrays.sort(children, (a, b) -> {
				if (a.isDirectory() != b.isDirectory()) {
					return a.isDirectory() ? -1 : 1;
				}
				return a.getName().compareToIgnoreCase(b.getName());
			});
			for (File f : children) {
				entries.add(new java.util.LinkedHashMap<String, Object>() {{
					put("name", f.getName());
					put("isDirectory", f.isDirectory());
					put("size", f.isFile() ? f.length() : 0);
				}});
			}
		}
		return entries;
	}

}

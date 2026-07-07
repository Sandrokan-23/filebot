package net.filebot.server;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsStore {

	private String workingDirectory = "";
	private String language = "en";

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(String path) {
		if (path != null && !path.isEmpty()) {
			File dir = new File(path);
			if (dir.isDirectory() || dir.mkdirs()) {
				workingDirectory = dir.getAbsolutePath();
			}
		} else {
			workingDirectory = "";
		}
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String lang) {
		if (lang != null && !lang.isEmpty()) {
			this.language = lang;
		}
	}

	public Map<String, Object> getAll() {
		Map<String, Object> s = new LinkedHashMap<String, Object>();
		s.put("workingDirectory", workingDirectory);
		s.put("language", language);
		return s;
	}

	public void apply(Map<String, Object> updates) {
		if (updates.containsKey("workingDirectory")) {
			setWorkingDirectory(updates.get("workingDirectory").toString());
		}
		if (updates.containsKey("language")) {
			setLanguage(updates.get("language").toString());
		}
	}

}

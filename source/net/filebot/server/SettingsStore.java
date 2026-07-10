package net.filebot.server;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class SettingsStore {

	private final File configFile;

	private String workingDirectory = "";
	private String language = "en";

	private String format = "{plex}";
	private String db = "TheTVDB";
	private String action = "move";
	private String conflict = "skip";
	private String order = "Airdate";
	private String filter = "";
	private boolean nonStrict = false;
	private String output = "";

	private String subtitlesLanguage = "en";
	private String subtitlesOutput = "";
	private String subtitlesEncoding = "";
	private String subtitlesNaming = "MATCH_VIDEO_ADD_LANGUAGE_TAG";
	private boolean subtitlesMissingOnly = true;

	public SettingsStore(File configFile) {
		this.configFile = configFile;
		load();
	}

	@SuppressWarnings("unchecked")
	private void load() {
		if (configFile != null && configFile.isFile()) {
			try {
				String json = new String(Files.readAllBytes(configFile.toPath()), "UTF-8");
				Map<String, Object> data = (Map<String, Object>) JsonReader.jsonToJava(json);
				apply(data);
			} catch (Exception e) {
				System.err.println("Failed to load settings: " + e.getMessage());
			}
		}
	}

	private void save() {
		if (configFile != null) {
			try {
				String json = JsonWriter.objectToJson(getAll());
				File tmp = new File(configFile.getParentFile(), configFile.getName() + ".tmp");
				Files.write(tmp.toPath(), json.getBytes("UTF-8"));
				Files.move(tmp.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception e) {
				System.err.println("Failed to save settings: " + e.getMessage());
			}
		}
	}

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

	public String getFormat() {
		return format;
	}

	public void setFormat(String value) {
		if (value != null) this.format = value;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String value) {
		if (value != null) this.db = value;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String value) {
		if (value != null) this.action = value;
	}

	public String getConflict() {
		return conflict;
	}

	public void setConflict(String value) {
		if (value != null) this.conflict = value;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String value) {
		if (value != null) this.order = value;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String value) {
		if (value != null) this.filter = value;
	}

	public boolean isNonStrict() {
		return nonStrict;
	}

	public void setNonStrict(boolean value) {
		this.nonStrict = value;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String value) {
		if (value != null) this.output = value;
	}

	public String getSubtitlesLanguage() {
		return subtitlesLanguage;
	}

	public void setSubtitlesLanguage(String value) {
		if (value != null) this.subtitlesLanguage = value;
	}

	public String getSubtitlesOutput() {
		return subtitlesOutput;
	}

	public void setSubtitlesOutput(String value) {
		if (value != null) this.subtitlesOutput = value;
	}

	public String getSubtitlesEncoding() {
		return subtitlesEncoding;
	}

	public void setSubtitlesEncoding(String value) {
		if (value != null) this.subtitlesEncoding = value;
	}

	public String getSubtitlesNaming() {
		return subtitlesNaming;
	}

	public void setSubtitlesNaming(String value) {
		if (value != null) this.subtitlesNaming = value;
	}

	public boolean isSubtitlesMissingOnly() {
		return subtitlesMissingOnly;
	}

	public void setSubtitlesMissingOnly(boolean value) {
		this.subtitlesMissingOnly = value;
	}

	public Map<String, Object> getAll() {
		Map<String, Object> s = new LinkedHashMap<String, Object>();
		s.put("workingDirectory", workingDirectory);
		s.put("language", language);
		s.put("format", format);
		s.put("db", db);
		s.put("action", action);
		s.put("conflict", conflict);
		s.put("order", order);
		s.put("filter", filter);
		s.put("nonStrict", nonStrict);
		s.put("output", output);
		s.put("subtitlesLanguage", subtitlesLanguage);
		s.put("subtitlesOutput", subtitlesOutput);
		s.put("subtitlesEncoding", subtitlesEncoding);
		s.put("subtitlesNaming", subtitlesNaming);
		s.put("subtitlesMissingOnly", subtitlesMissingOnly);
		return s;
	}

	public void apply(Map<String, Object> updates) {
		if (updates.containsKey("workingDirectory")) setWorkingDirectory(updates.get("workingDirectory").toString());
		if (updates.containsKey("language")) setLanguage(updates.get("language").toString());
		if (updates.containsKey("format")) setFormat(updates.get("format").toString());
		if (updates.containsKey("db")) setDb(updates.get("db").toString());
		if (updates.containsKey("action")) setAction(updates.get("action").toString());
		if (updates.containsKey("conflict")) setConflict(updates.get("conflict").toString());
		if (updates.containsKey("order")) setOrder(updates.get("order").toString());
		if (updates.containsKey("filter")) setFilter(updates.get("filter").toString());
		if (updates.containsKey("nonStrict")) setNonStrict("true".equals(updates.get("nonStrict").toString()) || Boolean.parseBoolean(updates.get("nonStrict").toString()));
		if (updates.containsKey("output")) setOutput(updates.get("output").toString());
		if (updates.containsKey("subtitlesLanguage")) setSubtitlesLanguage(updates.get("subtitlesLanguage").toString());
		if (updates.containsKey("subtitlesOutput")) setSubtitlesOutput(updates.get("subtitlesOutput").toString());
		if (updates.containsKey("subtitlesEncoding")) setSubtitlesEncoding(updates.get("subtitlesEncoding").toString());
		if (updates.containsKey("subtitlesNaming")) setSubtitlesNaming(updates.get("subtitlesNaming").toString());
		if (updates.containsKey("subtitlesMissingOnly")) setSubtitlesMissingOnly("true".equals(updates.get("subtitlesMissingOnly").toString()) || Boolean.parseBoolean(updates.get("subtitlesMissingOnly").toString()));
		save();
	}

}

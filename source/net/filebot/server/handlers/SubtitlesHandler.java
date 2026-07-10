package net.filebot.server.handlers;

import static net.filebot.subtitle.SubtitleUtilities.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.filebot.Language;
import net.filebot.cli.CmdlineOperations;
import net.filebot.server.SettingsStore;
import net.filebot.subtitle.SubtitleFormat;
import net.filebot.subtitle.SubtitleNaming;

public class SubtitlesHandler extends ApiHandler {

	private final SettingsStore settings;

	public SubtitlesHandler(SettingsStore settings) {
		this.settings = settings;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));
		String query = getString(params, "query");

		String langStr = getString(params, "lang", settings.getSubtitlesLanguage());
		Language lang = Language.findLanguage(langStr);

		String outputStr = getString(params, "output", settings.getSubtitlesOutput());
		SubtitleFormat output = outputStr != null && outputStr.length() > 0 ? getSubtitleFormatByName(outputStr) : null;

		String encodingStr = getString(params, "encoding", settings.getSubtitlesEncoding());
		Charset encoding = encodingStr != null && encodingStr.length() > 0 ? Charset.forName(encodingStr) : null;

		String namingStr = getString(params, "naming", settings.getSubtitlesNaming());
		SubtitleNaming naming = SubtitleNaming.forName(namingStr);

		boolean strict = !getBool(params, "nonStrict", false);
		boolean missingOnly = getBool(params, "missingOnly", settings.isSubtitlesMissingOnly());

		List<File> result;
		if (missingOnly) {
			result = cli.getMissingSubtitles(files, query, lang, output, encoding, naming, strict);
		} else {
			result = cli.getSubtitles(files, query, lang, output, encoding, naming, strict);
		}

		return toPathList(result);
	}

	private List<String> toPathList(List<File> files) {
		List<String> paths = new ArrayList<String>();
		for (File f : files) {
			paths.add(f.getPath());
		}
		return paths;
	}

}

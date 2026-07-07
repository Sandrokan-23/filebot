package net.filebot.server.handlers;

import static net.filebot.subtitle.SubtitleUtilities.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.filebot.Language;
import net.filebot.cli.CmdlineOperations;
import net.filebot.subtitle.SubtitleFormat;
import net.filebot.subtitle.SubtitleNaming;

public class SubtitlesHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));
		String query = getString(params, "query");
		Language lang = getString(params, "lang") != null ? Language.findLanguage(getString(params, "lang")) : Language.findLanguage("en");
		SubtitleFormat output = getString(params, "output") != null ? getSubtitleFormatByName(getString(params, "output")) : null;
		Charset encoding = getString(params, "encoding") != null ? Charset.forName(getString(params, "encoding")) : null;
		SubtitleNaming naming = getString(params, "naming") != null ? SubtitleNaming.forName(getString(params, "naming")) : SubtitleNaming.MATCH_VIDEO_ADD_LANGUAGE_TAG;
		boolean strict = !getBool(params, "nonStrict", false);

		boolean missingOnly = getBool(params, "missingOnly", true);
		List<File> result;
		if (missingOnly) {
			result = cli.getMissingSubtitles(files, query, lang, output, encoding, naming, strict);
		} else {
			result = cli.getSubtitles(files, query, lang, output, encoding, naming, strict);
		}

		return toPathList(result);
	}

	@SuppressWarnings("unchecked")
	private List<File> toFileList(Object obj) {
		List<File> files = new ArrayList<File>();
		if (obj instanceof List) {
			for (Object item : (List<Object>) obj) {
				files.add(new File(item.toString()));
			}
		}
		return files;
	}

	private List<String> toPathList(List<File> files) {
		List<String> paths = new ArrayList<String>();
		for (File f : files) {
			paths.add(f.getPath());
		}
		return paths;
	}

}

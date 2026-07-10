package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.filebot.RenameAction;
import net.filebot.StandardRenameAction;
import net.filebot.WebServices;
import net.filebot.cli.CmdlineOperations;
import net.filebot.cli.ConflictAction;
import net.filebot.format.ExpressionFileFormat;
import net.filebot.format.ExpressionFilter;
import net.filebot.server.SettingsStore;
import net.filebot.web.Datasource;
import net.filebot.web.SortOrder;

public class RenameHandler extends ApiHandler {

	private final SettingsStore settings;

	public RenameHandler(SettingsStore settings) {
		this.settings = settings;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"), settings.getWorkingDirectory());
		RenameAction action = StandardRenameAction.forName(getString(params, "action", settings.getAction()));

		ConflictAction conflict = ConflictAction.forName(getString(params, "conflict", settings.getConflict()));

		String outputPath = getString(params, "output", settings.getOutput());
		File output = outputPath != null && outputPath.length() > 0 ? new File(outputPath) : null;

		String formatStr = getString(params, "format", settings.getFormat());
		ExpressionFileFormat format = formatStr != null && formatStr.length() > 0 ? new ExpressionFileFormat(formatStr) : null;

		String dbStr = getString(params, "db", settings.getDb());
		Datasource db = dbStr != null && dbStr.length() > 0 ? WebServices.getService(dbStr) : null;

		String query = getString(params, "query");

		String orderStr = getString(params, "order", settings.getOrder());
		SortOrder order = orderStr != null ? SortOrder.forName(orderStr) : null;

		String filterStr = getString(params, "filter", settings.getFilter());
		ExpressionFilter filter = filterStr != null && filterStr.length() > 0 ? new ExpressionFilter(filterStr) : null;

		boolean strict = !getBool(params, "nonStrict", settings.isNonStrict());

		Locale locale = Locale.forLanguageTag(settings.getLanguage().replace("_", "-"));
		if (locale == null) {
			locale = Locale.ENGLISH;
		}

		// auto-detect query from filename if not provided
		if (query == null && files.size() > 0) {
			query = autoQuery(files.get(0).getName());
		}

		List<File> result = cli.rename(files, action, conflict, output, format, db, query, order, filter, locale, strict, null);
		return toPathList(result);
	}

	private String autoQuery(String name) {
		// strip extension
		int dot = name.lastIndexOf('.');
		String base = dot > 0 ? name.substring(0, dot) : name;

		// remove common release tags: quality, codec, language, release group, etc.
		base = base.replaceAll("(?i)\\.(PROPER|REPACK|REAL|iTA-?ENG|ENG-?iTA|ITA|ENG|MULTi|SUB|iTALiAN|AC3|DTS|DD5[.]1|AAC|x264|x265|HEVC|Bluray|WEB-DL|WEBRip|HDTV|DVDRip|HDRip|BrRip|1080p|720p|2160p|480p|576p)\\.", ".");
		base = base.replaceAll("(?i)\\.(PROPER|REPACK|REAL|iTA-?ENG|ENG-?iTA|ITA|ENG|MULTi|SUB|iTALiAN|AC3|DTS|DD5[.]1|AAC|x264|x265|HEVC|Bluray|WEB-DL|WEBRip|HDTV|DVDRip|HDRip|BrRip|1080p|720p|2160p|480p|576p)$", "");

		// keep year but strip trailing release group (last dash-segment)
		base = base.replaceAll("-\\w+$", "");

		// replace dots/underscores with spaces
		return base.replaceAll("[._]", " ").trim();
	}

	private List<String> toPathList(List<File> files) {
		List<String> paths = new ArrayList<String>();
		for (File f : files) {
			paths.add(f.getPath());
		}
		return paths;
	}

}

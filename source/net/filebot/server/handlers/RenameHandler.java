package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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

		List<File> result = cli.rename(files, action, conflict, output, format, db, query, order, filter, java.util.Locale.ENGLISH, strict, null);
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

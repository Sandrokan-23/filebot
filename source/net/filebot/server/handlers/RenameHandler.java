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
import net.filebot.web.Datasource;
import net.filebot.web.SortOrder;

public class RenameHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));
		RenameAction action = StandardRenameAction.forName(getString(params, "action"));
		ConflictAction conflict = ConflictAction.forName(getString(params, "conflict"));
		File output = getString(params, "output") != null ? new File(getString(params, "output")) : null;
		ExpressionFileFormat format = getString(params, "format") != null ? new ExpressionFileFormat(getString(params, "format")) : null;
		Datasource db = getString(params, "db") != null ? WebServices.getService(getString(params, "db")) : null;
		String query = getString(params, "query");
		SortOrder order = getString(params, "order") != null ? SortOrder.forName(getString(params, "order")) : null;
		ExpressionFilter filter = getString(params, "filter") != null ? new ExpressionFilter(getString(params, "filter")) : null;
		boolean strict = !getBool(params, "nonStrict", false);

		List<File> result = cli.rename(files, action, conflict, output, format, db, query, order, filter, java.util.Locale.ENGLISH, strict, null);
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

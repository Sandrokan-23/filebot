package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.filebot.cli.CmdlineOperations;
import net.filebot.cli.ConflictAction;

public class ExtractHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));
		File output = getString(params, "output") != null ? new File(getString(params, "output")) : null;
		ConflictAction conflict = getString(params, "conflict") != null ? ConflictAction.forName(getString(params, "conflict")) : ConflictAction.SKIP;
		boolean forceExtractAll = getBool(params, "forceExtractAll", false);

		List<File> result = cli.extract(files, output, conflict, null, forceExtractAll);
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

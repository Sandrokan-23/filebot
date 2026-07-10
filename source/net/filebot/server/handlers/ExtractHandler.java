package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.filebot.cli.CmdlineOperations;
import net.filebot.cli.ConflictAction;
import net.filebot.server.SettingsStore;

public class ExtractHandler extends ApiHandler {

	private final SettingsStore settings;

	public ExtractHandler(SettingsStore settings) {
		this.settings = settings;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));

		String outputPath = getString(params, "output", settings.getOutput());
		File output = outputPath != null && outputPath.length() > 0 ? new File(outputPath) : null;

		ConflictAction conflict = ConflictAction.forName(getString(params, "conflict", settings.getConflict()));
		boolean forceExtractAll = getBool(params, "forceExtractAll", false);

		List<File> result = cli.extract(files, output, conflict, null, forceExtractAll);
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

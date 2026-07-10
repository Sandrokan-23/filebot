package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.filebot.RenameAction;
import net.filebot.StandardRenameAction;
import net.filebot.cli.CmdlineOperations;
import net.filebot.server.SettingsStore;

public class RevertHandler extends ApiHandler {

	private final SettingsStore settings;

	public RevertHandler(SettingsStore settings) {
		this.settings = settings;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));

		String actionStr = getString(params, "action", settings.getAction());
		RenameAction action = actionStr != null ? StandardRenameAction.forName(actionStr) : null;

		List<File> result = cli.revert(files, null, action);
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

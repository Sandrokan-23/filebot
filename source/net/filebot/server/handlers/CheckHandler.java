package net.filebot.server.handlers;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.filebot.cli.CmdlineOperations;
import net.filebot.hash.HashType;
import net.filebot.server.SettingsStore;

public class CheckHandler extends ApiHandler {

	private final SettingsStore settings;

	public CheckHandler(SettingsStore settings) {
		this.settings = settings;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));
		String outputStr = getString(params, "output", settings.getOutput());
		File output = outputStr != null && !outputStr.isEmpty() ? new File(outputStr) : null;
		HashType hash = getString(params, "hash") != null ? parseHashType(getString(params, "hash")) : HashType.SFV;
		Charset encoding = getString(params, "encoding") != null ? Charset.forName(getString(params, "encoding")) : null;

		Map<String, Object> result = new LinkedHashMap<String, Object>();

		if (output != null && output.isFile()) {
			boolean ok = cli.check(files);
			result.put("type", "check");
			result.put("ok", ok);
		} else {
			File computed = cli.compute(files, output, hash, encoding);
			result.put("type", "compute");
			result.put("file", computed.getPath());
		}

		return result;
	}

	private HashType parseHashType(String name) {
		for (HashType t : HashType.values()) {
			if (t.name().equalsIgnoreCase(name)) {
				return t;
			}
		}
		return HashType.SFV;
	}

}

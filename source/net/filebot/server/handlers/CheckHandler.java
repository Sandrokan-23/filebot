package net.filebot.server.handlers;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.filebot.cli.CmdlineOperations;
import net.filebot.hash.HashType;

public class CheckHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));
		File output = getString(params, "output") != null ? new File(getString(params, "output")) : null;
		HashType hash = getString(params, "hash") != null ? parseHashType(getString(params, "hash")) : HashType.SFV;
		Charset encoding = getString(params, "encoding") != null ? Charset.forName(getString(params, "encoding")) : null;

		Map<String, Object> result = new LinkedHashMap<String, Object>();

		if (output != null && output.isFile()) {
			// check verification file
			boolean ok = cli.check(files);
			result.put("type", "check");
			result.put("ok", ok);
		} else {
			// compute hashes
			File computed = cli.compute(files, output, hash, encoding);
			result.put("type", "compute");
			result.put("file", computed.getPath());
		}

		return result;
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

	private HashType parseHashType(String name) {
		for (HashType t : HashType.values()) {
			if (t.name().equalsIgnoreCase(name)) {
				return t;
			}
		}
		return HashType.SFV;
	}

}

package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.filebot.cli.CmdlineOperations;
import net.filebot.format.ExpressionFormat;

public class MediaInfoHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		List<File> files = toFileList(params.get("files"));
		ExpressionFormat format = getString(params, "format") != null ? new ExpressionFormat(getString(params, "format")) : null;

		try (Stream<String> stream = cli.getMediaInfo(files, null, format)) {
			return stream.collect(Collectors.toList());
		}
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

}

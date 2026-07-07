package net.filebot.server.handlers;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.filebot.WebServices;
import net.filebot.cli.CmdlineOperations;
import net.filebot.format.ExpressionFilter;
import net.filebot.format.ExpressionFormat;
import net.filebot.web.EpisodeListProvider;
import net.filebot.web.SortOrder;

public class ListHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		EpisodeListProvider db = getString(params, "db") != null ? WebServices.getEpisodeListProvider(getString(params, "db")) : WebServices.TheTVDB;
		String query = getString(params, "query");
		ExpressionFormat format = getString(params, "format") != null ? new ExpressionFormat(getString(params, "format")) : null;
		ExpressionFilter filter = getString(params, "filter") != null ? new ExpressionFilter(getString(params, "filter")) : null;
		SortOrder order = getString(params, "order") != null ? SortOrder.forName(getString(params, "order")) : SortOrder.Airdate;
		boolean strict = !getBool(params, "nonStrict", false);

		try (Stream<String> stream = cli.fetchEpisodeList(db, query, format, filter, order, java.util.Locale.ENGLISH, strict)) {
			return stream.collect(Collectors.toList());
		}
	}

}

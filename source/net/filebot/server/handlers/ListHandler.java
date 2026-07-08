package net.filebot.server.handlers;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.filebot.WebServices;
import net.filebot.cli.CmdlineOperations;
import net.filebot.format.ExpressionFilter;
import net.filebot.format.ExpressionFormat;
import net.filebot.server.SettingsStore;
import net.filebot.web.EpisodeListProvider;
import net.filebot.web.SortOrder;

public class ListHandler extends ApiHandler {

	private final SettingsStore settings;

	public ListHandler(SettingsStore settings) {
		this.settings = settings;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		CmdlineOperations cli = new CmdlineOperations();

		String dbStr = getString(params, "db", settings.getDb());
		EpisodeListProvider db = dbStr != null ? WebServices.getEpisodeListProvider(dbStr) : WebServices.TheTVDB;

		String query = getString(params, "query");

		String formatStr = getString(params, "format", settings.getFormat());
		ExpressionFormat format = formatStr != null && formatStr.length() > 0 ? new ExpressionFormat(formatStr) : null;

		String filterStr = getString(params, "filter", settings.getFilter());
		ExpressionFilter filter = filterStr != null && filterStr.length() > 0 ? new ExpressionFilter(filterStr) : null;

		String orderStr = getString(params, "order", settings.getOrder());
		SortOrder order = orderStr != null ? SortOrder.forName(orderStr) : SortOrder.Airdate;

		boolean strict = !getBool(params, "nonStrict", settings.isNonStrict());

		try (Stream<String> stream = cli.fetchEpisodeList(db, query, format, filter, order, java.util.Locale.ENGLISH, strict)) {
			return stream.collect(Collectors.toList());
		}
	}

}

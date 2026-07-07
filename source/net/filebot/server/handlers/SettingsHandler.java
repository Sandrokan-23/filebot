package net.filebot.server.handlers;

import java.util.Map;

import net.filebot.server.SettingsStore;

public class SettingsHandler extends ApiHandler {

	private final SettingsStore store;

	public SettingsHandler(SettingsStore store) {
		this.store = store;
	}

	@Override
	protected Object handle(Map<String, Object> params) throws Exception {
		// POST with setting keys updates them; GET just returns current settings
		if (params.containsKey("workingDirectory") || params.containsKey("language")) {
			store.apply(params);
		}
		return store.getAll();
	}

}

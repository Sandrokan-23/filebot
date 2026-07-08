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
		// apply any settings keys that were sent
		store.apply(params);
		return store.getAll();
	}

}

package net.filebot.server.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import net.filebot.cli.ArgumentBean;
import net.filebot.cli.CmdlineOperations;
import net.filebot.cli.ScriptShell;
import net.filebot.cli.ScriptSource;

public class ScriptHandler extends ApiHandler {

	@Override
	protected Object handle(Map<String, Object> params) throws Throwable {
		CmdlineOperations cli = new CmdlineOperations();

		String script = getString(params, "script");
		if (script == null || script.isEmpty()) {
			throw new IllegalArgumentException("Missing 'script' parameter");
		}

		List<File> files = toFileList(params.get("files"));

		@SuppressWarnings("unchecked")
		Map<String, String> defines = params.get("def") instanceof Map ? (Map<String, String>) params.get("def") : new LinkedHashMap<String, String>();

		ScriptSource source = ScriptSource.findScriptProvider(script);
		ScriptShell shell = new ScriptShell(source.getScriptProvider(script), cli, defines);

		Bindings bindings = new SimpleBindings();
		bindings.put(ScriptShell.SHELL_ARGS_BINDING_NAME, new ArgumentBean());
		bindings.put(ScriptShell.ARGV_BINDING_NAME, files);

		shell.runScript(source.accept(script), bindings);

		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put("status", "completed");
		return result;
	}

}

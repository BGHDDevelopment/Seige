package me.ItsJasonn.Siege.Main;

public class Plugin {
	private static Core core;

	@SuppressWarnings("static-access")
	public Plugin(Core core) {
		this.core = core;
	}
	
	public static Core getCore() {
		return core;
	}
}

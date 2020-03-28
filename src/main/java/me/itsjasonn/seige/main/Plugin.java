package me.itsjasonn.seige.main;


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

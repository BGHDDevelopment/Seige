package me.ItsJasonn.Siege.Listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.ItsJasonn.Siege.Main.Core;
import me.ItsJasonn.Siege.Main.Plugin;
import me.ItsJasonn.Siege.Map.Map;

public class PlayerJoin implements Listener {
	Core core;

	public PlayerJoin(Core core) {
		this.core = core;
	}

	@SuppressWarnings("static-access")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);

		Player player = event.getPlayer();
		Map.getMapManager().addPlayer(player, true);
		Map.getMapManager().TeleportToLobby(player);
		
		if(Map.getMapManager().inLobby) {
			Map.getMapManager().TeleportToLobby(player);
		} else {
			Map.getMapManager().setSpectator(player);
		}
		
		if(!Plugin.getCore().getSQLManager().hasData("stats", player.getUniqueId().toString())) {
			try {
				Plugin.getCore().getSQLManager().createColumn("stats", player.getUniqueId().toString(), new String[] {"kills", "deaths", "gamesWon"}, new Object[] {0, 0, 0});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

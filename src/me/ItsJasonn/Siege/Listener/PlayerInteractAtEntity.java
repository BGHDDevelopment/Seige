package me.ItsJasonn.Siege.Listener;

import me.ItsJasonn.Siege.Main.Core;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class PlayerInteractAtEntity implements Listener {
	Core core;

	public PlayerInteractAtEntity(Core core) {
		this.core = core;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
		if (((event.getRightClicked() instanceof Zombie)) && (event.getRightClicked().getCustomName() != null) && (event.getRightClicked().getCustomName().equalsIgnoreCase(ChatColor.GOLD + "" + ChatColor.BOLD + "King Olaf")) && (event.getPlayer().getItemInHand() != null) && (event.getPlayer().getItemInHand().getType() == Material.BLAZE_ROD)) {
			event.getRightClicked().remove();
		}
	}
}

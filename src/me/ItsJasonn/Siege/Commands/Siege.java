package me.ItsJasonn.Siege.Commands;

import me.ItsJasonn.Siege.Main.Plugin;
import me.ItsJasonn.Siege.Map.Map;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Siege implements CommandExecutor {
	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		String command = cmd.getName();
		if (command.equalsIgnoreCase("Siege")) {
			if ((sender instanceof Player)) {
				Player player = (Player) sender;
				if (player.isOp() || player.hasPermission("siege.siege")) {
					if (args.length == 0) {
						player.sendMessage(ChatColor.DARK_AQUA + "/Siege" + ChatColor.AQUA + " - Show this help page.");
						player.sendMessage(ChatColor.DARK_AQUA + "/Siege Create" + ChatColor.AQUA + " - Create a new map with the given index number.");
						player.sendMessage(ChatColor.DARK_AQUA + "/Siege Delete" + ChatColor.AQUA + " - Delete an existing siege map.");
						player.sendMessage(ChatColor.DARK_AQUA + "/Siege Setlobby" + ChatColor.AQUA + " - Set the lobby spawn location.");
						player.sendMessage(ChatColor.DARK_AQUA + "/Siege Setspawn [Team]" + ChatColor.AQUA + " - Set a spawn location for a team.");
						player.sendMessage(ChatColor.DARK_AQUA + "/Siege Setking" + ChatColor.AQUA + " - Spawn the king for a siege map.");
						player.sendMessage(ChatColor.DARK_AQUA + "/Siege Forcestart" + ChatColor.AQUA + " - Start a match of the player's current game.");
					} else if (args.length == 1) {
						if (args[0].equalsIgnoreCase("Create")) {
							if (!Map.getMapManager().DoesExist()) {
								Map.getMapManager().CreateMap();
								player.sendMessage(ChatColor.GREEN + "Dungeon has been created!");
							} else {
								player.sendMessage(ChatColor.RED + "Dungeon does not exist!");
							}
						} else if (args[0].equalsIgnoreCase("Delete")) {
							Map.getMapManager().RemoveMap();
							player.sendMessage(ChatColor.GREEN + "Dungeon has been removed!");
						} else if (args[0].equalsIgnoreCase("Setlobby")) {
							if (Map.getMapManager().DoesExist()) {
								Map.getMapManager().SetLobby(player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
								player.sendMessage(ChatColor.GREEN + "Lobby location for Siege map has been set!");
							} else {
								player.sendMessage(ChatColor.RED + "This Siege map does not exist!");
							}
						} else {
							if (args[0].equalsIgnoreCase("Setking")) {
								player.sendMessage(ChatColor.GREEN + "You have placed the king!");

								Zombie zombie = (Zombie) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
								zombie.setCustomName(ChatColor.translateAlternateColorCodes('&', Plugin.getCore().getConfig().getString("siege.king.name")));
								zombie.setCustomNameVisible(true);
								zombie.setBaby(false);
								zombie.setAI(false);
								zombie.setGlowing(true);
								zombie.setCollidable(false);
								player.spigot().setCollidesWithEntities(false);

								ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
								SkullMeta headMeta = (SkullMeta) head.getItemMeta();
								headMeta.setOwner("SolidFlames");
								head.setItemMeta(headMeta);

								ItemStack sword = new ItemStack(Material.IRON_SWORD);
								ItemStack shield = new ItemStack(Material.SHIELD);
								sword.addEnchantment(Enchantment.DURABILITY, 1);
								shield.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

								ItemStack chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
								ItemStack leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
								ItemStack boots = new ItemStack(Material.DIAMOND_BOOTS);

								ItemMeta chestplateMeta = chestplate.getItemMeta();
								ItemMeta leggingsMeta = leggings.getItemMeta();
								ItemMeta bootsMeta = boots.getItemMeta();

								chestplateMeta.spigot().setUnbreakable(true);
								leggingsMeta.spigot().setUnbreakable(true);
								bootsMeta.spigot().setUnbreakable(true);

								chestplate.setItemMeta(chestplateMeta);
								leggings.setItemMeta(leggingsMeta);
								boots.setItemMeta(bootsMeta);

								zombie.getEquipment().setHelmet(head);
								zombie.getEquipment().setChestplate(chestplate);
								zombie.getEquipment().setLeggings(leggings);
								zombie.getEquipment().setBoots(boots);
								zombie.getEquipment().setItemInMainHand(sword);
								zombie.getEquipment().setItemInOffHand(shield);
							} else if (args[0].equalsIgnoreCase("SetSpawn")) {
								player.sendMessage(ChatColor.AQUA + "Usage: " + ChatColor.DARK_AQUA + "/Siege " + WordUtils.capitalizeFully(args[0]) + " [Team]");
							} else if (args[0].equalsIgnoreCase("Forcestart")) {
								for (Player players : Map.getMapManager().getPlayers()) {
									Map.getMapManager().StartRound(players, 1);
								}
							}
						}
					} else if ((args.length == 2) && (args[0].equalsIgnoreCase("Setspawn"))) {
						String team = args[1];
						if ((team.equalsIgnoreCase("Guards")) || (team.equalsIgnoreCase("Assassins"))) {
							if (Map.getMapManager().DoesExist()) {
								Map.getMapManager().SetSpawn(team, player.getLocation().getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());
								player.sendMessage(ChatColor.GREEN + "Spawn location in Siege for Team '" + team + "' has been set!");
							} else {
								player.sendMessage(ChatColor.RED + "Siege map does not exist!");
							}
						} else {
							player.sendMessage(ChatColor.RED + "Fourth parameter '" + args[1] + "' is not a valid team!");
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "You are not permitted to use this command!");
				}
			} else if ((sender instanceof ConsoleCommandSender)) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This is a player-only command!");
			}
		}
		return false;
	}
}

package me.itsjasonn.seige.listener;

import java.io.File;
import me.itsjasonn.seige.main.Core;
import me.itsjasonn.seige.main.Plugin;
import me.itsjasonn.seige.map.Map;
import me.itsjasonn.seige.utils.MySQLManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class EntityStats implements Listener {
	Core core;

	public EntityStats(Core core) {
		this.core = core;
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if ((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			if (Map.getMapManager().isInGame(player)) {
				if ((event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) || (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) || (Map.getMapManager().getRound() == 0)) {
					event.setCancelled(true);
				} else if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
					event.setDamage(event.getDamage() * 0.35d);
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
		if ((event.getEntity() instanceof Zombie)) {
			Zombie zombie = (Zombie) event.getEntity();
			if ((zombie.getCustomName() != null) && (zombie.getCustomName().equalsIgnoreCase(ChatColor.translateAlternateColorCodes('&', Plugin.getCore().getConfig().getString("siege.king.name"))))) {
				event.setCancelled(true);
				if ((event.getDamager() instanceof Player)) {
					Player player = (Player) event.getDamager();
					if ((Map.getMapManager().isInGame(player)) && (!event.getEntity().isInvulnerable())
							&& (Map.getMapManager().getTeam(player).equalsIgnoreCase("Assassin"))) {
						zombie.damage(0.0D);
						
						Map.kingsHealth -= 1;
						
						for (Player players : Map.getMapManager().getPlayers()) {
							Map.getMapManager().resetScoreboard(players);
						}
						if (Map.kingsHealth <= 0) {
							Map.kingsHealth = 0;

							event.getEntity().setInvulnerable(true);
							Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Plugin.getCore(), new Runnable() {
								public void run() {
									event.getEntity().setInvulnerable(false);
								}
							}, 120L);

							int newRound = Map.getMapManager().getRound() + 1;
							Map.getMapManager().addPoint("Assassins");
							for (Player players : Map.getMapManager().getPlayers()) {
								Map.getMapManager().StartRound(players, newRound);
							}
						}
					}
				}
			}
		} else if ((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			if ((event.getDamager() instanceof Player)) {
				Player damager = (Player) event.getDamager();
				if ((Map.getMapManager().isInGame(player)) && (Map.getMapManager().isInGame(damager))) {
					if (Map.getMapManager().getTeam(player) == Map.getMapManager().getTeam(damager)) {
						event.setCancelled(true);
					}
				} else if (Map.getMapManager().isInGame(player)) {
					event.setCancelled(true);
				} else if (Map.getMapManager().isInGame(damager)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if ((event.getEntity() instanceof Player)) {
			Player player = (Player) event.getEntity();
			if (Map.getMapManager().isInGame(player)) {
				event.setCancelled(true);
				player.setFoodLevel(20);
			}
		}
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (Map.getMapManager().isInGame(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (Map.getMapManager().isInGame(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (Map.getMapManager().isInGame(player)) {
			event.setDeathMessage(null);
			event.setDroppedExp(0);
			event.getDrops().clear();
			
			if(MySQLManager.using()) {
				try {
					Plugin.getCore().getSQLManager().updateCell("stats", player.getUniqueId().toString(), "deaths", Integer.toString(Integer.parseInt((String) Plugin.getCore().getSQLManager().getDataByField("stats", "uuid", player.getUniqueId().toString(), "deaths")) + 1));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if(player.getKiller() != null) {
				Player killer = player.getKiller();
				
				if(MySQLManager.using()) {
					try {
						Plugin.getCore().getSQLManager().updateCell("stats", killer.getUniqueId().toString(), "kills", Integer.toString(Integer.parseInt((String) Plugin.getCore().getSQLManager().getDataByField("stats", "uuid", killer.getUniqueId().toString(), "kills")) + 1));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			event.setDroppedExp(0);
			event.getDrops().clear();
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (Map.getMapManager().isInGame(player)) {
			File file = new File(Plugin.getCore().getDataFolder(), "/data/maps.yml");
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			String t = Map.getMapManager().getTeam(player) + "s";
			Location location = new Location(Bukkit.getServer().getWorld(config.getString("spawns." + t + ".world")), config.getDouble("spawns." + t + ".x"), config.getDouble("spawns." + t + ".y"), config.getDouble("spawns." + t + ".z"), (float) config.getDouble("spawns." + t + ".yaw"), (float) config.getDouble("spawns." + t + ".pitch"));
			event.setRespawnLocation(location);

			Map.getMapManager().resetScoreboard(player);

			player.setGameMode(GameMode.ADVENTURE);
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			player.setLevel(0);
			player.setExp(0.0F);
			player.getInventory().clear();
			if (Map.getMapManager().getTeam(player).equalsIgnoreCase("Assassin")) {
				ItemStack sword = new ItemStack(Material.IRON_SWORD);
				ItemMeta swordMeta = sword.getItemMeta();
				swordMeta.setDisplayName(ChatColor.AQUA + "Sword");
				swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				swordMeta.spigot().setUnbreakable(true);
				sword.setItemMeta(swordMeta);

				ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
				ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
				ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
				ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);

				ItemMeta helmetMeta = helmet.getItemMeta();
				ItemMeta chestplateMeta = chestplate.getItemMeta();
				ItemMeta leggingsMeta = leggings.getItemMeta();
				ItemMeta bootsMeta = boots.getItemMeta();

				helmetMeta.setDisplayName(ChatColor.AQUA + "Helmet");
				chestplateMeta.setDisplayName(ChatColor.AQUA + "Chestplate");
				leggingsMeta.setDisplayName(ChatColor.AQUA + "Leggings");
				bootsMeta.setDisplayName(ChatColor.AQUA + "Boots");

				helmetMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				chestplateMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				leggingsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				bootsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

				helmetMeta.spigot().setUnbreakable(true);
				chestplateMeta.spigot().setUnbreakable(true);
				leggingsMeta.spigot().setUnbreakable(true);
				bootsMeta.spigot().setUnbreakable(true);

				helmet.setItemMeta(helmetMeta);
				chestplate.setItemMeta(chestplateMeta);
				leggings.setItemMeta(leggingsMeta);
				boots.setItemMeta(bootsMeta);

				player.getInventory().setItem(0, sword);
				player.getInventory().setHelmet(helmet);
				player.getInventory().setChestplate(chestplate);
				player.getInventory().setLeggings(leggings);
				player.getInventory().setBoots(boots);
			} else if (Map.getMapManager().getTeam(player).equalsIgnoreCase("Guard")) {
				ItemStack sword = new ItemStack(Material.STONE_SWORD);
				ItemMeta swordMeta = sword.getItemMeta();
				swordMeta.setDisplayName(ChatColor.AQUA + "Sword");
				swordMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				swordMeta.spigot().setUnbreakable(true);
				sword.setItemMeta(swordMeta);

				ItemStack helmet = new ItemStack(Material.CHAINMAIL_HELMET);
				ItemStack chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
				ItemStack leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
				ItemStack boots = new ItemStack(Material.CHAINMAIL_BOOTS);

				ItemMeta helmetMeta = helmet.getItemMeta();
				ItemMeta chestplateMeta = chestplate.getItemMeta();
				ItemMeta leggingsMeta = leggings.getItemMeta();
				ItemMeta bootsMeta = boots.getItemMeta();

				helmetMeta.setDisplayName(ChatColor.AQUA + "Helmet");
				chestplateMeta.setDisplayName(ChatColor.AQUA + "Chestplate");
				leggingsMeta.setDisplayName(ChatColor.AQUA + "Leggings");
				bootsMeta.setDisplayName(ChatColor.AQUA + "Boots");

				helmetMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				chestplateMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				leggingsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
				bootsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

				helmetMeta.spigot().setUnbreakable(true);
				chestplateMeta.spigot().setUnbreakable(true);
				leggingsMeta.spigot().setUnbreakable(true);
				bootsMeta.spigot().setUnbreakable(true);

				helmet.setItemMeta(helmetMeta);
				chestplate.setItemMeta(chestplateMeta);
				leggings.setItemMeta(leggingsMeta);
				boots.setItemMeta(bootsMeta);

				player.getInventory().setItem(0, sword);
				player.getInventory().setHelmet(helmet);
				player.getInventory().setChestplate(chestplate);
				player.getInventory().setLeggings(leggings);
				player.getInventory().setBoots(boots);
			}
		}
	}
	
	@SuppressWarnings("static-access")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		
		if((Map.getMapManager().isInGame(player) || Map.getMapManager().inLobby || Map.getMapManager().ending) && !player.isOp()) {
			event.setCancelled(true);
		}
	}
	
	@SuppressWarnings("static-access")
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		if((Map.getMapManager().isInGame(player) || Map.getMapManager().inLobby || Map.getMapManager().ending) && !player.isOp()) {
			event.setCancelled(true);
		}
	}
}

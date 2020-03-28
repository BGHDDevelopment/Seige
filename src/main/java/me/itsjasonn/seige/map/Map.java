package me.itsjasonn.seige.map;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import me.itsjasonn.seige.main.Plugin;
import me.itsjasonn.seige.utils.DateCalculator;
import me.itsjasonn.seige.utils.MySQLManager;
import me.itsjasonn.seige.utils.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class Map {
	private File file = new File(Plugin.getCore().getDataFolder(), "/data/maps.yml");
	private YamlConfiguration config = YamlConfiguration.loadConfiguration(this.file);
	public static ArrayList<Player> playerList = new ArrayList<Player>();
	public static HashMap<Player, String> playerTeam = new HashMap<Player, String>();
	public static ArrayList<Player> spectators = new ArrayList<Player>();
	public static int currentRound = 0;
	public static int assassinsWins = 0;
	public static int guardsWins = 0;
	public static int kingsHealth = 0;
	public static boolean ending = false;
	public static boolean inLobby = true;
	public static int matchCounter = 0;
	public static int matchID = 0;
	private static Map instance = new Map();
	
	public static Map getMapManager() {
		return instance;
	}

	public void CreateMap() {
		getConfig().createSection("lobby");
		getConfig().createSection("spawns");
		getConfig().createSection("goals");
		saveFile();
	}

	public void RemoveMap() {
		this.file.delete();
	}

	public void SetLobby(World world, double x, double y, double z, float yaw, float pitch) {
		getConfig().set("lobby.world", world.getName());
		getConfig().set("lobby.x", Double.valueOf(x));
		getConfig().set("lobby.y", Double.valueOf(y));
		getConfig().set("lobby.z", Double.valueOf(z));
		getConfig().set("lobby.yaw", Float.valueOf(yaw));
		getConfig().set("lobby.pitch", Float.valueOf(pitch));
		saveFile();
	}

	public void SetSpawn(String team, World world, double x, double y, double z, float yaw, float pitch) {
		getConfig().set("spawns." + team + ".world", world.getName());
		getConfig().set("spawns." + team + ".x", Double.valueOf(x));
		getConfig().set("spawns." + team + ".y", Double.valueOf(y));
		getConfig().set("spawns." + team + ".z", Double.valueOf(z));
		getConfig().set("spawns." + team + ".yaw", Float.valueOf(yaw));
		getConfig().set("spawns." + team + ".pitch", Float.valueOf(pitch));
		saveFile();
	}

	public void TeleportToHub(Player player) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			out.writeUTF("Connect");
			out.writeUTF("lobby");
		} catch (Exception e) {
			e.printStackTrace();
		}
		player.sendPluginMessage(Plugin.getCore(), "BungeeCord", b.toByteArray());
	}

	public void resetScoreboard(Player player) {
		int counter = matchCounter;
		
		Scoreboard sb = Bukkit.getServer().getScoreboardManager().getNewScoreboard();
		Objective obj = sb.registerNewObjective("timer", "dummy");
		
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + " ---=[Siege]=--- ");
		if (getRound() == 0) {
			obj.getScore(ChatColor.GREEN + "          Time left: " + ChatColor.YELLOW + DateCalculator.getMS(counter, false) + "       ").setScore(0);
		} else {
			obj.getScore(ChatColor.GREEN + "          Time left: " + ChatColor.YELLOW + DateCalculator.getMS(counter, false) + "       ").setScore(8);
			obj.getScore(ChatColor.GREEN + "                               ").setScore(7);
			obj.getScore(ChatColor.GREEN + "          King's Health: " + ChatColor.YELLOW + kingsHealth).setScore(6);
			obj.getScore(ChatColor.GREEN + "                              ").setScore(5);
			obj.getScore(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "        Wins:").setScore(4);
			obj.getScore(ChatColor.GREEN + "          Assassins: " + ChatColor.YELLOW + assassinsWins).setScore(3);
			obj.getScore(ChatColor.GREEN + "          Guards: " + ChatColor.YELLOW + guardsWins).setScore(2);
			obj.getScore(ChatColor.GREEN + "                             ").setScore(1);
			obj.getScore(ChatColor.GREEN + "          Round: " + ChatColor.YELLOW + getRound() + "/3").setScore(0);
		}
		for (Player players : getPlayers()) {
			if (getRound() < 4) {
				players.setScoreboard(sb);
			} else {
				player.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
			}
		}
	}

	public void TeleportToLobby(Player player) {
		Location location = new Location(Bukkit.getServer().getWorld(this.config.getString("lobby.world")), this.config.getDouble("lobby.x"), this.config.getDouble("lobby.y"), this.config.getDouble("lobby.z"), (float) this.config.getDouble("lobby.yaw"), (float) this.config.getDouble("lobby.pitch"));
		player.teleport(location);
	}

	public void TeleportToSpawn(Player player, String team) {
		String t = "Guards";
		if (team.equalsIgnoreCase("Assassin")) {
			t = "Assassins";
		}
		Location location = new Location(Bukkit.getServer().getWorld(this.config.getString("spawns." + t + ".world")), this.config.getDouble("spawns." + t + ".x"), this.config.getDouble("spawns." + t + ".y"), this.config.getDouble("spawns." + t + ".z"), (float) this.config.getDouble("spawns." + t + ".yaw"), (float) this.config.getDouble("spawns." + t + ".pitch"));
		player.teleport(location);
	}

	@SuppressWarnings("deprecation")
	public void StartRound(final Player player, int round) {
		if(matchCounter != 0) {
			Bukkit.getServer().getScheduler().cancelTask(matchCounter);
		}
		
		if(round == 1) {
			Random random = new Random();
			int r = random.nextInt(2);
			if (r == 0) {
				if (getAssassins().size() < getPlayers().size() / 2) {
					playerTeam.put(player, "Assassin");
				} else {
					playerTeam.put(player, "Guard");
				}
			} else if (r == 1) {
				if (getGuards().size() < getPlayers().size() / 2) {
					playerTeam.put(player, "Guard");
				} else {
					playerTeam.put(player, "Assassin");
				}
			}
		}
		
		if ((round > 2) && (!ending)) {
			if ((assassinsWins >= 2) || (guardsWins >= 2)) {
				ending = true;
				
				String winners = "Guard";
				String losers = "Assassin";
				if (assassinsWins >= 2) {
					winners = "Assassin";
					losers = "Guard";
				}
				
				Bukkit.getServer().getScheduler().cancelTask(matchID);
				
				player.getInventory().clear();
				if (getTeam(player).equalsIgnoreCase(winners)) {
					if(MySQLManager.using()) {
						try {
							Plugin.getCore().getSQLManager().updateCell("stats", player.getUniqueId().toString(), "gamesWon", Integer.toString(Integer.parseInt((String) Plugin.getCore().getSQLManager().getDataByField("stats", "uuid", player.getUniqueId().toString(), "gamesWon")) + 1));
						} catch (NumberFormatException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					Title.sendFullTitle(player, Integer.valueOf(10), Integer.valueOf(50), Integer.valueOf(10), ChatColor.GOLD + "" + ChatColor.BOLD + "You Won!", "");
					player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "-----------------------------------------");
					player.sendMessage(ChatColor.GOLD + "    You won the Siege against the " + losers + "s!");
					player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "-----------------------------------------");

					Firework fw = (Firework) player.getWorld().spawn(player.getLocation(), Firework.class);
					FireworkMeta fwMeta = fw.getFireworkMeta();
					fwMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withFlicker().withTrail().withColor(Color.ORANGE).build());
					fw.setFireworkMeta(fwMeta);
					Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getCore(), new Runnable() {
						public void run() {
							Firework fw = (Firework) player.getWorld().spawn(player.getLocation(), Firework.class);
							FireworkMeta fwMeta = fw.getFireworkMeta();
							fwMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withFlicker().withTrail().withColor(Color.ORANGE).build());
							fw.setFireworkMeta(fwMeta);
							Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getCore(), new Runnable() {
								public void run() {
									Firework fw = (Firework) player.getWorld().spawn(player.getLocation(), Firework.class);
									FireworkMeta fwMeta = fw.getFireworkMeta();
									fwMeta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withFlicker().withTrail().withColor(Color.ORANGE).build());
									fw.setFireworkMeta(fwMeta);
								}
							}, 40L);
						}
					}, 40L);
				} else {
					Title.sendFullTitle(player, Integer.valueOf(10), Integer.valueOf(50), Integer.valueOf(10), ChatColor.RED + "" + ChatColor.BOLD + "You Lost!", "");
					player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-----------------------------------------");
					player.sendMessage(ChatColor.RED + "    You lost the Siege against the " + winners + "s!");
					player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "-----------------------------------------");
				}
				Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.getCore(), new Runnable() {
					public void run() {
						StopMatch();
					}
				}, 140);
				return;
			}
		}
		player.spigot().setCollidesWithEntities(false);
		for (Player players : Bukkit.getOnlinePlayers()) {
			if (players != player) {
				players.showPlayer(players);
			}
		}
		currentRound = round;
		kingsHealth = Plugin.getCore().getConfig().getInt("siege.king.health");
		if (round == 0) {
			if(player == getPlayers().get(0)) {
				StartCountdown(true);
			}
		} else {
			if (round == 1) {
				assassinsWins = 0;
				guardsWins = 0;
			}
			if(player == getPlayers().get(0)) {
				StartCountdown(false);
			}
		}
		getMapManager().resetScoreboard(player);

		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0.0F);
		player.getInventory().clear();

		TeleportToSpawn(player, (String) playerTeam.get(player));
		Title.sendFullTitle(player, Integer.valueOf(10), Integer.valueOf(50), Integer.valueOf(10), ChatColor.RED + "" + ChatColor.BOLD + (String) playerTeam.get(player) + "s", ChatColor.GOLD + "Round " + round + " / 3");

		player.getInventory().clear();
		
		if (getTeam(player).equalsIgnoreCase("Assassin")) {
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
		} else if (getTeam(player).equalsIgnoreCase("Guard")) {
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

	public void StopMatch() {
		ArrayList<Player> list = new ArrayList<Player>();
		for (Player players : getPlayers()) {
			list.add(players);
		}
		for (Player players : list) {
			TeleportToHub(players);
			removePlayer(players, false);
		}
		
		if(matchID != 0) {
			Bukkit.getServer().getScheduler().cancelTask(matchID);
		}
		
		playerList.clear();
		matchCounter = 0;
		matchID = 0;
		currentRound = 0;
		guardsWins = 0;
		assassinsWins = 0;
		ending = false;
	}

	@SuppressWarnings("deprecation")
	public void addPlayer(Player player, boolean sendMessage) {
		playerList.add(player);

		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0.0F);
		player.getInventory().clear();
		for (Player joinedPlayers : getPlayers()) {
			if (sendMessage) {
				joinedPlayers.sendMessage(ChatColor.DARK_AQUA + player.getName() + ChatColor.AQUA + " has joined the game! " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + getPlayers().size() + ChatColor.DARK_AQUA + "/" + ChatColor.AQUA + Bukkit.getServer().getMaxPlayers() + ChatColor.DARK_AQUA + ")");
			}
		}
		if (getPlayers().size() == Plugin.getCore().getConfig().getInt("siege.game.min-players")) {
			StartRound(player, 0);
		}
		
		if(getPlayers().size() == 1) {
			matchCounter = Plugin.getCore().getConfig().getInt("siege.timers.countdown");
		}
		
		resetScoreboard(player);
	}

	@SuppressWarnings("deprecation")
	public void removePlayer(Player player, boolean sendMessage) {
		player.setGameMode(GameMode.ADVENTURE);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0.0F);
		player.getInventory().clear();
		for (PotionEffect effects : player.getActivePotionEffects()) {
			player.removePotionEffect(effects.getType());
		}
		playerTeam.remove(player);
		playerList.remove(player);
		if (getPlayers().size() > 0) {
			for (Player joinedPlayers : getPlayers()) {
				if (sendMessage) {
					joinedPlayers.sendMessage(ChatColor.DARK_AQUA + player.getName() + ChatColor.AQUA + " has left the game! " + ChatColor.DARK_AQUA + "(" + ChatColor.AQUA + getPlayers().size() + ChatColor.DARK_AQUA + "/" + ChatColor.AQUA + Bukkit.getServer().getMaxPlayers() + ChatColor.DARK_AQUA + ")");
				}
			}
		}
		if (getRound() == 0 && getPlayers().size() == 5) {
			for (Player joinedPlayers : getPlayers()) {
				matchCounter = Plugin.getCore().getConfig().getInt("siege.timers.countdown");

				joinedPlayers.sendMessage(ChatColor.GRAY + "Timer stopped! Not enough players!");

				if (matchID != 0) {
					Bukkit.getServer().getScheduler().cancelTask(matchID);
				}
			}
		}
		
		if(getPlayers().size() == 0) {
			StopMatch();
		}
	}

	public void StartCountdown(final boolean lobby) {
		if(matchID != 0) {
			Bukkit.getServer().getScheduler().cancelTask(matchID);
		}
		
		if (lobby) {
			matchCounter = Plugin.getCore().getConfig().getInt("siege.timers.countdown");
		} else {
			matchCounter = Plugin.getCore().getConfig().getInt("siege.timers.match");
		}
		matchID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.getCore(), new Runnable() {
			@SuppressWarnings("deprecation")
			public void run() {
				if (matchCounter > 1) {
					matchCounter -= 1;
					for (Player players : getPlayers()) {
						getMapManager().resetScoreboard(players);
					}
					if (matchCounter == 60 || matchCounter == 3 || matchCounter == 10 || matchCounter == 5 || matchCounter == 4
							|| matchCounter == 3 || matchCounter == 2 || matchCounter == 1) {
						for (Player players : getPlayers()) {
							String s = "seconds";
							if (matchCounter == 1) {
								s = "second";
							}
							if (lobby) {
								Title.sendFullTitle(players, Integer.valueOf(10), Integer.valueOf(40), Integer.valueOf(10), "", ChatColor.AQUA + "The game will start in " + matchCounter + " " + s + "...");
							} else {
								Title.sendFullTitle(players, Integer.valueOf(10), Integer.valueOf(40), Integer.valueOf(10), "", ChatColor.AQUA + "The round will end in " + matchCounter + " " + s + "...");
							}
						}
					}
				} else {
					Bukkit.getServer().getScheduler().cancelTask(matchID);
					if (!ending) {
						int newRound = getRound() + 1;
						addPoint("Guards");
						for (Player players : getPlayers()) {
							StartRound(players, newRound);
						}
						for (Player players : getPlayers()) {
							players.setScoreboard(Bukkit.getServer().getScoreboardManager().getNewScoreboard());
						}
						if (getPlayers().size() == 0) {
							StopMatch();
						}
					}
				}
			}
		}, 20L, 20L);
	}
	
	public void setSpectator(Player player) {
		player.setGameMode(GameMode.SPECTATOR);
		player.setFoodLevel(20);
		player.setExp(0.0F);
		player.setLevel(0);

		spectators.add(player);
		
		String team = "";
		Random random = new Random();
		int r = random.nextInt(2);
		if(r == 0) {
			team = "Assassins";
		} else if(r == 1) {
			team = "Guards";
		}
		
		Location location = new Location(Bukkit.getServer().getWorld(config.getString("spawns." + team + ".world")), this.config.getDouble("spawns." + team + ".x"), this.config.getDouble("spawns." + team + ".y"), this.config.getDouble("spawns." + team + ".z"), (float) this.config.getDouble("spawns." + team + ".yaw"), (float) this.config.getDouble("spawns." + team + ".pitch"));
		player.teleport(location);
	}
	
	public void addPoint(String team) {
		if (team.equalsIgnoreCase("Guards")) {
			guardsWins += 1;
		} else if (team.equalsIgnoreCase("Assassins")) {
			assassinsWins += 1;
		}
	}

	public ArrayList<Player> getPlayers() {
		return playerList;
	}

	public int getRound() {
		return currentRound;
	}

	public ArrayList<Player> getAssassins() {
		ArrayList<Player> list = new ArrayList<Player>();
		for (Player players : getPlayers()) {

			if ((playerTeam.containsKey(players)) && (getTeam(players).equalsIgnoreCase("Assassin"))) {
				list.add(players);
			}
		}
		return list;
	}

	public ArrayList<Player> getGuards() {
		ArrayList<Player> list = new ArrayList<Player>();
		for (Player players : getPlayers()) {
			if ((playerTeam.containsKey(players)) && (getTeam(players).equalsIgnoreCase("Guard"))) {
				list.add(players);
			}
		}
		return list;
	}

	public boolean DoesExist() {
		return this.file.exists();
	}

	public boolean isInGame(Player player) {
		return playerList.contains(player);
	}

	public void saveFile() {
		try {
			this.config.save(this.file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getTeam(Player player) {
		return (String) playerTeam.get(player);
	}

	public YamlConfiguration getConfig() {
		return this.config;
	}
}

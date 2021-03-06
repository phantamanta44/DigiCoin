package co.uk.silvania.cities.digicoin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

public class MoneyCommand implements CommandExecutor {

	private final DigiCoin digiCoin;
	
	public MoneyCommand(DigiCoin digiCoin) {
		this.digiCoin = digiCoin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			help(sender);
		} else {
			switch (args[0].toLowerCase()) {
				case "balance":
					OfflinePlayer target;
					if (args.length > 1) {
						target = Bukkit.getServer().getOfflinePlayer(args[1]);
						if (!target.hasPlayedBefore() && !target.isOnline()) {
							sender.sendMessage(ChatColor.RED + "Unknown player!");
							return true;
						}
					} else {
						if (!(sender instanceof Player)) {
							sender.sendMessage(ChatColor.RED + "You must specify a player to look up!");
							return true;
						}
						target = (Player)sender;
					}
					sender.sendMessage(ChatColor.GOLD + "Player " +
							ChatColor.GRAY + target.getName() +
							ChatColor.GOLD + " has a balance of "
							+ ChatColor.GRAY + format(digiCoin.getBalance(target))
							+ ChatColor.GOLD + ".");
					break;
				case "top":
					if (sender.hasPermission("digicoin.top")) {
						final ConfigurationSection bals = digiCoin.balances();
						List<String> top = new ArrayList<>(bals.getKeys(false));
						Collections.sort(top, new Comparator<String>() {
							@Override
							public int compare(String a, String b) {
								return (int)Math.signum(bals.getDouble(b) - bals.getDouble(a));
							}
						});
						int limit = Math.min(10, top.size());
						String[] msg = new String[top.size() + 1];
						msg[0] = ChatColor.GOLD + "== Leaderboard ==";
						for (int i = 0; i < limit; i++) {
							msg[i + 1] = formatBaltop(i,
									Bukkit.getServer().getOfflinePlayer(UUID.fromString(top.get(i))).getName(),
									bals.getDouble(top.get(i)));
						}
						sender.sendMessage(msg);
					} else {
						noPerms(sender);
					}
					break;
				case "pay":
					if (!(sender instanceof Player)) {
						sender.sendMessage(ChatColor.RED + "Only player may use this command!");
					} else if (sender.hasPermission("digicoin.pay")) {
						if (args.length < 3) {
							sender.sendMessage(ChatColor.RED + "Usage: /money pay <player> <amount>");
						} else {
							target = Bukkit.getServer().getOfflinePlayer(args[1]);
							if (!target.hasPlayedBefore() && !target.isOnline()) {
								sender.sendMessage(ChatColor.RED + "Unknown player!");
								return true;
							} else if (target == sender) {
								sender.sendMessage(ChatColor.RED + "You cannot pay yourself!");
								return true;
							}
							try {
								double amount = Double.parseDouble(args[2]);
								if (amount <= 0)
									throw new NumberFormatException();
								if (digiCoin.transfer((Player)sender, target, amount)) {
									sender.sendMessage(ChatColor.GOLD + "Paid " +
											ChatColor.GRAY + format(amount) +
											ChatColor.GOLD + " to " +
											ChatColor.GRAY + target.getName() +
											ChatColor.GOLD + ".");
									showBal((Player)sender);
									if (target.isOnline()) {
										target.getPlayer().sendMessage(ChatColor.GOLD + "Received " +
												ChatColor.GRAY + format(amount) +
												ChatColor.GOLD + " from " +
												ChatColor.GRAY + sender.getName() +
												ChatColor.GOLD + ".");
										showBal(target.getPlayer());
									}
								} else {
									sender.sendMessage(ChatColor.RED + "You cannot afford this transaction!");
								}
							} catch (NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "Invalid payment amount!");
							}
						}
					} else {
						noPerms(sender);
					}
					break;
				case "set":
					if (sender.hasPermission("digicoin.admin")) {
						if (args.length < 3) {
							sender.sendMessage(ChatColor.RED + "Usage: /money set <player> <balance>");
						} else {
							target = Bukkit.getServer().getOfflinePlayer(args[1]);
							if (!target.hasPlayedBefore() && !target.isOnline()) {
								sender.sendMessage(ChatColor.RED + "Unknown player!");
								return true;
							}
							try {
								double amount = Double.parseDouble(args[2]);
								if (!digiCoin.setBalance(target, amount))
									throw new NumberFormatException();
								sender.sendMessage(ChatColor.GOLD + "Assigned balance of " +
										ChatColor.GRAY + format(amount) +
										ChatColor.GOLD + " to " +
										ChatColor.GRAY + target.getName() +
										ChatColor.GOLD + ".");
							} catch (NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "Invalid balance!");
							}
						}
					} else {
						noPerms(sender);
					}
					break;
				case "add":
					if (sender.hasPermission("digicoin.admin")) {
						if (args.length < 3) {
							sender.sendMessage(ChatColor.RED + "Usage: /money add <player> <amount>");
						} else {
							target = Bukkit.getServer().getOfflinePlayer(args[1]);
							if (!target.hasPlayedBefore() && !target.isOnline()) {
								sender.sendMessage(ChatColor.RED + "Unknown player!");
								return true;
							}
							try {
								double amount = Double.parseDouble(args[2]);
								if (!digiCoin.addBalance(target, amount))
									throw new NumberFormatException();
								sender.sendMessage(ChatColor.GOLD + "Added " +
										ChatColor.GRAY + format(amount) +
										ChatColor.GOLD + " to balance of " +
										ChatColor.GRAY + target.getName() +
										ChatColor.GOLD + ".");
							} catch (NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "Invalid amount!");
							}
						}
					} else {
						noPerms(sender);
					}
					break;
				case "subtract":
					if (sender.hasPermission("digicoin.admin")) {
						if (args.length < 3) {
							sender.sendMessage(ChatColor.RED + "Usage: /money subtract <player> <amount>");
						} else {
							target = Bukkit.getServer().getOfflinePlayer(args[1]);
							if (!target.hasPlayedBefore() && !target.isOnline()) {
								sender.sendMessage(ChatColor.RED + "Unknown player!");
								return true;
							}
							try {
								double amount = Double.parseDouble(args[2]);
								if (!digiCoin.removeBalance(target, amount))
									throw new NumberFormatException();
								sender.sendMessage(ChatColor.GOLD + "Subtracted " +
										ChatColor.GRAY + format(amount) +
										ChatColor.GOLD + " from balance of " +
										ChatColor.GRAY + target.getName() +
										ChatColor.GOLD + ".");
							} catch (NumberFormatException e) {
								sender.sendMessage(ChatColor.RED + "Invalid amount!");
							}
						}
					} else {
						noPerms(sender);
					}
					break;
				case "reload":
					if (sender.hasPermission("digicoin.admin")) {
						digiCoin.reloadPlugin();
						sender.sendMessage(ChatColor.BLUE + "DigiCoin reloaded.");
					} else {
						noPerms(sender);
					}
					break;
				default:
					help(sender);
					break;
			}
		}
		return true;
	}

	private void showBal(Player player) {
		player.sendMessage(ChatColor.GOLD + "New balance: " + ChatColor.GRAY + format(digiCoin.getBalance(player)));
	}

	private void noPerms(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "No permissions!");
	}

	private String formatBaltop(int place, String name, double balance) {
		return ChatColor.GOLD + Integer.toString(place + 1) + ": " +
				ChatColor.WHITE + name + ChatColor.DARK_GRAY + " | " +
				ChatColor.GRAY + format(balance);
	}

	private String format(double amount) {
		return amount + " coin" + (amount != 1 ? "s" : ""); // TODO Find a way to hack vault so this is mutable
	}

	private void help(CommandSender sender) {
		sender.sendMessage(new String[] {
				ChatColor.RED + "Invalid command usage!",
				ChatColor.RED + "/money balance [player]",
				ChatColor.RED + "/money pay <player> <amount>",
				ChatColor.RED + "/money top"
		});
	}

}
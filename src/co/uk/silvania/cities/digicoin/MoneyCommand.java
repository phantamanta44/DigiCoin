package co.uk.silvania.cities.digicoin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MoneyCommand implements CommandExecutor {

	private final DigiCoin digiCoin;
	
	public MoneyCommand(DigiCoin digiCoin) {
		this.digiCoin = digiCoin;
	}
	
	//TODO
	//Correctly display the balance of other players //Done?
	//Send the other player a message they got paid
	//Fix the paid message spacing //Done?
	//Fix formatting so it's a bit prettier //Done?
	//Add help command and remove the fallback //Done?
	//Edit the fallback failed command to differ depending on the command. //Done?
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String gold = ChatColor.GOLD + "";
		String dgreen = ChatColor.DARK_GREEN + "";
		String red = ChatColor.RED + "";
		if (cmd.getName().equalsIgnoreCase("DigiCoin")) {
			if (args.length < 1) {
				if (!(sender instanceof Player)) {
					sender.sendMessage(red + "Only in-game players can view their balance");
					return false;
				} else {
					Player player = (Player) sender;
					get(sender, ((Player)sender).getName());
					return true;
				}
			}

			if (args[0].equalsIgnoreCase("Pay")) {
				if (sender.hasPermission("digicoin.pay")) {
					if (args.length == 3) {
						if (sender instanceof Player) {
							double amount = parseDouble(args[2]);
							if (digiCoin.payPlayer(((Player)sender).getName(), args[1], amount).equals(true)) {
								Player player = (Player) sender;
								Player receiver = player.getServer().getPlayer(args[1]);
								sender.sendMessage(dgreen + "You have paid " + gold + args[2] + dgreen + " to " + gold + args[1]);
								//receiver.sendMessage(dgreen + "You have been paid " + gold + args[2] + dgreen + " by " + gold + sender.getName());	
							} else {
								double balance = digiCoin.getBalance(sender.getName());
								double x = amount - balance;
								sender.sendMessage(red + "You do not have enough funds! You need " + gold + x + red + " more.");
							}
						} else {
							sender.sendMessage(red + "Only in-game players can pay one another!");
							
						}
					} else {
						sender.sendMessage(gold + "/DigiCoin Pay <Player> <Amount>" + dgreen + " - Pay the player the amount.");
					}
				} else {
					noPermsMessage(sender);
				}
				
			} else if (args[0].equalsIgnoreCase("Forge")) {
				if (sender.hasPermission("digicoin.pay")) {
					if (args.length == 3) {
						if (sender instanceof Player) {
							double amount = parseDouble(args[2]);
							if (digiCoin.payPlayer(((Player)sender).getName(), args[1], amount).equals(true)) {
								Player player = (Player) sender;
								Player receiver = player.getServer().getPlayer(args[1]);
								
								sender.sendMessage(dgreen + "You have deposited " + gold + args[2] + dgreen + " to your Forge balance!");
							} else {
								double balance = digiCoin.getBalance(sender.getName());
								double x = amount - balance;
								sender.sendMessage(red + "You do not have enough funds! You need " + gold + x + red + " more.");
							}
						} else {
							sender.sendMessage(red + "Only in-game players can pay one another!");
							
						}
					} else {
						sender.sendMessage(gold + "/DigiCoin Pay <Player> <Amount>" + dgreen + " - Pay the player the amount.");
					}
				} else {
					noPermsMessage(sender);
				}
				
			} else if (args[0].equalsIgnoreCase("Get") || (args[0].equalsIgnoreCase("Balance"))) {
				if (sender.hasPermission("digicoin.balance")) {
					if (args.length == 2) {
						getOther(sender, args[1]);
					} else if (args.length == 1) {
						if (sender instanceof Player) {
							get(sender, sender.getName());
						}
					} else {
						sender.sendMessage(gold + "/DigiCoin Get|Balance [Player]" + dgreen + " - See the player's balance.");
						sender.sendMessage(dgreen + "Player is optional and will default to yourself.");
					}
				} else {
					noPermsMessage(sender);
				}
				
			} else if (args[0].equalsIgnoreCase("Set")) {
				if (sender.hasPermission("digicoin.admin")) {
					if (args.length == 3) {
						double amount = parseDouble(args[2]);
						digiCoin.setBalance(args[1], amount);
						sender.sendMessage(dgreen + "You have set " + gold + args[1] + "'s" + dgreen + " balance to " + gold + args[2]);
					} else if (args.length == 2) {
						if (sender instanceof Player) {
							double amount = parseDouble(args[1]);
							digiCoin.setBalance(sender.getName(), amount);
							sender.sendMessage(dgreen + "You have set your balance to " + gold + args[1]);
						} else {
							sender.sendMessage("Please remember to specify the player name when using the console!");
						}
					} else {
						sender.sendMessage(gold + "/DigiCoin Set [Player] <Amount>" + dgreen + " - Set the player balance.");
						sender.sendMessage(dgreen + "Player is optional and will default to yourself.");
					}
				}
			} else if (args[0].equalsIgnoreCase("Reset") || args[0].equalsIgnoreCase("Clear")) {
				if (sender.hasPermission("digicoin.admin")) {
					if (args.length == 2) {
						digiCoin.setBalance(args[1], 0);
						sender.sendMessage(gold + args[1] + "'s " + dgreen + "balance has been reset!");
					} else if (args.length == 1) {
						digiCoin.setBalance(sender.getName(), 0);
						sender.sendMessage(gold + "Your balance has been reset!");
					} else {
						sender.sendMessage(gold + "/DigiCoin Reset [Player]" + dgreen + " - Reset the player to 0.");
						sender.sendMessage(dgreen + "Player is optional and will default to yourself.");
					}
				}

			//If the command is "/DigiCoin add" or "/DigiCoin give" - Either work.	
			} else if (args[0].equalsIgnoreCase("Add") || args[0].equalsIgnoreCase("Give")) {
				if (sender.hasPermission("digicoin.admin")) {
					if (args.length == 3) {
						double amount = parseDouble(args[2]);
						digiCoin.addBalance(args[1], amount);
						sender.sendMessage(dgreen + "You have added " + gold + args[2] + dgreen + " to " + gold + args[1] + "'s" + dgreen +" account!");
					} else if (args.length == 2) {
						if (sender instanceof Player) {
							double amount = parseDouble(args[1]);
							digiCoin.addBalance(sender.getName(), amount);
							sender.sendMessage(dgreen + "You have added " + gold + args[1] + dgreen + " to your DigiCoin account!");
						} else {
							sender.sendMessage("Please remember to specify the player name when using the console!");
						}

					} else {
						sender.sendMessage(gold + "/DigiCoin Add|Give [Player] <Amount>" + dgreen + " - Give cash to the player.");
						sender.sendMessage(dgreen + "Player is optional and will default to yourself.");
					} 
				} else {
					noPermsMessage(sender);
				}

			} else if (args[0].equalsIgnoreCase("Take") || (args[0].equalsIgnoreCase("Remove") || (args[0].equalsIgnoreCase("Take|Remove")))) {
				if (sender.hasPermission("digicoin.admin")) {
					if (args.length == 3) {
						double amount = parseDouble(args[2]);
						digiCoin.removeBalance(args[1], amount);
						sender.sendMessage(dgreen + "You have taken " + gold + args[2] + dgreen + " from " + gold + args[1]);
					} else if (args.length == 2) {
						if (sender instanceof Player) {
							double amount = parseDouble(args[1]);
							digiCoin.removeBalance(args[1], amount);
							sender.sendMessage(dgreen + "You have thrown " + gold + args[1] + dgreen + " DigiCoins away!");
						}
					} else {
						sender.sendMessage(gold + "/DigiCoin Take|Remove [Player] <Amount>" + dgreen + " - Take cash from the player.");
						sender.sendMessage(dgreen + "Player is optional and will default to yourself.");
					}
				} else {
					noPermsMessage(sender);
				}

			} else if (args[0].equalsIgnoreCase("Reload")) {
				if (sender.hasPermission("digicoin.admin")) {
					digiCoin.reloadPlugin();
					sender.sendMessage(gold + "DigiCoin Reloaded!");
				} else {
					noPermsMessage(sender);
				}

			} else if (args[0].equalsIgnoreCase("Help")) {
				help(sender);
			} else
				sender.sendMessage(red + "Type " + gold + "/DigiCoin Help" + red + " For a list of commands.");
			return true;
		}
		return false;
	}
	
	private double parseDouble(String s) {
		try { 
			return Double.parseDouble(s);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}
	
	private void noPermsMessage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
	}
	
	private void get(CommandSender sender, String player) {
		double balance = digiCoin.getBalance(player);
		String gold = ChatColor.GOLD + "";
		String dgreen = ChatColor.DARK_GREEN + "";
		if (balance != 1) {
			sender.sendMessage(dgreen + "Your current balance is: " + gold + balance + " " + dgreen + "DigiCoins.");
		} else {
			sender.sendMessage(dgreen + "Your current balance is: " + gold + balance + " " + dgreen + "DigiCoin.");
		}
	}
	
	private void getOther(CommandSender sender, String player) {
		double balance = digiCoin.getBalance(player);
		String gold = ChatColor.GOLD + "";
		String dgreen = ChatColor.DARK_GREEN + "";
		if (balance != 1) {
			sender.sendMessage(gold + player + "'s" + dgreen + " current balance is: " + gold + balance + " " + dgreen + "DigiCoins.");
		} else {
			sender.sendMessage(gold + player + "'s" + dgreen + " current balance is: " + gold + balance + " " + dgreen + "DigiCoin.");
		}
	}
	
	private void help(CommandSender sender) {
		String gold = ChatColor.GOLD + "";
		String dgreen = ChatColor.DARK_GREEN + "";
		if (sender instanceof Player) {
			sender.sendMessage(gold + "/DigiCoin" + dgreen + " - Gets your current balance");
			sender.sendMessage(gold + "/DigiCoin Help" + dgreen + " - Shows you this message.");
		}
		if (sender.hasPermission("digicoin.balance")) {
			sender.sendMessage(gold + "/DigiCoin Get [player]" + dgreen + " - Display the specified players balance.");
		}
		if (sender.hasPermission("digicoin.pay")) {
			sender.sendMessage(gold + "/DigiCoin Pay <Player> <Amount>" + dgreen + " - Pay a player some money.");
		}
		if (sender.hasPermission("digicoin.admin")) {
			sender.sendMessage(ChatColor.YELLOW + "[[Admin Only Commands]]");
			if (sender instanceof Player) {
				sender.sendMessage(ChatColor.BLUE + "'Player' is optional, and will default to you if not specified.");
				sender.sendMessage(gold + "/DigiCoin Get [player]" + dgreen + " - Display the specified players balance.");
				sender.sendMessage(gold + "/DigiCoin Set [player] <amount>" + dgreen + " - Sets the players balance to the specified amount.");
				sender.sendMessage(gold + "/DigiCoin Give [player] <amount>" + dgreen + " - Gives money to the specified player");
				sender.sendMessage(gold + "/DigiCoin Take [player] <amount>" + dgreen + " - Takes money from the specified player");
			} else {
				sender.sendMessage(gold + "/DigiCoin get <player>" + dgreen + " - Display the specified players balance.");
				sender.sendMessage(gold + "/DigiCoin set <player> <amount>" + dgreen + " - Sets the players balance to the specified amount.");
				sender.sendMessage(gold + "/DigiCoin give <player> <amount>" + dgreen + " - Gives money to the specified player");
				sender.sendMessage(gold + "/DigiCoin take <player> <amount>" + dgreen + " - Takes money from the specified player");
			}
		}
		sender.sendMessage(ChatColor.BLUE + "DigiCoin plugin by " + ChatColor.RED + "Flenix.");
	}
}
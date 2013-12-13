package co.uk.silvania.cities.digicoin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class DigiCoin extends JavaPlugin {
	
	private static final Logger log = Logger.getLogger("Minecraft");

	private double defaultBalance = 0.0;
	private final ReentrantLock lockPlugin = new ReentrantLock();
	
	@Override
	public void onEnable() {
		this.getCommand("digicoin").setExecutor(new MoneyCommand(this));
		log.info(String.format("DigiCoin has been successfully enabled!", getDescription().getName(), getDescription().getVersion()));
		getServer().getMessenger().registerIncomingPluginChannel(this, "FCDigiCoinPkt", new PacketListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, "FCDigiCoinPkt");
	}
	
	public void reloadPlugin() {
		lockPlugin.lock();
		try {
			reloadConfig();
			defaultBalance = getConfig().getDouble("config.default_balance", 0.0);
		} finally {
			lockPlugin.unlock();
		}
	}
	
	public double getBalance(String Player) {
		lockPlugin.lock();
		try {
			String playerName = Player.toLowerCase();
			return getConfig().getDouble("accounts." + playerName, defaultBalance);	
		} finally {
			lockPlugin.unlock();
		}
	}
	
	public Boolean setBalance(String Player, double NewBalance) {
		lockPlugin.lock();
		try {
			String playerName = Player.toLowerCase();
			if (NewBalance < 0) {
				return false;
			} else {
				getConfig().set("accounts." + playerName, NewBalance);
				saveConfig();
				return true;
			}
		} finally {
			lockPlugin.unlock();
		}
	}
	
	public Boolean addBalance(String Player, double Amount) {
		lockPlugin.lock();
		try {
			String playerName = Player.toLowerCase();
			if (Amount < 0) {
				return false;
			} else {
				double newBalance = getConfig().getDouble("accounts." + playerName, defaultBalance) + Amount;
				getConfig().set("accounts." + playerName, newBalance);
				saveConfig();
				return true;
			}
		} finally {
			lockPlugin.unlock();
		}
	}
	
	public Boolean removeBalance(String Player, double Amount) {
		lockPlugin.lock();
		try {
			String playerName = Player.toLowerCase();
			if (Amount < 0) {
				return false;
			} else {
				double newBalance = getConfig().getDouble("accounts." + playerName, defaultBalance) - Amount;
				if (newBalance < 0) {
					return false;
				} else {
					getConfig().set("accounts." + playerName, newBalance);
					saveConfig();
					return true;
				}
			}
		} finally {
			lockPlugin.unlock();
		}
	}
	
	
	public Boolean payPlayer(String Player, String PaidPlayer, double Amount) {
		lockPlugin.lock();
		try {
			if (Amount > 0) {
				String playerName = Player.toLowerCase();
				String paidPlayerName = PaidPlayer.toLowerCase();
				if (playerName.equals(paidPlayerName)) {
					return false;
				} else {
					double playerBalance = getConfig().getDouble("accounts." + playerName, defaultBalance) - Amount;
					if (playerBalance < 0) {
						return false;
					} else {
						double paidPlayerBalance = getConfig().getDouble("accounts." + paidPlayerName, defaultBalance) + Amount;
						getConfig().set("accounts." + playerName, playerBalance);
						getConfig().set("accounts." + paidPlayerName, paidPlayerBalance);
						saveConfig();
						return true;
					}
				}
			} else {
				return false;
			}
		} finally {
			lockPlugin.unlock();
		}
	}
}
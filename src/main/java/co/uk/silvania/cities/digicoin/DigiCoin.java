package co.uk.silvania.cities.digicoin;

import com.google.common.base.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class DigiCoin extends JavaPlugin {
	
	private static final Logger log = Logger.getLogger("DigiCoin");

	private double defaultBalance = 0.0;
	private final ReentrantLock lockPlugin = new ReentrantLock();
	
	@Override
	public void onEnable() {
		this.getCommand("money").setExecutor(new MoneyCommand(this));
		log.info(String.format("%s %s has been successfully enabled!", getDescription().getName(), getDescription().getVersion()));
	}
	
	public void reloadPlugin() {
		withLock(new Runnable() {
			@Override
			public void run() {
				reloadConfig();
				defaultBalance = getConfig().getDouble("config.default_balance", 0.0);
			}
		});
	}
	
	public double getBalance(String Player) {
		return getBalance(Bukkit.getServer().getOfflinePlayer(Player));
	}

	public double getBalance(final OfflinePlayer player) {
		return withLock(new Supplier<Double>() {
			@Override
			public Double get() {
				ConfigurationSection bals = balances();
				String id = player.getUniqueId().toString();
				if (!bals.contains(id)) {
					bals.set(id, defaultBalance);
					return defaultBalance;
				} else {
					return bals.getDouble(id);
				}
			}
		});
	}
	
	public Boolean setBalance(String Player, double NewBalance) {
		return setBalance(Bukkit.getServer().getOfflinePlayer(Player), NewBalance);
	}

	public boolean setBalance(final OfflinePlayer player, final double balance) {
		if (balance < 0)
			return false;
		withLock(new Runnable() {
			@Override
			public void run() {
				DigiCoin.this.balances().set(player.getUniqueId().toString(), balance);
				saveConfig();
			}
		});
		return true;
	}
	
	public Boolean addBalance(String Player, double Amount) {
		return addBalance(Bukkit.getServer().getOfflinePlayer(Player), Amount);
	}

	public boolean addBalance(OfflinePlayer player, double amount) {
		return setBalance(player, getBalance(player) + amount);
	}
	
	public Boolean removeBalance(String Player, double Amount) {
		return removeBalance(Bukkit.getServer().getOfflinePlayer(Player), Amount);
	}

	public boolean removeBalance(OfflinePlayer player, double amount) {
		return setBalance(player, getBalance(player) - amount);
	}
	
	public Boolean payPlayer(String Player, String PaidPlayer, double Amount) {
		return transfer(Bukkit.getServer().getOfflinePlayer(Player),
				Bukkit.getServer().getOfflinePlayer(PaidPlayer),
				Amount);
	}

	public boolean transfer(OfflinePlayer source, OfflinePlayer dest, double amount) {
		if (amount <= 0 || getBalance(source) < amount)
			return false;
		removeBalance(source, amount);
		addBalance(dest, amount);
		return true;
	}

	ConfigurationSection balances() {
		return getConfig().getConfigurationSection("balance");
	}

	private void withLock(Runnable runnable) {
		lockPlugin.lock();
		try {
			runnable.run();
		} finally {
			lockPlugin.unlock();
		}
	}

	private <T> T withLock(Supplier<T> supplier) {
		lockPlugin.lock();
		try {
			return supplier.get();
		} finally {
			lockPlugin.unlock();
		}
	}

}
package co.uk.silvania.cities.digicoin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.plugin.messaging.PluginMessageRecipient;

public class PacketListener implements PluginMessageListener {
	
	private final DigiCoin digiCoin;
	
	public PacketListener(DigiCoin digiCoin) {
		this.digiCoin = digiCoin;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] msg) {
		double value;
		String playerName = "";
		String checkSide = "";
		if (channel.equals("FCDigiCoinPkt")) {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(msg));
			System.out.println("[DigiCoin] DigiCoin-side Packet get");
			try {
				checkSide = dis.readUTF();
				value = dis.readDouble();
				playerName = dis.readUTF();
				if (checkSide.equals("digicoinDeposit")) {
					System.out.println("[DigiCoin] DigiCoin-Side Deposit Packet Received: " + value);
					digiCoin.addBalance(playerName, value);	
				} else if (checkSide.equals("digicoinWithdraw")) {
					System.out.println("[DigiCoin] DigiCoin-Side Withdraw Packet Received: " + value);
					double dcBalance = digiCoin.getBalance(playerName);
					if (dcBalance >= value) {
						digiCoin.removeBalance(playerName, value);
						respondPacket(playerName, value);
						
					}
				} else if (checkSide.equals("digicoinInstallCheck")) {
					System.out.println("[DigiCoin] DigiCoin install check received! It's installed, obviously...");
					System.out.println("[DigiCoin] Let's send a reply to confirm that!");
					confirmCheckPacket();
				}
			}
			catch (IOException e) {
				System.out.println("[DigiCoin] Packet failed!");
			}
			finally {}
		}		
	}
	
	public void respondPacket(String playerName, double value) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		Player p = Bukkit.getOnlinePlayers()[0];
		System.out.println("[DigiCoin] Prepare to confirm the withdrawl amount; " + value + " for " + playerName);
		try {
			System.out.println("[DigiCoin] Off it goes!");
			out.writeUTF("digicoinWithdrawConfirm");
			out.writeDouble(value);
			out.writeUTF(playerName);
		} catch (IOException e) {
			System.out.println("[DigiCoin] Packet Failed!");
		}
		System.out.println("[DigiCoin] It should now have been sent");
		p.sendPluginMessage(this.digiCoin, "FCDigiCoinPkt", b.toByteArray());
	}
	
	public void confirmCheckPacket() {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		Player p = Bukkit.getOnlinePlayers()[0];
		System.out.println("[DigiCoin] Here goes the packet!");
		try {
			out.writeUTF("digicoinInstallConfirm");
			out.writeBoolean(true);
		} catch (IOException e) {
			System.out.println("[DigiCoin] Packet Failed!");
		}
		p.sendPluginMessage(this.digiCoin, "FCDigiCoinPkt", b.toByteArray());
	}
	
	private double parseDouble(String s) {
		try { 
			return Double.parseDouble(s);
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

}

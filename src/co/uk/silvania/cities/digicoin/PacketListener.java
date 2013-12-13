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
			System.out.println("DigiCoin-side Packet get");
			try {
				checkSide = dis.readUTF();
				value = dis.readDouble();
				playerName = dis.readUTF();
				if (checkSide.equals("digicoinDeposit")) {
					System.out.println("DigiCoin-Side Deposit Packet Received: " + value);
					digiCoin.addBalance(playerName, value);	
				} else if (checkSide.equals("digicoinWithdraw")) {
					System.out.println("DigiCoin-Side Withdraw Packet Received: " + value);
					double dcBalance = digiCoin.getBalance(playerName);
					if (dcBalance >= value) {
						digiCoin.removeBalance(playerName, value);
						respondPacket(playerName, value);
						
					}
				}
			}
			catch (IOException e) {
				System.out.println("Packet failed!");
			}
			finally {}
		}		
	}
	
	public void respondPacket(String playerName, double value) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		Player p = Bukkit.getOnlinePlayers()[0];
		
		try {
			out.writeUTF("digicoinWithdrawConfirm");
			out.writeDouble(value);
			out.writeUTF(playerName);
		} catch (IOException e) {
			System.out.println("Packet Failed!");
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

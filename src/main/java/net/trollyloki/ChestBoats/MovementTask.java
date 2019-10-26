package net.trollyloki.ChestBoats;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class MovementTask extends BukkitRunnable {
	
	public static List<Boat> chestBoats = new ArrayList<Boat>();
	
	public void run() {
		
		/*List<Boat> boats = new ArrayList<Boat>();
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			
			for (Entity entity : player.getNearbyEntities(512, 512, 512)) {
				
				if (entity instanceof Boat) {
					boats.add((Boat) entity);
				}
				
			}
			
		}*/
		
		for (Boat boat : chestBoats) {
			
			String id = boat.getPersistentDataContainer().get(Manager.ID, PersistentDataType.STRING);
			if (id != null) {
				Entity stand = Bukkit.getEntity(UUID.fromString(id));
				stand.teleport(Manager.getChestLocation(boat));
			}
			
		}
		
	}
	
	public static void start(Plugin plugin) {
		new MovementTask().runTaskTimer(plugin, 20, 0);
	}

}
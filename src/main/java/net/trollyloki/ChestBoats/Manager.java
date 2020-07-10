package net.trollyloki.ChestBoats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class Manager implements Listener {
	
	private Map<Inventory, UUID> invIds = new HashMap<Inventory, UUID>();
	private Map<UUID, Inventory> invIdsRev = new HashMap<UUID, Inventory>();
	
	public static final NamespacedKey ID = new NamespacedKey(Main.getPlugin(), "chestboats_id");
	private final NamespacedKey INV = new NamespacedKey(Main.getPlugin(), "chestboats_inventory");
	
	@EventHandler
	public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
		Entity entity = event.getRightClicked();
		
		if (entity.getType() == EntityType.ARMOR_STAND) {
			boolean cancel = armorStandClicked(event.getPlayer(), (ArmorStand) entity);
			if (cancel) event.setCancelled(true);
			return;
		}
		
	}
	
	@EventHandler
	public void onBoatInteract(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		
		if (entity.getType() == EntityType.BOAT) {
			boolean cancel = boatClicked(event.getPlayer(), (Boat) entity);
			if (cancel) event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onBoatEntry(VehicleEnterEvent event) {
		Vehicle vehicle = event.getVehicle();
		
		if (vehicle.getType() == EntityType.BOAT) {
			if (vehicle.getPersistentDataContainer().has(ID, PersistentDataType.STRING)) {
				if (!MovementTask.chestBoats.contains(vehicle)) MovementTask.chestBoats.add((Boat) vehicle);
				if (!vehicle.isEmpty()) {
					
					event.setCancelled(true);
					
				}
			}
		}
	}
	
	public boolean boatClicked(Player player, Boat boat) {
		
		boolean hasChest = boat.getPersistentDataContainer().has(ID, PersistentDataType.STRING);
		
		if (!hasChest && player.getInventory().getItemInMainHand().getType() == Material.CHEST) {
			
			addChest(boat);
			if (player.getGameMode() != GameMode.CREATIVE)
				player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
			
			return true;
			
		}
		
		return false;
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void boatRemoved(VehicleDestroyEvent event) {
		if (event.getVehicle().getType() == EntityType.BOAT) {
			
			if (event.getVehicle().getPersistentDataContainer().has(ID, PersistentDataType.STRING)) {
				
				if (event.getAttacker() == null || !(event.getAttacker() instanceof Player)
						|| ((Player) event.getAttacker()).getGameMode() != GameMode.CREATIVE) {
					
					event.getVehicle().getLocation().getWorld().dropItemNaturally(getChestLocation((Boat) event.getVehicle()), new ItemStack(Material.CHEST));
				}
				
				removeChest((Boat) event.getVehicle());
				
			}
			
		}
	}
	
	public void removeChest(Boat boat, UUID uuid) {
		
		Entity stand = Bukkit.getEntity(uuid);
		Location location = stand.getLocation().add(0, 1.3, 0);
		ItemStack[] contents = Base64Serialization.fromBase64(stand.getPersistentDataContainer().get(INV, PersistentDataType.STRING)).getContents();
		
		for (int i = 0; i < contents.length; i++) {
			ItemStack stack = contents[i];
			if (stack != null) location.getWorld().dropItemNaturally(location, stack);
		}
		
		MovementTask.chestBoats.remove(boat);
		boat.getPersistentDataContainer().remove(ID);
		stand.remove();
		
	}
	public void removeChest(Boat boat) {
		UUID uuid = UUID.fromString(boat.getPersistentDataContainer().get(ID, PersistentDataType.STRING));
		removeChest(boat, uuid);
	}
	public void removeChest(ArmorStand stand) {
		UUID uuid = stand.getUniqueId();
		Entity boat = Bukkit.getEntity(UUID.fromString(stand.getPersistentDataContainer().get(ID, PersistentDataType.STRING)));
		removeChest((Boat) boat, uuid);
	}
	
	public void addChest(Boat boat) {
		
		ArmorStand stand = boat.getLocation().getWorld().spawn(getChestLocation(boat), ArmorStand.class);
		boat.getPersistentDataContainer().set(ID, PersistentDataType.STRING, stand.getUniqueId().toString());
		
		stand.getPersistentDataContainer().set(ID, PersistentDataType.STRING, boat.getUniqueId().toString());
		stand.getPersistentDataContainer().set(INV, PersistentDataType.STRING, Base64Serialization.toBase64(27));
		stand.setVisible(false);
		stand.setGravity(false);
		stand.setSilent(true);
		stand.getEquipment().setHelmet(new ItemStack(Material.CHEST));
		MovementTask.chestBoats.add(boat);
		
	}
	
	public static Location getChestLocation(Boat boat) {
		Vector vector = boat.getLocation().getDirection().rotateAroundY(Math.toRadians(180)).multiply(0.5).setY(-1.19);
		return boat.getLocation().clone().add(vector);
	}
	
	
	public boolean armorStandClicked(Player player, ArmorStand stand) {
		
		UUID uuid = stand.getUniqueId();
		String inv = stand.getPersistentDataContainer().get(INV, PersistentDataType.STRING);
		boolean isChest = inv != null;
		
		if (isChest) {
			
			if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR) {
				
				removeChest(stand);
				player.getInventory().setItemInMainHand(new ItemStack(Material.CHEST));
				
				return true;
				
			}
			
			
			Inventory inventory = invIdsRev.get(uuid);
			if (inventory != null) {
				
				player.openInventory(inventory);
				return true;
				
			}
			
			else {
				
				inventory = Base64Serialization.fromBase64(inv);
				invIds.put(inventory, uuid);
				invIdsRev.put(uuid, inventory);
				player.openInventory(inventory);
				return true;
				
			}
			
		}
		
		return false;
		
	}
	
	public void saveInventory(Inventory inventory) {
		
		UUID uuid = invIds.get(inventory);
		if (uuid == null) return;
		
		Bukkit.getEntity(uuid).getPersistentDataContainer().set(INV, PersistentDataType.STRING, Base64Serialization.toBase64(inventory));
		if (inventory.getViewers().isEmpty()) {
			invIds.remove(inventory);
			invIdsRev.remove(uuid);
		}
		
	}
	
	@EventHandler
	public void onInventoryClosed(InventoryCloseEvent event) {
		saveInventory(event.getInventory());
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		saveInventory(event.getPlayer().getOpenInventory().getTopInventory());
	}
	
}
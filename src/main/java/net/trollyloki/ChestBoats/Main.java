package net.trollyloki.ChestBoats;

import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	
	private static JavaPlugin plugin = null;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		saveDefaultConfig();
		
		this.getServer().getPluginManager().registerEvents(new Manager(), plugin);
		MovementTask.start(this);
	}
	
	@Override
	public void onDisable() {
		plugin = null;
	}
	
	public static JavaPlugin getPlugin() {
		return plugin;
	}
	
	public static String translate(String key) {
		return ChatColor.translateAlternateColorCodes('&', getPlugin().getConfig().getString("translations." + key));
	}
	
}
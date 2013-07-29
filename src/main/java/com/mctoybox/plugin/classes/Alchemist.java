package com.mctoybox.plugin.classes;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.getspout.spoutapi.event.screen.ScreenOpenEvent;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.material.Material;
import org.getspout.spoutapi.material.MaterialData;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.mctoybox.plugin.MainClass;
import com.mctoybox.plugin.Permissions;
import com.mctoybox.plugin.util.ItemChecker;
import com.mctoybox.plugin.util.Recipes;

public class Alchemist extends ClassBase {
	Material downgradeCatalyst = MaterialData.feather;
	Material upgradeCatalyst = MaterialData.slimeball;
	
	private ArrayList<PotionEffectType> helpfulPotions;
	
	public Alchemist(MainClass mainClass) {
		super(mainClass, "Alchemist", Permissions.CLASSES_ALCHEMIST);
		
		Recipes.NewShapelessRecipe(MaterialData.ironIngot, new Material[] { upgradeCatalyst, MaterialData.cobblestone }, new int[] { 1, 8 });
		Recipes.NewShapelessRecipe(MaterialData.goldIngot, new Material[] { upgradeCatalyst, MaterialData.ironIngot }, new int[] { 1, 8 });
		Recipes.NewShapelessRecipe(MaterialData.diamond, new Material[] { upgradeCatalyst, MaterialData.goldIngot }, new int[] { 1, 4 });
		
		Recipes.NewShapelessRecipe(MaterialData.cobblestone, 8, new Material[] { downgradeCatalyst, MaterialData.ironIngot }, new int[] { 1, 1 });
		Recipes.NewShapelessRecipe(MaterialData.ironIngot, 8, new Material[] { downgradeCatalyst, MaterialData.goldIngot }, new int[] { 1, 1 });
		Recipes.NewShapelessRecipe(MaterialData.goldIngot, 4, new Material[] { downgradeCatalyst, MaterialData.diamond }, new int[] { 1, 1 });
		
		helpfulPotions = new ArrayList<PotionEffectType>();
		PotionEffectType[] types = { PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FAST_DIGGING, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.INCREASE_DAMAGE,
				PotionEffectType.INVISIBILITY, PotionEffectType.JUMP, PotionEffectType.NIGHT_VISION, PotionEffectType.REGENERATION, PotionEffectType.SPEED, PotionEffectType.WATER_BREATHING };
		
		for (PotionEffectType t : types) {
			helpfulPotions.add(t);
		}
	}
	
	@EventHandler
	public void onScreenOpen(ScreenOpenEvent event) {
		SpoutPlayer player = event.getPlayer();
		if (!event.getScreenType().equals(ScreenType.BREWING_STAND_INVENTORY))
			return;
		if (!mainClass.playerClasses.getSecondaryClass(player).equals(classRef)) {
			event.setCancelled(true);
			player.sendMessage(ChatColor.RED + "Only an alchemist can use brewing stands!");
		}
	}
	
	@EventHandler
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		mainClass.debugOutput("PlayerItemConsumeEvent triggered in Alchemist.java");
		SpoutPlayer player = (SpoutPlayer) event.getPlayer();
		ItemStack item = event.getItem();
		if (item == null)
			return;
		
		if (!item.getType().equals(org.bukkit.Material.POTION))
			return;
		
		mainClass.debugOutput("Item consumed is a potion");
		
		if (!ClassTypes.ALCHEMIST.equals(mainClass.playerClasses.getSecondaryClass(player)))
			return;
		// If the player is not an alchemist, the potion effect is applied as
		// normal
		
		mainClass.debugOutput("Player involved is an alchemist");
		event.setCancelled(true);
		
		Potion potion = Potion.fromItemStack(item);
		
		player.setItemInHand(new SpoutItemStack(MaterialData.glassBottle));
		PotionEffectType effectType = PotionEffectType.getById(item.getDurability() % 64);
		
		if (helpfulPotions.contains(effectType)) {
			// baseTime and duration are measured in seconds
			int baseTime = (effectType.equals(PotionEffectType.REGENERATION) ? 45 : 180);
			int duration;
			if (potion.getLevel() == 2)
				duration = baseTime / 2;
			else if (potion.hasExtendedDuration())
				duration = (int) (baseTime * 2.6);
			else
				duration = baseTime;
			
			PotionEffect toApply = new PotionEffect(effectType, (int) (duration * 1.5) * 20, potion.getLevel());
			player.addPotionEffect(toApply);
			player.sendMessage("Your knowledge of alchemy makes your potion last longer!");
		}
		
	}
	
	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		SpoutPlayer player = (SpoutPlayer) event.getWhoClicked();
		ItemStack[] craftWindow = event.getInventory().getContents();
		
		boolean AlchemistRecipe = false;
		if (ItemChecker.CraftingArrayHas(mainClass, craftWindow, this.downgradeCatalyst) || ItemChecker.CraftingArrayHas(mainClass, craftWindow, this.upgradeCatalyst)) {
			if (ItemChecker.CraftingArrayHasMinAmount(mainClass, craftWindow, MaterialData.cobblestone, 8) || ItemChecker.CraftingArrayHasMinAmount(mainClass, craftWindow, MaterialData.ironIngot, 8)
					|| ItemChecker.CraftingArrayHasMinAmount(mainClass, craftWindow, MaterialData.goldIngot, 4)
					|| ItemChecker.CraftingArrayHasExactAmount(mainClass, craftWindow, MaterialData.diamond, 1)
					|| ItemChecker.CraftingArrayHasExactAmount(mainClass, craftWindow, MaterialData.goldIngot, 1)
					|| ItemChecker.CraftingArrayHasExactAmount(mainClass, craftWindow, MaterialData.ironIngot, 1)) {
				AlchemistRecipe = true;
				if (!ClassTypes.ALCHEMIST.equals(mainClass.playerClasses.getSecondaryClass(player))) {
					event.setCancelled(true);
					player.sendMessage(ChatColor.RED + "Only an alchemist can transmute things!");
				}
			}
		}
		
		if (!event.isCancelled() && AlchemistRecipe) {
			// player.getInventory().addItem(new SpoutItemStack(catalyst, 1));
		}
		
	}
}

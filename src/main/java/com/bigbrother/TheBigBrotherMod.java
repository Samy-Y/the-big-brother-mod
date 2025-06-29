package com.bigbrother;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.item.Items.register;

public class TheBigBrotherMod implements ModInitializer {
	public static final String MOD_ID = "the-big-brother-mod";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// creating item

	public static final RegistryKey<ItemGroup> TBBMBLOCKS_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(TheBigBrotherMod.MOD_ID, "tbbm_blocks"));
	public static final ItemGroup TBBMBLOCKS = FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModBlocks.LOUDSPEAKER))
			.displayName(Text.translatable("itemGroup.the-big-brother-mod.blocks"))
			.build();



	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		ModSounds.initialize();
		// Register the item/block groups.
		Registry.register(Registries.ITEM_GROUP, TBBMBLOCKS_KEY, TBBMBLOCKS);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
				.register((itemGroup) -> itemGroup.add(ModItems.MONEY));
		ItemGroupEvents.modifyEntriesEvent(TBBMBLOCKS_KEY).register(itemGroup -> {
			itemGroup.add(ModBlocks.LOUDSPEAKER);
		});


		LOGGER.info("The Big Brother Mod has been initialized.");
	}
}
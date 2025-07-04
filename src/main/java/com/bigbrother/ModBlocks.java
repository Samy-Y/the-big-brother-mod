package com.bigbrother;

import com.bigbrother.bust.PlayerBustBlock;
import com.bigbrother.camera.SecurityCameraBlock;
import com.bigbrother.fakevillager.FakeVillagerBlock;
import com.bigbrother.loudspeaker.LoudspeakerBlock;
import com.bigbrother.propaganda.PropagandaPosterBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static com.bigbrother.TheBigBrotherMod.MOD_ID;

public class ModBlocks {
    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings, boolean shouldRegisterItem) {
        // Create a registry key for the block
        RegistryKey<Block> blockKey = keyOfBlock(name);
        // Create the block instance
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:moving_piston` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            // Items need to be registered with a different type of registry key, but the ID
            // can be the same.
            RegistryKey<Item> itemKey = keyOfItem(name);

            BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
            Registry.register(Registries.ITEM, itemKey, blockItem);
        }

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, name));
    }

    public static void initialize() {
        // stays empty
    }

    public static final Block LOUDSPEAKER = register(
            "loudspeaker",
            LoudspeakerBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE),
            true
    );

    public static final Block SECURITY_CAMERA = register(
            "security_camera",
            SecurityCameraBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE),
            true
    );

    // In ModBlocks.java

    public static final Block PLAYER_BUST = register(
            "player_bust",
            PlayerBustBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE),
            true
    );

    public static final Block PROPAGANDA_POSTER = register(
            "propaganda_poster",
            PropagandaPosterBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.WOOD),
            true
    );

    public static final Block FAKE_VILLAGER = register(
            "fake_villager",
            FakeVillagerBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.STONE),
            true
    );


}

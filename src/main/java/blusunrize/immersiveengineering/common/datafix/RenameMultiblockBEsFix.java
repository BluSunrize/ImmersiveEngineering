/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafix;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder.DUMMY_BE_SUFFIX;
import static blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder.MASTER_BE_SUFFIX;

/**
 * Many multiblock BEs use "nameslikethis" in 1.19.2 and below, on 1.19.3 this is replaced by "names_like_this"
 * (matching the block names)
 */
public class RenameMultiblockBEsFix
{
	private static final Map<String, String> MULTIBLOCK_BE_RENAMES = new HashMap<>();

	static
	{
		registerFix("cokeoven", IEMultiblockLogic.COKE_OVEN);
		registerFix("blastfurnace", IEMultiblockLogic.BLAST_FURNACE);
		registerFix("blastfurnaceadvanced", IEMultiblockLogic.ADV_BLAST_FURNACE);
		registerFix("alloysmelter", IEMultiblockLogic.ALLOY_SMELTER);
		registerFix("lightningrod", IEMultiblockLogic.LIGHTNING_ROD);
		registerFix("dieselgenerator", IEMultiblockLogic.DIESEL_GENERATOR);
		registerFix("metalpress", IEMultiblockLogic.METAL_PRESS);
		registerFix("autoworkbench", IEMultiblockLogic.AUTO_WORKBENCH);
		registerFix("bottlingmachine", IEMultiblockLogic.BOTTLING_MACHINE);
		registerFix("sheetmetaltank", IEMultiblockLogic.TANK);
		registerFix("bucketwheel", IEMultiblockLogic.BUCKET_WHEEL);
		registerFix("arcfurnace", IEMultiblockLogic.ARC_FURNACE);
	}

	private static void registerFix(String oldName, MultiblockRegistration<?> newVersion)
	{
		MULTIBLOCK_BE_RENAMES.put(oldName, newVersion.id().getPath());
	}

	public static Optional<BlockEntityType<?>> replaceIfMissing(Optional<BlockEntityType<?>> baseType, CompoundTag tag)
	{
		if(baseType.isPresent())
			return baseType;
		ResourceLocation nbtName = new ResourceLocation(tag.getString("id"));
		if(!nbtName.getNamespace().equals(Lib.MODID))
			return baseType;
		String suffix;
		if(nbtName.getPath().endsWith(MASTER_BE_SUFFIX))
			suffix = MASTER_BE_SUFFIX;
		else if(nbtName.getPath().endsWith(DUMMY_BE_SUFFIX))
			suffix = DUMMY_BE_SUFFIX;
		else
			return baseType;
		final String baseName = nbtName.getPath().substring(0, nbtName.getPath().length()-suffix.length());
		final String remapped = MULTIBLOCK_BE_RENAMES.get(baseName);
		if(remapped==null)
			return baseType;
		final ResourceLocation newName = nbtName.withPath(remapped+suffix);
		return BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(newName);
	}
}

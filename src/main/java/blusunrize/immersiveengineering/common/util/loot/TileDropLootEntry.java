/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class TileDropLootEntry extends StandaloneLootEntry
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "tile_drop");

	protected TileDropLootEntry(int weightIn, int qualityIn, ILootCondition[] conditionsIn, ILootFunction[] functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void func_216154_a(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.has(LootParameters.BLOCK_ENTITY))
		{
			TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
			if(te instanceof ITileDrop)
				((ITileDrop)te).getTileDrops(context).forEach(output);
		}
	}

	public static StandaloneLootEntry.Builder<?> builder()
	{
		return builder(TileDropLootEntry::new);
	}

	@Nonnull
	@Override
	public LootPoolEntryType func_230420_a_()
	{
		return IELootFunctions.tileDrop;
	}

	public static class Serializer extends StandaloneLootEntry.Serializer<TileDropLootEntry>
	{
		@Nonnull
		@Override
		protected TileDropLootEntry func_212829_b_(
				@Nonnull JsonObject json,
				@Nonnull JsonDeserializationContext context,
				int weight,
				int quality,
				@Nonnull ILootCondition[] conditions,
				@Nonnull ILootFunction[] functions
		)
		{
			return new TileDropLootEntry(weight, quality, conditions, functions);
		}
	}
}

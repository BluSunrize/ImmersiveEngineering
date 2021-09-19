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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class TileDropLootEntry extends LootPoolSingletonContainer
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "tile_drop");

	protected TileDropLootEntry(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.hasParam(LootContextParams.BLOCK_ENTITY))
		{
			BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			if(te instanceof ITileDrop)
				((ITileDrop)te).getTileDrops(context).forEach(output);
		}
	}

	public static LootPoolSingletonContainer.Builder<?> builder()
	{
		return simpleBuilder(TileDropLootEntry::new);
	}

	@Nonnull
	@Override
	public LootPoolEntryType getType()
	{
		return IELootFunctions.tileDrop;
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<TileDropLootEntry>
	{
		@Nonnull
		@Override
		protected TileDropLootEntry deserialize(
				@Nonnull JsonObject json,
				@Nonnull JsonDeserializationContext context,
				int weight,
				int quality,
				@Nonnull LootItemCondition[] conditions,
				@Nonnull LootItemFunction[] functions
		)
		{
			return new TileDropLootEntry(weight, quality, conditions, functions);
		}
	}
}

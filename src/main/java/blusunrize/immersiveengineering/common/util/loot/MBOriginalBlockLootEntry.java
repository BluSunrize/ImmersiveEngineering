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
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
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

public class MBOriginalBlockLootEntry extends LootPoolSingletonContainer
{
	public static ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "multiblock_original_block");

	protected MBOriginalBlockLootEntry(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.hasParam(LootContextParams.BLOCK_ENTITY))
		{
			BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			if(te instanceof MultiblockPartBlockEntity)
			{
				MultiblockPartBlockEntity<?> multiblockTile = (MultiblockPartBlockEntity<?>)te;
				Utils.getDrops(multiblockTile.getOriginalBlock(),
						new LootContext.Builder(context.getLevel())
								.withOptionalParameter(LootContextParams.TOOL, context.getParamOrNull(LootContextParams.TOOL))
								.withOptionalParameter(LootContextParams.ORIGIN, context.getParamOrNull(LootContextParams.ORIGIN))
				).forEach(output);
			}
		}
	}

	public static LootPoolSingletonContainer.Builder<?> builder()
	{
		return simpleBuilder(MBOriginalBlockLootEntry::new);
	}

	@Override
	public LootPoolEntryType getType()
	{
		return IELootFunctions.multiblockOrigBlock;
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<MBOriginalBlockLootEntry>
	{
		@Nonnull
		@Override
		protected MBOriginalBlockLootEntry deserialize(
				@Nonnull JsonObject json,
				@Nonnull JsonDeserializationContext context,
				int weight,
				int quality,
				@Nonnull LootItemCondition[] conditions,
				@Nonnull LootItemFunction[] functions
		)
		{
			return new MBOriginalBlockLootEntry(weight, quality, conditions, functions);
		}
	}

}

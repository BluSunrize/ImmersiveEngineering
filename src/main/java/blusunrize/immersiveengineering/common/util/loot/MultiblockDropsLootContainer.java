/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.ComponentInstance;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperCommon;
import blusunrize.immersiveengineering.common.blocks.multiblocks.blockimpl.MultiblockBEHelperMaster;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MultiblockDropsLootContainer extends LootPoolSingletonContainer
{
	protected MultiblockDropsLootContainer(int weightIn, int qualityIn, LootItemCondition[] conditionsIn, LootItemFunction[] functionsIn)
	{
		super(weightIn, qualityIn, conditionsIn, functionsIn);
	}

	@Override
	protected void createItemStack(@Nonnull Consumer<ItemStack> output, LootContext context)
	{
		if(context.getParamOrNull(LootContextParams.BLOCK_ENTITY) instanceof IMultiblockBE<?> multiblockBE)
		{
			final var helper = multiblockBE.getHelper();
			dropOriginalBlock(helper, context, output);
			dropExtraItems(helper, output);
		}
	}

	private void dropOriginalBlock(IMultiblockBEHelper<?> helper, LootContext context, Consumer<ItemStack> drop)
	{
		final var originalBlock = helper.getOriginalBlock(context.getLevel());
		final var dropCTX = new LootContext.Builder(context.getLevel())
				.withOptionalParameter(LootContextParams.TOOL, context.getParamOrNull(LootContextParams.TOOL))
				.withOptionalParameter(LootContextParams.ORIGIN, context.getParamOrNull(LootContextParams.ORIGIN));
		Utils.getDrops(originalBlock, dropCTX).forEach(drop);
	}

	private <S extends IMultiblockState>
	void dropExtraItems(IMultiblockBEHelper<S> iHelper, Consumer<ItemStack> drop)
	{
		if(!(iHelper instanceof MultiblockBEHelperCommon<S> helper))
			return;
		final var masterHelper = helper.getMasterHelperDuringDisassembly();
		if(masterHelper instanceof MultiblockBEHelperMaster<S> nonAPI)
			for(final ComponentInstance<?> component : nonAPI.getComponentInstances())
				component.dropExtraItems(drop);
		if(masterHelper!=null)
			helper.getMultiblock().logic().dropExtraItems(masterHelper.getState(), drop);
	}

	public static LootPoolSingletonContainer.Builder<?> builder()
	{
		return simpleBuilder(MultiblockDropsLootContainer::new);
	}

	@Override
	public LootPoolEntryType getType()
	{
		return IELootFunctions.MULTIBLOCK_DROPS.get();
	}

	public static class Serializer extends LootPoolSingletonContainer.Serializer<MultiblockDropsLootContainer>
	{
		@Nonnull
		@Override
		protected MultiblockDropsLootContainer deserialize(
				@Nonnull JsonObject json,
				@Nonnull JsonDeserializationContext context,
				int weight,
				int quality,
				@Nonnull LootItemCondition[] conditions,
				@Nonnull LootItemFunction[] functions
		)
		{
			return new MultiblockDropsLootContainer(weight, quality, conditions, functions);
		}
	}

}

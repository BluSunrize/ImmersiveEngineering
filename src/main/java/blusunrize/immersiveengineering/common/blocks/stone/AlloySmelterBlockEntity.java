/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ArgContainer;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AlloySmelterBlockEntity extends FurnaceLikeBlockEntity<AlloyRecipe, AlloySmelterBlockEntity>
{
	private final Supplier<AlloyRecipe> cachedRecipe = CachedRecipe.cached(
			AlloyRecipe::findRecipe, () -> level, () -> inventory.get(0), () -> inventory.get(1)
	);

	public AlloySmelterBlockEntity(BlockEntityType<AlloySmelterBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(
				IEMultiblocks.ALLOY_SMELTER, type, 2,
				ImmutableList.of(new InputSlot<>(a -> a.input0, 0), new InputSlot<>(a -> a.input1, 1)),
				ImmutableList.of(new OutputSlot<>(a -> a.output, 3)),
				a -> a.time,
				pos, state
		);
	}

	@Override
	public ArgContainer<AlloySmelterBlockEntity, ?> getContainerType()
	{
		return IEMenuTypes.ALLOY_SMELTER;
	}

	@Nullable
	@Override
	protected AlloyRecipe getRecipeForInput()
	{
		return cachedRecipe.get();
	}

	@Override
	protected int getBurnTimeOf(ItemStack fuel)
	{
		//TODO more specific type?
		return ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING);
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return slot==0||slot==1||FurnaceBlockEntity.isFuel(stack);
	}
}
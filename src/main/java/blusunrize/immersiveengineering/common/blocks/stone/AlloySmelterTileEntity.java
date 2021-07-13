/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.TileContainer;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class AlloySmelterTileEntity extends FurnaceLikeTileEntity<AlloyRecipe, AlloySmelterTileEntity>
{
	private final Supplier<AlloyRecipe> cachedRecipe = CachedRecipe.cached(
			AlloyRecipe::findRecipe, () -> inventory.get(0), () -> inventory.get(1)
	);

	public AlloySmelterTileEntity()
	{
		super(
				IEMultiblocks.ALLOY_SMELTER, IETileTypes.ALLOY_SMELTER.get(), 2,
				ImmutableList.of(new InputSlot<>(a -> a.input0, 0), new InputSlot<>(a -> a.input1, 1)),
				ImmutableList.of(new OutputSlot<>(a -> a.output, 3)),
				a -> a.time
		);
	}

	@Override
	public TileContainer<AlloySmelterTileEntity, ?> getContainerType()
	{
		return IEContainerTypes.ALLOY_SMELTER;
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
		return ForgeHooks.getBurnTime(fuel);
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return slot==0||slot==1||FurnaceTileEntity.isFuel(stack);
	}
}
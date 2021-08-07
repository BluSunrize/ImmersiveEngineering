/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.register.IEContainerTypes;
import blusunrize.immersiveengineering.common.register.IEContainerTypes.TileContainer;
import blusunrize.immersiveengineering.common.util.CachedRecipe;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class BlastFurnaceTileEntity<T extends BlastFurnaceTileEntity<T>> extends FurnaceLikeTileEntity<BlastFurnaceRecipe, T>
{
	private final Supplier<BlastFurnaceRecipe> cachedRecipe = CachedRecipe.cached(
			BlastFurnaceRecipe::findRecipe, () -> inventory.get(0)
	);

	public BlastFurnaceTileEntity(IETemplateMultiblock mb, BlockEntityType<T> type, BlockPos pos, BlockState state)
	{
		super(
				mb, type, 1,
				ImmutableList.of(new InputSlot<>(r -> r.input, 0)),
				ImmutableList.of(new OutputSlot<>(r -> r.output, 2), new OutputSlot<>(r -> r.slag, 3)),
				r -> r.time,
				pos, state
		);
	}

	@Override
	public TileContainer<? super T, ?> getContainerType()
	{
		return IEContainerTypes.BLAST_FURNACE;
	}

	@Nullable
	@Override
	protected BlastFurnaceRecipe getRecipeForInput()
	{
		return cachedRecipe.get();
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return slot==0?BlastFurnaceRecipe.findRecipe(stack, null)!=null: slot==1&&BlastFurnaceFuel.isValidBlastFuel(stack);
	}

	@Override
	protected int getBurnTimeOf(ItemStack fuel)
	{
		return BlastFurnaceFuel.getBlastFuelTime(fuel);
	}

	public static class CrudeBlastFurnaceTileEntity extends BlastFurnaceTileEntity<CrudeBlastFurnaceTileEntity>
	{

		public CrudeBlastFurnaceTileEntity(
				BlockEntityType<CrudeBlastFurnaceTileEntity> type, BlockPos pos, BlockState state
		)
		{
			super(IEMultiblocks.BLAST_FURNACE, type, pos, state);
		}
	}
}
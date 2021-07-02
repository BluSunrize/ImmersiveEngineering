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
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.TileContainer;
import com.google.common.collect.ImmutableList;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nullable;

public class BlastFurnaceTileEntity<T extends BlastFurnaceTileEntity<T>> extends FurnaceLikeTileEntity<BlastFurnaceRecipe, T>
{
	public BlastFurnaceTileEntity(IETemplateMultiblock mb, TileEntityType<T> type)
	{
		super(
				mb, type, 1,
				ImmutableList.of(new InputSlot<>(r -> r.input, 0)),
				ImmutableList.of(new OutputSlot<>(r -> r.output, 2), new OutputSlot<>(r -> r.slag, 3)),
				r -> r.time
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
		if(inventory.get(0).isEmpty())
			return null;
		return BlastFurnaceRecipe.findRecipe(inventory.get(0));
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return slot==0?BlastFurnaceRecipe.findRecipe(stack)!=null: slot==1&&BlastFurnaceFuel.isValidBlastFuel(stack);
	}

	@Override
	protected int getBurnTimeOf(ItemStack fuel)
	{
		return BlastFurnaceFuel.getBlastFuelTime(fuel);
	}

	public static class CrudeBlastFurnaceTileEntity extends BlastFurnaceTileEntity<CrudeBlastFurnaceTileEntity>
	{

		public CrudeBlastFurnaceTileEntity()
		{
			super(IEMultiblocks.BLAST_FURNACE, IETileTypes.BLAST_FURNACE.get());
		}
	}
}
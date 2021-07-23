/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.GenericDeferredWork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class IESeedItem extends BlockItem implements IPlantable
{
	public IESeedItem(Block cropBlock)
	{
		super(cropBlock, new Properties().tab(ImmersiveEngineering.ITEM_GROUP));

		// Register for composting
		GenericDeferredWork.enqueue(() -> ComposterBlock.COMPOSTABLES.putIfAbsent(this, 0.3f));
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
	{
		if(this.allowdedIn(group))
			items.add(new ItemStack(this));
	}

	@Override
	public String getDescriptionId()
	{
		return this.getOrCreateDescriptionId();
	}

	@Override
	public PlantType getPlantType(BlockGetter world, BlockPos pos)
	{
		return ((IPlantable)this.getBlock()).getPlantType(world, pos);
	}

	@Override
	public BlockState getPlant(BlockGetter world, BlockPos pos)
	{
		return this.getBlock().defaultBlockState();
	}
}
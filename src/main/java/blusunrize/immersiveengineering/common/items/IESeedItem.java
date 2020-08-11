/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.fml.DeferredWorkQueue;

public class IESeedItem extends BlockItem implements IPlantable
{
	public IESeedItem(Block cropBlock)
	{
		super(cropBlock, new Properties().group(ImmersiveEngineering.itemGroup));
		setRegistryName(ImmersiveEngineering.MODID, "seed");
		IEContent.registeredIEItems.add(this);

		// Register for composting
		DeferredWorkQueue.runLater(
				() -> ComposterBlock.CHANCES.putIfAbsent(this, 0.3f)
		);
	}

	@Override
	public String getTranslationKey()
	{
		return this.getDefaultTranslationKey();
	}

	@Override
	public PlantType getPlantType(IBlockReader world, BlockPos pos)
	{
		return ((IPlantable)this.getBlock()).getPlantType(world, pos);
	}

	@Override
	public BlockState getPlant(IBlockReader world, BlockPos pos)
	{
		return this.getBlock().getDefaultState();
	}
}
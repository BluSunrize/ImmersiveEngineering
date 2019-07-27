/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.*;
import net.minecraft.block.BlockState;


public class IEStairsBlock extends StairsBlock
{
	public boolean hasFlavour = false;
	public boolean isFlammable = false;

	public IEStairsBlock(String name, BlockState state, Block.Properties properties)
	{
		super(state, properties);
		setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, name));
		IEContent.registeredIEBlocks.add(this);
		Item.Properties itemProps = new Item.Properties().group(ImmersiveEngineering.itemGroup);
		IEContent.registeredIEItems.add(new BlockItem(this, itemProps));
	}

	// @todo: review required if this is needed.
	public IEStairsBlock setFlammable(boolean b)
	{
		this.isFlammable = b;
		return this;
	}

	public IEStairsBlock setHasFlavour(boolean hasFlavour)
	{
		this.hasFlavour = hasFlavour;
		return this;
	}
}
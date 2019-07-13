/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.FenceBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class IEFenceBlock extends FenceBlock
{
	public IEFenceBlock(String name, Properties properties)
	{
		super(properties);
		setRegistryName(new ResourceLocation(ImmersiveEngineering.MODID, name));
		IEContent.registeredIEBlocks.add(this);
		Item.Properties itemProps = new Item.Properties().group(ImmersiveEngineering.itemGroup);
		IEContent.registeredIEItems.add(new BlockItem(this, itemProps));
	}
}

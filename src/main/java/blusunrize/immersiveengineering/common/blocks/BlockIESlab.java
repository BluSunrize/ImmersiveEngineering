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
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;

public class BlockIESlab extends BlockSlab
{

	public BlockIESlab(String name, Properties props, Class<? extends ItemBlock> itemBlock)
	{
		super(props);
		ResourceLocation registryName = new ResourceLocation(ImmersiveEngineering.MODID, name);
		setRegistryName(registryName);

		IEContent.registeredIEBlocks.add(this);
		try
		{
			Item.Properties itemProps = new Item.Properties().group(ImmersiveEngineering.itemGroup);
			IEContent.registeredIEItems.add(itemBlock.getConstructor(Block.class, Item.Properties.class)
					.newInstance(this, itemProps));
		} catch(Exception e)
		{
			//TODO e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
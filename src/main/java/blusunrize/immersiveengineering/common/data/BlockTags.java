/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;

class BlockTags extends BlockTagsProvider
{

	public BlockTags(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerTags()
	{

		for(EnumMetals metal : EnumMetals.values())
			if(!metal.isVanillaMetal())
			{
				MetalTags tags = IETags.getTagsFor(metal);
				Block storage = IEBlocks.Metals.storage.get(metal);
				getBuilder(tags.storage).add(storage);
				if(metal.shouldAddOre())
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					assert (tags.ore!=null&&ore!=null);
					getBuilder(tags.ore).add(ore);
				}
			}
	}
}

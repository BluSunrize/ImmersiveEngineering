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
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;

class ItemTags extends ItemTagsProvider
{

	public ItemTags(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerTags()
	{
		for(EnumMetals metal : EnumMetals.values())
		{
			Item nugget = Metals.nuggets.get(metal);
			Item ingot = Metals.ingots.get(metal);
			Item plate = Metals.plates.get(metal);
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				getBuilder(tags.ingot).add(ingot);
				getBuilder(tags.nugget).add(nugget);
			}
			getBuilder(tags.plate).add(plate);
		}

		IETags.forAllBlocktags(this::copy);
	}
}

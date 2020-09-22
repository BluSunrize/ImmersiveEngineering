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
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

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
			Item dust = Metals.dusts.get(metal);
			MetalTags tags = IETags.getTagsFor(metal);
			if(!metal.isVanillaMetal())
			{
				getBuilder(tags.ingot).add(ingot);
				getBuilder(Tags.Items.INGOTS).add(ingot);
				getBuilder(tags.nugget).add(nugget);
				getBuilder(Tags.Items.NUGGETS).add(nugget);
				getBuilder(Tags.Items.STORAGE_BLOCKS).add(IEBlocks.Metals.storage.get(metal).asItem());
				if(metal.shouldAddOre())
					getBuilder(Tags.Items.ORES).add(IEBlocks.Metals.ores.get(metal).asItem());
			}
			getBuilder(tags.plate).add(plate);
			getBuilder(IETags.plates).add(plate);
			getBuilder(tags.dust).add(dust);
			getBuilder(Tags.Items.DUSTS).add(dust);
		}

		IETags.forAllBlocktags(this::copy);

		getBuilder(IETags.clay).add(Items.CLAY_BALL);
		getBuilder(IETags.charCoal).add(Items.CHARCOAL);

		getBuilder(Tags.Items.SEEDS).add(Misc.hempSeeds);
		getBuilder(Tags.Items.RODS_WOODEN).add(Ingredients.stickTreated);
		getBuilder(IETags.treatedStick).add(Ingredients.stickTreated);
		getBuilder(IETags.slag).add(Ingredients.slag);
		getBuilder(IETags.ironRod).add(Ingredients.stickIron);
		getBuilder(IETags.steelRod).add(Ingredients.stickSteel);
		getBuilder(IETags.aluminumRod).add(Ingredients.stickAluminum);
		getBuilder(IETags.fiberHemp).add(Ingredients.hempFiber);
		getBuilder(IETags.fabricHemp).add(Ingredients.hempFabric);
		getBuilder(IETags.coalCoke).add(Ingredients.coalCoke);
		getBuilder(IETags.coalCokeDust).add(Ingredients.dustCoke);
		getBuilder(IETags.hopGraphiteDust).add(Ingredients.dustHopGraphite);
		getBuilder(IETags.hopGraphiteIngot).add(Ingredients.ingotHopGraphite);
		getBuilder(IETags.copperWire).add(Ingredients.wireCopper);
		getBuilder(IETags.electrumWire).add(Ingredients.wireElectrum);
		getBuilder(IETags.aluminumWire).add(Ingredients.wireAluminum);
		getBuilder(IETags.steelWire).add(Ingredients.wireSteel);
		getBuilder(IETags.saltpeterDust).add(Ingredients.dustSaltpeter);
		getBuilder(IETags.sulfurDust).add(Ingredients.dustSulfur);
		getBuilder(IETags.sawdust).add(Ingredients.dustWood);
		getBuilder(IETags.metalRods).add(IETags.aluminumRod, IETags.ironRod, IETags.steelRod);
		getBuilder(IETags.sawblades).add(Tools.sawblade);
	}
}

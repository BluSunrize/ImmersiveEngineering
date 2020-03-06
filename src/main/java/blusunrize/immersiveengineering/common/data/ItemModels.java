/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;

import javax.annotation.Nonnull;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class ItemModels extends ItemModelProvider
{
	BlockStates blockStates;

	public ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper, BlockStates blockStates)
	{
		super(generator, ImmersiveEngineering.MODID, existingFileHelper);
		this.blockStates = blockStates;
	}

	@Override
	protected void registerModels()
	{
		for(Entry<Block, ModelFile> model : blockStates.itemModels.entrySet())
			getBuilder(model.getKey())
					.parent(model.getValue());

		cubeBottomTop(name(WoodenDevices.woodenBarrel),
				rl("block/wooden_device/barrel_side"),
				rl("block/wooden_device/barrel_up_none"),
				rl("block/wooden_device/barrel_up_none"));

		cubeBottomTop(name(MetalDevices.barrel),
				rl("block/metal_device/barrel_side"),
				rl("block/metal_device/barrel_up_none"),
				rl("block/metal_device/barrel_up_none"));
	}

	private ItemModelBuilder getBuilder(IItemProvider item)
	{
		return getBuilder(name(item));
	}

	private String name(IItemProvider item)
	{
		return item.asItem().getRegistryName().getPath();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Item models";
	}
}

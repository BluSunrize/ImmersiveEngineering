/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

/**
 * @author BluSunrize - 03.10.2016
 */
public class IEDefaultColourHandlers implements ItemColor, BlockColor
{
	public static IEDefaultColourHandlers INSTANCE = new IEDefaultColourHandlers();

	public static void register()
	{
		/*Colours*/
		for(RegistryObject<Item> itemRO : IEItems.REGISTER.getEntries())
		{
			Item item = itemRO.get();
			if(item instanceof IColouredItem)
				mc().getItemColors().register(INSTANCE, item);
		}
		for(BlockEntry<?> blockEntry : BlockEntry.ALL_ENTRIES)
		{
			Block block = blockEntry.get();
			if(block instanceof IColouredBlock colouredBlock&&colouredBlock.hasCustomBlockColours())
				mc().getBlockColors().register(INSTANCE, block);
		}
	}

	@Override
	public int getColor(BlockState state, @Nullable BlockAndTintGetter worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if(state.getBlock() instanceof IColouredBlock colouredBlock)
			return colouredBlock.getRenderColour(state, worldIn, pos, tintIndex);
		return 0xffffff;
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex)
	{
		if(stack.getItem() instanceof IColouredItem colouredItem)
			return colouredItem.getColourForIEItem(stack, tintIndex);
		return 0xffffff;
	}
}

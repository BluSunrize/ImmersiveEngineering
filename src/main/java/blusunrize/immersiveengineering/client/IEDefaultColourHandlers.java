/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

/**
 * @author BluSunrize - 03.10.2016
 */
public class IEDefaultColourHandlers implements IItemColor, IBlockColor
{
	public static IEDefaultColourHandlers INSTANCE = new IEDefaultColourHandlers();

	public static void register()
	{
		/*Colours*/
		for(RegistryObject<Item> itemRO : IEItems.REGISTER.getEntries())
		{
			Item item = itemRO.get();
			if(item instanceof IColouredItem&&((IColouredItem)item).hasCustomItemColours())
				mc().getItemColors().register(INSTANCE, item);
		}
		for(BlockEntry<?> blockEntry : BlockEntry.ALL_ENTRIES)
		{
			Block block = blockEntry.get();
			if(block instanceof IColouredBlock&&((IColouredBlock)block).hasCustomBlockColours())
				mc().getBlockColors().register(INSTANCE, block);
		}
	}

	@Override
	public int getColor(BlockState state, @Nullable IBlockDisplayReader worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if(state.getBlock() instanceof IColouredBlock)
			return ((IColouredBlock)state.getBlock()).getRenderColour(state, worldIn, pos, tintIndex);
		return 0xffffff;
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex)
	{
		if(stack.getItem() instanceof IColouredItem)
			return ((IColouredItem)stack.getItem()).getColourForIEItem(stack, tintIndex);
		return 0xffffff;
	}
}

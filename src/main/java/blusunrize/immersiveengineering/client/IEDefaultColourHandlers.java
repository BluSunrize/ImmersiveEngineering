/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import javax.annotation.Nullable;

/**
 * @author BluSunrize - 03.10.2016
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID, bus = Bus.MOD)
public class IEDefaultColourHandlers implements ItemColor, BlockColor
{
	public static IEDefaultColourHandlers INSTANCE = new IEDefaultColourHandlers();

	@SubscribeEvent
	public static void registerItemColors(RegisterColorHandlersEvent.Item ev)
	{
		for(Holder<Item> itemRO : IEItems.REGISTER.getEntries())
		{
			Item item = itemRO.value();
			if(item instanceof IColouredItem)
				ev.register(INSTANCE, item);
		}
	}

	@SubscribeEvent
	public static void registerBlockColors(RegisterColorHandlersEvent.Block ev)
	{
		for(BlockEntry<?> blockEntry : BlockEntry.ALL_ENTRIES)
		{
			Block block = blockEntry.get();
			if(block instanceof IColouredBlock colouredBlock&&colouredBlock.hasCustomBlockColours())
				ev.register(INSTANCE, block);
		}
	}

	@Override
	public int getColor(BlockState state, @Nullable BlockAndTintGetter worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if(state.getBlock() instanceof IColouredBlock colouredBlock)
			return colouredBlock.getRenderColour(state, worldIn, pos, tintIndex)|0xff000000;
		return 0xffffff;
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex)
	{
		if(stack.getItem() instanceof IColouredItem colouredItem)
			return colouredItem.getColourForIEItem(stack, tintIndex)|0xff000000;
		return 0xffffffff;
	}
}

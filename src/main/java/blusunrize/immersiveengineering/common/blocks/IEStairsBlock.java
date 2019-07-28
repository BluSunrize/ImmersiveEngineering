/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.block.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;


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

	//TODO: review required if this is needed.
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

	//TODO: review: added flavour information here, so that `ItemBlockIEStairs` can be removed.
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable IBlockReader world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		super.addInformation(stack, world, tooltip, flag);
		if((stack.getItem() instanceof BlockItem))
		{
			final Block block = ((BlockItem)stack.getItem()).getBlock();
			if((block instanceof IEStairsBlock)&&((IEStairsBlock)block).hasFlavour)
				tooltip.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+block.getRegistryName().getPath()));
		}
	}
}

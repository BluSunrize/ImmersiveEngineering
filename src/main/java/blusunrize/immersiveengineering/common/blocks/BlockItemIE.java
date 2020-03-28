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
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class BlockItemIE extends BlockItem
{
	private int burnTime;

	public BlockItemIE(Block b, Item.Properties props)
	{
		super(b, props);
	}

	public BlockItemIE(Block b)
	{
		this(b, new Item.Properties().group(ImmersiveEngineering.itemGroup));
		setRegistryName(b.getRegistryName());
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return getBlock().getTranslationKey();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced)
	{
		if(getBlock() instanceof IIEBlock)
		{
			IIEBlock ieBlock = (IIEBlock)getBlock();
			if(ieBlock.hasFlavour())
			{
				String flavourKey = Lib.DESC_FLAVOUR+ieBlock.getNameForFlavour();
				tooltip.add(new TranslationTextComponent(flavourKey).setStyle(new Style().setColor(TextFormatting.GRAY)));
			}
		}
		super.addInformation(stack, world, tooltip, advanced);
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"energyStored",
					ItemNBTHelper.getInt(stack, "energyStorage")).setStyle(new Style().setColor(TextFormatting.GRAY)));
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"fluidStored",
						fs.getDisplayName(), fs.getAmount()).setStyle(new Style().setColor(TextFormatting.GRAY)));
		}
	}


	public BlockItemIE setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack)
	{
		return this.burnTime;
	}

	@Override
	protected boolean placeBlock(BlockItemUseContext context, BlockState newState)
	{
		Block b = newState.getBlock();
		if(b instanceof IEBaseBlock)
		{
			IEBaseBlock ieBlock = (IEBaseBlock)b;
			if(!ieBlock.canIEBlockBePlaced(newState, context))
				return false;
			boolean ret = super.placeBlock(context, newState);
			if(ret)
				ieBlock.onIEBlockPlacedBy(context, newState);
			return ret;
		}
		else
			return super.placeBlock(context, newState);
	}

	public static class BlockItemIENoInventory extends BlockItemIE
	{
		public BlockItemIENoInventory(Block b, Properties props)
		{
			super(b, props);
		}

		@Nullable
		@Override
		public CompoundNBT getShareTag(ItemStack stack)
		{
			CompoundNBT ret = super.getShareTag(stack);
			if(ret!=null)
			{
				ret = ret.copy();
				ret.remove("inventory");
			}
			return ret;
		}
	}
}
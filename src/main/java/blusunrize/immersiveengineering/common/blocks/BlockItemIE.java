/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
		this(b, new Item.Properties().tab(ImmersiveEngineering.ITEM_GROUP));
	}

	@Override
	public String getDescriptionId(ItemStack stack)
	{
		return getBlock().getDescriptionId();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		if(getBlock() instanceof IIEBlock)
		{
			IIEBlock ieBlock = (IIEBlock)getBlock();
			if(ieBlock.hasFlavour())
			{
				String flavourKey = Lib.DESC_FLAVOUR+ieBlock.getNameForFlavour();
				tooltip.add(TextUtils.applyFormat(new TranslatableComponent(flavourKey),
						ChatFormatting.GRAY));
			}
		}
		super.appendHoverText(stack, world, tooltip, advanced);
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			tooltip.add(TextUtils.applyFormat(new TranslatableComponent(Lib.DESC_INFO+"energyStored",
							ItemNBTHelper.getInt(stack, "energyStorage")),
					ChatFormatting.GRAY));
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(TextUtils.applyFormat(
						new TranslatableComponent(Lib.DESC_INFO+"fluidStored", fs.getDisplayName(), fs.getAmount()),
						ChatFormatting.GRAY));
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
	protected boolean placeBlock(BlockPlaceContext context, BlockState newState)
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

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state)
	{
		// Skip reading the tile from NBT if the block is a (general) multiblock
		if(!state.hasProperty(IEProperties.MULTIBLOCKSLAVE))
			return super.updateCustomBlockEntityTag(pos, worldIn, player, stack, state);
		else
			return false;
	}

	public static class BlockItemIENoInventory extends BlockItemIE
	{
		public BlockItemIENoInventory(Block b, Properties props)
		{
			super(b, props);
		}

		@Nullable
		@Override
		public CompoundTag getShareTag(ItemStack stack)
		{
			CompoundTag ret = super.getShareTag(stack);
			if(ret!=null)
			{
				ret = ret.copy();
				ret.remove("inventory");
			}
			return ret;
		}
	}
}
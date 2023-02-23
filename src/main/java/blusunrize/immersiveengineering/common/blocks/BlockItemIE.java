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
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class BlockItemIE extends BlockItem
{
	private int burnTime;

	public BlockItemIE(Block b, Item.Properties props)
	{
		super(b, props);
	}

	public BlockItemIE(Block b)
	{
		this(b, new Item.Properties());
	}

	@Override
	public String getDescriptionId(ItemStack stack)
	{
		return getBlock().getDescriptionId();
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced)
	{
		if(getBlock() instanceof IIEBlock ieBlock&&ieBlock.hasFlavour())
		{
			String flavourKey = Lib.DESC_FLAVOUR+ieBlock.getNameForFlavour();
			tooltip.add(TextUtils.applyFormat(Component.translatable(flavourKey), ChatFormatting.GRAY));
		}
		super.appendHoverText(stack, world, tooltip, advanced);
		if(ItemNBTHelper.hasKey(stack, EnergyHelper.ENERGY_KEY))
			tooltip.add(TextUtils.applyFormat(Component.translatable(Lib.DESC_INFO+"energyStored",
							ItemNBTHelper.getInt(stack,  EnergyHelper.ENERGY_KEY)),
					ChatFormatting.GRAY));
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(TextUtils.applyFormat(
						Component.translatable(Lib.DESC_INFO+"fluidStored", fs.getDisplayName(), fs.getAmount()),
						ChatFormatting.GRAY));
		}
	}


	public BlockItemIE setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
	{
		return this.burnTime;
	}

	@Override
	protected boolean placeBlock(BlockPlaceContext context, BlockState newState)
	{
		Block b = newState.getBlock();
		if(b instanceof IEBaseBlock ieBlock)
		{
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

	@Nonnull
	@Override
	public Optional<TooltipComponent> getTooltipImage(@Nonnull ItemStack stack)
	{
		if(stack.hasTag())
		{
			CompoundTag tag = stack.getOrCreateTag();
			if(tag.contains("Items"))
			{
				// manual readout, skipping empty slots
				ListTag list = tag.getList("Items", 10);
				NonNullList<ItemStack> items = NonNullList.create();
				list.forEach(e -> {
					ItemStack s = ItemStack.of((CompoundTag)e);
					if(!s.isEmpty())
						items.add(s);
				});
				return Optional.of(new BundleTooltip(items, 0));
			}
		}
		return super.getTooltipImage(stack);
	}

	@Override
	public boolean canFitInsideContainerItems()
	{
		return !(getBlock() instanceof IEBaseBlock ieBlock)||ieBlock.fitsIntoContainer();
	}

	public static class BlockItemIENoInventory extends BlockItemIE
	{
		public BlockItemIENoInventory(Block b)
		{
			super(b);
		}

		@Nullable
		@Override
		public CompoundTag getShareTag(ItemStack stack)
		{
			CompoundTag ret = super.getShareTag(stack);
			if(ret!=null)
			{
				ret = ret.copy();
				ret.remove("Items");
			}
			return ret;
		}
	}
}
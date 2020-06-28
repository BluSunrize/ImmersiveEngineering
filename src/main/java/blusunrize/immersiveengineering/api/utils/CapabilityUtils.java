/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;

public class CapabilityUtils
{
	public static LazyOptional<IItemHandler> findItemHandlerAtPos(World world, BlockPos pos, Direction side, boolean allowCart)
	{
		TileEntity neighbourTile = world.getTileEntity(pos);
		if(neighbourTile!=null)
		{
			LazyOptional<IItemHandler> cap = neighbourTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
			if(cap.isPresent())
				return cap;
		}
		if(allowCart)
		{
			if(AbstractRailBlock.isRail(world, pos))
			{
				List<Entity> list = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(pos), entity -> entity instanceof IForgeEntityMinecart);
				if(!list.isEmpty())
				{
					LazyOptional<IItemHandler> cap = list.get(world.rand.nextInt(list.size())).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
					if(cap.isPresent())
						return cap;
				}
			}
		}
		return LazyOptional.empty();
	}

	public static boolean canInsertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side)
	{
		if(!stack.isEmpty()&&inventory!=null)
		{
			return inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
					.map(handler -> {
						ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
						return temp.isEmpty()||temp.getCount() < stack.getCount();
					})
					.orElse(false);
		}
		return false;
	}

	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side)
	{
		if(!stack.isEmpty()&&inventory!=null)
		{
			return inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
					.map(handler -> {
						ItemStack temp = ItemHandlerHelper.insertItem(handler, stack.copy(), true);
						if(temp.isEmpty()||temp.getCount() < stack.getCount())
							return ItemHandlerHelper.insertItem(handler, stack, false);
						return stack;
					})
					.orElse(stack);
		}
		return stack;
	}

	public static ItemStack insertStackIntoInventory(TileEntity inventory, ItemStack stack, Direction side, boolean simulate)
	{
		if(inventory!=null&&!stack.isEmpty())
		{
			return inventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)
					.map(handler -> ItemHandlerHelper.insertItem(handler, stack.copy(), simulate))
					.orElse(stack);
		}
		return stack;
	}

	public static <T> LazyOptional<T> constantOptional(T val)
	{
		return LazyOptional.of(() -> val);
	}
}

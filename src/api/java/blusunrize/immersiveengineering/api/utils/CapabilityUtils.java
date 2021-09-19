/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.extensions.IForgeEntityMinecart;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.List;
import java.util.Objects;

public class CapabilityUtils
{
	public static <T> LazyOptional<T> findCapabilityAtPos(Capability<T> capability, Level world, BlockPos pos, Direction side, boolean allowCart)
	{
		BlockEntity neighbourTile = world.getBlockEntity(pos);
		if(neighbourTile!=null)
		{
			LazyOptional<T> cap = neighbourTile.getCapability(capability, side);
			if(cap.isPresent())
				return cap;
		}
		if(allowCart)
		{
			if(BaseRailBlock.isRail(world, pos))
			{
				List<Entity> list = world.getEntities((Entity) null, new AABB(pos), entity -> entity instanceof IForgeEntityMinecart);
				if(!list.isEmpty())
				{
					LazyOptional<T> cap = list.get(world.random.nextInt(list.size())).getCapability(capability);
					if(cap.isPresent())
						return cap;
				}
			}
		}
		return LazyOptional.empty();
	}

	public static LazyOptional<IItemHandler> findItemHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
	{
		return findCapabilityAtPos(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, world, pos, side, allowCart);
	}

	public static LazyOptional<IFluidHandler> findFluidHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
	{
		return findCapabilityAtPos(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, world, pos, side, allowCart);
	}

	public static boolean canInsertStackIntoInventory(BlockEntity inventory, ItemStack stack, Direction side)
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

	public static ItemStack insertStackIntoInventory(BlockEntity inventory, ItemStack stack, Direction side)
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

	public static ItemStack insertStackIntoInventory(BlockEntity inventory, ItemStack stack, Direction side, boolean simulate)
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
		LazyOptional<T> result = LazyOptional.of(() -> Objects.requireNonNull(val));
		// Resolve directly: There is currently a bug in the LO resolve code that can cause problems in multithreaded
		// contexts ("resolved" is set to a reference to null during the resolution on one thread, any other thread
		// trying to access the value will get null)
		result.resolve();
		return result;
	}
}

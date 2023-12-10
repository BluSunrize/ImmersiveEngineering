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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.common.extensions.IAbstractMinecartExtension;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.List;

public class CapabilityUtils
{
	public static @Nullable IItemHandler findItemHandlerAtPos(
			Level world, BlockPos pos, Direction side, boolean allowCart
	)
	{
		IItemHandler blockHandler = world.getCapability(ItemHandler.BLOCK, pos, side);
		if(blockHandler!=null||!allowCart||!BaseRailBlock.isRail(world, pos))
			return blockHandler;
		List<Entity> list = world.getEntities((Entity)null, new AABB(pos), entity -> entity instanceof IAbstractMinecartExtension);
		if(!list.isEmpty())
			return list.get(world.random.nextInt(list.size())).getCapability(ItemHandler.ENTITY);
		return null;
	}
}

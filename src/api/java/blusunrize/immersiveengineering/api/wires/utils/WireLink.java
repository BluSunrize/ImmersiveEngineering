/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record WireLink(
		ConnectionPoint cp, ResourceKey<Level> dimension, BlockPos offset, TargetingInfo target
)
{
	public WireLink
	{
		offset = offset.immutable();
	}

	public static WireLink create(ConnectionPoint cp, Level world, BlockPos offset, TargetingInfo info)
	{
		return new WireLink(cp, world.dimension(), offset, info);
	}

	@Deprecated(forRemoval = true)
	public void writeToItem(ItemStack stack)
	{
		stack.set(IEApiDataComponents.WIRE_LINK, this);
	}

	@Deprecated(forRemoval = true)
	public static WireLink readFromItem(ItemStack stack)
	{
		return stack.get(IEApiDataComponents.WIRE_LINK);
	}
}

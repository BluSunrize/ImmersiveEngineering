/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.gui.IEContainerTypes;
import blusunrize.immersiveengineering.common.gui.IEContainerTypes.ItemContainerType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MaintenanceKitItem extends IEBaseItem
{
	public MaintenanceKitItem()
	{
		super(new Properties().maxStackSize(1).defaultMaxDamage(50));
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{
		if(!world.isRemote)
			openGui(player, hand);
		ItemStack stack = player.getHeldItem(hand);
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Nullable
	@Override
	protected ItemContainerType<?> getContainerType()
	{
		return IEContainerTypes.MAINTENANCE_KIT;
	}
}

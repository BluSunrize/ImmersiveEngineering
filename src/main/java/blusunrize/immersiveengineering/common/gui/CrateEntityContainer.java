/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.entities.CrateMinecartEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nonnull;

public class CrateEntityContainer extends CrateContainer
{
	protected final Entity wrappingEntity;

	public CrateEntityContainer(MenuType<?> type, int id, Inventory inventoryPlayer, CrateMinecartEntity entity)
	{
		super(type, id, inventoryPlayer, entity.getContainedBlockEntity());
		this.wrappingEntity = entity;
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return wrappingEntity.isAlive()&&wrappingEntity.distanceToSqr(player.getX(), player.getY(), player.getZ()) < 64;
	}

}
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.entities.CrateMinecartEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

import javax.annotation.Nonnull;

public class CrateEntityContainer extends CrateContainer
{
	protected final Entity wrappingEntity;

	public CrateEntityContainer(ContainerType<?> type, int id, PlayerInventory inventoryPlayer, CrateMinecartEntity entity)
	{
		super(type, id, inventoryPlayer, entity.getContainedTileEntity());
		this.wrappingEntity = entity;
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity player)
	{
		return wrappingEntity.isAlive()&&wrappingEntity.getDistanceSq(player.getPosX(), player.getPosY(), player.getPosZ()) < 64;
	}

}
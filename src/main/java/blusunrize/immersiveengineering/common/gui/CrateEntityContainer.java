/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

import javax.annotation.Nonnull;

public class CrateEntityContainer extends CrateContainer
{
	protected final Entity wrappingEntity;

	public CrateEntityContainer(int id, PlayerInventory inventoryPlayer, WoodenCrateTileEntity tile, Entity entity)
	{
		super(id, inventoryPlayer, tile);
		this.wrappingEntity = entity;
	}

	@Override
	public ContainerType<?> getType()
	{
		return GuiHandler.getContainerTypeFor(wrappingEntity);
	}

	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity player)
	{
		return wrappingEntity.isAlive()&&wrappingEntity.getDistanceSq(player.posX, player.posY, player.posZ) < 64;
	}

}
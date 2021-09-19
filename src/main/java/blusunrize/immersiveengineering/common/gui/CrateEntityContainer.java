/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.wooden.WoodenCrateTileEntity;
import javax.annotation.Nonnull;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;

public class CrateEntityContainer extends CrateContainer
{
	protected final Entity wrappingEntity;

	public CrateEntityContainer(int id, Inventory inventoryPlayer, WoodenCrateTileEntity tile, Entity entity)
	{
		super(id, inventoryPlayer, tile);
		this.wrappingEntity = entity;
	}

	@Override
	public MenuType<?> getType()
	{
		return GuiHandler.getContainerTypeFor(wrappingEntity);
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return wrappingEntity.isAlive()&&wrappingEntity.distanceToSqr(player.getX(), player.getY(), player.getZ()) < 64;
	}

}
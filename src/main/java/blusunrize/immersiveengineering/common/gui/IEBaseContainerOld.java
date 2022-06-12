/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * For new container, and over time for the existing ones, directly extend IEBaseContainer and do not involve the BE
 * directly (following vanilla standards)
 */
@Deprecated
public class IEBaseContainerOld<T extends BlockEntity> extends IEBaseContainer
{
	public T tile;
	@Nullable
	public Container inv;

	public IEBaseContainerOld(MenuType<?> type, T tile, int id)
	{
		super(type, id);
		this.tile = tile;
		if(tile instanceof IIEInventory)
			this.inv = new BlockEntityInventory(tile, this);
	}

	@Override
	public boolean stillValid(@Nonnull Player player)
	{
		return inv!=null&&inv.stillValid(player);//Override for TE's that don't implement IIEInventory
	}

	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		if(inv!=null)
			this.inv.stopOpen(playerIn);
	}
}
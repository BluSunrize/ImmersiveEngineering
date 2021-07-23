/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.blocks.metal.TurretChemTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretTileEntity;
import blusunrize.immersiveengineering.common.items.BulletItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class TurretContainer<T extends TurretTileEntity<T>> extends IEBaseContainer<T>
{
	public TurretContainer(MenuType<?> type, int id, Inventory inventoryPlayer, T tile)
	{
		super(type, inventoryPlayer, tile, id);
		this.tile = tile;

		if(tile instanceof TurretGunTileEntity)
		{
			this.addSlot(new IESlot.Bullet(
					((TurretGunTileEntity)tile).containerHandler.orElseThrow(RuntimeException::new), 0, 134, 13, 64)
			{
				@Override
				public boolean mayPlace(ItemStack itemStack)
				{
					if(!super.mayPlace(itemStack))
						return false;
					IBullet bullet = ((BulletItem)itemStack.getItem()).getType();
					return bullet!=null&&bullet.isValidForTurret();
				}
			});
			this.addSlot(new IESlot.Output(this, this.inv, 1, 134, 49));
			slotCount = 2;
		}

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 109+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 167));
	}

	public static class ChemTurretContainer extends TurretContainer<TurretChemTileEntity> {
		public ChemTurretContainer(MenuType<?> type, int id, Inventory inventoryPlayer, TurretChemTileEntity tile)
		{
			super(type, id, inventoryPlayer, tile);
		}
	}

	public static class GunTurretContainer extends TurretContainer<TurretGunTileEntity> {
		public GunTurretContainer(MenuType<?> type, int id, Inventory inventoryPlayer, TurretGunTileEntity tile)
		{
			super(type, id, inventoryPlayer, tile);
		}
	}
}
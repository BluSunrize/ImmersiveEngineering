/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.blocks.metal.TurretBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretChemBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import blusunrize.immersiveengineering.common.items.BulletItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class TurretContainer<T extends TurretBlockEntity<T>> extends IEBaseContainerOld<T>
{
	public TurretContainer(MenuType<?> type, int id, Inventory inventoryPlayer, T tile)
	{
		super(type, tile, id);
		this.tile = tile;

		if(tile instanceof TurretGunBlockEntity gunTurret)
		{
			this.addSlot(new IESlot.Bullet(gunTurret.containerHandler.get(), 0, 134, 13, 64)
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
			ownSlotCount = 2;
		}

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 109+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 167));
		addGenericData(GenericContainerData.energy(tile.energyStorage));
	}

	public static class ChemTurretContainer extends TurretContainer<TurretChemBlockEntity> {
		public ChemTurretContainer(MenuType<?> type, int id, Inventory inventoryPlayer, TurretChemBlockEntity tile)
		{
			super(type, id, inventoryPlayer, tile);
			addGenericData(GenericContainerData.fluid(tile.tank));
			addGenericData(GenericContainerData.bool(() -> tile.ignite, b -> tile.ignite = b));
		}
	}

	public static class GunTurretContainer extends TurretContainer<TurretGunBlockEntity> {
		public GunTurretContainer(MenuType<?> type, int id, Inventory inventoryPlayer, TurretGunBlockEntity tile)
		{
			super(type, id, inventoryPlayer, tile);
			addGenericData(GenericContainerData.bool(() -> tile.expelCasings, b -> tile.expelCasings = b));
		}
	}
}
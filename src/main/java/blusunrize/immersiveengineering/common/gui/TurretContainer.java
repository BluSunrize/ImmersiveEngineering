/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretTileEntity;
import blusunrize.immersiveengineering.common.items.BulletItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TurretContainer extends IEBaseContainer<TurretTileEntity>
{
	public TurretContainer(int id, Inventory inventoryPlayer, TurretTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
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
}
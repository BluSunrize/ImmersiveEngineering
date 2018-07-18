/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurret;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurretGun;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;

public class ContainerTurret extends ContainerIEBase<TileEntityTurret>
{
	public ContainerTurret(InventoryPlayer inventoryPlayer, TileEntityTurret tile)
	{
		super(inventoryPlayer, tile);
		this.tile = tile;

		if(tile instanceof TileEntityTurretGun)
		{
			this.addSlotToContainer(new IESlot.Bullet(tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), 0, 134, 13, 64)
			{
				@Override
				public boolean isItemValid(ItemStack itemStack)
				{
					if(!super.isItemValid(itemStack))
						return false;
					String key = ItemNBTHelper.getString(itemStack, "bullet");
					IBullet bullet = BulletHandler.getBullet(key);
					return bullet!=null&&bullet.isValidForTurret();
				}
			});
			this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 134, 49));
			slotCount = 2;
		}

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 109+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 167));
	}
}
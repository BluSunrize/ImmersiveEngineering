package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntityZiplineHook;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.ZiplineHelper;

public class ItemSkyHook extends ItemIEBase
{
	public ItemSkyHook()
	{
		super("skyHook", 1);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{

	}

	public static HashMap<String, EntityZiplineHook> existingHooks = new HashMap<String, EntityZiplineHook>();

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		for(int xx=-2; xx<=2; xx++)
			for(int zz=-2; zz<=2; zz++)
				for(int yy=0; yy<=3; yy++)
				{
					TileEntity tile = world.getTileEntity((int)player.posX+xx, (int)player.posY+yy, (int)player.posZ+zz);
					if(tile!=null)
					{
						Connection line = ZiplineHelper.getTargetConnection(world, tile.xCoord,tile.yCoord,tile.zCoord, player, null);
						if(line!=null)
						{
							ZiplineHelper.spawnHook(player, tile, line);
							player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
							break;
						}
					}
				}
		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int ticks)
	{
		if(existingHooks.containsKey(player.getCommandSenderName()))
		{
			existingHooks.get(player.getCommandSenderName()).setDead();
			existingHooks.remove(player.getCommandSenderName());
		}
	}

}
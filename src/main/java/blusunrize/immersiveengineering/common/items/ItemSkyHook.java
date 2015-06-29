package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntityZiplineHook;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.ZiplineHelper;

public class ItemSkyHook extends ItemIEBase
{
	public ItemSkyHook()
	{
		super("skyhook", 1);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		list.add(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"skyhook"));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{

	}

	public static HashMap<String, EntityZiplineHook> existingHooks = new HashMap<String, EntityZiplineHook>();

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		TileEntity connector = null;
		double lastDist = 0;
		Connection line = null;
		double py = player.posY+player.getEyeHeight();
		for(int xx=-2; xx<=2; xx++)
			for(int zz=-2; zz<=2; zz++)
				for(int yy=0; yy<=3; yy++)
				{
					TileEntity tile = world.getTileEntity((int)player.posX+xx, (int)py+yy, (int)player.posZ+zz);
					if(tile!=null)
					{
						Connection con = ZiplineHelper.getTargetConnection(world, tile.xCoord,tile.yCoord,tile.zCoord, player, null);
						if(con!=null)
						{
							double d = tile.getDistanceFrom(player.posX,py,player.posZ);
							if(connector==null || d<lastDist)
							{
								connector=tile;
								lastDist=d;
								line=con;
							}
						}
					}
				}
		if(line!=null&&connector!=null)
		{
			ZiplineHelper.spawnHook(player, connector, line);
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
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
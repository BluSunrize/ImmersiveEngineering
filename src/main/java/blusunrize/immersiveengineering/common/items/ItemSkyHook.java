package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntityZiplineHook;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSkyHook extends ItemIEBase
{
	public ItemSkyHook()
	{
		super("skyHook", 1);
	}

	@SideOnly(Side.CLIENT)
	public static Connection grabableConnection;
	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		if(inHand && world.isRemote)
		{
			if(grabableConnection!=null)
				grabableConnection = null;
			for(int xx=-2; xx<=2; xx++)
				for(int zz=-2; zz<=2; zz++)
					for(int yy=0; yy<=3; yy++)
					{
						if(world.getTileEntity((int)ent.posX+xx, (int)ent.posY+yy, (int)ent.posZ+zz) instanceof IImmersiveConnectable)
						{
							List<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(world.getTileEntity((int)ent.posX+xx, (int)ent.posY+yy, (int)ent.posZ+zz)));
							if(outputs.size()>0)
							{
								Vec3 vec = ent.getLookVec();
								vec = vec.normalize();
								Connection line = null;
								for(Connection c : outputs)
								{
									if(line==null)
										line = c;
									else
									{
										Vec3 lineVec = Vec3.createVectorHelper(line.end.posX-line.start.posX, line.end.posY-line.start.posY, line.end.posZ-line.start.posZ).normalize();
										Vec3 conVec = Vec3.createVectorHelper(c.end.posX-c.start.posX, c.end.posY-c.start.posY, c.end.posZ-c.start.posZ).normalize();
										if(conVec.distanceTo(vec)<lineVec.distanceTo(vec))
											line = c;
									}
								}

								if(line!=null)
									grabableConnection = line;
							}
						}
					}
		}
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
					if(tile instanceof IImmersiveConnectable)
					{
						List<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, Utils.toCC(tile));
						if(outputs.size()>0)
						{
							Vec3 vec = player.getLookVec();
							vec = vec.normalize();
							Connection line = null;
							for(Connection c : outputs)
							{
								if(line==null)
									line = c;
								else
								{
									Vec3 lineVec = Vec3.createVectorHelper(line.end.posX-line.start.posX, line.end.posY-line.start.posY, line.end.posZ-line.start.posZ).normalize();
									Vec3 conVec = Vec3.createVectorHelper(c.end.posX-c.start.posX, c.end.posY-c.start.posY, c.end.posZ-c.start.posZ).normalize();
									if(conVec.distanceTo(vec)<lineVec.distanceTo(vec))
										line = c;
								}
							}

							if(line!=null)
							{
								ChunkCoordinates cc0 = line.end==Utils.toCC(tile)?line.start:line.end;
								ChunkCoordinates cc1 = line.end==Utils.toCC(tile)?line.end:line.start;
								double dx = cc0.posX-cc1.posX;
								double dy = cc0.posY-cc1.posY;
								double dz = cc0.posZ-cc1.posZ;

								EntityZiplineHook zip = new EntityZiplineHook(world, tile.xCoord+.5,tile.yCoord+.5,tile.zCoord+.5, line, cc0);
								zip.motionX = dx*.05f;
								zip.motionY = dy*.05f;
								zip.motionZ = dz*.05f;
								if(!world.isRemote)
									world.spawnEntityInWorld(zip);
								existingHooks.put(player.getCommandSenderName(), zip);
								player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
								player.mountEntity(zip);
							}
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
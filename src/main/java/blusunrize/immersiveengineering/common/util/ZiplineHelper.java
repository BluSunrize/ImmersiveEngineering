package blusunrize.immersiveengineering.common.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.entities.EntityZiplineHook;
import blusunrize.immersiveengineering.common.items.ItemSkyHook;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZiplineHelper
{


	@SideOnly(Side.CLIENT)
	public static Set<Connection> grabableConnections = new HashSet();

	public static Connection getTargetConnection(World world, int x, int y, int z, EntityLivingBase living, Connection invalidCon)
	{
		if(!(world.getTileEntity(x,y,z) instanceof IImmersiveConnectable))
			return null;

		List<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(world, new ChunkCoordinates(x,y,z));
		if(outputs.size()>0)
		{
			Vec3 vec = living.getLookVec();
			vec = vec.normalize();
			Connection line = null;
			for(Connection c : outputs)
				if(c!=null && !c.equals(invalidCon))
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
			return line;
		}
		return null;
	}

	public static EntityZiplineHook spawnHook(EntityLivingBase living, TileEntity start, Connection connection)
	{
		ChunkCoordinates cc0 = connection.end==Utils.toCC(start)?connection.start:connection.end;
		ChunkCoordinates cc1 = connection.end==Utils.toCC(start)?connection.end:connection.start;
		double dx = (cc0.posX-cc1.posX);
		double dy = (cc0.posY-cc1.posY);
		double dz = (cc0.posZ-cc1.posZ);
		double d = Math.sqrt(dx*dx+dz*dz+dy*dy);
		
		Vec3 moveVec = Vec3.createVectorHelper(dx/d,dy/d,dz/d);

		EntityZiplineHook hook = new EntityZiplineHook(living.worldObj, start.xCoord+.5,start.yCoord+.5,start.zCoord+.5, connection, cc0);
		
		hook.motionX = moveVec.xCoord*.5f;
		hook.motionY = moveVec.yCoord*.5f;
		hook.motionZ = moveVec.zCoord*.5f;
		if(!living.worldObj.isRemote)
			living.worldObj.spawnEntityInWorld(hook);
		ItemSkyHook.existingHooks.put(living.getCommandSenderName(), hook);
		living.mountEntity(hook);
		return hook;
	}
}

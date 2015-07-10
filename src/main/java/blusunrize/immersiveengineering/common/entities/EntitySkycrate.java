package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.IELogger;

public class EntitySkycrate extends EntitySkylineHook
{
	public ItemStack[] inventory = new ItemStack[27];

	public EntitySkycrate(World world)
	{
		super(world);
		this.setSize(1,1.5f);
	}
	public EntitySkycrate(World world, double x, double y, double z, Connection connection, ChunkCoordinates target, Vec3[] subPoints)
	{
		super(world, x, y, z, connection, target, subPoints);
		this.setSize(1,1.5f);
	}


	@Override
	public void setDead()
	{
		if(!worldObj.isRemote)
			for(ItemStack s: this.inventory)
				if(s!=null)
					worldObj.spawnEntityInWorld(new EntityItem(worldObj,posX,posY,posZ, s.copy()));

		super.setDead();
	}


	@Override
	public void reachedTarget(TileEntity end)
	{
		IELogger.info("last tick at "+System.currentTimeMillis());
		IELogger.info("killing Skycrate!");
		this.setDead();
		//		ItemStack hook = ((EntityPlayer)this.riddenByEntity).getCurrentEquippedItem();
		//		if(hook==null || !(hook.getItem() instanceof ItemSkyhook))
		//			return;
		//		Connection line = SkylineHelper.getTargetConnection(worldObj, target.posX,target.posY,target.posZ, (EntityLivingBase)this.riddenByEntity, connection);

		//		if(line!=null)
		//		{
		//			((EntityPlayer)this.riddenByEntity).setItemInUse(hook, hook.getItem().getMaxItemUseDuration(hook));
		//			SkylineHelper.spawnHook((EntityPlayer)this.riddenByEntity, end, line);
		//			//					ChunkCoordinates cc0 = line.end==target?line.start:line.end;
		//			//					ChunkCoordinates cc1 = line.end==target?line.end:line.start;
		//			//					double dx = cc0.posX-cc1.posX;
		//			//					double dy = cc0.posY-cc1.posY;
		//			//					double dz = cc0.posZ-cc1.posZ;
		//			//
		//			//					EntityZiplineHook zip = new EntityZiplineHook(worldObj, target.posX+.5,target.posY+.5,target.posZ+.5, line, cc0);
		//			//					zip.motionX = dx*.05f;
		//			//					zip.motionY = dy*.05f;
		//			//					zip.motionZ = dz*.05f;
		//			//					if(!worldObj.isRemote)
		//			//						worldObj.spawnEntityInWorld(zip);
		//			//					ItemSkyHook.existingHooks.put(this.riddenByEntity.getCommandSenderName(), zip);
		//			//					this.riddenByEntity.mountEntity(zip);
		//		}
	}
}
package blusunrize.immersiveengineering.common.entities;

import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntitySkycrate extends EntitySkylineHook
{
	public ItemStack[] inventory = new ItemStack[27];

	public EntitySkycrate(World world)
	{
		super(world);
		this.setSize(1,1.5f);
		this.width=.05f;
		this.height=.05f;
		this.renderDistanceWeight = 100.0D;
		ignoreFrustumCheck=true;
		//		this.setSize(.05f,.05f);
	}
	public EntitySkycrate(World world, double x, double y, double z, Connection connection, ChunkCoordinates target, Vec3[] subPoints)
	{
		super(world, x, y, z, connection, target, subPoints);
		this.setSize(1,1.5f);
		this.width=.05f;
		this.height=.05f;
		ignoreFrustumCheck=true;
		//		this.setSize(.05f,.05f);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance)
	{
		double d1 = 16;
		d1 *= 64.0D * this.renderDistanceWeight;
		return distance < d1 * d1;
	}

	@Override
	public void setDead()
	{
		if(!worldObj.isRemote)
		{
			ItemStack stack = new ItemStack(IEContent.blockWoodenDevice,1,4);
			NBTTagCompound nbt = new NBTTagCompound();

			NBTTagList invList = new NBTTagList();
			for(int i=0; i<this.inventory.length; i++)
				if(this.inventory[i] != null)
				{
					NBTTagCompound itemTag = new NBTTagCompound();
					itemTag.setByte("Slot", (byte)i);
					this.inventory[i].writeToNBT(itemTag);
					invList.appendTag(itemTag);
				}
			nbt.setTag("inventory", invList);

			if(!nbt.hasNoTags())
				stack.setTagCompound(nbt);
			EntityItem crate = new EntityItem(worldObj,posX,posY-.5,posZ,stack);
			crate.motionX=0;
			crate.motionY=-.25f;
			crate.motionZ=0;
			worldObj.spawnEntityInWorld(crate);
		}
		super.setDead();
	}

	@Override
	public void reachedTarget(TileEntity end)
	{
		IELogger.info("last tick at "+System.currentTimeMillis());
		IELogger.info("killing Skycrate!");

		//		ItemStack hook = ((EntityPlayer)this.riddenByEntity).getCurrentEquippedItem();
		//		if(hook==null || !(hook.getItem() instanceof ItemSkyhook))
		//			return;

		if(!(worldObj.getTileEntity(target.posX,target.posY,target.posZ) instanceof IImmersiveConnectable))
		{
			this.setDead();
			return;
		}
		ConcurrentSkipListSet<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(worldObj, target);
		if(outputs!=null && outputs.size()>0)
		{
			Vec3 vec = Vec3.createVectorHelper(connection.end.posX-connection.start.posX, connection.end.posY-connection.start.posY, connection.end.posZ-connection.start.posZ);
			//					getLookVec();
			vec = vec.normalize();
			Connection line = null;
			for(Connection c : outputs)
				if(c!=null && !c.hasSameConnectors(this.connection))
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
				ChunkCoordinates cc0 = line.end==target?line.start:line.end;
				ChunkCoordinates cc1 = line.end==target?line.end:line.start;
				IImmersiveConnectable iicStart = Utils.toIIC(cc1, worldObj);
				IImmersiveConnectable iicEnd = Utils.toIIC(cc0, worldObj);
				Vec3 vStart = Vec3.createVectorHelper(cc1.posX,cc1.posY,cc1.posZ);
				Vec3 vEnd = Vec3.createVectorHelper(cc0.posX,cc0.posY,cc0.posZ);

				if(iicStart!=null)
					vStart = Utils.addVectors(vStart, iicStart.getConnectionOffset(connection));
				if(iicEnd!=null)
					vEnd = Utils.addVectors(vEnd, iicEnd.getConnectionOffset(connection));

				Vec3[] steps = SkylineHelper.getConnectionCatenary(connection,vStart,vEnd);
				Vec3 moveVec = SkylineHelper.getSubMovementVector(vStart, steps[0], 3f);

				this.setPosition(vStart.xCoord,vStart.yCoord,vStart.zCoord);
				this.connection = line;
				this.target = cc0;
				this.subPoints = steps;
				this.targetPoint = 0;
				this.motionX = moveVec.xCoord;
				this.motionY = moveVec.yCoord;
				this.motionZ = moveVec.zCoord;
			}
			else
			{
				this.setDead();
				return;
			}
		}
		else
		{
			this.setDead();
			return;
		}




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
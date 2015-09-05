package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.entities.EntitySkycrate;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.Utils;

public class TileEntitySkycrateDispenser extends TileEntityIEBase implements ISidedInventory
//, IBlockOverlayText
{
	public TileEntitySkycrateDispenser()
	{
	}

	@Override
	public void updateEntity()
	{
		if(worldObj.getTotalWorldTime()%100==0)
		{
			ChunkCoordinates cc = new ChunkCoordinates(xCoord,yCoord+1,zCoord);
			cc.posY++;


			if(!(worldObj.getTileEntity(cc.posX,cc.posY,cc.posZ) instanceof IImmersiveConnectable))
				return;

			ConcurrentSkipListSet<Connection> outputs = ImmersiveNetHandler.INSTANCE.getConnections(worldObj, cc);
			if(outputs!=null && outputs.size()>0)
			{
				//				Vec3 vec = living.getLookVec();
				//				vec = vec.normalize();
				//				Connection line = null;
				//				for(Connection c : outputs)
				//					if(c!=null && !c.equals(invalidCon))
				//					{
				//						if(line==null)
				//							line = c;
				//						else
				//						{
				//							Vec3 lineVec = Vec3.createVectorHelper(line.end.posX-line.start.posX, line.end.posY-line.start.posY, line.end.posZ-line.start.posZ).normalize();
				//							Vec3 conVec = Vec3.createVectorHelper(c.end.posX-c.start.posX, c.end.posY-c.start.posY, c.end.posZ-c.start.posZ).normalize();
				//							if(conVec.distanceTo(vec)<lineVec.distanceTo(vec))
				//								line = c;
				//						}
				//					}
				//				return line;

				Connection connection = outputs.first();

				ChunkCoordinates cc0 = connection.end==cc?connection.start:connection.end;
				ChunkCoordinates cc1 = connection.end==cc?connection.end:connection.start;
				IImmersiveConnectable iicStart = Utils.toIIC(cc1, worldObj);
				IImmersiveConnectable iicEnd = Utils.toIIC(cc0, worldObj);
				Vec3 vStart = Vec3.createVectorHelper(cc1.posX,cc1.posY,cc1.posZ);
				Vec3 vEnd = Vec3.createVectorHelper(cc0.posX,cc0.posY,cc0.posZ);

				if(iicStart!=null)
					vStart = Utils.addVectors(vStart, iicStart.getConnectionOffset(connection));
				if(iicEnd!=null)
					vEnd = Utils.addVectors(vEnd, iicEnd.getConnectionOffset(connection));

				Vec3[] steps = SkylineHelper.getConnectionCatenary(connection,vStart,vEnd);

				double dx = (steps[0].xCoord-vStart.xCoord);
				double dy = (steps[0].yCoord-vStart.yCoord);
				double dz = (steps[0].zCoord-vStart.zCoord);
				//		double d = Math.sqrt(dx*dx+dz*dz+dy*dy);

				Vec3 moveVec = Vec3.createVectorHelper(dx,dy,dz);
				//		Vec3 moveVec = Vec3.createVectorHelper(dx/d,dy/d,dz/d);


				EntitySkycrate crate = new EntitySkycrate(worldObj, vStart.xCoord,vStart.yCoord,vStart.zCoord, connection, cc0, steps);
				float speed = .2f;
				crate.motionX = moveVec.xCoord*speed;
				crate.motionY = moveVec.yCoord*speed;
				crate.motionZ = moveVec.zCoord*speed;
				//		hook.motionX = (steps[0].xCoord-cc1.posX)*.5f;
				//		hook.motionY = (steps[0].yCoord-cc1.posY)*.5f;
				//		hook.motionZ = (steps[0].zCoord-cc1.posZ)*.5f;

				//		for(Vec3 v : steps)
				//			living.worldObj.spawnParticle("smoke", v.xCoord,v.yCoord,v.zCoord, 0,0,0 );
				crate.inventory[0] = new ItemStack(Items.apple);

				if(!worldObj.isRemote)
				{
					IELogger.info("Spawn!");
					worldObj.spawnEntityInWorld(crate);
				}
				//				ItemSkyhook.existingHooks.put(player.getCommandSenderName(), hook);
				//				player.mountEntity(hook);
			}
			//			return null;

		}
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		return null;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		return null;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		//			master().addStackToInputs(stack);
	}
	@Override
	public String getInventoryName()
	{
		return "IESkycrateDispenser";
	}
	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	@Override
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_)
	{
		return true;
	}
	@Override
	public void openInventory()
	{
	}
	@Override
	public void closeInventory()
	{

	}
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return new int[]{};
	}
	@Override
	public boolean canInsertItem(int slot, ItemStack item, int side)
	{
		return false;
	}
	@Override
	public boolean canExtractItem(int slot, ItemStack item, int side)
	{
		return false;
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
	}


	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			return true;
		}
		return false;
	}

	//	@Override
	//	public String[] getOverlayText(MovingObjectPosition mop)
	//	{
	//		return new String []{
	//				StatCollector.translateToLocal("desc.ImmersiveEngineering.info.blockSide."+ForgeDirection.getOrientation(mop.sideHit)),
	//				StatCollector.translateToLocal("desc.ImmersiveEngineering.info.oreDict."+(oreDictFilter[mop.sideHit]==-1?"off":"on"))
	//		};
	//	}
}

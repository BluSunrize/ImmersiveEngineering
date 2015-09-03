package blusunrize.immersiveengineering.common.blocks.metal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.WireType;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileEntityTransformer extends TileEntityImmersiveConnectable
{
	WireType secondCable;
	public int facing=0;
	public boolean dummy=false;
	public int postAttached=0;

	public static boolean _Immovable()
	{
		return true;
	}
	
	@Override
	protected boolean canTakeLV()
	{
		return true;
	}
	@Override
	protected boolean canTakeMV()
	{
		return true;
	}
	
	@Override
	public boolean canConnect()
	{
		return !dummy;
	}
	@Override
	public boolean canUpdate()
	{
		return false;
	}
	@Override
	public boolean isEnergyOutput()
	{
		return false;
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		return 0;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		if(secondCable!=null)
			nbt.setString("secondCable", secondCable.getUniqueName());
		nbt.setBoolean("dummy", dummy);
		nbt.setInteger("postAttached", postAttached);
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		if(nbt.hasKey("secondCable"))
			secondCable = ApiUtils.getWireTypeFromNBT(nbt, "secondCable");
		dummy = nbt.getBoolean("dummy");
		postAttached = nbt.getInteger("postAttached");
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target)
	{
		if(cableType==WireType.STEEL&&!canTakeHV())
			return false;
		if(dummy)
			if(worldObj.getTileEntity(xCoord, yCoord+1, zCoord) instanceof TileEntityTransformer)
				return ((TileEntityTransformer)worldObj.getTileEntity(xCoord, yCoord+1, zCoord)).canConnectCable(cableType, target);
			else
				return false;
		int tc = getTargetedConnector(target);
		switch(tc)
		{
		case 0:
			return limitType==null && secondCable!=cableType;
		case 1:
			return secondCable==null && limitType!=cableType;
		}
		return false;
	}
	@Override
	public void connectCable(WireType cableType, TargetingInfo target)
	{
		if(dummy)
			if(worldObj.getTileEntity(xCoord, yCoord+1, zCoord) instanceof TileEntityTransformer)
				((TileEntityTransformer)worldObj.getTileEntity(xCoord, yCoord+1, zCoord)).connectCable(cableType, target);
			else
				return;
		switch(getTargetedConnector(target))
		{
		case 0:
			if(this.limitType==null)
				this.limitType = cableType;
			break;
		case 1:
			if(secondCable==null)
				this.secondCable = cableType;
			break;
		}
	}
	@Override
	public WireType getCableLimiter(TargetingInfo target)
	{
		switch(getTargetedConnector(target))
		{
		case 0:
			return limitType;
		case 1:
			return secondCable;
		}
		return null;
	}
	@Override
	public void removeCable(Connection connection)
	{
		WireType type = connection!=null?connection.cableType:null;
		if(type==null)
		{
			limitType=null;
			secondCable=null;
		}
		if(type==limitType)
			this.limitType = null;
		if(type==secondCable)
			this.secondCable = null;
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		if(postAttached>0)
			return Vec3.createVectorHelper(.5, 1.5, .5);
		return Vec3.createVectorHelper(.5, 2.5, .5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		boolean b = con.cableType==limitType;
		if(postAttached>0)
		{
			if(b)
				return Vec3.createVectorHelper(.5+(postAttached==4?.4375: postAttached==5?-.4375: 0),1.4375,.5+(postAttached==2?.4375: postAttached==3?-.4375: 0));
			else
				return Vec3.createVectorHelper(.5+(postAttached==4?-.0625: postAttached==5?.0625: 0),.25,.5+(postAttached==2?-.0625: postAttached==3?.0625: 0));

		}
		else
		{
			double conRadius = con.cableType.getRenderDiameter()/2;
			if(facing==2)
				return Vec3.createVectorHelper(b?.8125:.1875, 1.5-conRadius, .5);
			if(facing==3)
				return Vec3.createVectorHelper(b?.1875:.8125, 1.5-conRadius, .5);
			if(facing==4)
				return Vec3.createVectorHelper(.5, 1.5-conRadius, b?.1875:.8125);
			if(facing==5)
				return Vec3.createVectorHelper(.5, 1.5-conRadius, b?.8125:.1875);
		}
		return Vec3.createVectorHelper(.5,.5,.5);
	}

	public int getTargetedConnector(TargetingInfo target)
	{
		if(postAttached>0)
		{
			if(target.hitY>=.5)
				return 0;
			else
				return 1;
		}
		else
		{
			if(facing==2)
				if(target.hitX<.5)
					return 1;
				else
					return 0;
			else if(facing==3)
				if(target.hitX<.5)
					return 0;
				else
					return 1;
			else if(facing==4)
				if(target.hitZ<.5)
					return 0;
				else
					return 1;
			else if(facing==5)
				if(target.hitZ<.5)
					return 1;
				else
					return 0;
		}
		return -1;
	}

	public WireType getLimiter(int side)
	{
		if(side==0)
			return limitType;
		return secondCable;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(Config.getBoolean("increasedRenderboxes"))
			return AxisAlignedBB.getBoundingBox(xCoord-16,yCoord-16,zCoord-16, xCoord+17,yCoord+17,zCoord+17);
		if(!dummy)
			return AxisAlignedBB.getBoundingBox(xCoord,yCoord-1,zCoord, xCoord+1,yCoord+1.5,zCoord+1);
		return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord, xCoord,yCoord,zCoord);
	}
}
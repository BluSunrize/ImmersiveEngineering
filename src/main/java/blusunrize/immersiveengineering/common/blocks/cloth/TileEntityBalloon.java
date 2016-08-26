package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ILightValue;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;

public class TileEntityBalloon extends TileEntityConnectorStructural implements ILightValue
{
	public int style = 0;
	public byte colour0 = 15;
	public byte colour1 = 15;
	public ItemStack shader;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt,descPacket);
		style = nbt.getInteger("style");
		colour0 = nbt.getByte("colour0");
		colour1 = nbt.getByte("colour1");
		shader = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("shader"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt,descPacket);
		nbt.setInteger("style",style);
		nbt.setByte("colour0",colour0);
		nbt.setByte("colour1",colour1);
		if(shader!=null)
		{
			NBTTagCompound shaderTag = shader.writeToNBT(new NBTTagCompound());
			nbt.setTag("shader", shaderTag);
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{.125f,0,.125f,.875f,.9375f,.875f};
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.worldObj.markBlockForUpdate(pos);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		int xDif = ((TileEntity)link).getPos().getX()-getPos().getX();
		int zDif = ((TileEntity)link).getPos().getZ()-getPos().getZ();
		int yDif = ((TileEntity)link).getPos().getY()-getPos().getY();
		if(yDif<0)
		{
			double dist = Math.sqrt(xDif*xDif + zDif*zDif);
			if(dist/Math.abs(yDif)<2.5)
				return new Vec3(.5,.09375,.5);
		}
		if(Math.abs(zDif)>Math.abs(xDif))
			return new Vec3(.5,.09375,zDif>0?.8125:.1875);
		else
			return new Vec3(xDif>0?.8125:.1875,.09375,.5);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(this.getPos())&&con.end!=null)? con.end.getX()-getPos().getX(): (con.end.equals(this.getPos())&& con.start!=null)?con.start.getX()-getPos().getX(): 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(this.getPos())&&con.end!=null)? con.end.getZ()-getPos().getZ(): (con.end.equals(this.getPos())&& con.start!=null)?con.start.getZ()-getPos().getZ(): 0;
		int yDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(this.getPos())&&con.end!=null)? con.end.getY()-getPos().getY(): (con.end.equals(this.getPos())&& con.start!=null)?con.start.getY()-getPos().getY(): 0;
		if(yDif<0)
		{
			double dist = Math.sqrt(xDif*xDif + zDif*zDif);
			if(dist/Math.abs(yDif)<2.5)
				return new Vec3(.5,.09375,.5);
		}
		if(Math.abs(zDif)>Math.abs(xDif))
			return new Vec3(.5,.09375,zDif>0?.78125:.21875);
		else
			return new Vec3(xDif>0?.78125:.21875,.09375,.5);
	}

	@Override
	public int getLightValue()
	{
		return 13;
	}
}
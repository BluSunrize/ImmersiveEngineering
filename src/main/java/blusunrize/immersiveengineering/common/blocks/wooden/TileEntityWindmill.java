package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasObjProperty;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDynamo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class TileEntityWindmill extends TileEntityIEBase implements ITickable, IDirectionalTile, IHasObjProperty
{
	public EnumFacing facing = EnumFacing.NORTH;
	public float prevRotation=0;
	public float rotation=0;
	public float turnSpeed=0;

	public boolean canTurn = false;

	@Override
	public boolean hasFastRenderer()
	{
		return true;
	}

	@Override
	public void update()
	{
		if(worldObj.getTotalWorldTime()%128==((getPos().getX()^getPos().getZ())&127))
			canTurn = checkArea();
		if(!canTurn)
			return;

		double mod = .00005;
		if(!worldObj.isRaining())
			mod *= .75;
		if(!worldObj.isThundering())
			mod *= .66;
		if(getPos().getY()>200)
			mod *= 2;
		else if(getPos().getY()>150)
			mod *= 1.5;
		else if(getPos().getY()>100)
			mod *= 1.25;
		else if(getPos().getY()<70)
			mod *= .33;
		mod*=getSpeedModifier();
		
		
		prevRotation = (float) (turnSpeed*mod);
		rotation += turnSpeed*mod;
		rotation %= 1;

		if(!worldObj.isRemote)
		{
			TileEntity tileEntity = worldObj.getTileEntity(pos.offset(facing));
			if(tileEntity instanceof TileEntityDynamo)
			{
				TileEntityDynamo dynamo = (TileEntityDynamo)tileEntity;
				double power = turnSpeed*mod * 400;
				dynamo.inputRotation(Math.abs(power), facing);
			}
		}
	}
	protected float getSpeedModifier()
	{
		return .5f;
	}

	public boolean checkArea()
	{
		turnSpeed=0;
		for(int hh=-6;hh<=6;hh++)
		{
			int r=Math.abs(hh)==6?1: Math.abs(hh)==5?3: Math.abs(hh)==4?4: Math.abs(hh)>1?5: 6;
			for(int ww=-r;ww<=r;ww++)
				if((hh!=0||ww!=0)&&!worldObj.isAirBlock(getPos().add((facing.getAxis()==Axis.Z?ww:0),hh,(facing.getAxis()==Axis.Z?0:ww))))
					return false;
		}

		int blocked = 0;
		for(int hh=-6;hh<=6;hh++)
		{
			int r=Math.abs(hh)==6?1: Math.abs(hh)==5?3: Math.abs(hh)==4?4: Math.abs(hh)>1?5: 6;
			for(int ww=-r;ww<=r;ww++)
			{
				for(int dd=1;dd<8;dd++)
				{
					int xx = (facing.getAxis()==Axis.Z?ww:0)+(facing==EnumFacing.WEST?-dd: facing==EnumFacing.EAST?dd: 0);
					int zz = (facing.getAxis()==Axis.Z?0:ww)+(facing==EnumFacing.NORTH?-dd: facing==EnumFacing.SOUTH?dd: 0);
					if(worldObj.isAirBlock(getPos().add(xx,hh,zz)))
						turnSpeed ++;
					else if(worldObj.getTileEntity(getPos().add(xx,hh,zz)) instanceof TileEntityWindmill)
					{
						blocked+=20;
						turnSpeed-=179;
					}
					else
						blocked++;
				}
			}
			if(blocked>100)
				return false;
		}

		return true;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		//prevRotation = nbt.getFloat("prevRotation");
		rotation = nbt.getFloat("rotation");
		turnSpeed = nbt.getFloat("turnSpeed");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		//nbt.setFloat("prevRotation", prevRotation);
		nbt.setFloat("rotation", rotation);
		nbt.setFloat("turnSpeed", turnSpeed);
	}

	@SideOnly(Side.CLIENT)
	private AxisAlignedBB renderAABB;
	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			renderAABB = new AxisAlignedBB(getPos().getX()-(facing.getAxis()==Axis.Z?6:0),getPos().getY()-6,getPos().getZ()-(facing.getAxis()==Axis.Z?0:6), getPos().getX()+(facing.getAxis()==Axis.Z?7:0),getPos().getY()+7,getPos().getZ()+(facing.getAxis()==Axis.Z?0:7));
		return renderAABB;
	}
	@Override
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}
	
	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}
	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}
	@Override
	public int getFacingLimitation()
	{
		return 2;
	}
	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}
	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	static ArrayList<String> emptyDisplayList = new ArrayList();
	@Override
	public ArrayList<String> compileDisplayList()
	{
		return emptyDisplayList;
	}
}
package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockFakeLight.TileEntityFakeLight;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;

public class TileEntityFloodlight extends TileEntityImmersiveConnectable
{
	public int energyStorage = 0;
	public boolean active = false;
	public int facing=-1;
	public int side=1;
	public float rotY=0;
	public float rotX=0;
	public List<ChunkCoordinates> fakeLights = new ArrayList();
	public List<ChunkCoordinates> lightsToBePlaced = new ArrayList();
	public List<ChunkCoordinates> lightsToBeRemoved = new ArrayList();
	final int timeBetweenSwitches = 20;
	int switchCooldown = 0;
	public int computerTurnCooldown = 0;
	public boolean computerControlled;
	public boolean computerOn;
	private boolean shouldUpdate = true;

	@Override
	public void updateEntity()
	{
		if(worldObj.isRemote)
			return;
		boolean b = active;
		boolean enabled;
		if (shouldUpdate)
		{
			updateFakeLights(true, active);
			markDirty();
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			shouldUpdate = false;
		}
		if (computerControlled)
			enabled = computerOn;
		else
			enabled = !worldObj.isBlockIndirectlyGettingPowered(xCoord,yCoord,zCoord);
		if(energyStorage>=(!active?50:5) && enabled&&switchCooldown<=0)
		{
			energyStorage-=5;
			if(!active)
				active=true;
		}
		else if(active)
		{
			active=false;
			switchCooldown = timeBetweenSwitches;
		}
		switchCooldown--;
		computerTurnCooldown--;
		if (computerTurnCooldown==0)
			synchronized (this)
			{
				this.notifyAll();
			}
		if(active!=b || worldObj.getTotalWorldTime()%512==((xCoord^zCoord)&511))
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			updateFakeLights(true,active);
		}

		if(!active)
		{
			if(!lightsToBePlaced.isEmpty())
				lightsToBePlaced.clear();
		}
		else if(!lightsToBePlaced.isEmpty()||!lightsToBeRemoved.isEmpty() && worldObj.getTotalWorldTime()%8==((xCoord^zCoord)&7))
		{
			Iterator<ChunkCoordinates> it = lightsToBePlaced.iterator();
			int timeout = 0;
			while(it.hasNext() && timeout++<16)
			{
				ChunkCoordinates cc = it.next();
				worldObj.setBlock(cc.posX,cc.posY,cc.posZ, IEContent.blockFakeLight,0, 2);
				TileEntity te = worldObj.getTileEntity(cc.posX,cc.posY,cc.posZ);
				if (te instanceof TileEntityFakeLight)
					((TileEntityFakeLight)te).floodlightCoords = new int[]{xCoord,yCoord,zCoord};

				fakeLights.add(cc);
				it.remove();
			}
			it = lightsToBeRemoved.iterator();
			timeout = 0;
			while(it.hasNext() && timeout++<16)
			{
				ChunkCoordinates cc = it.next();
				if(worldObj.getTileEntity(cc.posX, cc.posY, cc.posZ) instanceof TileEntityFakeLight)
					worldObj.setBlockToAir(cc.posX, cc.posY, cc.posZ);
				it.remove();
			}
		}
	}

	public void updateFakeLights(boolean deleteOld, boolean genNew)
	{
		Iterator<ChunkCoordinates> it = this.fakeLights.iterator();
		ArrayList<ChunkCoordinates> tempRemove = new ArrayList<ChunkCoordinates>();
		while(it.hasNext())
		{
			ChunkCoordinates cc = it.next();
			TileEntity te = worldObj.getTileEntity(cc.posX, cc.posY, cc.posZ);
			if(te instanceof TileEntityFakeLight)
			{
				if(deleteOld)
					tempRemove.add(cc);
			}
			else
				it.remove();
		}

		if(genNew)
		{
			float angle =(float)( facing==3?180: facing==4?90: facing==5?-90: 0);
			double angleX = Math.toRadians(rotX);
			double angleY = Math.toRadians(angle+rotY);
			Vec3[] rays = {
					/*Straight*/Vec3.createVectorHelper(0,0,1),
					/*U,D,L,R*/Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),
					/*Intermediate*/Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),
					/*Diagonal*/Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1),Vec3.createVectorHelper(0,0,1)};
			Matrix4 mat = new Matrix4();
			if(side==0)
				mat.rotate(Math.PI, facing<4?0:1,0,facing<4?1:0);
			else if(side!=1)
				mat.rotate(Math.PI/2, side==2?-1:side==3?1:0,0,side==5?-1:side==4?1:0);

			mat.rotate(angleY, 0,1,0);
			mat.rotate(-angleX, 1,0,0);
			mat.apply(rays[0]);

			mat.rotate(Math.PI/8, 0,1,0);
			mat.apply(rays[1]);
			mat.rotate(-Math.PI/16, 0,1,0);
			mat.apply(rays[5]);

			mat.rotate(-Math.PI/8, 0,1,0);
			mat.apply(rays[6]);
			mat.rotate(-Math.PI/16, 0,1,0);
			mat.apply(rays[2]);

			mat.rotate(Math.PI/8, 0,1,0);
			mat.rotate(Math.PI/8, 1,0,0);
			mat.apply(rays[3]);
			mat.rotate(-Math.PI/16, 1,0,0);
			mat.apply(rays[7]);

			mat.rotate(-Math.PI/8, 1,0,0);
			mat.apply(rays[8]);
			mat.rotate(-Math.PI/16, 1,0,0);
			mat.apply(rays[4]);

			mat.rotate(Math.PI/8, 1,0,0);
			mat.rotate(Math.PI/16, 1,0,0);
			mat.rotate(Math.PI/16, 0,1,0);
			mat.apply(rays[9]);
			mat.rotate(-Math.PI/8, 0,1,0);
			mat.apply(rays[10]);
			mat.rotate(-Math.PI/8, 1,0,0);
			mat.apply(rays[11]);
			mat.rotate(Math.PI/8, 0,1,0);
			mat.apply(rays[12]);

			for(int ray=0; ray<rays.length; ray++)
			{
				int offset = ray==0?0: ray<4?3: 1;
				placeLightAlongVector(rays[ray], offset, tempRemove);
			}
		}

		this.lightsToBeRemoved.addAll(tempRemove);
	}

	public void placeLightAlongVector(Vec3 vec, int offset, ArrayList<ChunkCoordinates> checklist)
	{
		Vec3 light = Vec3.createVectorHelper(xCoord+.5,yCoord+.75,zCoord+.5);
		int range = 32;
		HashSet<ChunkCoordinates> ignore = new HashSet<ChunkCoordinates>();
		ignore.add(new ChunkCoordinates(xCoord, yCoord, zCoord));
		ChunkCoordinates hit = Utils.rayTraceForFirst(Utils.addVectors(vec,light), light.addVector(vec.xCoord*range,vec.yCoord*range,vec.zCoord*range), worldObj, ignore);
		double maxDistance = hit!=null?Vec3.createVectorHelper(hit.posX+.5,hit.posY+.75,hit.posZ+.5).squareDistanceTo(light):range*range;
		for(int i=1+offset; i<=range; i++)
		{
			int xx = xCoord+(int)Math.round(vec.xCoord*i);
			int yy = yCoord+(int)Math.round(vec.yCoord*i);
			int zz = zCoord+(int)Math.round(vec.zCoord*i);
			double dist = (vec.xCoord*i*vec.xCoord*i)+(vec.yCoord*i*vec.yCoord*i)+(vec.zCoord*i*vec.zCoord*i);
			if(dist>maxDistance)
				break;
			if (yy>255||yy<0)
				continue;
			//&&worldObj.getBlockLightValue(xx,yy,zz)<12 using this makes it not work in daylight .-.
			if((xx!=xCoord||yy!=yCoord||zz!=zCoord)&&worldObj.isAirBlock(xx,yy,zz))
			{
				ChunkCoordinates cc = new ChunkCoordinates(xx,yy,zz);
				if(!checklist.remove(cc))
					lightsToBePlaced.add(cc);
				i+=2;
			}
		}
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		active = nbt.getBoolean("active");
		energyStorage = nbt.getInteger("energy");
		facing = nbt.getInteger("facing");
		side = nbt.getInteger("side");
		rotY = nbt.getFloat("rotY");
		rotX = nbt.getFloat("rotX");
		int lightAmount = nbt.getInteger("lightAmount");
		fakeLights.clear();
		for(int i=0; i<lightAmount; i++)
		{
			int[] icc = nbt.getIntArray("fakeLight_"+i);
			fakeLights.add(new ChunkCoordinates(icc[0],icc[1],icc[2]));
		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT && worldObj!=null)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setBoolean("active",active);
		nbt.setInteger("energyStorage",energyStorage);
		nbt.setInteger("facing",facing);
		nbt.setInteger("side",side);
		nbt.setFloat("rotY",rotY);
		nbt.setFloat("rotX",rotX);
		nbt.setInteger("lightAmount",fakeLights.size());
		for(int i=0; i<fakeLights.size(); i++)
		{
			ChunkCoordinates cc = fakeLights.get(i);
			nbt.setIntArray("fakeLight_"+i, new int[]{cc.posX,cc.posY,cc.posZ});
		}
	}

	@Override
	protected boolean canTakeLV()
	{
		return true;
	}
	@Override
	public boolean isEnergyOutput()
	{
		return true;
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		if(amount>0 && energyStorage<80)
		{
			int accepted = Math.min(80-energyStorage, amount);
			if(!simulate)
				energyStorage+=accepted;
			return accepted;
		}
		return 0;
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
		{
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}
	@Override
	public Vec3 getRaytraceOffset(IImmersiveConnectable link)
	{
		int xDif = ((TileEntity)link).xCoord-xCoord;
		int yDif = ((TileEntity)link).yCoord-yCoord;
		int zDif = ((TileEntity)link).zCoord-zCoord;
		double x, y, z;

		switch(side)
		{
			case 0:
			case 1:
				x = (Math.abs(xDif)>=Math.abs(zDif))? (xDif>=0)?.9375: .0625: .5;
				y = (side==0)?.9375: .0625;
				z = (Math.abs(zDif)>Math.abs(xDif))? (zDif>=0)?.9375: .0625: .5;
				break;
			case 2:
			case 3:
				x = (Math.abs(xDif)>=Math.abs(yDif))? (xDif>=0)?.9375: .0625: .5;
				y = (Math.abs(yDif)>Math.abs(xDif))? (yDif>=0)?.9375: .0625: .5;
				z = (side==2)?.9375: .0625;
				break;
			case 4:
			case 5:
			default:
				x = (side==4)?.9375: .0625;
				y = (Math.abs(yDif)>=Math.abs(zDif))? (yDif>=0)?.9375: .0625: .5;
				z = (Math.abs(zDif)>Math.abs(yDif))? (zDif>=0)?.9375: .0625: .5;
				break;
		}

		return Vec3.createVectorHelper(x,y,z);
	}
	@Override
	public Vec3 getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posX-xCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posX-xCoord: 0;
		int yDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posY-yCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posY-yCoord: 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.posZ-zCoord: (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.posZ-zCoord: 0;
		double x, y, z;

		switch(side)
		{
			case 0:
			case 1:
				x = (Math.abs(xDif)>=Math.abs(zDif))? (xDif>=0)?.9375: .0625: .5;
				y = (side==0)?.9375: .0625;
				z = (Math.abs(zDif)>Math.abs(xDif))? (zDif>=0)?.9375: .0625: .5;
				break;
			case 2:
			case 3:
				x = (Math.abs(xDif)>=Math.abs(yDif))? (xDif>=0)?.9375: .0625: .5;
				y = (Math.abs(yDif)>Math.abs(xDif))? (yDif>=0)?.9375: .0625: .5;
				z = (side==2)?.9375: .0625;
				break;
			case 4:
			case 5:
			default:
				x = (side==4)?.9375: .0625;
				y = (Math.abs(yDif)>=Math.abs(zDif))? (yDif>=0)?.9375: .0625: .5;
				z = (Math.abs(zDif)>Math.abs(yDif))? (zDif>=0)?.9375: .0625: .5;
				break;
		}
		return Vec3.createVectorHelper(x,y,z);
	}
	public void turnY(boolean down, boolean computer)
	{
		rotY += down? -11.25: 11.25;
		rotY %= 360;
		if (computer)
			computerTurnCooldown = 20;
		shouldUpdate = true;
	}
	public void turnX(boolean up, boolean computer)
	{
		float newX = (rotX+(up? 11.25f: -11.25f))%360;
		if(newX>=-11.25 && newX<=191.25)
			rotX = newX;
		if (computer)
			computerTurnCooldown = 20;
		shouldUpdate = true;
	}
	public boolean canComputerTurn()
	{
		return computerTurnCooldown<=0||!active;
	}
}
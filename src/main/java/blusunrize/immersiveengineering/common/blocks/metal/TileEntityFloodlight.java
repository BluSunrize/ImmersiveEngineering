/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockFakeLight.TileEntityFakeLight;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.*;

public class TileEntityFloodlight extends TileEntityImmersiveConnectable implements ITickable, IAdvancedDirectionalTile, IHammerInteraction, ISpawnInterdiction, IBlockBounds, IActiveState, ILightValue, IOBJModelCallback<IBlockState>
{
	public int energyStorage = 0;
	private int energyDraw = IEConfig.Machines.floodlight_energyDraw;
	private int maximumStorage = IEConfig.Machines.floodlight_maximumStorage;
	public boolean active = false;
	public boolean redstoneControlInverted = false;
	public EnumFacing facing = EnumFacing.NORTH;
	public EnumFacing side = EnumFacing.UP;
	public float rotY=0;
	public float rotX=0;
	public List<BlockPos> fakeLights = new ArrayList<>();
	public List<BlockPos> lightsToBePlaced = new ArrayList<>();
	public List<BlockPos> lightsToBeRemoved = new ArrayList<>();
	final int timeBetweenSwitches = 20;
	int switchCooldown = 0;
	private boolean shouldUpdate = true;
	public boolean computerOn = true;
	public int controllingComputers = 0;
	public int turnCooldown = 0;

	@Override
	public void update()
	{
		if(world.isRemote)
			return;
		if(turnCooldown > 0)
			turnCooldown--;
		// Needed for CC floodlight compat, specifically the waiting function
		// Commented out since there is no CC compat right now
//		if(turnCooldown == 0)
//			notifyAll();
		boolean b = active;
		boolean enabled;
		if(shouldUpdate)
		{
			lightsToBePlaced.clear();
			updateFakeLights(true, active);
			markDirty();
			this.markContainingBlockForUpdate(null);
			shouldUpdate = false;
		}

		enabled = (controllingComputers > 0 && computerOn) || (world.isBlockIndirectlyGettingPowered(getPos())>0^redstoneControlInverted);
		if(energyStorage >= (!active ? energyDraw*10 : energyDraw) && enabled && switchCooldown <= 0)
		{
			energyStorage -= energyDraw;
			if(!active)
				active=true;
		}
		else if(active)
		{
			active=false;
			switchCooldown = timeBetweenSwitches;
		}

		switchCooldown--;
		if(active != b || world.getTotalWorldTime() % 512 == ((getPos().getX() ^ getPos().getZ()) & 511))
		{
			this.markContainingBlockForUpdate(null);
			updateFakeLights(true,active);
			world.checkLightFor(EnumSkyBlock.BLOCK, getPos());
		}
		if(!active)
		{
			if(!lightsToBePlaced.isEmpty())
				lightsToBePlaced.clear();
		}
		if((!lightsToBePlaced.isEmpty()||!lightsToBeRemoved.isEmpty()) && world.getTotalWorldTime()%8==((getPos().getX()^getPos().getZ())&7))
		{
			Iterator<BlockPos> it = lightsToBePlaced.iterator();
			int timeout = 0;
			while(it.hasNext() && timeout++<Math.max(16, 32-lightsToBeRemoved.size()))
			{
				BlockPos cc = it.next();
				//				world.setBlockState(cc, Blocks.glass.getDefaultState(), 2);
				world.setBlockState(cc, IEContent.blockFakeLight.getStateFromMeta(0), 2);
				TileEntity te = world.getTileEntity(cc);
				if (te instanceof TileEntityFakeLight)
					((TileEntityFakeLight)te).floodlightCoords = new int[]{getPos().getX(),getPos().getY(),getPos().getZ()};
				fakeLights.add(cc);
				it.remove();
			}
			it = lightsToBeRemoved.iterator();
			while(it.hasNext() && timeout++<32)
			{
				BlockPos cc = it.next();
				if(Utils.getExistingTileEntity(world, cc) instanceof TileEntityFakeLight)
					world.setBlockToAir(cc);
				it.remove();
			}
		}
	}


	public void updateFakeLights(boolean deleteOld, boolean genNew)
	{
		Iterator<BlockPos> it = this.fakeLights.iterator();
		ArrayList<BlockPos> tempRemove = new ArrayList<BlockPos>();
		while(it.hasNext())
		{
			BlockPos cc = it.next();
			TileEntity te = world.getTileEntity(cc);
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
			float angle =(float)( facing==EnumFacing.NORTH?180: facing==EnumFacing.EAST?90: facing==EnumFacing.WEST?-90: 0);
			float yRotation = rotY;
			double angleX = Math.toRadians(rotX);

			Vec3d[] rays = {
					/*Straight*/new Vec3d(0,0,1),
					/*U,D,L,R*/new Vec3d(0,0,1),new Vec3d(0,0,1),new Vec3d(0,0,1),new Vec3d(0,0,1),
					/*Intermediate*/new Vec3d(0,0,1),new Vec3d(0,0,1),new Vec3d(0,0,1),new Vec3d(0,0,1),
					/*Diagonal*/new Vec3d(0,0,1),new Vec3d(0,0,1),new Vec3d(0,0,1),new Vec3d(0,0,1)};
			Matrix4 mat = new Matrix4();
			if(side==EnumFacing.DOWN)
				mat.scale(1, -1, 1);
			else if(side!=EnumFacing.UP)
			{
				angle = facing==EnumFacing.DOWN?180: facing==EnumFacing.NORTH?-90: facing==EnumFacing.SOUTH?90: angle;
				if(side.getAxis()==Axis.X)
				{
					mat.rotate(Math.PI/2,-1,0,0);
					mat.rotate(Math.PI/2, 0,0,-side.getAxisDirection().getOffset());
				}
				else
				{
					mat.rotate(Math.PI/2,-1,0,0);
					if(side==EnumFacing.SOUTH)//I dunno why south is giving me so much trouble, but this works, so who cares
					{
						mat.rotate(Math.PI, 0,0,1);
						if(facing.getAxis()==Axis.X)
							angle = -angle;
					}
				}
			}

			double angleY = Math.toRadians(angle+yRotation);
			mat.rotate(angleY, 0,1,0);
			mat.rotate(-angleX, 1,0,0);
			rays[0] = mat.apply(rays[0]);
			mat.rotate(Math.PI/8, 0,1,0);
			rays[1] = mat.apply(rays[1]);
			mat.rotate(-Math.PI/16, 0,1,0);
			rays[5] = mat.apply(rays[5]);
			mat.rotate(-Math.PI/8, 0,1,0);
			rays[6] = mat.apply(rays[6]);
			mat.rotate(-Math.PI/16, 0,1,0);
			rays[2] = mat.apply(rays[2]);
			mat.rotate(Math.PI/8, 0,1,0);
			mat.rotate(Math.PI/8, 1,0,0);
			rays[3] = mat.apply(rays[3]);
			mat.rotate(-Math.PI/16, 1,0,0);
			rays[7] = mat.apply(rays[7]);
			mat.rotate(-Math.PI/8, 1,0,0);
			rays[8] = mat.apply(rays[8]);
			mat.rotate(-Math.PI/16, 1,0,0);
			rays[4] = mat.apply(rays[4]);
			mat.rotate(Math.PI/8, 1,0,0);
			mat.rotate(Math.PI/16, 1,0,0);
			mat.rotate(Math.PI/16, 0,1,0);
			rays[9] = mat.apply(rays[9]);
			mat.rotate(-Math.PI/8, 0,1,0);
			rays[10] = mat.apply(rays[10]);
			mat.rotate(-Math.PI/8, 1,0,0);
			rays[11] = mat.apply(rays[11]);
			mat.rotate(Math.PI/8, 0,1,0);
			rays[12] = mat.apply(rays[12]);
			for(int ray=0; ray<rays.length; ray++)
			{
				int offset = ray==0?0: ray<4?3: 1;
				placeLightAlongVector(rays[ray], offset, tempRemove);
			}
		}
		this.lightsToBeRemoved.addAll(tempRemove);
	}
	public void placeLightAlongVector(Vec3d vec, int offset, ArrayList<BlockPos> checklist)
	{
		Vec3d light = new Vec3d(getPos()).addVector(.5,.75,.5);
		int range = 32;
		HashSet<BlockPos> ignore = new HashSet<BlockPos>();
		ignore.add(getPos());
		BlockPos hit = Utils.rayTraceForFirst(Utils.addVectors(vec,light), light.addVector(vec.x*range,vec.y*range,vec.z*range), world, ignore);
		double maxDistance = hit!=null?new Vec3d(hit).addVector(.5,.75,.5).squareDistanceTo(light):range*range;
		for(int i=1+offset; i<=range; i++)
		{
			BlockPos target = getPos().add(Math.round(vec.x*i), Math.round(vec.y*i), Math.round(vec.z*i));
			double dist = (vec.x*i*vec.x*i)+(vec.y*i*vec.y*i)+(vec.z*i*vec.z*i);
			if(dist>maxDistance)
				break;
			if(target.getY()>255||target.getY()<0)
				continue;
			//&&world.getBlockLightValue(xx,yy,zz)<12 using this makes it not work in daylight .-.

			if(!target.equals(getPos())&&world.isAirBlock(target))
			{
				if(!checklist.remove(target))
					lightsToBePlaced.add(target);
				i+=2;
			}
		}
	}


	@Override
	public double getInterdictionRangeSquared()
	{
		return active?1024:0;
	}

	@Override
	public void invalidate()
	{
		synchronized (EventHandler.interdictionTiles) {
			if (EventHandler.interdictionTiles.contains(this))
				EventHandler.interdictionTiles.remove(this);
		}
		super.invalidate();
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		boolean oldActive = active;
		active = nbt.getBoolean("active");
		energyStorage = nbt.getInteger("energy");
		redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		side = EnumFacing.getFront(nbt.getInteger("side"));
		rotY = nbt.getFloat("rotY");
		rotX = nbt.getFloat("rotX");
		int lightAmount = nbt.getInteger("lightAmount");
		fakeLights.clear();
		for(int i=0; i<lightAmount; i++)
		{
			int[] icc = nbt.getIntArray("fakeLight_"+i);
			fakeLights.add(new BlockPos(icc[0],icc[1],icc[2]));
		}
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT && world!=null)
			this.markContainingBlockForUpdate(null);
		if(descPacket)
		{
			controllingComputers = nbt.getBoolean("computerControlled") ? 1 : 0;
			computerOn = nbt.getBoolean("computerOn");
		}
		if (world!=null&&oldActive!=active) {
			world.checkLightFor(EnumSkyBlock.BLOCK, pos);
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setBoolean("active",active);
		nbt.setInteger("energyStorage",energyStorage);
		nbt.setBoolean("redstoneControlInverted",redstoneControlInverted);
		nbt.setInteger("facing",facing.ordinal());
		nbt.setInteger("side",side.ordinal());
		nbt.setFloat("rotY",rotY);
		nbt.setFloat("rotX",rotX);
		nbt.setInteger("lightAmount",fakeLights.size());
		for(int i=0; i<fakeLights.size(); i++)
		{
			BlockPos cc = fakeLights.get(i);
			nbt.setIntArray("fakeLight_"+i, new int[]{cc.getX(),cc.getY(),cc.getZ()});
		}
		if(descPacket)
		{
			nbt.setBoolean("computerControlled", controllingComputers > 0);
			nbt.setBoolean("computerOn", computerOn);
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
	protected boolean isRelay()
	{
		return true;
	}
	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType)
	{
		if(amount > 0 && energyStorage < maximumStorage)
		{
			if(!simulate)
			{
				int rec = Math.min(maximumStorage - energyStorage, amount);
				energyStorage+=rec;
				return rec;
			}
			return Math.min(maximumStorage - energyStorage, amount);
		}
		return 0;
	}
	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==1)
		{
			this.markContainingBlockForUpdate(null);
			world.checkLightFor(EnumSkyBlock.BLOCK, getPos());
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	@Override
	public Vec3d getConnectionOffset(Connection con)
	{
		int xDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.getX()-getPos().getX(): (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.getX()-getPos().getX(): 0;
		int yDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.getY()-getPos().getY(): (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.getY()-getPos().getY(): 0;
		int zDif = (con==null||con.start==null||con.end==null)?0: (con.start.equals(Utils.toCC(this))&&con.end!=null)? con.end.getZ()-getPos().getZ(): (con.end.equals(Utils.toCC(this))&& con.start!=null)?con.start.getZ()-getPos().getZ(): 0;
		double x, y, z;
		switch(side)
		{
			case DOWN:
			case UP:
				x = (Math.abs(xDif) >= Math.abs(zDif)) ? (xDif >= 0) ? .9375 : .0625 : .5;
				y = (side == EnumFacing.DOWN) ? .9375 : .0625;
				z = (Math.abs(zDif) > Math.abs(xDif)) ? (zDif >= 0) ? .9375 : .0625 : .5;
				break;
			case NORTH:
			case SOUTH:
				x = (Math.abs(xDif) >= Math.abs(yDif)) ? (xDif >= 0) ? .9375 : .0625 : .5;
				y = (Math.abs(yDif) > Math.abs(xDif)) ? (yDif >= 0) ? .9375 : .0625 : .5;
				z = (side == EnumFacing.NORTH) ? .9375 : .0625;
				break;
			case WEST:
			case EAST:
			default:
				x = (side == EnumFacing.WEST) ? .9375 : .0625;
				y = (Math.abs(yDif) >= Math.abs(zDif)) ? (yDif >= 0) ? .9375 : .0625 : .5;
				z = (Math.abs(zDif) > Math.abs(yDif)) ? (zDif >= 0) ? .9375 : .0625 : .5;
				break;
		}
		return new Vec3d(x,y,z);
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[] {
				side.getAxis()==Axis.X?0:.0625f,
				side.getAxis() == Axis.Y ? 0 : .0625f,
				side.getAxis() == Axis.Z ? 0 : .0625f,
				side.getAxis() == Axis.X ? 1 : .9375f,
				side.getAxis() == Axis.Y ? 1 : .9375f,
				side.getAxis() == Axis.Z ? 1 : .9375f
		};
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf)
	{
		return IEProperties.BOOLEANS[0];
	}
	@Override
	public boolean getIsActive()
	{
		return active;
	}

	@Override
	public int getLightValue()
	{
		return active?15:0;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		if(player.isSneaking() && side!=this.side)
		{
			boolean base = this.side==EnumFacing.DOWN?hitY>=.8125: this.side==EnumFacing.UP?hitY<=.1875: this.side==EnumFacing.NORTH?hitZ>=.8125: this.side==EnumFacing.UP?hitZ<=.1875: this.side==EnumFacing.WEST?hitX>=.8125: hitX<=.1875;
			if(base)
			{
				redstoneControlInverted = !redstoneControlInverted;
				ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn":"invertedOff")));
				markDirty();
				this.markContainingBlockForUpdate(null);
				return true;
			}
		}
		if(side.getAxis()==this.side.getAxis())
			turnY(player.isSneaking(), false);
		else
			turnX(player.isSneaking(), false);
		return true;
	}

	@Override
	public EnumFacing getFacing()
	{
		return side;
	}
	@Override
	public void setFacing(EnumFacing facing)
	{
		this.side=facing;
	}
	@Override
	public int getFacingLimitation()
	{
		return 0;
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
	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return false;
	}
	@Override
	public void onDirectionalPlacement(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer)
	{
		EnumFacing f = EnumFacing.fromAngle(placer.rotationYaw);
		if(f==side.getOpposite())
			f = placer.rotationPitch>0?EnumFacing.DOWN:EnumFacing.UP;
		facing = f;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldRenderGroup(IBlockState object, String group)
	{
		if("glass".equals(group))
			return MinecraftForgeClient.getRenderLayer()== BlockRenderLayer.TRANSLUCENT;
		else
			return MinecraftForgeClient.getRenderLayer()== BlockRenderLayer.SOLID;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public Optional<TRSRTransformation> applyTransformations(IBlockState object, String group, Optional<TRSRTransformation> transform)
	{
		if(!transform.isPresent())
			transform = Optional.of(new TRSRTransformation((Matrix4f)null));
		Matrix4f mat = transform.get().getMatrix();
		Vector3f transl = new Vector3f(.5f,.5f,.5f);

		double yaw = 0;
		double pitch = 0;
		double roll = 0;

		//		pitch, yaw, roll
		if(side.getAxis()==Axis.Y)
		{
			yaw = facing==EnumFacing.SOUTH?180:facing==EnumFacing.WEST?90:facing==EnumFacing.EAST?-90:0;
			if(side==EnumFacing.DOWN)
				roll = 180;
		}
		else //It's a mess, but it works!
		{
			if(side==EnumFacing.NORTH)
			{
				pitch = 90;
				yaw = 180;
			}
			if(side==EnumFacing.SOUTH)
				pitch = 90;
			if(side==EnumFacing.WEST)
			{
				pitch = 90;
				yaw = -90;
			}
			if(side==EnumFacing.EAST)
			{
				pitch = 90;
				yaw = 90;
			}

			if(facing==EnumFacing.DOWN)
				roll += 180;
			else if(side.getAxis()==Axis.X && facing.getAxis()==Axis.Z)
				roll += 90*facing.getAxisDirection().getOffset()*side.getAxisDirection().getOffset();
			else if(side.getAxis()==Axis.Z && facing.getAxis()==Axis.X)
				roll += -90*facing.getAxisDirection().getOffset()*side.getAxisDirection().getOffset();
		}

		transl.add(new Vector3f(side.getFrontOffsetX()*.125f,side.getFrontOffsetY()*.125f,side.getFrontOffsetZ()*.125f));
		if("axis".equals(group)||"light".equals(group)||"off".equals(group)||"glass".equals(group))
		{
			if(side.getAxis()==Axis.Y)
				yaw += rotY;
			else
				roll += rotY;
			if("light".equals(group)||"off".equals(group)||"glass".equals(group))
				pitch += rotX;
		}
		mat.setRotation(ClientUtils.degreeToQuaterion(pitch, yaw, roll));
		mat.setTranslation(transl);
		return Optional.of(new TRSRTransformation(mat));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getCacheKey(IBlockState object) {
		return side+":"+facing+":"+rotX+":"+rotY+":"+active;
	}

	//computer stuff
	public boolean canComputerTurn()
	{
		return turnCooldown <= 0 || !active;
	}

	public void turnX(boolean dir, boolean throwException)
	{
		if(!canComputerTurn())
		{
			if(throwException)
				throw new IllegalArgumentException("The floodlight can't turn again yet.");
			else
				return;
		}
		this.rotX = Math.min(191.25f, Math.max(-11.25f, rotX + (dir ? -11.25f : 11.25f)));
		world.addBlockEvent(getPos(), getBlockType(), 255, 0);
		turnCooldown = 20;
		shouldUpdate = true;
	}

	public void turnY(boolean dir, boolean throwException)
	{
		if(!canComputerTurn())
		{
			if(throwException)
				throw new IllegalArgumentException("The floodlight can't turn again yet.");
			else
				return;
		}
		this.rotY += dir ? -11.25 : 11.25;
		this.rotY %= 360;
		world.addBlockEvent(getPos(), getBlockType(), 255, 0);
		turnCooldown = 20;
		shouldUpdate = true;
	}
}
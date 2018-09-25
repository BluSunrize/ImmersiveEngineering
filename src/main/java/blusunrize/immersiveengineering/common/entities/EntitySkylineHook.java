/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.network.MessageSkyhookSync;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class EntitySkylineHook extends Entity
{
	public static final double GRAVITY = 10;
	private Connection connection;
	public double linePos;//Start is 0, end is 1
	public double horizontalSpeed;//Blocks per tick
	private double angle;

	public EntitySkylineHook(World world)
	{
		super(world);
		this.setSize(.125f, .125f);
		//		this.noClip=true;
	}

	//TODO vertical connections?
	public EntitySkylineHook(World world, Connection connection, double linePos)
	{
		this(world);
		setConnectionAndPos(connection, linePos);

		float f1 = MathHelper.sqrt(this.motionX*this.motionX+this.motionZ*this.motionZ);
		this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX)*180.0D/Math.PI)+90.0F;
		this.rotationPitch = (float)(Math.atan2((double)f1, this.motionY)*180.0D/Math.PI)-90.0F;
		while(this.rotationPitch-this.prevRotationPitch < -180.0F)
			this.prevRotationPitch -= 360.0F;
		while(this.rotationPitch-this.prevRotationPitch >= 180.0F)
			this.prevRotationPitch += 360.0F;
		while(this.rotationYaw-this.prevRotationYaw < -180.0F)
			this.prevRotationYaw -= 360.0F;
		while(this.rotationYaw-this.prevRotationYaw >= 180.0F)
			this.prevRotationYaw += 360.0F;
	}

	public void setConnectionAndPos(Connection c, double linePos)
	{
		this.linePos = linePos;
		this.connection = c;
		Vec3d pos = connection.getVecAt(this.linePos).add(new Vec3d(connection.start));//TODO maybe keep previous player motion?
		this.setLocationAndAngles(pos.x, pos.y, pos.z, this.rotationYaw, this.rotationPitch);
		this.setPosition(pos.x, pos.y, pos.z);
		this.angle = Math.atan2(connection.across.z, connection.across.x);
	}

	@Override
	protected void entityInit()
	{
	}


	@SideOnly(Side.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.getEntityBoundingBox().getAverageEdgeLength()*4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1*d1;
	}


	@Override
	public void onUpdate()
	{
		EntityPlayer player = null;
		List<Entity> list = this.getPassengers();
		if(!list.isEmpty()&&list.get(0) instanceof EntityPlayer)
			player = (EntityPlayer)list.get(0);
		if(connection==null)
			return;//TODO
		if(this.ticksExisted >= 1&&!world.isRemote)
		{
			IELogger.debug("init tick at "+System.currentTimeMillis());//TODO do we need this
			if(player instanceof EntityPlayerMP)
				ImmersiveEngineering.packetHandler.sendTo(new MessageSkyhookSync(this), (EntityPlayerMP)player);
		}
		boolean moved = false;
		if(player!=null)
		{
			float forward = player.moveForward;
			double strafing = player.moveStrafing;
			double playerAngle = Math.toRadians(player.rotationYaw)+Math.PI/2;
			double angleToLine = playerAngle-angle;
			double inLineDirection = Math.cos(angleToLine)*forward+Math.sin(angleToLine)*strafing;
			if(inLineDirection!=0)
			{
				double slope = connection.getSlopeAt(linePos);
				double slopeInDirection = Math.signum(inLineDirection)*slope;
				if(slopeInDirection > -.1)
				{
					double slopeAngle = Math.atan(slopeInDirection);
					horizontalSpeed = (3*horizontalSpeed+inLineDirection*.5/(2+Math.sin(slopeAngle))/(1+Math.abs(slope)))/4;
					moved = true;
				}
			}
		}
		if(!moved)//Gravity based motion
		{
			double deltaVHor;
			{
				double param = (linePos*connection.horizontalLength-connection.catOffsetX)/connection.catA;
				double ePlus = Math.exp(param);
				double eMinus = Math.exp(-param);
				deltaVHor = -GRAVITY*2*(ePlus-eMinus)/((ePlus+eMinus)*(ePlus+eMinus));//-GRAV*sinh/cosh^2
				deltaVHor *= .75;//Friction
			}
			horizontalSpeed += deltaVHor/20;
		}
		if(horizontalSpeed > 0)
			horizontalSpeed = Math.min(connection.horizontalLength*(1-linePos), horizontalSpeed);
		else
			horizontalSpeed = Math.max(-connection.horizontalLength*linePos, horizontalSpeed);
		horizontalSpeed *= .95;
		linePos += horizontalSpeed/connection.horizontalLength;
		motionX = horizontalSpeed*connection.across.x/connection.horizontalLength;
		motionZ = horizontalSpeed*connection.across.z/connection.horizontalLength;
		motionY = connection.getSlopeAt(linePos)*horizontalSpeed;
		Vec3d pos = connection.getVecAt(linePos);
		this.posX = pos.x+connection.start.getX();
		this.posY = pos.y+connection.start.getY();
		this.posZ = pos.z+connection.start.getZ();

		super.onUpdate();
		float f1 = MathHelper.sqrt(this.motionX*this.motionX+this.motionZ*this.motionZ);
		this.rotationYaw = (float)(Math.atan2(this.motionZ, this.motionX)*180.0D/Math.PI)+90.0F;
		this.rotationPitch = (float)(Math.atan2((double)f1, this.motionY)*180.0D/Math.PI)-90.0F;

		while(this.rotationPitch-this.prevRotationPitch < -180.0F)
			this.prevRotationPitch -= 360.0F;
		while(this.rotationPitch-this.prevRotationPitch >= 180.0F)
			this.prevRotationPitch += 360.0F;
		while(this.rotationYaw-this.prevRotationYaw < -180.0F)
			this.prevRotationYaw -= 360.0F;
		while(this.rotationYaw-this.prevRotationYaw >= 180.0F)
			this.prevRotationYaw += 360.0F;

		this.rotationPitch = this.prevRotationPitch+(this.rotationPitch-this.prevRotationPitch)*0.2F;
		this.rotationYaw = this.prevRotationYaw+(this.rotationYaw-this.prevRotationYaw)*0.2F;

		if(this.isInWater())
		{
			for(int j = 0; j < 4; ++j)
			{
				float f3 = 0.25F;
				this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX-this.motionX*(double)f3, this.posY-this.motionY*(double)f3, this.posZ-this.motionZ*(double)f3, this.motionX, this.motionY, this.motionZ);
			}
		}

		if(player!=null)
		{
			double dx = this.posX-this.prevPosX;
			double dy = this.posY-this.prevPosY;
			double dz = this.posZ-this.prevPosZ;
			int distTrvl = Math.round(MathHelper.sqrt(dx*dx+dy*dy+dz*dz)*100.0F);
//			if(distTrvl>0)
//				player.addStat(IEAchievements.statDistanceSkyhook, distTrvl);
			if(!world.isRemote&&SkylineHelper.isInBlock(player, world))
			{
//				setDead();
//				player.setPosition(posX-3*dx, posY-3*dy+getMountedYOffset(),posZ-3*dz);
			}

			//TODO
//			if(player instanceof EntityPlayerMP)
//				if(((EntityPlayerMP)player).getStatFile().func_150870_b(IEAchievements.statDistanceSkyhook)>100000)
//					player.triggerAchievement(IEAchievements.skyhookPro);
		}

		this.setPosition(this.posX, this.posY, this.posZ);
	}

	public void reachedTarget(TileEntity end)
	{
		this.setDead();
		IELogger.debug("last tick at "+System.currentTimeMillis());
		List<Entity> list = this.getPassengers();
		if(list.isEmpty()||!(list.get(0) instanceof EntityPlayer))
			return;
//		if(!(this.getControllingPassenger() instanceof EntityPlayer))
//			return;
//		EntityPlayer player = (EntityPlayer)this.getControllingPassenger();
		EntityPlayer player = (EntityPlayer)list.get(0);
		ItemStack hook = player.getActiveItemStack();
		if(hook.isEmpty()||!(hook.getItem() instanceof ItemSkyhook))
			return;
		Optional<Connection> line = Optional.empty();
		Set<Connection> possible = ImmersiveNetHandler.INSTANCE.getConnections(world, connection.end);
		if(possible!=null)
		{
			Vec3d look = player.getLookVec();
			line = possible.stream().filter(c -> !c.hasSameConnectors(connection))
					.max(Comparator.comparingDouble(c -> {
						Vec3d[] vertices = c.getSubVertices(world);
						Vec3d across = vertices[vertices.length-1].subtract(vertices[0]).normalize();
						return across.dotProduct(look);
					}));//Maximum dot product=>Minimum angle=>Player goes in as close to a straight line as possible
		}
		if(line.isPresent())
		{
			player.setActiveHand(player.getActiveHand());
//					setItemInUse(hook, hook.getItem().getMaxItemUseDuration(hook));
			SkylineHelper.spawnHook(player, end, line.get());
			//					ChunkCoordinates cc0 = line.end==target?line.start:line.end;
			//					ChunkCoordinates cc1 = line.end==target?line.end:line.start;
			//					double dx = cc0.posX-cc1.posX;
			//					double dy = cc0.posY-cc1.posY;
			//					double dz = cc0.posZ-cc1.posZ;
			//
			//					EntityZiplineHook zip = new EntityZiplineHook(world, target.posX+.5,target.posY+.5,target.posZ+.5, line, cc0);
			//					zip.motionX = dx*.05f;
			//					zip.motionY = dy*.05f;
			//					zip.motionZ = dz*.05f;
			//					if(!world.isRemote)
			//						world.spawnEntity(zip);
			//					ItemSkyHook.existingHooks.put(this.riddenByEntity.getCommandSenderName(), zip);
			//					this.riddenByEntity.mountEntity(zip);
		}
		else
		{
			player.motionX = motionX;
			player.motionY = motionY;
			player.motionZ = motionZ;
			IELogger.debug("player motion: "+player.motionX+","+player.motionY+","+player.motionZ);
		}
	}

	@Override
	public Vec3d getLookVec()
	{
		float f1;
		float f2;
		float f3;
		float f4;

		//		if (1 == 1.0F)
		//		{
		f1 = MathHelper.cos(-this.rotationYaw*0.017453292F-(float)Math.PI);
		f2 = MathHelper.sin(-this.rotationYaw*0.017453292F-(float)Math.PI);
		f3 = -MathHelper.cos(-this.rotationPitch*0.017453292F);
		f4 = MathHelper.sin(-this.rotationPitch*0.017453292F);
		return new Vec3d((double)(f2*f3), (double)f4, (double)(f1*f3));
		//		}
		//		else
		//		{
		//			f1 = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 1;
		//			f2 = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 1;
		//			f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
		//			f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
		//			float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		//			float f6 = MathHelper.sin(-f1 * 0.017453292F);
		//			return Vec3.createVectorHelper((double)(f4 * f5), (double)f6, (double)(f3 * f5));
		//		}
	}

	@Override
	@Nullable
	public Entity getControllingPassenger()
	{
		List<Entity> list = this.getPassengers();
		return list.isEmpty()?null: list.get(0);
	}

	@Override
	public boolean shouldRiderSit()
	{
		return false;
	}

	@Override
	public boolean isInvisible()
	{
		return true;
	}

	@Override
	public boolean canRenderOnFire()
	{
		return false;
	}

	@Override
	public boolean isPushedByWater()
	{
		return false;
	}

	@Override
	public double getMountedYOffset()
	{
		return -2;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt)
	{
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt)
	{
	}

	@Override
	public float getCollisionBorderSize()
	{
		return 0.0F;
	}

	@Override
	public float getBrightness()
	{
		return 1.0F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender()
	{
		return 15728880;
	}

	@Override
	public boolean canBeCollidedWith()
	{
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource p_70097_1_, float p_70097_2_)
	{
		this.setDead();
		return true;
	}

	@Override
	public boolean canPassengerSteer()
	{
		return true;
	}

	@Override
	protected void removePassenger(Entity passenger)
	{
		super.removePassenger(passenger);
		IELogger.logger.info("Dismounting at {}, velocity {}", getPositionVector(), new Vec3d(motionX, motionY, motionZ));
		passenger.setPositionAndUpdate(posX, posY+getMountedYOffset(), posZ);
		passenger.setVelocity(motionX, motionY, motionZ);
	}

	@Override
	public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch)
	{
		//NOP
	}

	public Connection getConnection()
	{
		return connection;
	}
}
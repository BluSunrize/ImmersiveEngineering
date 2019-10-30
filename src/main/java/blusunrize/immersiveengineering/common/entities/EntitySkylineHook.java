/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.network.MessageSkyhookSync;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class EntitySkylineHook extends Entity
{
	public static final double GRAVITY = 10;
	private static final double MAX_SPEED = 2.5;
	private static final double LIMIT_SPEED = .25;
	public static final double MOVE_SPEED_HOR = .25;
	public static final double MOVE_SPEED_VERT = .1;
	private Connection connection;
	public double linePos;//Start is 0, end is 1
	public double horizontalSpeed;//Blocks per tick, vertical iff the connection is vertical
	private double angle;
	public double friction = .99;
	public EnumHand hand;
	private boolean limitSpeed;
	private final Set<BlockPos> ignoreCollisions = new HashSet<>();


	public EntitySkylineHook(World world)
	{
		super(world);
		this.setSize(.125f, .125f);
		//		this.noClip=true;
	}

	public EntitySkylineHook(World world, Connection connection, double linePos, EnumHand hand, double horSpeed,
							 boolean limitSpeed)
	{
		this(world);
		this.hand = hand;
		this.limitSpeed = limitSpeed;
		setConnectionAndPos(connection, linePos, horSpeed);

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

	public void setConnectionAndPos(Connection c, double linePos, double speed)
	{
		this.linePos = linePos;
		this.horizontalSpeed = speed;
		this.connection = c;
		Vec3d pos = connection.getVecAt(this.linePos).add(new Vec3d(connection.start));
		this.setLocationAndAngles(pos.x, pos.y, pos.z, this.rotationYaw, this.rotationPitch);
		this.setPosition(pos.x, pos.y, pos.z);
		if(!connection.vertical)
			this.angle = Math.atan2(connection.across.z, connection.across.x);
		ignoreCollisions.clear();
		IImmersiveConnectable iicStart = ApiUtils.toIIC(c.start, world, false);
		IImmersiveConnectable iicEnd = ApiUtils.toIIC(c.end, world, false);
		if(iicStart!=null&&iicEnd!=null)
		{
			ignoreCollisions.addAll(iicStart.getIgnored(iicEnd));
			ignoreCollisions.addAll(iicEnd.getIgnored(iicStart));
		}
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
		if(ticksExisted==1 && world.isRemote)
			ImmersiveEngineering.proxy.startSkyhookSound(this);
		EntityPlayer player = null;
		List<Entity> list = this.getPassengers();
		if(!list.isEmpty()&&list.get(0) instanceof EntityPlayer)
			player = (EntityPlayer)list.get(0);
		if(connection==null||player==null||(hand!=null&&player.getHeldItem(hand).getItem()!=IEContent.itemSkyhook))
		{
			if(!world.isRemote)
				setDead();
			return;
		}
		//TODO figure out how to get the speed keeping on dismount working with less sync packets
		if(this.ticksExisted%5==0&&!world.isRemote)
			sendUpdatePacketTo(player);
		boolean moved = false;
		double inLineDirection;
		double horSpeedToUse = horizontalSpeed;
		if(connection.vertical)
			inLineDirection = -player.moveForward*Math.sin(Math.toRadians(player.rotationPitch))
					*Math.signum(connection.across.y);
		else
		{
			float forward = player.moveForward;
			double strafing = player.moveStrafing;
			double playerAngle = Math.toRadians(player.rotationYaw)+Math.PI/2;
			double angleToLine = playerAngle-angle;
			inLineDirection = Math.cos(angleToLine)*forward+Math.sin(angleToLine)*strafing;
		}
		if(inLineDirection!=0)
		{
			double slope = connection.getSlopeAt(linePos);
			double slopeInDirection = Math.signum(inLineDirection)*slope;
			double speed = MOVE_SPEED_VERT;
			double slopeFactor = 1;
			if(!connection.vertical)
			{
				//Linear interpolation w.r.t. the angle of the line
				double lambda = Math.atan(slopeInDirection)/(Math.PI/2);
				speed = lambda*MOVE_SPEED_VERT+(1-lambda)*MOVE_SPEED_HOR;
				slopeFactor = 1/Math.sqrt(1+slope*slope);
			}
			if(slopeInDirection > -.1)
			{

				horizontalSpeed = (3*horizontalSpeed+inLineDirection*speed*slopeFactor)/4;
				moved = true;
			}
		}
		BlockPos switchingAtPos = null;
		if(!moved)//Gravity based motion
		{
			double deltaVHor;
			if(connection.vertical)
				deltaVHor = -GRAVITY*Math.signum(connection.across.y);
			else
			{
				double param = (linePos*connection.horizontalLength-connection.catOffsetX)/connection.catA;
				double pos = Math.exp(param);
				double neg = 1/pos;
				double cosh = (pos+neg)/2;
				double sinh = (pos-neg)/2;
				//Formula taken from https://physics.stackexchange.com/a/83592 (x coordinate of the final vector),
				//after plugging in the correct function
				double vSquared = horizontalSpeed*horizontalSpeed*cosh*cosh*20*20;//cosh^2=1+sinh^2 and horSpeed*sinh=vertSpeed. 20 to convert from blocks/tick to block/s
				deltaVHor = -sinh/(cosh*cosh)*(GRAVITY+vSquared/(connection.catA*cosh));
			}
			horizontalSpeed += deltaVHor/(20*20);// First 20 is because this happens in one tick rather than one second, second 20 is to convert units
		}

		if(limitSpeed)
		{
			double totSpeed = getSpeed();
			double max = limitSpeed?LIMIT_SPEED: MAX_SPEED;
			if(totSpeed > max)
				horizontalSpeed *= max/totSpeed;
		}
		if(horizontalSpeed > 0)
		{
			double distToEnd = connection.horizontalLength*(1-linePos);
			if (horizontalSpeed>distToEnd)
			{
				switchingAtPos = connection.end;
				horSpeedToUse = distToEnd;
			}
		}
		else
		{
			double distToStart = -connection.horizontalLength*linePos;
			if (horizontalSpeed<distToStart)
			{
				switchingAtPos = connection.start;
				horSpeedToUse = distToStart;
			}
		}
		horizontalSpeed *= friction;
		linePos += horSpeedToUse/connection.horizontalLength;
		Vec3d pos = connection.getVecAt(linePos);
		double posXTemp = pos.x+connection.start.getX();
		double posYTemp = pos.y+connection.start.getY();
		double posZTemp = pos.z+connection.start.getZ();
		motionX = posXTemp-posX;
		motionZ = posZTemp-posZ;
		motionY = posYTemp-posY;
		if(!isValidPosition(posXTemp, posYTemp, posZTemp, player))
		{
			setDead();
			return;
		}
		posX = posXTemp;
		posY = posYTemp;
		posZ = posZTemp;

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

		double dx = this.posX-this.prevPosX;
		double dy = this.posY-this.prevPosY;
		double dz = this.posZ-this.prevPosZ;
		int distTrvl = Math.round(MathHelper.sqrt(dx*dx+dy*dy+dz*dz)*100.0F);
//			if(distTrvl>0)
//				player.addStat(IEAchievements.statDistanceSkyhook, distTrvl);

		//TODO
//			if(player instanceof EntityPlayerMP)
//				if(((EntityPlayerMP)player).getStatFile().func_150870_b(IEAchievements.statDistanceSkyhook)>100000)
//					player.triggerAchievement(IEAchievements.skyhookPro);

		this.setPosition(this.posX, this.posY, this.posZ);
		if(switchingAtPos!=null)
			switchConnection(switchingAtPos, player, horSpeedToUse);
	}

	private void sendUpdatePacketTo(EntityPlayer player)
	{
		if(player instanceof EntityPlayerMP)
			ImmersiveEngineering.packetHandler.sendTo(new MessageSkyhookSync(this), (EntityPlayerMP)player);
	}

	public void switchConnection(BlockPos posForSwitch, EntityPlayer player, double lastHorSpeed)
	{
		Optional<Connection> line = Optional.empty();
		Set<Connection> possible = ImmersiveNetHandler.INSTANCE.getConnections(world, posForSwitch);
		if(possible!=null)
		{
			Vec3d look = player.getLookVec();
			line = possible.stream().filter(c -> !c.hasSameConnectors(connection))
					.filter(c->
							c.getSubVertices(world)[0].distanceTo(
									getPositionVector().subtract(c.start.getX(), c.start.getY(), c.start.getZ())) < .6)
					.max(Comparator.comparingDouble(c -> {
						c.getSubVertices(world);
						return c.across.normalize().dotProduct(look);
					}));//Maximum dot product=>Minimum angle=>Player goes in as close to a straight line as possible
		}
		if (line.isPresent())
		{
			Connection newCon = line.get();
			newCon.getSubVertices(world);

			double oldSpeedPerHor = getSpeedPerHor(connection, posForSwitch.equals(connection.start)?0: 1);
			double newSpeedPerHor = getSpeedPerHor(newCon, 0);
			double horConversionFactor = oldSpeedPerHor/newSpeedPerHor;
			setConnectionAndPos(newCon, (Math.abs(horizontalSpeed-lastHorSpeed))*horConversionFactor,
					Math.abs(horizontalSpeed)*horConversionFactor);
			sendUpdatePacketTo(player);
		}
		else
			setDead();
	}

	private static double getSpeedPerHor(Connection connection, double pos)
	{
		if(connection.vertical)
			return 1;
		else
		{
			double slope = connection.getSlopeAt(pos);
			return Math.sqrt(slope*slope+1);
		}
	}

	public boolean isValidPosition(double x, double y, double z, @Nonnull EntityLivingBase player)
	{
		final double tolerance = connection.vertical?5: 10;//TODO are these values good?
		double radius = player.width/2;
		double height = player.height;
		double yOffset = getMountedYOffset()+player.getYOffset();
		AxisAlignedBB playerBB = new AxisAlignedBB(x-radius, y+yOffset, z-radius, x+radius, y+yOffset+height, z+radius);
		double playerHeight = playerBB.maxY-playerBB.minY;
		AxisAlignedBB feet = new AxisAlignedBB(playerBB.minX, playerBB.minY, playerBB.minZ,
				playerBB.maxX, playerBB.minY+.05*playerHeight, playerBB.maxZ);
		List<AxisAlignedBB> boxes = SkylineHelper.getCollisionBoxes(player, playerBB, world, ignoreCollisions);
		// Heuristic to prevent dragging players through blocks too much, but also keep most setups working
		// Allow positions where the intersection is less than 10% of the player BB volume
		double totalCollisionVolume = 0;
		double totalCollisionArea = 0;
		double playerVolume = getVolume(playerBB);
		double playerArea = playerVolume/playerHeight;
		for(AxisAlignedBB box : boxes)
		{
			AxisAlignedBB intersection = box.intersect(playerBB);
			totalCollisionVolume += getVolume(intersection);
			if(totalCollisionVolume*tolerance > playerVolume)
				return false;
			if(!connection.vertical&&box.intersects(feet))
			{
				AxisAlignedBB feetIntersect = box.intersect(feet);
				totalCollisionArea += (feetIntersect.maxX-feetIntersect.minX)*(feetIntersect.maxZ-feetIntersect.minZ);
				if(totalCollisionArea > .5*playerArea)
					return false;
			}
		}
		return true;
	}

	private double getVolume(AxisAlignedBB box)
	{
		return (box.maxX-box.minX)*(box.maxY-box.minY)*(box.maxZ-box.minZ);
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
		return false;
	}

	private void handleDismount(Entity passenger)
	{
		passenger.setPositionAndUpdate(posX, posY+getMountedYOffset()+passenger.getYOffset(), posZ);
		passenger.motionX = motionX;
		passenger.motionY = motionY;
		passenger.motionZ = motionZ;
		if(motionY < 0)
		{
			passenger.fallDistance = SkylineHelper.fallDistanceFromSpeed(motionY);
			passenger.onGround = false;
		}
		if(passenger.hasCapability(SKYHOOK_USER_DATA, EnumFacing.UP))
			Objects.requireNonNull(passenger.getCapability(SKYHOOK_USER_DATA, EnumFacing.UP))
					.release();
		if(hand!=null&&passenger instanceof EntityPlayer)
		{
			ItemStack held = ((EntityPlayer)passenger).getHeldItem(hand);
			if(held.getItem()==IEContent.itemSkyhook)
				((EntityPlayer)passenger).getCooldownTracker().setCooldown(IEContent.itemSkyhook,
						10);
		}
	}

	@Override
	protected void removePassenger(Entity passenger)
	{
		super.removePassenger(passenger);
		if (!world.isRemote)
			ApiUtils.addFutureServerTask(world, () -> handleDismount(passenger));
		else
			ApiUtils.callFromOtherThread(Minecraft.getMinecraft()::addScheduledTask, ()->handleDismount(passenger));
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

	public double getSpeed()
	{
		if(connection==null)
			return 0;
		if(connection.vertical)
		{
			return Math.abs(horizontalSpeed);//In this case vertical speed
		}
		else
		{
			double slope = connection.getSlopeAt(linePos);
			return Math.abs(horizontalSpeed)*Math.sqrt(1+slope*slope);
		}
	}
}

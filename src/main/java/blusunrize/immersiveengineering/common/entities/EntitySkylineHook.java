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
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemSkyhook;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.network.MessageSkyhookSync;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
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
	public double friction = .75;
	public double upwardSpeed = .5;
	public String owner;
	public EnumHand hand;

	public EntitySkylineHook(World world)
	{
		super(world);
		this.setSize(.125f, .125f);
		//		this.noClip=true;
	}

	//TODO vertical connections?
	public EntitySkylineHook(World world, Connection connection, double linePos, String owner, EnumHand hand, double horSpeed)
	{
		this(world);
		this.hand = hand;
		this.owner = owner;
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
		this.angle = Math.atan2(connection.across.z, connection.across.x);
		if (!world.isRemote)
			IELogger.logger.info("New conn: a={}, Ox={}, lengthHor={}", c.catA, c.catOffsetX, c.horizontalLength);
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
		if(connection==null||owner==null||player==null||player.getHeldItem(hand).getItem()!=IEContent.itemSkyhook)
		{
			if(!world.isRemote)
				setDead();
			return;
		}
		if(this.ticksExisted >= 1&&!world.isRemote)
		{
			IELogger.debug("init tick at "+System.currentTimeMillis());//TODO do we need this
			sendUpdatePacketTo(player);
		}
		boolean moved = false;
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
				horizontalSpeed = (3*horizontalSpeed+inLineDirection*upwardSpeed/(2+Math.sin(slopeAngle))/(1+Math.abs(slope)))/4;
				moved = true;
			}
		}
		BlockPos switchingAtPos = null;
		double horSpeedToUse = horizontalSpeed;
		if(!moved)//Gravity based motion
		{
			double deltaVHor;
			{
				double param = (linePos*connection.horizontalLength-connection.catOffsetX)/connection.catA;
				double pos = Math.exp(param);
				double neg = Math.exp(-param);
				double cosh = (pos+neg)/2;
				double sinh = (pos-neg)/2;
				double vSquared = horizontalSpeed*horizontalSpeed*cosh*cosh*20*20;//cosh^2=1+sinh^2 and horSpeed*sinh=vertSpeed. 20 to convert from blocks/tick to block/s
				deltaVHor = -sinh/(cosh*cosh)*(GRAVITY+vSquared/(connection.catA*cosh));
				//deltaVHor *= friction;
			}
			horizontalSpeed += deltaVHor/(20*20);// First 20 is because this happens in one tick rather than one second, second 20 is to convert units
		}
		//horizontalSpeed *= .95;
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
		linePos += horSpeedToUse/connection.horizontalLength;
		motionX = horizontalSpeed*connection.across.x/connection.horizontalLength;
		motionZ = horizontalSpeed*connection.across.z/connection.horizontalLength;
		motionY = connection.getSlopeAt(linePos)*horizontalSpeed;
		Vec3d pos = connection.getVecAt(linePos);
		double posXTemp = pos.x+connection.start.getX();
		double posYTemp = pos.y+connection.start.getY();
		double posZTemp = pos.z+connection.start.getZ();
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
									getPositionVector().subtract(c.start.getX(), c.start.getY(), c.start.getZ()))<.1)//TODO is this a good threshold
					.max(Comparator.comparingDouble(c -> {
						c.getSubVertices(world);
						return c.across.normalize().dotProduct(look);
					}));//Maximum dot product=>Minimum angle=>Player goes in as close to a straight line as possible
		}
		IELogger.logger.info("Switching conn at {}, possible: {}, chosen: {}", posForSwitch, possible, line);
		if (line.isPresent())
		{
			Connection newCon = line.get();
			newCon.getSubVertices(world);
			double slopeOld = connection.getSlopeAt(linePos);
			double slopeNew = newCon.getSlopeAt(0);
			double horConversionFactor = Math.sqrt((1+slopeOld*slopeOld)/(1+slopeNew*slopeNew));
			double oldHorSpeed = horizontalSpeed;
			//TODO is this broken?
			setConnectionAndPos(newCon, (Math.abs(oldHorSpeed-lastHorSpeed))*horConversionFactor,
					Math.abs(horizontalSpeed)*horConversionFactor);
			IELogger.logger.info("Changed connection. Old slope {}, new slope {}, conv factor {}, hor speed changed from {} to {}",
					slopeOld, slopeNew, horConversionFactor, oldHorSpeed, horizontalSpeed);
			sendUpdatePacketTo(player);
		}
		else
			setDead();
	}

	private boolean isValidPosition(double x, double y, double z, @Nonnull EntityPlayer player)
	{
		double radius = player.width/2;
		double height = player.height;
		double yOffset = getMountedYOffset()+player.getYOffset();
		AxisAlignedBB playerBB = new AxisAlignedBB(x-radius, y+yOffset, z-radius, x+radius, y+yOffset+height, z+radius);
		List<AxisAlignedBB> boxes = world.getCollisionBoxes(player, playerBB);
		return boxes.isEmpty();
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

	private void handleDismount(Entity passenger)
	{
		IELogger.logger.info("Dismounting at {}, velocity {}", getPositionVector(),
				new Vec3d(motionX, motionY, motionZ));
		passenger.setPositionAndUpdate(posX, posY+getMountedYOffset(), posZ);
		passenger.motionX = motionX;
		passenger.motionY = motionY;
		passenger.motionZ = motionZ;
		if(motionY < 0)
		{
			double fallTime = -20*motionY/GRAVITY;
			//The fall distance is reset when the player stops riding the skyhook
			passenger.fallDistance = (float)(.5*GRAVITY*fallTime*fallTime);
			passenger.onGround = false;
			IELogger.logger.info("Fall speed {}, time {}, distance {}", motionY, fallTime, passenger.fallDistance);
		}
		if(owner!=null)
			ItemSkyhook.existingHooks.remove(owner);//TODO will this cause race conditions?
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
}
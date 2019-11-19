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
import blusunrize.immersiveengineering.api.CapabilitySkyhookData.SkyhookUserData;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.network.MessageSkyhookSync;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class SkylineHookEntity extends Entity
{
	public static final EntityType<SkylineHookEntity> TYPE = Builder
			.<SkylineHookEntity>create(SkylineHookEntity::new, EntityClassification.MISC)
			.size(.125F, .125F)
			.build(ImmersiveEngineering.MODID+":skyline_hook");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "skyline_hook");
	}

	public static final double GRAVITY = 10;
	private static final double MAX_SPEED = 2.5;
	private static final double LIMIT_SPEED = .25;
	public static final double MOVE_SPEED_HOR = .25;
	public static final double MOVE_SPEED_VERT = .1;
	private Connection connection;
	public ConnectionPoint start;
	public double linePos;//Start is 0, end is 1
	public double horizontalSpeed;//Blocks per tick, vertical iff the connection is vertical
	private double angle;
	public double friction = .99;
	public Hand hand;
	private boolean limitSpeed;
	private final Set<BlockPos> ignoreCollisions = new HashSet<>();


	public SkylineHookEntity(EntityType<SkylineHookEntity> type, World world)
	{
		super(type, world);
		//		this.noClip=true;
	}

	public SkylineHookEntity(World world, Connection connection, ConnectionPoint start, double linePos, Hand hand, double horSpeed,
							 boolean limitSpeed)
	{
		this(TYPE, world);
		this.hand = hand;
		this.limitSpeed = limitSpeed;
		setConnectionAndPos(connection, start, linePos, horSpeed);

		Vec3d motion = getMotion();
		float f1 = MathHelper.sqrt(motion.x*motion.x+motion.z*motion.z);
		this.rotationYaw = (float)(Math.atan2(motion.z, motion.x)*180.0D/Math.PI)+90.0F;
		this.rotationPitch = (float)(Math.atan2((double)f1, motion.y)*180.0D/Math.PI)-90.0F;
		while(this.rotationPitch-this.prevRotationPitch < -180.0F)
			this.prevRotationPitch -= 360.0F;
		while(this.rotationPitch-this.prevRotationPitch >= 180.0F)
			this.prevRotationPitch += 360.0F;
		while(this.rotationYaw-this.prevRotationYaw < -180.0F)
			this.prevRotationYaw -= 360.0F;
		while(this.rotationYaw-this.prevRotationYaw >= 180.0F)
			this.prevRotationYaw += 360.0F;
	}

	public void setConnectionAndPos(Connection c, ConnectionPoint start, double linePos, double speed)
	{
		c.generateCatenaryData(world);
		this.linePos = linePos;
		this.horizontalSpeed = speed;
		this.connection = c;
		this.start = start;
		Vec3d pos = connection.getPoint(this.linePos, start);
		this.setLocationAndAngles(pos.x, pos.y, pos.z, this.rotationYaw, this.rotationPitch);
		this.setPosition(pos.x, pos.y, pos.z);
		if(!connection.catData.isVertical())
			this.angle = Math.atan2(connection.catData.getDeltaZ(), connection.catData.getDeltaX());
		ignoreCollisions.clear();
		LocalWireNetwork net = GlobalWireNetwork.getNetwork(world).getLocalNet(start);
		IImmersiveConnectable iicStart = ApiUtils.toIIC(net.getConnector(start), world, false);
		IImmersiveConnectable iicEnd = ApiUtils.toIIC(net.getConnector(c.getOtherEnd(start)), world, false);
		if(iicStart!=null&&iicEnd!=null)
		{
			ignoreCollisions.addAll(iicStart.getIgnored(iicEnd));
			ignoreCollisions.addAll(iicEnd.getIgnored(iicStart));
		}
	}

	@Override
	protected void registerData()
	{
	}


	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean isInRangeToRenderDist(double p_70112_1_)
	{
		double d1 = this.getBoundingBox().getAverageEdgeLength()*4.0D;
		d1 *= 64.0D;
		return p_70112_1_ < d1*d1;
	}

	@Override
	public void tick()
	{
		if(ticksExisted==1)
			ImmersiveEngineering.proxy.startSkyhookSound(this);
		PlayerEntity player = null;
		List<Entity> list = this.getPassengers();
		if(!list.isEmpty()&&list.get(0) instanceof PlayerEntity)
			player = (PlayerEntity)list.get(0);
		if(connection==null||player==null||(hand!=null&&player.getHeldItem(hand).getItem()!=Misc.skyhook))
		{
			if(!world.isRemote)
				remove();
			return;
		}
		//TODO figure out how to get the speed keeping on dismount working with less sync packets
		if(this.ticksExisted%5==0&&!world.isRemote)
			sendUpdatePacketTo(player);
		boolean moved = false;
		double inLineDirection;
		double horSpeedToUse = horizontalSpeed;
		if(connection.catData.isVertical())
			inLineDirection = -player.moveForward*Math.sin(Math.toRadians(player.rotationPitch))
					*Math.signum(connection.catData.getDeltaY());
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
			double slope = connection.catData.getSlope(linePos);
			double slopeInDirection = Math.signum(inLineDirection)*slope;
			double speed = MOVE_SPEED_VERT;
			double slopeFactor = 1;
			if(!connection.catData.isVertical())
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
		ConnectionPoint switchingAtPos = null;
		if(!moved)//Gravity based motion
		{
			double deltaVHor;
			if(connection.catData.isVertical())
				deltaVHor = -GRAVITY*Math.signum(connection.catData.getDeltaY());
			else
			{
				double param = (linePos*connection.catData.getHorLength()-connection.catData.getOffsetX())/connection.catData.getA();
				double pos = Math.exp(param);
				double neg = 1/pos;
				double cosh = (pos+neg)/2;
				double sinh = (pos-neg)/2;
				//Formula taken from https://physics.stackexchange.com/a/83592 (x coordinate of the final vector),
				//after plugging in the correct function
				double vSquared = horizontalSpeed*horizontalSpeed*cosh*cosh*20*20;//cosh^2=1+sinh^2 and horSpeed*sinh=vertSpeed. 20 to convert from blocks/tick to block/s
				deltaVHor = -sinh/(cosh*cosh)*(GRAVITY+vSquared/(connection.catData.getA()*cosh));
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
			double distToEnd = connection.catData.getHorLength()*(1-linePos);
			if (horizontalSpeed>distToEnd)
			{
				switchingAtPos = connection.getOtherEnd(start);
				horSpeedToUse = distToEnd;
			}
		}
		else
		{
			double distToStart = -connection.catData.getHorLength()*linePos;
			if (horizontalSpeed<distToStart)
			{
				switchingAtPos = start;
				horSpeedToUse = distToStart;
			}
		}
		horizontalSpeed *= friction;
		linePos += horSpeedToUse/connection.catData.getHorLength();
		Vec3d pos = connection.getPoint(linePos, start);
		setMotion(pos.x-posX, pos.z-posZ, pos.y-posY);
		if(!isValidPosition(pos.x, pos.y, pos.z, player))
		{
			remove();
			return;
		}
		posX = pos.x;
		posY = pos.y;
		posZ = pos.z;

		super.tick();
		Vec3d motion = getMotion();
		float f1 = MathHelper.sqrt(motion.x*motion.x+motion.z*motion.z);
		this.rotationYaw = (float)(Math.atan2(motion.z, motion.x)*180.0D/Math.PI)+90.0F;
		this.rotationPitch = (float)(Math.atan2((double)f1, motion.y)*180.0D/Math.PI)-90.0F;

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
				this.world.addParticle(ParticleTypes.BUBBLE,
						this.posX-motion.x*(double)f3,
						this.posY-motion.y*(double)f3,
						this.posZ-motion.z*(double)f3,
						motion.x,
						motion.y,
						motion.z);
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

	private void sendUpdatePacketTo(PlayerEntity player)
	{
		if(player instanceof ServerPlayerEntity)
			ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new MessageSkyhookSync(this));
	}

	public void switchConnection(ConnectionPoint posForSwitch, PlayerEntity player, double lastHorSpeed)
	{
		Optional<Connection> line = Optional.empty();
		LocalWireNetwork net = GlobalWireNetwork.getNetwork(world).getLocalNet(posForSwitch);
		Collection<Connection> possible = net.getConnections(posForSwitch);
		if(possible!=null)
		{
			Vec3d look = player.getLookVec();
			line = possible.stream().filter(c -> !c.hasSameConnectors(connection))
					.max(Comparator.comparingDouble(c -> {
						c.generateCatenaryData(world);
						return c.catData.getDelta().normalize().dotProduct(look);
					}));//Maximum dot product=>Minimum angle=>Player goes in as close to a straight line as possible
		}
		if (line.isPresent())
		{
			Connection newCon = line.get();

			double oldSpeedPerHor = getSpeedPerHor(connection, posForSwitch, 0);
			double newSpeedPerHor = getSpeedPerHor(newCon, posForSwitch, 0);
			double horConversionFactor = oldSpeedPerHor/newSpeedPerHor;
			setConnectionAndPos(newCon, posForSwitch,
					(Math.abs(horizontalSpeed-lastHorSpeed))*horConversionFactor, Math.abs(horizontalSpeed)*horConversionFactor);
			sendUpdatePacketTo(player);
		}
		else
			remove();
	}

	private static double getSpeedPerHor(Connection connection, ConnectionPoint start, double pos)
	{
		if(connection.catData.isVertical())
			return 1;
		else
		{
			double slope = connection.getSlope(pos, start);
			return Math.sqrt(slope*slope+1);
		}
	}

	public boolean isValidPosition(double x, double y, double z, @Nonnull LivingEntity player)
	{
		final double tolerance = connection.catData.isVertical()?5: 10;//TODO are these values good?
		double radius = player.getWidth()/2;
		double height = player.getHeight();
		double yOffset = getMountedYOffset()+player.getYOffset();
		AxisAlignedBB playerBB = new AxisAlignedBB(x-radius, y+yOffset, z-radius, x+radius, y+yOffset+height, z+radius);
		double playerHeight = playerBB.maxY-playerBB.minY;
		AxisAlignedBB feet = new AxisAlignedBB(playerBB.minX, playerBB.minY, playerBB.minZ,
				playerBB.maxX, playerBB.minY+.05*playerHeight, playerBB.maxZ);
		List<VoxelShape> shapes = SkylineHelper.getCollisionBoxes(player, playerBB, world, ignoreCollisions);
		// Heuristic to prevent dragging players through blocks too much, but also keep most setups working
		// Allow positions where the intersection is less than 10% of the player BB volume
		double totalCollisionVolume = 0;
		double totalCollisionArea = 0;
		VoxelShape playerShape = VoxelShapes.create(playerBB);
		double playerVolume = getVolume(playerShape);
		double playerArea = playerVolume/playerHeight;
		VoxelShape feetShape = VoxelShapes.create(feet);
		for(VoxelShape shape : shapes)
		{
			VoxelShape intersection = VoxelShapes.combine(playerShape, shape, IBooleanFunction.AND);
			totalCollisionVolume += getVolume(intersection);
			if(totalCollisionVolume*tolerance > playerVolume)
				return false;
			if(!connection.catData.isVertical()&&VoxelShapes.compare(feetShape, shape, IBooleanFunction.AND))
			{
				VoxelShape feetIntersectShape = VoxelShapes.combine(feetShape, shape, IBooleanFunction.AND);
				for(AxisAlignedBB feetIntersect : feetIntersectShape.toBoundingBoxList())
					totalCollisionArea += (feetIntersect.maxX-feetIntersect.minX)*(feetIntersect.maxZ-feetIntersect.minZ);
				if(totalCollisionArea > .5*playerArea)
					return false;
			}
		}
		return true;
	}

	private double getVolume(VoxelShape shape)
	{
		return shape
				.toBoundingBoxList()
				.stream()
				.mapToDouble(box -> (box.maxX-box.minX)*(box.maxY-box.minY)*(box.maxZ-box.minZ))
				.sum();
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
	protected void writeAdditional(CompoundNBT nbt)
	{
	}

	@Override
	protected void readAdditional(CompoundNBT nbt)
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

	@OnlyIn(Dist.CLIENT)
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
		this.remove();
		return true;
	}

	@Override
	public boolean canPassengerSteer()
	{
		return false;
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	private void handleDismount(Entity passenger)
	{
		passenger.setPositionAndUpdate(posX, posY+getMountedYOffset()+passenger.getYOffset(), posZ);
		passenger.setMotion(getMotion());
		if(getMotion().y < 0)
		{
			passenger.fallDistance = SkylineHelper.fallDistanceFromSpeed(getMotion().y);
			passenger.onGround = false;
		}
		passenger.getCapability(SKYHOOK_USER_DATA, Direction.UP).ifPresent(SkyhookUserData::release);
		if(hand!=null&&passenger instanceof PlayerEntity)
		{
			ItemStack held = ((PlayerEntity)passenger).getHeldItem(hand);
			if(held.getItem()==Misc.skyhook)
				((PlayerEntity)passenger).getCooldownTracker().setCooldown(Misc.skyhook,
						10);
		}
	}

	@Override
	protected void removePassenger(Entity passenger)
	{
		super.removePassenger(passenger);
		if (!world.isRemote)
			ApiUtils.addFutureServerTask(world, () -> handleDismount(passenger));
		//TODO else
		//	ApiUtils.callFromOtherThread(Minecraft.getInstance()::addScheduledTask, () -> handleDismount(passenger));
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
		if(connection.catData.isVertical())
		{
			return Math.abs(horizontalSpeed);//In this case vertical speed
		}
		else
		{
			double slope = connection.getSlope(linePos, start);
			return Math.abs(horizontalSpeed)*Math.sqrt(1+slope*slope);
		}
	}
}
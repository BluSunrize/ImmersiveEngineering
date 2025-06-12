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
import blusunrize.immersiveengineering.api.utils.PlayerUtils;
import blusunrize.immersiveengineering.api.wires.*;
import blusunrize.immersiveengineering.common.network.MessageSkyhookSync;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEStats;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class SkylineHookEntity extends Entity
{
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
	public InteractionHand hand;
	private boolean limitSpeed;
	private float slopeModifier;
	private static final EntityDataAccessor<Float> SLOPE_MODIFIER = SynchedEntityData.defineId(SkylineHookEntity.class, EntityDataSerializers.FLOAT);

	private final Set<BlockPos> ignoreCollisions = new HashSet<>();


	public SkylineHookEntity(EntityType<SkylineHookEntity> type, Level world)
	{
		super(type, world);
		//		this.noClip=true;
	}

	public SkylineHookEntity(Level world, Connection connection, ConnectionPoint start, double linePos, InteractionHand hand, double horSpeed,
							 boolean limitSpeed, float slopeModifier)
	{
		this(IEEntityTypes.SKYLINE_HOOK.get(), world);
		this.hand = hand;
		this.limitSpeed = limitSpeed;
		this.slopeModifier = slopeModifier;
		this.setSlopeModifier(slopeModifier);
		setConnectionAndPos(connection, start, linePos, horSpeed);

		Vec3 motion = getDeltaMovement();
		float f1 = Mth.sqrt((float)(motion.x*motion.x+motion.z*motion.z));
		this.setYRot((float)(Math.atan2(motion.z, motion.x)*180.0D/Math.PI)+90.0F);
		this.setXRot((float)(Math.atan2(f1, motion.y)*180.0D/Math.PI)-90.0F);
		while(this.getXRot()-this.xRotO < -180.0F)
			this.xRotO -= 360.0F;
		while(this.getXRot()-this.xRotO >= 180.0F)
			this.xRotO += 360.0F;
		while(this.getYRot()-this.yRotO < -180.0F)
			this.yRotO -= 360.0F;
		while(this.getYRot()-this.yRotO >= 180.0F)
			this.yRotO += 360.0F;
	}

	public void setConnectionAndPos(Connection c, ConnectionPoint start, double linePos, double speed)
	{
		this.linePos = linePos;
		this.horizontalSpeed = speed;
		this.connection = c;
		this.start = start;
		Vec3 pos = connection.getPoint(this.linePos, start).add(Vec3.atLowerCornerOf(start.position()));
		this.moveTo(pos.x, pos.y, pos.z, this.getYRot(), this.getXRot());
		if(!connection.getCatenaryData().isVertical())
			this.angle = Math.atan2(connection.getCatenaryData().getDeltaZ(), connection.getCatenaryData().getDeltaX());
		ignoreCollisions.clear();
		LocalWireNetwork net = GlobalWireNetwork.getNetwork(level()).getLocalNet(start);
		IImmersiveConnectable iicStart = net.getConnector(start);
		IImmersiveConnectable iicEnd = net.getConnector(c.getOtherEnd(start));
		if(iicStart!=null&&iicEnd!=null)
		{
			ignoreCollisions.addAll(iicStart.getIgnored(iicEnd));
			ignoreCollisions.addAll(iicEnd.getIgnored(iicStart));
		}
	}

	private float getSlopeModifier()
	{
		return this.entityData.get(SLOPE_MODIFIER);
	}

	private void setSlopeModifier(float slopeModifier)
	{
		this.entityData.set(SLOPE_MODIFIER, slopeModifier);
	}

	@Override
	protected void defineSynchedData(Builder builder)
	{
		builder.define(SLOPE_MODIFIER, 1f);
	}


	@Override
	public boolean shouldRenderAtSqrDistance(double distance)
	{
		double d1 = this.getBoundingBox().getSize()*4.0D;
		d1 *= 64.0D;
		return distance < d1*d1;
	}

	@Override
	public void tick()
	{
		if(tickCount==1&&level().isClientSide)
			ImmersiveEngineering.proxy.startSkyhookSound(this);
		Player player = null;
		List<Entity> list = this.getPassengers();
		if(!list.isEmpty()&&list.get(0) instanceof Player)
			player = (Player)list.get(0);
		if(connection==null||player==null||(hand!=null&&player.getItemInHand(hand).getItem()!=Misc.SKYHOOK.asItem()))
		{
			if(!level().isClientSide)
				discard();
			return;
		}
		//TODO figure out how to get the speed keeping on dismount working with less sync packets
		if(this.tickCount%5==0&&!level().isClientSide)
			sendUpdatePacketTo(player);
		PlayerUtils.resetFloatingState(player);
		boolean moved = false;
		double inLineDirection;
		if(connection.getCatenaryData().isVertical())
			inLineDirection = -player.zza*Math.sin(Math.toRadians(player.getXRot()))
					*Math.signum(connection.getCatenaryData().getDeltaY())
					*getStartSignum();
		else
		{
			float forward = player.zza;
			double strafing = player.xxa;
			double playerAngle = Math.toRadians(player.getYRot())+Math.PI/2;
			double angleToLine = playerAngle-angle;
			inLineDirection = (Math.cos(angleToLine)*forward+Math.sin(angleToLine)*strafing)
					*getStartSignum();
		}
		if(inLineDirection!=0)
		{
			double slope = connection.getSlope(linePos, start);
			double slopeInDirection = Math.signum(inLineDirection)*slope;
			double speed = MOVE_SPEED_VERT;
			double slopeFactor = 1;
			if(!connection.getCatenaryData().isVertical())
			{
				//Linear interpolation w.r.t. the angle of the line
				double lambda = Math.atan(slopeInDirection)/(Math.PI/2);
				speed = lambda*MOVE_SPEED_VERT+(1-lambda)*MOVE_SPEED_HOR;
				slopeFactor = 1/(Math.sqrt(1+slope*slope)*getSlopeModifier());
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
			if(connection.getCatenaryData().isVertical())
				deltaVHor = -GRAVITY*Math.signum(connection.getCatenaryData().getDeltaY()*getStartSignum());
			else
			{
				final double realLinePos = connection.transformPosition(linePos, start);
				double param = (realLinePos*getHorizontalLength()-connection.getCatenaryData().offsetX())/connection.getCatenaryData().scale();
				double pos = Math.exp(param);
				double neg = 1/pos;
				double cosh = (pos+neg)/2;
				double sinh = (pos-neg)/2;
				//Formula taken from https://physics.stackexchange.com/a/83592 (x coordinate of the final vector),
				//after plugging in the correct function
				double vSquared = horizontalSpeed*horizontalSpeed*cosh*cosh*20*20;//cosh^2=1+sinh^2 and horSpeed*sinh=vertSpeed. 20 to convert from blocks/tick to block/s
				deltaVHor = -sinh/(cosh*cosh)*(GRAVITY+vSquared/(connection.getCatenaryData().scale()*cosh));
				if(connection.getEndB().equals(start))
					deltaVHor *= -1;
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
		double horSpeedToUse = horizontalSpeed;
		if(horizontalSpeed > 0)
		{
			double distToEnd = getHorizontalLength()*(1-linePos);
			if(horizontalSpeed > distToEnd)
			{
				switchingAtPos = connection.getOtherEnd(start);
				horSpeedToUse = distToEnd;
			}
		}
		else
		{
			double distToStart = -getHorizontalLength()*linePos;
			if(horizontalSpeed < distToStart)
			{
				switchingAtPos = start;
				horSpeedToUse = distToStart;
			}
		}
		horizontalSpeed *= friction;
		linePos += horSpeedToUse/getHorizontalLength();
		Vec3 pos = connection.getPoint(linePos, start).add(Vec3.atLowerCornerOf(start.position()));
		setDeltaMovement(pos.x-getX(), pos.z-getZ(), pos.y-getY());
		if(!isValidPosition(pos.x, pos.y, pos.z, player))
		{
			discard();
			return;
		}
		this.setPos(pos.x, pos.y, pos.z);

		super.tick();
		Vec3 motion = getDeltaMovement();
		float f1 = Mth.sqrt((float)(motion.x*motion.x+motion.z*motion.z));
		this.setYRot((float)(Math.atan2(motion.z, motion.x)*180.0D/Math.PI)+90.0F);
		this.setXRot((float)(Math.atan2(f1, motion.y)*180.0D/Math.PI)-90.0F);

		this.xRotO = this.getXRot()-Mth.wrapDegrees(this.getXRot()-this.xRotO);
		this.yRotO = this.getYRot()-Mth.wrapDegrees(this.getYRot()-this.yRotO);

		this.setXRot(this.xRotO+(this.getXRot()-this.xRotO)*0.2F);
		this.setYRot(this.yRotO+(this.getYRot()-this.yRotO)*0.2F);

		if(this.isInWater())
		{
			for(int j = 0; j < 4; ++j)
			{
				float f3 = 0.25F;
				this.level().addParticle(ParticleTypes.BUBBLE,
						this.getX()-motion.x*(double)f3,
						this.getY()-motion.y*(double)f3,
						this.getZ()-motion.z*(double)f3,
						motion.x,
						motion.y,
						motion.z);
			}
		}

		double dx = this.getX()-this.xo;
		double dy = this.getY()-this.yo;
		double dz = this.getZ()-this.zo;
		int distTrvl = Math.round(Mth.sqrt((float)(dx*dx+dy*dy+dz*dz))*100.0F);
		if(distTrvl > 0)
			player.awardStat(IEStats.SKYHOOK_DISTANCE.value(), distTrvl);

		if(player instanceof ServerPlayer serverPlayer)
			if(serverPlayer.getStats().getValue(Stats.CUSTOM, IEStats.SKYHOOK_DISTANCE.value()) > 100000)
				Utils.unlockIEAdvancement(player, "tools/skyhook_distance");

		this.setPos(this.getX(), this.getY(), this.getZ());
		if(switchingAtPos!=null)
			switchConnection(switchingAtPos, player, horSpeedToUse);
	}

	private void sendUpdatePacketTo(Player player)
	{
		if(player instanceof ServerPlayer)
			PacketDistributor.sendToPlayer((ServerPlayer)player, new MessageSkyhookSync(this));
	}

	public void switchConnection(ConnectionPoint posForSwitch, Player player, double lastHorSpeed)
	{
		Optional<Connection> line = Optional.empty();
		LocalWireNetwork net = GlobalWireNetwork.getNetwork(level()).getLocalNet(posForSwitch);
		Collection<Connection> possible = net.getConnections(posForSwitch);
		if(possible!=null)
		{
			Vec3 look = player.getLookAngle();
			line = possible.stream().filter(c -> !c.equals(connection)&&!c.isInternal())
					.max(Comparator.comparingDouble(c -> {
						double factor;
						if(posForSwitch.equals(c.getEndA()))
							factor = 1;
						else
							factor = -1;
						return c.getCatenaryData().delta().normalize().dot(look)*factor;
					}));//Maximum dot product=>Minimum angle=>Player goes in as close to a straight line as possible
		}
		if(line.isPresent())
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
			discard();
	}

	private double getSpeedPerHor(Connection connection, ConnectionPoint start, double pos)
	{
		if(connection.getCatenaryData().isVertical())
			return 1;
		else
		{
			double slope = connection.getSlope(pos, start);
			return Math.sqrt(slope*slope+1)*getSlopeModifier();
		}
	}

	public boolean isValidPosition(double x, double y, double z, @Nonnull LivingEntity player)
	{
		final double tolerance = connection.getCatenaryData().isVertical()?5: 10;//TODO are these values good?
		double radius = player.getBbWidth()/2;
		double height = player.getBbHeight();
		var offset = getPassengerRidingPosition(player);
		AABB playerBB = new AABB(x-radius, y, z-radius, x+radius, y+height, z+radius).move(offset);
		double playerHeight = playerBB.maxY-playerBB.minY;
		AABB feet = new AABB(playerBB.minX, playerBB.minY, playerBB.minZ,
				playerBB.maxX, playerBB.minY+.05*playerHeight, playerBB.maxZ);
		List<VoxelShape> shapes = SkylineHelper.getCollisionBoxes(player, playerBB, level(), ignoreCollisions);
		// Heuristic to prevent dragging players through blocks too much, but also keep most setups working
		// Allow positions where the intersection is less than 10% of the player BB volume
		double totalCollisionVolume = 0;
		double totalCollisionArea = 0;
		VoxelShape playerShape = Shapes.create(playerBB);
		double playerVolume = getVolume(playerShape);
		double playerArea = playerVolume/playerHeight;
		VoxelShape feetShape = Shapes.create(feet);
		for(VoxelShape shape : shapes)
		{
			VoxelShape intersection = Shapes.joinUnoptimized(playerShape, shape, BooleanOp.AND);
			totalCollisionVolume += getVolume(intersection);
			if(totalCollisionVolume*tolerance > playerVolume)
				return false;
			if(!connection.getCatenaryData().isVertical()&&Shapes.joinIsNotEmpty(feetShape, shape, BooleanOp.AND))
			{
				VoxelShape feetIntersectShape = Shapes.joinUnoptimized(feetShape, shape, BooleanOp.AND);
				for(AABB feetIntersect : feetIntersectShape.toAabbs())
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
				.toAabbs()
				.stream()
				.mapToDouble(box -> (box.maxX-box.minX)*(box.maxY-box.minY)*(box.maxZ-box.minZ))
				.sum();
	}

	@Override
	@Nullable
	public LivingEntity getControllingPassenger()
	{
		List<Entity> list = this.getPassengers();
		return list.isEmpty()?null: (LivingEntity)list.get(0);
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
	public boolean displayFireAnimation()
	{
		return false;
	}

	@Override
	public boolean isPushedByFluid()
	{
		return false;
	}

	@Override
	public Vec3 getPassengerAttachmentPoint(Entity rider, EntityDimensions size, float p_296362_)
	{
		return new Vec3(0.0F, size.height()-2, 0.0F);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag nbt)
	{
		nbt.putFloat("slopeModifier", this.slopeModifier);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt)
	{
		this.slopeModifier = nbt.getFloat("slopeModifier");
		this.setSlopeModifier(this.slopeModifier);
	}

	@Override
	public float getPickRadius()
	{
		return 0.0F;
	}

	@Override
	public boolean isPickable()
	{
		return false;
	}

	@Override
	public boolean hurt(DamageSource source, float amount)
	{
		this.discard();
		return true;
	}

	@Override
	public boolean isControlledByLocalInstance()
	{
		return false;
	}

	private void handleDismount(Entity passenger)
	{
		final var passengerPosition = getPassengerRidingPosition(passenger);
		passenger.teleportTo(passengerPosition.x, passengerPosition.y, passengerPosition.z);
		passenger.setDeltaMovement(getDeltaMovement());
		if(getDeltaMovement().y < 0)
		{
			passenger.fallDistance = SkylineHelper.fallDistanceFromSpeed(getDeltaMovement().y);
			passenger.setOnGround(false);
		}
		passenger.getData(IEDataAttachments.SKYHOOK_USER.get()).release();
		if(hand!=null&&passenger instanceof Player)
		{
			ItemStack held = ((Player)passenger).getItemInHand(hand);
			if(held.getItem()==Misc.SKYHOOK.asItem())
				((Player)passenger).getCooldowns().addCooldown(Misc.SKYHOOK.asItem(), 10);
		}
	}

	@Override
	protected void removePassenger(Entity passenger)
	{
		super.removePassenger(passenger);
		if(!level().isClientSide)
			ApiUtils.addFutureServerTask(level(), () -> handleDismount(passenger));
		else
			//TODO is this still needed?
			ApiUtils.addFutureServerTask(level(), () -> handleDismount(passenger), true);
	}

	@Override
	public void absMoveTo(double x, double y, double z, float yaw, float pitch)
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
		if(connection.getCatenaryData().isVertical())
			return Math.abs(horizontalSpeed);//In this case vertical speed
		else
		{
			double slope = connection.getSlope(linePos, start);
			return Math.abs(horizontalSpeed)*Math.sqrt(1+slope*slope)*getSlopeModifier();
		}
	}

	private double getHorizontalLength()
	{
		if(connection.getCatenaryData().isVertical())
			return Math.abs(connection.getCatenaryData().getDeltaY());
		else
			return connection.getCatenaryData().horLength();
	}

	private double getStartSignum()
	{
		if(start.equals(connection.getEndA()))
			return 1;
		else
			return -1;
	}
}
/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment.ElectricSource;
import blusunrize.immersiveengineering.api.tool.ITeslaEntity;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IScrewdriverInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.ticking.IEClientTickableBE;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeslaCoilBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IEClientTickableBE, IHasDummyBlocks,
		IStateBasedDirectional, IBlockBounds, IScrewdriverInteraction, IModelOffsetProvider
{
	public MutableEnergyStorage energyStorage = new MutableEnergyStorage(48000);
	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, TeslaCoilBlockEntity::master, registerEnergyInput(energyStorage)
	);
	public boolean redstoneControlInverted = false;
	public boolean lowPower = false;
	public final List<LightningAnimation> effectMap = new ArrayList<>();
	private static final ElectricSource TC_FIELD = new ElectricSource(-1);

	public TeslaCoilBlockEntity(BlockEntityType<TeslaCoilBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	@Override
	public void tickClient()
	{
		effectMap.removeIf(LightningAnimation::tick);
	}

	@Override
	public void tickServer()
	{
		int timeKey = getBlockPos().getX()^getBlockPos().getZ();
		int energyDrain = IEServerConfig.MACHINES.teslacoil_consumption.get();
		if(lowPower)
			energyDrain /= 2;
		if(level.getGameTime()%32==(timeKey&31)&&canRun(energyDrain))
		{
			this.energyStorage.extractEnergy(energyDrain, false);

			double radius = 6;
			if(lowPower)
				radius /= 2;
			AABB aabbSmall = new AABB(getBlockPos().getX()+.5-radius, getBlockPos().getY()+.5-radius, getBlockPos().getZ()+.5-radius, getBlockPos().getX()+.5+radius, getBlockPos().getY()+.5+radius, getBlockPos().getZ()+.5+radius);
			AABB aabb = aabbSmall.inflate(radius/2);
			List<Entity> targetsAll = level.getEntitiesOfClass(Entity.class, aabb);
			List<Entity> targets = targetsAll.stream().filter((e) -> (e instanceof LivingEntity&&aabbSmall.intersects(e.getBoundingBox()))).collect(Collectors.toList());
			LivingEntity target = null;
			if(!targets.isEmpty())
			{
				ElectricDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(level, IEServerConfig.MACHINES.teslacoil_damage.get().floatValue(), lowPower);
				int randomTarget = ApiUtils.RANDOM.nextInt(targets.size());
				target = (LivingEntity)targets.get(randomTarget);
				if(target!=null)
				{
					if(!level.isClientSide)
					{
						energyDrain = IEServerConfig.MACHINES.teslacoil_consumption_active.get();
						if(lowPower)
							energyDrain /= 2;
						if(energyStorage.extractEnergy(energyDrain, true)==energyDrain)
						{
							energyStorage.extractEnergy(energyDrain, false);
							target.addEffect(new MobEffectInstance(IEPotions.STUNNED.get(), 128));
							if(dmgsrc.apply(target))
							{
								int prevFire = target.getRemainingFireTicks();
								target.setRemainingFireTicks(1);
								target.setRemainingFireTicks(prevFire);
							}
							this.sendRenderPacket(target);
						}
					}
				}
			}
			for(Entity e : targetsAll)
				if(e!=target)
				{
					if(e instanceof ITeslaEntity)
						((ITeslaEntity)e).onHit(this, lowPower);
					else if(e instanceof LivingEntity)
						IElectricEquipment.applyToEntity((LivingEntity)e, null, TC_FIELD);
				}
			if(targets.isEmpty()&&level.getGameTime()%128==(timeKey&127))
			{
				//target up to 4 blocks away
				double tV = (ApiUtils.RANDOM.nextDouble()-.5)*8;
				double tH = (ApiUtils.RANDOM.nextDouble()-.5)*8;
				if(lowPower)
				{
					tV /= 2;
					tH /= 2;
				}
				//Minimal distance to the coil is 2 blocks
				tV += tV < 0?-2: 2;
				tH += tH < 0?-2: 2;

				// TODO this looks like it might be wrong?
				BlockPos targetBlock = getBlockPos().offset(
						(int)(getFacing().getAxis()==Axis.X?0: tH),
						(int)(getFacing().getAxis()==Axis.Y?0: tV),
						(int)(getFacing().getAxis()==Axis.Y?tV: getFacing().getAxis()==Axis.X?tH: 0)
				);
				double tL = 0;
				boolean targetFound = false;
				if(!level.isEmptyBlock(targetBlock))
				{
					BlockState state = level.getBlockState(targetBlock);
					VoxelShape shape = state.getShape(level, targetBlock);
					if(!shape.isEmpty())
					{
						AABB blockBounds = shape.bounds();
						if(getFacing()==Direction.UP)
							tL = targetBlock.getY()-getBlockPos().getY()+blockBounds.maxY;
						else if(getFacing()==Direction.DOWN)
							tL = targetBlock.getY()-getBlockPos().getY()+blockBounds.minY;
						else if(getFacing()==Direction.NORTH)
							tL = targetBlock.getZ()-getBlockPos().getZ()+blockBounds.minZ;
						else if(getFacing()==Direction.SOUTH)
							tL = targetBlock.getZ()-getBlockPos().getZ()+blockBounds.maxZ;
						else if(getFacing()==Direction.WEST)
							tL = targetBlock.getX()-getBlockPos().getX()+blockBounds.minX;
						else
							tL = targetBlock.getX()-getBlockPos().getX()+blockBounds.maxX;
						targetFound = true;
					}
				}
				if(!targetFound)
				{
					boolean positiveFirst = ApiUtils.RANDOM.nextBoolean();
					for(int i = 0; i < 2; i++)
					{
						for(int ll = 0; ll <= 6; ll++)
						{
							BlockPos targetBlock2 = targetBlock.relative(positiveFirst?getFacing(): getFacing().getOpposite(), ll);
							if(!level.isEmptyBlock(targetBlock2))
							{
								BlockState state = level.getBlockState(targetBlock2);
								VoxelShape shape = state.getShape(level, targetBlock2);
								if(shape.isEmpty())
									continue;
								AABB blockBounds = shape.bounds();
								tL = getFacing().getAxis()==Axis.Y?(targetBlock2.getY()-getBlockPos().getY()): getFacing().getAxis()==Axis.Z?(targetBlock2.getZ()-getBlockPos().getZ()): (targetBlock2.getZ()-getBlockPos().getZ());
								Direction tempF = positiveFirst?getFacing(): getFacing().getOpposite();
								if(tempF==Direction.UP)
									tL += blockBounds.maxY;
								else if(tempF==Direction.DOWN)
									tL += blockBounds.minY;
								else if(tempF==Direction.NORTH)
									tL += blockBounds.minZ;
								else if(tempF==Direction.SOUTH)
									tL += blockBounds.maxZ;
								else if(tempF==Direction.WEST)
									tL += blockBounds.minX;
								else
									tL += blockBounds.maxX;
								targetFound = true;
								break;
							}
						}
						if(targetFound)
							break;
						positiveFirst = !positiveFirst;
					}
				}
				if(targetFound)
					sendFreePacket(tL, tH, tV);
			}
			this.setChanged();
		}
	}

	protected void sendRenderPacket(Entity target)
	{
		CompoundTag tag = new CompoundTag();
		tag.putInt("targetEntity", target.getId());
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), new MessageBlockEntitySync(this, tag));
	}

	protected void sendFreePacket(double tL, double tH, double tV)
	{
		CompoundTag tag = new CompoundTag();
		tag.putDouble("tL", tL);
		tag.putDouble("tV", tV);
		tag.putDouble("tH", tH);
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(worldPosition)), new MessageBlockEntitySync(this, tag));
	}

	@Override
	public void receiveMessageFromServer(CompoundTag message)
	{
		if(message.contains("targetEntity", Tag.TAG_INT))
		{
			Entity target = level.getEntity(message.getInt("targetEntity"));
			if(target instanceof LivingEntity)
			{
				double dx = target.getX()-getBlockPos().getX();
				double dy = target.getY()-getBlockPos().getY();
				double dz = target.getZ()-getBlockPos().getZ();

				Direction f;
				if(getFacing().getAxis()==Axis.Y)
				{
					if(Math.abs(dz) > Math.abs(dx))
						f = dz < 0?Direction.NORTH: Direction.SOUTH;
					else
						f = dx < 0?Direction.WEST: Direction.EAST;
				}
				else if(getFacing().getAxis()==Axis.Z)
				{
					if(Math.abs(dy) > Math.abs(dx))
						f = dy < 0?Direction.DOWN: Direction.UP;
					else
						f = dx < 0?Direction.WEST: Direction.EAST;
				}
				else
				{
					if(Math.abs(dy) > Math.abs(dz))
						f = dy < 0?Direction.DOWN: Direction.UP;
					else
						f = dz < 0?Direction.NORTH: Direction.SOUTH;
				}
				double verticalOffset = 1+ApiUtils.RANDOM.nextDouble()*.25;
				Vec3 coilPos = Vec3.atCenterOf(getBlockPos());
				//Vertical offset
				coilPos = coilPos.add(getFacing().getStepX()*verticalOffset, getFacing().getStepY()*verticalOffset, getFacing().getStepZ()*verticalOffset);
				//offset to direction
				if(f!=null)
				{
					coilPos = coilPos.add(f.getStepX()*.375, f.getStepY()*.375, f.getStepZ()*.375);
					//random side offset
					f = DirectionUtils.rotateAround(f, getFacing().getAxis());
					double dShift = (ApiUtils.RANDOM.nextDouble()-.5)*.75;
					coilPos = coilPos.add(f.getStepX()*dShift, f.getStepY()*dShift, f.getStepZ()*dShift);
				}

				addAnimation(new LightningAnimation(coilPos, (LivingEntity)target));
				level.playLocalSound(coilPos.x, coilPos.y, coilPos.z, IESounds.tesla.get(), SoundSource.BLOCKS, 2.5F, 0.5F+ApiUtils.RANDOM.nextFloat(), true);
			}
		}
		else if(message.contains("tL", Tag.TAG_DOUBLE))
			initFreeStreamer(message.getDouble("tL"), message.getDouble("tV"), message.getDouble("tH"));
	}

	public void initFreeStreamer(double tL, double tV, double tH)
	{
		double tx = getFacing().getAxis()==Axis.X?tL: tH;
		double ty = getFacing().getAxis()==Axis.Y?tL: tV;
		double tz = getFacing().getAxis()==Axis.Y?tV: getFacing().getAxis()==Axis.X?tH: tL;

		Direction f = null;
		if(getFacing().getAxis()==Axis.Y)
		{
			if(Math.abs(tz) > Math.abs(tx))
				f = tz < 0?Direction.NORTH: Direction.SOUTH;
			else
				f = tx < 0?Direction.WEST: Direction.EAST;
		}
		else if(getFacing().getAxis()==Axis.Z)
		{
			if(Math.abs(ty) > Math.abs(tx))
				f = ty < 0?Direction.DOWN: Direction.UP;
			else
				f = tx < 0?Direction.WEST: Direction.EAST;
		}
		else
		{
			if(Math.abs(ty) > Math.abs(tz))
				f = ty < 0?Direction.DOWN: Direction.UP;
			else
				f = tz < 0?Direction.NORTH: Direction.SOUTH;
		}

		double verticalOffset = 1+ApiUtils.RANDOM.nextDouble()*.25;
		Vec3 coilPos = Vec3.atCenterOf(getBlockPos());
		//Vertical offset
		coilPos = coilPos.add(getFacing().getStepX()*verticalOffset, getFacing().getStepY()*verticalOffset, getFacing().getStepZ()*verticalOffset);
		//offset to direction
		coilPos = coilPos.add(f.getStepX()*.375, f.getStepY()*.375, f.getStepZ()*.375);
		//random side offset
		f = DirectionUtils.rotateAround(f, getFacing().getAxis());
		double dShift = (ApiUtils.RANDOM.nextDouble()-.5)*.75;
		coilPos = coilPos.add(f.getStepX()*dShift, f.getStepY()*dShift, f.getStepZ()*dShift);
		addAnimation(new LightningAnimation(coilPos, Vec3.atLowerCornerOf(getBlockPos()).add(tx, ty, tz)));
//		world.playSound(null, getPos(), IESounds.tesla, SoundCategory.BLOCKS,2.5f, .5f + ApiUtils.RANDOM.nextFloat());
		level.playLocalSound(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), IESounds.tesla.get(), SoundSource.BLOCKS, 2.5F, 0.5F+ApiUtils.RANDOM.nextFloat(), true);
	}

	private void addAnimation(LightningAnimation ani)
	{
		Minecraft.getInstance().submitAsync(() -> effectMap.add(ani));
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		lowPower = nbt.getBoolean("lowPower");
		EnergyHelper.deserializeFrom(energyStorage, nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
		nbt.putBoolean("lowPower", lowPower);
		EnergyHelper.serializeTo(energyStorage, nbt);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(!isDummy())
			return Shapes.block();
		switch(getFacing())
		{
			case DOWN:
				return Shapes.box(.125f, .125f, .125f, .875f, 1, .875f);
			case UP:
				return Shapes.box(.125f, 0, .125f, .875f, .875f, .875f);
			case NORTH:
				return Shapes.box(.125f, .125f, .125f, .875f, .875f, 1);
			case SOUTH:
				return Shapes.box(.125f, .125f, 0, .875f, .875f, .875f);
			case WEST:
				return Shapes.box(.125f, .125f, .125f, 1, .875f, .875f);
			case EAST:
				return Shapes.box(0, .125f, .125f, .875f, .875f, .875f);
		}
		return Shapes.block();
	}

	AABB renderBB;

	@Override
	public AABB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = new AABB(getBlockPos().offset(-8, -8, -8), getBlockPos().offset(8, 8, 8));
		return renderBB;
	}

	@Override
	public InteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(isDummy())
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().relative(getFacing(), -1));
			if(te instanceof TeslaCoilBlockEntity)
				return ((TeslaCoilBlockEntity)te).screwdriverUseSide(side, player, hand, hitVec);
			return InteractionResult.PASS;
		}
		if(!level.isClientSide)
		{
			if(player.isShiftKeyDown())
			{
				int energyDrain = IEServerConfig.MACHINES.teslacoil_consumption.get();
				if(lowPower)
					energyDrain /= 2;
				if(canRun(energyDrain))
					player.hurt(IEDamageSources.causeTeslaPrimaryDamage(level), Float.MAX_VALUE);
				else
				{
					lowPower = !lowPower;
					player.displayClientMessage(
							Component.translatable(Lib.CHAT_INFO+"tesla."+(lowPower?"lowPower": "highPower")), true
					);
					setChanged();
				}
			}
			else
			{
				redstoneControlInverted = !redstoneControlInverted;
				player.displayClientMessage(
						Component.translatable(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")),
						true
				);
				setChanged();
				this.markContainingBlockForUpdate(null);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Nullable
	@Override
	public TeslaCoilBlockEntity master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().below();
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return te instanceof TeslaCoilBlockEntity tc?tc: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		level.setBlockAndUpdate(worldPosition.relative(getFacing()), state.setValue(IEProperties.MULTIBLOCKSLAVE, true));
		((TeslaCoilBlockEntity)level.getBlockEntity(worldPosition.relative(getFacing()))).setFacing(getFacing());
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		boolean dummy = isDummy();
		for(int i = 0; i <= 1; i++)
			if(level.getBlockEntity(getBlockPos().relative(getFacing(), dummy?-1: 0).relative(getFacing(), i)) instanceof TeslaCoilBlockEntity)
				level.removeBlock(getBlockPos().relative(getFacing(), dummy?-1: 0).relative(getFacing(), i), false);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==ForgeCapabilities.ENERGY&&(side==null||!isDummy()))
			return energyCap.getAndCast();
		return super.getCapability(cap, side);
	}

	public boolean canRun(int energyDrain)
	{
		return (isRSPowered()^redstoneControlInverted)&&energyStorage.getEnergyStored() >= energyDrain;
	}

	public static class LightningAnimation
	{
		public Vec3 startPos;
		public LivingEntity targetEntity;
		public Vec3 targetPos;
		private int lifeTimer = 20;
		private final int ANIMATION_MAX = 4;
		private int animationTimer = ANIMATION_MAX;

		public List<Vec3> subPoints = new ArrayList<>();
		private Vec3 prevTarget;

		public LightningAnimation(Vec3 startPos, LivingEntity targetEntity)
		{
			this.startPos = startPos;
			this.targetEntity = targetEntity;
		}

		public LightningAnimation(Vec3 startPos, Vec3 targetPos)
		{
			this.startPos = startPos;
			this.targetPos = targetPos;
		}

		public boolean shoudlRecalculateLightning()
		{
			if(subPoints.isEmpty()||animationTimer==0)
				return true;
			boolean b = false;
			Vec3 end = targetEntity!=null?targetEntity.position(): targetPos;
			if(prevTarget!=null)
				b = prevTarget.distanceTo(end) > 1;
			prevTarget = end;
			return b;
		}

		public void createLightning(RandomSource rand)
		{
			subPoints.clear();
			Vec3 end = targetEntity!=null?targetEntity.position(): targetPos;
			Vec3 dist = end.subtract(startPos);
			double points = 12;
			for(int i = 0; i < points; i++)
			{
				Vec3 sub = startPos.add(dist.x/points*i, dist.y/points*i, dist.z/points*i);
				//distance to the middle point and by that, distance from the start and end. -1 is start, 1 is end
				double fixPointDist = (i-points/2)/(points/2);
				//Randomization modifier, closer to start/end means smaller divergence
				double mod = 1-.75*Math.abs(fixPointDist);
				double offX = (rand.nextDouble()-.5)*mod;
				double offY = (rand.nextDouble()-.5)*mod;
				double offZ = (rand.nextDouble()-.5)*mod;
				if(fixPointDist < 0)
				{
					offY += .75*mod*(.75+fixPointDist);//Closer to the coil should arc upwards
					offX = (sub.x-startPos.x) < 0?-Math.abs(offX): Math.abs(offX);
					offZ = (sub.z-startPos.z) < 0?-Math.abs(offZ): Math.abs(offZ);
				}
				else
				{
					offY = Math.min(end.y+1*(1-fixPointDist)*-Math.signum(dist.y), offY);//final points should be higher/lower than end, depending on if lightning goes up or down
					offX = Math.abs(offX)*(end.x-sub.x);
					offZ = Math.abs(offZ)*(end.z-sub.z);
				}
				subPoints.add(sub.add(offX, offY, offZ));
			}
			animationTimer = ANIMATION_MAX+ApiUtils.RANDOM.nextInt(5)-2;
		}

		public boolean tick()
		{
			animationTimer--;
			lifeTimer--;
			return lifeTimer <= 0;
		}
	}

	@Override
	public BlockPos getModelOffset(BlockState state, @Nullable Vec3i size)
	{
		if(isDummy())
			return new BlockPos(0, 0, -1);
		else
			return BlockPos.ZERO;
	}
}

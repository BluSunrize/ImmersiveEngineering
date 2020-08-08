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
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment.ElectricSource;
import blusunrize.immersiveengineering.api.tool.ITeslaEntity;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import com.google.common.collect.ArrayListMultimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TeslaCoilTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxHandler, IHasDummyBlocks,
		IStateBasedDirectional, IBlockBounds, IScrewdriverInteraction
{
	public static TileEntityType<TeslaCoilTileEntity> TYPE;

	public FluxStorage energyStorage = new FluxStorage(48000);
	public boolean redstoneControlInverted = false;
	public boolean lowPower = false;
	private Vec3d soundPos = null;
	@OnlyIn(Dist.CLIENT)
	public static ArrayListMultimap<BlockPos, LightningAnimation> effectMap;
	private static final ElectricSource TC_FIELD = new ElectricSource(-1);

	public TeslaCoilTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(isDummy())
			return;
		synchronized(this)
		{
			if(world.isRemote&&soundPos!=null)
			{
				world.playSound(soundPos.x, soundPos.y, soundPos.z, IESounds.tesla, SoundCategory.BLOCKS, 2.5F, 0.5F+Utils.RAND.nextFloat(), true);
				soundPos = null;
			}
		}
		if(world.isRemote&&effectMap.containsKey(pos))
			effectMap.get(pos).removeIf(LightningAnimation::tick);

		int timeKey = getPos().getX()^getPos().getZ();
		int energyDrain = IEConfig.MACHINES.teslacoil_consumption.get();
		if(lowPower)
			energyDrain /= 2;
		if(!world.isRemote&&world.getGameTime()%32==(timeKey&31)&&canRun(energyDrain))
		{
			this.energyStorage.extractEnergy(energyDrain, false);

			double radius = 6;
			if(lowPower)
				radius /= 2;
			AxisAlignedBB aabbSmall = new AxisAlignedBB(getPos().getX()+.5-radius, getPos().getY()+.5-radius, getPos().getZ()+.5-radius, getPos().getX()+.5+radius, getPos().getY()+.5+radius, getPos().getZ()+.5+radius);
			AxisAlignedBB aabb = aabbSmall.grow(radius/2);
			List<Entity> targetsAll = world.getEntitiesWithinAABB(Entity.class, aabb);
			List<Entity> targets = targetsAll.stream().filter((e) -> (e instanceof LivingEntity&&aabbSmall.intersects(e.getBoundingBox()))).collect(Collectors.toList());
			LivingEntity target = null;
			if(!targets.isEmpty())
			{
				ElectricDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(IEConfig.MACHINES.teslacoil_damage.get().floatValue(), lowPower);
				int randomTarget = Utils.RAND.nextInt(targets.size());
				target = (LivingEntity)targets.get(randomTarget);
				if(target!=null)
				{
					if(!world.isRemote)
					{
						energyDrain = IEConfig.MACHINES.teslacoil_consumption_active.get();
						if(lowPower)
							energyDrain /= 2;
						if(energyStorage.extractEnergy(energyDrain, true)==energyDrain)
						{
							energyStorage.extractEnergy(energyDrain, false);
							if(dmgsrc.apply(target))
							{
								int prevFire = target.fire;
								target.fire = 1;
								target.addPotionEffect(new EffectInstance(IEPotions.stunned, 128));
								target.fire = prevFire;
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
			if(targets.isEmpty()&&world.getGameTime()%128==(timeKey&127))
			{
				//target up to 4 blocks away
				double tV = (Utils.RAND.nextDouble()-.5)*8;
				double tH = (Utils.RAND.nextDouble()-.5)*8;
				if(lowPower)
				{
					tV /= 2;
					tH /= 2;
				}
				//Minimal distance to the coil is 2 blocks
				tV += tV < 0?-2: 2;
				tH += tH < 0?-2: 2;

				BlockPos targetBlock = getPos().add(getFacing().getAxis()==Axis.X?0: tH, getFacing().getAxis()==Axis.Y?0: tV, getFacing().getAxis()==Axis.Y?tV: getFacing().getAxis()==Axis.X?tH: 0);
				double tL = 0;
				boolean targetFound = false;
				if(!world.isAirBlock(targetBlock))
				{
					BlockState state = world.getBlockState(targetBlock);
					AxisAlignedBB blockBounds = state.getShape(world, targetBlock).getBoundingBox();
					if(getFacing()==Direction.UP)
						tL = targetBlock.getY()-getPos().getY()+blockBounds.maxY;
					else if(getFacing()==Direction.DOWN)
						tL = targetBlock.getY()-getPos().getY()+blockBounds.minY;
					else if(getFacing()==Direction.NORTH)
						tL = targetBlock.getZ()-getPos().getZ()+blockBounds.minZ;
					else if(getFacing()==Direction.SOUTH)
						tL = targetBlock.getZ()-getPos().getZ()+blockBounds.maxZ;
					else if(getFacing()==Direction.WEST)
						tL = targetBlock.getX()-getPos().getX()+blockBounds.minX;
					else
						tL = targetBlock.getX()-getPos().getX()+blockBounds.maxX;
					targetFound = true;
				}
				else
				{
					boolean positiveFirst = Utils.RAND.nextBoolean();
					for(int i = 0; i < 2; i++)
					{
						for(int ll = 0; ll <= 6; ll++)
						{
							BlockPos targetBlock2 = targetBlock.offset(positiveFirst?getFacing(): getFacing().getOpposite(), ll);
							if(!world.isAirBlock(targetBlock2))
							{
								BlockState state = world.getBlockState(targetBlock2);
								VoxelShape shape = state.getShape(world, targetBlock2);
								if(shape.isEmpty())
									continue;
								AxisAlignedBB blockBounds = shape.getBoundingBox();
								tL = getFacing().getAxis()==Axis.Y?(targetBlock2.getY()-getPos().getY()): getFacing().getAxis()==Axis.Z?(targetBlock2.getZ()-getPos().getZ()): (targetBlock2.getZ()-getPos().getZ());
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
			this.markDirty();
		}
	}

	protected void sendRenderPacket(Entity target)
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("targetEntity", target.getEntityId());
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), new MessageTileSync(this, tag));
	}

	protected void sendFreePacket(double tL, double tH, double tV)
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putDouble("tL", tL);
		tag.putDouble("tV", tV);
		tag.putDouble("tH", tH);
		ImmersiveEngineering.packetHandler.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), new MessageTileSync(this, tag));
	}

	@Override
	public void receiveMessageFromServer(CompoundNBT message)
	{
		if(message.contains("targetEntity", NBT.TAG_INT))
		{
			Entity target = world.getEntityByID(message.getInt("targetEntity"));
			if(target instanceof LivingEntity)
			{
				double dx = target.posX-getPos().getX();
				double dy = target.posY-getPos().getY();
				double dz = target.posZ-getPos().getZ();

				Direction f = null;
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
				double verticalOffset = 1+Utils.RAND.nextDouble()*.25;
				Vec3d coilPos = new Vec3d(getPos()).add(.5, .5, .5);
				//Vertical offset
				coilPos = coilPos.add(getFacing().getXOffset()*verticalOffset, getFacing().getYOffset()*verticalOffset, getFacing().getZOffset()*verticalOffset);
				//offset to direction
				if(f!=null)
				{
					coilPos = coilPos.add(f.getXOffset()*.375, f.getYOffset()*.375, f.getZOffset()*.375);
					//random side offset
					f = f.rotateAround(getFacing().getAxis());
					double dShift = (Utils.RAND.nextDouble()-.5)*.75;
					coilPos = coilPos.add(f.getXOffset()*dShift, f.getYOffset()*dShift, f.getZOffset()*dShift);
				}

				addAnimation(new LightningAnimation(coilPos, (LivingEntity)target));
				synchronized(this)
				{
					soundPos = coilPos;
				}
			}
		}
		else if(message.contains("tL", NBT.TAG_DOUBLE))
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

		double verticalOffset = 1+Utils.RAND.nextDouble()*.25;
		Vec3d coilPos = new Vec3d(getPos()).add(.5, .5, .5);
		//Vertical offset
		coilPos = coilPos.add(getFacing().getXOffset()*verticalOffset, getFacing().getYOffset()*verticalOffset, getFacing().getZOffset()*verticalOffset);
		//offset to direction
		coilPos = coilPos.add(f.getXOffset()*.375, f.getYOffset()*.375, f.getZOffset()*.375);
		//random side offset
		f = f.rotateAround(getFacing().getAxis());
		double dShift = (Utils.RAND.nextDouble()-.5)*.75;
		coilPos = coilPos.add(f.getXOffset()*dShift, f.getYOffset()*dShift, f.getZOffset()*dShift);
		addAnimation(new LightningAnimation(coilPos, new Vec3d(getPos()).add(tx, ty, tz)));
//		world.playSound(null, getPos(), IESounds.tesla, SoundCategory.BLOCKS,2.5f, .5f + Utils.RAND.nextFloat());
		world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), IESounds.tesla, SoundCategory.BLOCKS, 2.5F, 0.5F+Utils.RAND.nextFloat(), true);
	}

	private void addAnimation(LightningAnimation ani)
	{
		Minecraft.getInstance().deferTask(() -> effectMap.put(getPos(), ani));
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		lowPower = nbt.getBoolean("lowPower");
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
		nbt.putBoolean("lowPower", lowPower);
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		if(!isDummy())
			return VoxelShapes.fullCube();
		switch(getFacing())
		{
			case DOWN:
				return VoxelShapes.create(.125f, .125f, .125f, .875f, 1, .875f);
			case UP:
				return VoxelShapes.create(.125f, 0, .125f, .875f, .875f, .875f);
			case NORTH:
				return VoxelShapes.create(.125f, .125f, .125f, .875f, .875f, 1);
			case SOUTH:
				return VoxelShapes.create(.125f, .125f, 0, .875f, .875f, .875f);
			case WEST:
				return VoxelShapes.create(.125f, .125f, .125f, 1, .875f, .875f);
			case EAST:
				return VoxelShapes.create(0, .125f, .125f, .875f, .875f, .875f);
		}
		return VoxelShapes.fullCube();
	}

	AxisAlignedBB renderBB;

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = new AxisAlignedBB(getPos().add(-8, -8, -8), getPos().add(8, 8, 8));
		return renderBB;
	}

	@Override
	public boolean screwdriverUseSide(Direction side, PlayerEntity player, Hand hand, Vec3d hitVec)
	{
		if(isDummy())
		{
			TileEntity te = world.getTileEntity(getPos().offset(getFacing(), -1));
			if(te instanceof TeslaCoilTileEntity)
				return ((TeslaCoilTileEntity)te).screwdriverUseSide(side, player, hand, hitVec);
			return false;
		}
		if(!world.isRemote)
		{
			if(player.isSneaking())
			{
				int energyDrain = IEConfig.MACHINES.teslacoil_consumption.get();
				if(lowPower)
					energyDrain /= 2;
				if(canRun(energyDrain))
					player.attackEntityFrom(IEDamageSources.causeTeslaPrimaryDamage(), Float.MAX_VALUE);
				else
				{
					lowPower = !lowPower;
					ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"tesla."+(lowPower?"lowPower": "highPower")));
					markDirty();
				}
			}
			else
			{
				redstoneControlInverted = !redstoneControlInverted;
				ChatUtils.sendServerNoSpamMessages(player, new TranslationTextComponent(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")));
				markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
		return true;
	}

	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3d hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction axis)
	{
		return false;
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getPos().down();
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		world.setBlockState(pos.offset(getFacing()), state.with(IEProperties.MULTIBLOCKSLAVE, true));
		((TeslaCoilTileEntity)world.getTileEntity(pos.offset(getFacing()))).setFacing(getFacing());
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		boolean dummy = isDummy();
		for(int i = 0; i <= 1; i++)
			if(world.getTileEntity(getPos().offset(getFacing(), dummy?-1: 0).offset(getFacing(), i)) instanceof TeslaCoilTileEntity)
				world.removeBlock(getPos().offset(getFacing(), dummy?-1: 0).offset(getFacing(), i), false);
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(isDummy())
		{
			TileEntity te = world.getTileEntity(getPos().offset(getFacing(), -1));
			if(te instanceof TeslaCoilTileEntity)
				return ((TeslaCoilTileEntity)te).getFluxStorage();
		}
		return energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return !isDummy()?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper[] wrappers = IEForgeEnergyWrapper.getDefaultWrapperArray(this);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(!isDummy())
			return wrappers[facing==null?0: facing.ordinal()];
		return null;
	}

	public boolean canRun(int energyDrain)
	{
		return (isRSPowered()^redstoneControlInverted)&&energyStorage.getEnergyStored() >= energyDrain;
	}

	public static class LightningAnimation
	{
		public Vec3d startPos;
		public LivingEntity targetEntity;
		public Vec3d targetPos;
		private int lifeTimer = 20;
		private final int ANIMATION_MAX = 4;
		private int animationTimer = ANIMATION_MAX;

		public List<Vec3d> subPoints = new ArrayList<>();
		private Vec3d prevTarget;

		public LightningAnimation(Vec3d startPos, LivingEntity targetEntity)
		{
			this.startPos = startPos;
			this.targetEntity = targetEntity;
		}

		public LightningAnimation(Vec3d startPos, Vec3d targetPos)
		{
			this.startPos = startPos;
			this.targetPos = targetPos;
		}

		public boolean shoudlRecalculateLightning()
		{
			if(subPoints.isEmpty()||animationTimer==0)
				return true;
			boolean b = false;
			Vec3d end = targetEntity!=null?targetEntity.getPositionVector(): targetPos;
			if(prevTarget!=null)
				b = prevTarget.distanceTo(end) > 1;
			prevTarget = end;
			return b;
		}

		public void createLightning(Random rand)
		{
			subPoints.clear();
			Vec3d end = targetEntity!=null?targetEntity.getPositionVector(): targetPos;
			Vec3d dist = end.subtract(startPos);
			double points = 12;
			for(int i = 0; i < points; i++)
			{
				Vec3d sub = startPos.add(dist.x/points*i, dist.y/points*i, dist.z/points*i);
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
			animationTimer = ANIMATION_MAX+Utils.RAND.nextInt(5)-2;
		}

		public boolean tick()
		{
			animationTimer--;
			lifeTimer--;
			return lifeTimer <= 0;
		}
	}
}
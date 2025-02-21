/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.ComputerControlState;
import blusunrize.immersiveengineering.api.utils.SafeChunkUtils;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.blocks.FakeLightBlock.FakeLightBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEBlocks.Misc;
import blusunrize.immersiveengineering.common.util.SpawnInterdictionHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerControllable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static blusunrize.immersiveengineering.common.config.IEServerConfig.getOrDefault;

public class FloodlightBlockEntity extends ImmersiveConnectableBlockEntity implements IEServerTickableBE,
		IAdvancedDirectionalBE, IHammerInteraction, IScrewdriverInteraction, ISpawnInterdiction, IBlockBounds,
		IActiveState, EnergyConnector, IStateBasedDirectional, ComputerControllable
{
	public int energyStorage = 0;
	private final int energyDraw = getOrDefault(IEServerConfig.MACHINES.floodlight_energyDraw);
	public final int maximumStorage = getOrDefault(IEServerConfig.MACHINES.floodlight_maximumStorage);
	public boolean redstoneControlInverted = false;
	public Direction facing = Direction.NORTH;
	public float rotY = 0;
	public float rotX = 0;
	public List<BlockPos> fakeLights = new ArrayList<>();
	public List<BlockPos> lightsToBePlaced = new ArrayList<>();
	public List<BlockPos> lightsToBeRemoved = new ArrayList<>();
	final int timeBetweenSwitches = 20;
	int switchCooldown = 0;
	private boolean shouldUpdate = true;
	public final ComputerControlState computerControl = new ComputerControlState();
	public int turnCooldown = 0;

	public FloodlightBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.FLOODLIGHT.get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		if(turnCooldown > 0)
			turnCooldown--;
		boolean activeBeforeTick = getIsActive();
		boolean enabled;
		if(shouldUpdate)
		{
			lightsToBePlaced.clear();
			updateFakeLights(true, activeBeforeTick);
			setChanged();
			this.markContainingBlockForUpdate(null);
			shouldUpdate = false;
		}

		if(computerControl.isAttached())
			enabled = computerControl.isEnabled();
		else
			enabled = isRSPowered()^redstoneControlInverted;
		if(energyStorage >= (!activeBeforeTick?energyDraw*10: energyDraw)&&enabled&&switchCooldown <= 0)
		{
			energyStorage -= energyDraw;
			if(!activeBeforeTick)
				setActive(true);
		}
		else if(activeBeforeTick)
		{
			setActive(false);
			switchCooldown = timeBetweenSwitches;
		}

		switchCooldown--;
		boolean activeAfterTick = getIsActive();
		if(activeAfterTick!=activeBeforeTick||level.getGameTime()%512==((getBlockPos().getX()^getBlockPos().getZ())&511))
		{
			this.markContainingBlockForUpdate(null);
			updateFakeLights(true, activeAfterTick);
			checkLight();
		}
		if(!activeAfterTick)
		{
			if(!lightsToBePlaced.isEmpty())
				lightsToBePlaced.clear();
		}
		if((!lightsToBePlaced.isEmpty()||!lightsToBeRemoved.isEmpty())&&level.getGameTime()%8==((getBlockPos().getX()^getBlockPos().getZ())&7))
		{
			Iterator<BlockPos> it = lightsToBePlaced.iterator();
			int timeout = 0;
			while(it.hasNext()&&timeout++ < Math.max(16, 32-lightsToBeRemoved.size()))
			{
				BlockPos cc = it.next();
				//				world.setBlockState(cc, Blocks.glass.getDefaultState(), 2);
				level.setBlock(cc, Misc.FAKE_LIGHT.defaultBlockState(), 2);
				BlockEntity te = level.getBlockEntity(cc);
				if(te instanceof FakeLightBlockEntity)
					((FakeLightBlockEntity)te).setFloodlightCoords(getBlockPos());
				fakeLights.add(cc);
				it.remove();
			}
			it = lightsToBeRemoved.iterator();
			while(it.hasNext()&&timeout++ < 32)
			{
				BlockPos cc = it.next();
				if(Utils.getExistingTileEntity(level, cc) instanceof FakeLightBlockEntity)
					level.removeBlock(cc, false);
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
			BlockEntity te = level.getBlockEntity(cc);
			if(te instanceof FakeLightBlockEntity)
			{
				if(deleteOld)
					tempRemove.add(cc);
			}
			else
				it.remove();
		}
		if(genNew)
		{
			float angle = (float)(facing==Direction.NORTH?180: facing==Direction.EAST?90: facing==Direction.WEST?-90: 0);
			float yRotation = rotY;
			double angleX = Math.toRadians(rotX);

			Vec3[] rays = {
					/*Straight*/new Vec3(0, 0, 1),
					/*U,D,L,R*/new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vec3(0, 0, 1),
					/*Intermediate*/new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vec3(0, 0, 1),
					/*Diagonal*/new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vec3(0, 0, 1), new Vec3(0, 0, 1)};
			Matrix4 mat = new Matrix4();
			if(getFacing()==Direction.DOWN)
				mat.scale(1, -1, 1);
			else if(getFacing()!=Direction.UP)
			{
				angle = facing==Direction.DOWN?180: facing==Direction.NORTH?-90: facing==Direction.SOUTH?90: angle;
				if(getFacing().getAxis()==Axis.X)
				{
					mat.rotate(Math.PI/2, -1, 0, 0);
					mat.rotate(Math.PI/2, 0, 0, -getFacing().getAxisDirection().getStep());
				}
				else
				{
					mat.rotate(Math.PI/2, -1, 0, 0);
					if(getFacing()==Direction.SOUTH)//I dunno why south is giving me so much trouble, but this works, so who cares
					{
						mat.rotate(Math.PI, 0, 0, 1);
						if(facing.getAxis()==Axis.X)
							angle = -angle;
					}
				}
			}

			double angleY = Math.toRadians(angle+yRotation);
			mat.rotate(angleY, 0, 1, 0);
			mat.rotate(-angleX, 1, 0, 0);
			rays[0] = mat.apply(rays[0]);
			mat.rotate(Math.PI/8, 0, 1, 0);
			rays[1] = mat.apply(rays[1]);
			mat.rotate(-Math.PI/16, 0, 1, 0);
			rays[5] = mat.apply(rays[5]);
			mat.rotate(-Math.PI/8, 0, 1, 0);
			rays[6] = mat.apply(rays[6]);
			mat.rotate(-Math.PI/16, 0, 1, 0);
			rays[2] = mat.apply(rays[2]);
			mat.rotate(Math.PI/8, 0, 1, 0);
			mat.rotate(Math.PI/8, 1, 0, 0);
			rays[3] = mat.apply(rays[3]);
			mat.rotate(-Math.PI/16, 1, 0, 0);
			rays[7] = mat.apply(rays[7]);
			mat.rotate(-Math.PI/8, 1, 0, 0);
			rays[8] = mat.apply(rays[8]);
			mat.rotate(-Math.PI/16, 1, 0, 0);
			rays[4] = mat.apply(rays[4]);
			mat.rotate(Math.PI/8, 1, 0, 0);
			mat.rotate(Math.PI/16, 1, 0, 0);
			mat.rotate(Math.PI/16, 0, 1, 0);
			rays[9] = mat.apply(rays[9]);
			mat.rotate(-Math.PI/8, 0, 1, 0);
			rays[10] = mat.apply(rays[10]);
			mat.rotate(-Math.PI/8, 1, 0, 0);
			rays[11] = mat.apply(rays[11]);
			mat.rotate(Math.PI/8, 0, 1, 0);
			rays[12] = mat.apply(rays[12]);
			for(int ray = 0; ray < rays.length; ray++)
			{
				int offset = ray==0?0: ray < 4?3: 1;
				placeLightAlongVector(rays[ray], offset, tempRemove);
			}
		}
		this.lightsToBeRemoved.addAll(tempRemove);
	}

	public void placeLightAlongVector(Vec3 vec, int offset, ArrayList<BlockPos> checklist)
	{
		Vec3 light = Vec3.atCenterOf(getBlockPos()).add(0, 0.25, 0);
		int range = 32;
		HashSet<BlockPos> ignore = new HashSet<>();
		ignore.add(getBlockPos());
		BlockPos hit = Utils.rayTraceForFirst(vec.add(light), light.add(vec.x*range, vec.y*range, vec.z*range), level, ignore);
		double maxDistance = hit!=null?Vec3.atCenterOf(hit).add(0, 0.25, 0).distanceToSqr(light): range*range;
		for(int i = 1+offset; i <= range; i++)
		{
			BlockPos target = getBlockPos().offset((int)Math.round(vec.x*i), (int)Math.round(vec.y*i), (int)Math.round(vec.z*i));
			double dist = (vec.x*i*vec.x*i)+(vec.y*i*vec.y*i)+(vec.z*i*vec.z*i);
			if(dist > maxDistance)
				break;
			if(getLevelNonnull().isOutsideBuildHeight(worldPosition))
				continue;
			//&&world.getBlockLightValue(xx,yy,zz)<12 using this makes it not work in daylight .-.

			if(!target.equals(getBlockPos())&&level.isEmptyBlock(target))
			{
				if(!checklist.remove(target))
					lightsToBePlaced.add(target);
				i += 2;
			}
		}
	}


	@Override
	public double getInterdictionRangeSquared()
	{
		return getIsActive()?1024: 0;
	}

	@Override
	public void setRemovedIE()
	{
		SpawnInterdictionHandler.removeFromInterdictionTiles(this);
		for(BlockPos pos : fakeLights)
			if(SafeChunkUtils.getSafeBE(level, pos) instanceof FakeLightBlockEntity light)
				level.removeBlock(pos, false);
		super.setRemovedIE();
	}

	@Override
	public void onChunkUnloaded()
	{
		SpawnInterdictionHandler.removeFromInterdictionTiles(this);
		super.onChunkUnloaded();
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		SpawnInterdictionHandler.addInterdictionTile(this);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.readCustomNBT(nbt, descPacket, provider);
		energyStorage = nbt.getInt("energy");
		redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
		facing = Direction.from3DDataValue(nbt.getInt("facing"));
		rotY = nbt.getFloat("rotY");
		rotX = nbt.getFloat("rotX");
		int lightAmount = nbt.getInt("lightAmount");
		fakeLights.clear();
		for(int i = 0; i < lightAmount; i++)
		{
			int[] icc = nbt.getIntArray("fakeLight_"+i);
			fakeLights.add(new BlockPos(icc[0], icc[1], icc[2]));
		}
		if(level!=null&&level.isClientSide)
			this.markContainingBlockForUpdate(null);
		if(descPacket&&nbt.contains("computerOn"))
		{
			boolean computerOn = nbt.getBoolean("computerOn");
			computerControl.setOneRef();
			computerControl.setEnabled(computerOn);
		}
		else
			computerControl.clear();
		if(level!=null&&getIsActive())
			checkLight();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		super.writeCustomNBT(nbt, descPacket, provider);
		nbt.putInt("energyStorage", energyStorage);
		nbt.putBoolean("redstoneControlInverted", redstoneControlInverted);
		nbt.putInt("facing", facing.ordinal());
		nbt.putFloat("rotY", rotY);
		nbt.putFloat("rotX", rotX);
		nbt.putInt("lightAmount", fakeLights.size());
		for(int i = 0; i < fakeLights.size(); i++)
		{
			BlockPos cc = fakeLights.get(i);
			nbt.putIntArray("fakeLight_"+i, new int[]{cc.getX(), cc.getY(), cc.getZ()});
		}
		if(descPacket&&computerControl.isAttached())
			nbt.putBoolean("computerOn", computerControl.isEnabled());
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==1)
		{
			this.markContainingBlockForUpdate(null);
			checkLight();
			return true;
		}
		return super.triggerEvent(id, arg);
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		return WireType.LV_CATEGORY.equals(cableType.getCategory());
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		BlockPos otherPos = other.position();
		int xDif = otherPos.getX()-worldPosition.getX();
		int yDif = otherPos.getY()-worldPosition.getY();
		int zDif = otherPos.getZ()-worldPosition.getZ();
		double x, y, z;
		switch(getFacing().getAxis())
		{
			case Y:
				x = (Math.abs(xDif) >= Math.abs(zDif))?(xDif >= 0)?.9375: .0625: .5;
				y = (getFacing()==Direction.DOWN)?.9375: .0625;
				z = (Math.abs(zDif) > Math.abs(xDif))?(zDif >= 0)?.9375: .0625: .5;
				break;
			case Z:
				x = (Math.abs(xDif) >= Math.abs(yDif))?(xDif >= 0)?.9375: .0625: .5;
				y = (Math.abs(yDif) > Math.abs(xDif))?(yDif >= 0)?.9375: .0625: .5;
				z = (getFacing()==Direction.NORTH)?.9375: .0625;
				break;
			case X:
			default:
				x = (getFacing()==Direction.WEST)?.9375: .0625;
				y = (Math.abs(yDif) >= Math.abs(zDif))?(yDif >= 0)?.9375: .0625: .5;
				z = (Math.abs(zDif) > Math.abs(yDif))?(zDif >= 0)?.9375: .0625: .5;
				break;
		}
		return new Vec3(x, y, z);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		return Shapes.box(
				getFacing().getAxis()==Axis.X?0: .0625,
				getFacing().getAxis()==Axis.Y?0: .0625,
				getFacing().getAxis()==Axis.Z?0: .0625,
				getFacing().getAxis()==Axis.X?1: .9375,
				getFacing().getAxis()==Axis.Y?1: .9375,
				getFacing().getAxis()==Axis.Z?1: .9375
		);
	}

	@Override
	public boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(!level.isClientSide)
		{
			if(side.getAxis()==this.getFacing().getAxis())
				turnY(player.isShiftKeyDown(), false);
			else
				turnX(player.isShiftKeyDown(), false);
		}
		return true;
	}

	@Override
	public ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(!level.isClientSide)
		{
			redstoneControlInverted = !redstoneControlInverted;
			player.displayClientMessage(
					Component.translatable(Lib.CHAT_INFO+"rsControl."+(redstoneControlInverted?"invertedOn": "invertedOff")),
					true
			);
			setChanged();
			this.markContainingBlockForUpdate(null);
		}
		return ItemInteractionResult.SUCCESS;
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

	@Override
	public void onDirectionalPlacement(Direction side, float hitX, float hitY, float hitZ, LivingEntity placer)
	{
		Direction f = Direction.fromYRot(placer.getYRot());
		if(f==side.getOpposite())
			f = placer.getXRot() > 0?Direction.DOWN: Direction.UP;
		facing = f;
	}
	//computer stuff
	public boolean canComputerTurn()
	{
		return turnCooldown <= 0||!getIsActive();
	}

	public void turnX(boolean dir, boolean throwException)
	{
		if(!canComputerTurn())
		{
			if(throwException)
				throw new RuntimeException("The floodlight can't turn again yet.");
			else
				return;
		}
		this.rotX = Math.min(191.25f, Math.max(-11.25f, rotX+(dir?-11.25f: 11.25f)));
		level.blockEvent(getBlockPos(), getBlockState().getBlock(), 255, 0);
		if(getIsActive())
		{
			turnCooldown = 20;
			shouldUpdate = true;
		}
	}

	public void turnY(boolean dir, boolean throwException)
	{
		if(!canComputerTurn())
		{
			if(throwException)
				throw new RuntimeException("The floodlight can't turn again yet.");
			else
				return;
		}
		this.rotY += dir?-11.25: 11.25;
		this.rotY %= 360;
		level.blockEvent(getBlockPos(), getBlockState().getBlock(), 255, 0);
		if(getIsActive())
		{
			turnCooldown = 20;
			shouldUpdate = true;
		}
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return false;
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return true;
	}

	@Override
	public int getRequestedEnergy()
	{
		if(energyStorage < maximumStorage)
			return maximumStorage-energyStorage;
		return 0;
	}

	@Override
	public void insertEnergy(int amount)
	{
		energyStorage += amount;
	}

	@Override
	public Stream<ComputerControlState> getAllComputerControlStates()
	{
		return Stream.of(computerControl);
	}
}

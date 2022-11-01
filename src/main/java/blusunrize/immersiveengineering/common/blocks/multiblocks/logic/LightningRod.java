package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.multiblocks.blocks.*;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LightningRod implements IMultiblockLogic<LightningRod.State>, IServerTickableMultiblock<LightningRod.State>
{
	public static final BlockPos MASTER_OFFSET = new BlockPos(1, 1, 1);

	@Override
	public void tickServer(IMultiblockContext<State> context)
	{
		final State state = context.getState();
		final IMultiblockLevel level = context.getLevel();
		if(state.energy.getEnergyStored() > 0)
			for(final CapabilityReference<IEnergyStorage> outputRef : state.energyOutputs)
			{
				final IEnergyStorage output = outputRef.getNullable();
				if(output!=null)
				{
					final int accepted = output.receiveEnergy(state.energy.getEnergyStored(), false);
					state.energy.modifyEnergyStored(-accepted);
				}
			}

		if(level.shouldTickModulo(256))
			state.fenceNet = null;
		if(state.fenceNet==null)
			state.fenceNet = getFenceNet(level.getRawLevel(), level.toAbsolute(MASTER_OFFSET));
		if(state.fenceNet.isValid()
				&&level.shouldTickModulo(128)
				&&(level.isThundering()||(level.isRaining()&&ApiUtils.RANDOM.nextInt(10)==0)))
		{
			final BlockPos strikePosition = state.fenceNet.getAbsoluteStrikePosition(level);
			if(strikePosition!=null)
			{
				state.energy.setStoredEnergy(IEServerConfig.MACHINES.lightning_output.get());
				LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(level.getRawLevel());
				lightningboltentity.moveTo(Vec3.atBottomCenterOf(strikePosition));
				lightningboltentity.setVisualOnly(true);
				level.getRawLevel().addFreshEntity(lightningboltentity);
			}
		}
	}

	@Nonnull
	private static FenceNet getFenceNet(Level level, BlockPos absoluteMasterPos)
	{
		int height = 0;
		boolean broken = false;
		BlockPos lastFence = null;
		for(int i = absoluteMasterPos.getY()+2; i < level.getMaxBuildHeight()-1; i++)
		{
			BlockPos pos = new BlockPos(absoluteMasterPos.getX(), i, absoluteMasterPos.getZ());
			if(!broken&&isFence(level, pos))
			{
				height++;
				lastFence = pos;
			}
			else if(!level.isEmptyBlock(pos))
				return FenceNet.INVALID;
			else
			{
				if(!broken)
					broken = true;
			}
		}
		if(lastFence==null)
			return FenceNet.INVALID;

		ArrayList<BlockPos> openList = new ArrayList<>();
		ArrayList<BlockPos> closedList = new ArrayList<>();
		openList.add(lastFence);
		while(!openList.isEmpty()&&closedList.size() < 256)
		{
			BlockPos next = openList.get(0);
			if(!closedList.contains(next)&&isFence(level, next))
			{
				closedList.add(next);
				openList.add(next.relative(Direction.WEST));
				openList.add(next.relative(Direction.EAST));
				openList.add(next.relative(Direction.NORTH));
				openList.add(next.relative(Direction.SOUTH));
				openList.add(next.relative(Direction.UP));
			}
			openList.remove(0);
		}
		return new FenceNet(height, closedList);
	}

	@Override
	public State createInitialState(MultiblockCapabilitySource capabilitySource)
	{
		return new State(capabilitySource);
	}

	@Override
	public <T> LazyOptional<T> getCapability(
			IMultiblockContext<State> ctx, BlockPos posInMultiblock, @Nullable RelativeBlockFace side, Capability<T> cap
	)
	{
		final State state = ctx.getState();
		if(side==null||(posInMultiblock.getY()==1&&(posInMultiblock.getX()+posInMultiblock.getZ())%2==1))
		{
			state.energyCap = ctx.orRegisterCapability(state.energyCap, state.energy);
			return ForgeCapabilities.ENERGY.orEmpty(cap, state.energyCap);
		}
		else
			return LazyOptional.empty();
	}

	@Override
	public VoxelShape getShape(BlockPos posInMultiblock)
	{
		if(new BlockPos(1, 2, 1).equals(posInMultiblock))
			return Shapes.box(-.125f, 0, -.125f, 1.125f, 1, 1.125f);
		if((posInMultiblock.getX()==1&&posInMultiblock.getZ()==1)
				||(posInMultiblock.getY() < 2&&(posInMultiblock.getX()+posInMultiblock.getZ())%2==1))
			return Shapes.block();
		if(posInMultiblock.getY()==0)
			return Shapes.box(0, 0, 0, 1, .5f, 1);
		float xMin = 0;
		float xMax = 1;
		float yMin = 0;
		float yMax = 1;
		float zMin = 0;
		float zMax = 1;
		if(posInMultiblock.getX()%2==0&&posInMultiblock.getZ()%2==0)
		{
			if(posInMultiblock.getY() < 2)
			{
				yMin = -.5f;
				yMax = 1.25f;
				xMin = posInMultiblock.getX()!=0?.1875f: .5625f;
				xMax = posInMultiblock.getX()!=2?.8125f: .4375f;
				zMin = posInMultiblock.getZ() >= 2?.1875f: .5625f;
				zMax = posInMultiblock.getZ()!=2?.8125f: .4375f;
			}
			else
			{
				yMin = .25f;
				yMax = .75f;
				xMin = posInMultiblock.getX()!=0?0: .375f;
				xMax = posInMultiblock.getX()!=2?1: .625f;
				zMin = posInMultiblock.getZ() >= 2?0: .375f;
				zMax = posInMultiblock.getZ()!=2?1: .625f;
			}
		}
		else if(posInMultiblock.getY() >= 2)
		{
			yMin = .25f;
			yMax = .75f;
			xMin = posInMultiblock.getX()==0?.375f: 0;
			xMax = posInMultiblock.getX()==2?.625f: 1;
			zMin = posInMultiblock.getZ()==0?.375f: 0;
			zMax = posInMultiblock.getZ()==2?.625f: 1;
		}
		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	private static boolean isFence(Level level, BlockPos pos)
	{
		return Utils.isBlockAt(level, pos, MetalDecoration.STEEL_FENCE.get());
	}

	public static class State implements IMultiblockState
	{
		private final MutableEnergyStorage energy = new MutableEnergyStorage(
				IEServerConfig.MACHINES.lightning_output.get()
		);
		private final ImmutableList<CapabilityReference<IEnergyStorage>> energyOutputs;
		@Nullable
		private FenceNet fenceNet = null;
		@Nullable
		private LazyOptional<IEnergyStorage> energyCap;

		public State(MultiblockCapabilitySource capabilitySource)
		{
			ImmutableList.Builder<CapabilityReference<IEnergyStorage>> builder = ImmutableList.builder();
			for(RelativeBlockFace face : RelativeBlockFace.HORIZONTAL)
				builder.add(capabilitySource.getCapabilityAt(
						ForgeCapabilities.ENERGY,
						face.offsetRelative(MASTER_OFFSET, 2),
						face.getOpposite()
				));
			this.energyOutputs = builder.build();
		}

		@Override
		public void writeSaveNBT(CompoundTag nbt)
		{
			EnergyHelper.serializeTo(energy, nbt);
		}

		@Override
		public void writeSyncNBT(CompoundTag nbt)
		{
		}

		@Override
		public void readSaveNBT(CompoundTag nbt)
		{
			EnergyHelper.deserializeFrom(energy, nbt);
		}

		@Override
		public void readSyncNBT(CompoundTag nbt)
		{
		}
	}

	private record FenceNet(int height, List<BlockPos> absoluteFencePositions)
	{
		public static final FenceNet INVALID = new FenceNet(0, List.of());

		public boolean isValid()
		{
			return !absoluteFencePositions.isEmpty();
		}

		public BlockPos getAbsoluteStrikePosition(IMultiblockLevel level)
		{
			int i = height+absoluteFencePositions.size();
			final int masterY = level.getAbsoluteOrigin().getY();
			if(ApiUtils.RANDOM.nextInt(4096*level.getMaxBuildHeight()) < i*(masterY+i))
				return absoluteFencePositions.get(ApiUtils.RANDOM.nextInt(absoluteFencePositions.size()));
			else
				return null;
		}
	}
}

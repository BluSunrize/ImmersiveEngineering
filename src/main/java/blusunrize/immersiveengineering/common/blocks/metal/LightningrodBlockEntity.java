/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LightningrodBlockEntity extends MultiblockPartBlockEntity<LightningrodBlockEntity> implements IBlockBounds
{
	private final MutableEnergyStorage energyStorage = new MutableEnergyStorage(IEServerConfig.MACHINES.lightning_output.get());
	private final MultiblockCapability<?, IEnergyStorage> energyCap = new MultiblockCapability<>(
			be -> be.energyCap, LightningrodBlockEntity::master, this, registerEnergyOutput(energyStorage)
	);

	@Nullable
	private List<BlockPos> fenceNet = null;
	private int height;

	public LightningrodBlockEntity(BlockEntityType<LightningrodBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(IEMultiblocks.LIGHTNING_ROD, type, false, pos, state);
	}

	@Override
	public void tickServer()
	{
		if(energyStorage.getEnergyStored() > 0)
		{
			BlockEntity tileEntity;
			for(Direction f : DirectionUtils.BY_HORIZONTAL_INDEX)
			{
				tileEntity = Utils.getExistingTileEntity(level, getBlockPos().relative(f, 2));
				int output = EnergyHelper.insertFlux(tileEntity, f.getOpposite(), energyStorage.getMaxEnergyStored(), true);
				output = energyStorage.extractEnergy(output, false);
				EnergyHelper.insertFlux(tileEntity, f.getOpposite(), output, false);
			}
		}

		if(level.getGameTime()%256==((getBlockPos().getX()^getBlockPos().getZ())&255))
			fenceNet = null;
		if(fenceNet==null)
			fenceNet = this.getFenceNet();
		if(fenceNet!=null&&fenceNet.size() > 0
				&&level.getGameTime()%128==((getBlockPos().getX()^getBlockPos().getZ())&127)
				&&(level.isThundering()||(level.isRaining()&&Utils.RAND.nextInt(10)==0)))
		{
			int i = this.height+this.fenceNet.size();
			if(Utils.RAND.nextInt(4096*level.getMaxBuildHeight()) < i*(getBlockPos().getY()+i))
			{
				this.energyStorage.setStoredEnergy(IEServerConfig.MACHINES.lightning_output.get());
				BlockPos pos = fenceNet.get(Utils.RAND.nextInt(fenceNet.size()));
				LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(level);
				lightningboltentity.moveTo(Vec3.atBottomCenterOf(pos));
				lightningboltentity.setVisualOnly(true);
				level.addFreshEntity(lightningboltentity);
			}
		}
	}

	@Nullable
	private List<BlockPos> getFenceNet()
	{
		this.height = 0;
		boolean broken = false;
		BlockPos lastFence = null;
		for(int i = getBlockPos().getY()+2; i < level.getMaxBuildHeight()-1; i++)
		{
			BlockPos pos = new BlockPos(getBlockPos().getX(), i, getBlockPos().getZ());
			if(!broken&&isFence(pos))
			{
				this.height++;
				lastFence = pos;
			}
			else if(!level.isEmptyBlock(pos))
				return null;
			else
			{
				if(!broken)
					broken = true;
			}
		}
		if(lastFence==null)
			return null;

		ArrayList<BlockPos> openList = new ArrayList<>();
		ArrayList<BlockPos> closedList = new ArrayList<>();
		openList.add(lastFence);
		while(!openList.isEmpty()&&closedList.size() < 256)
		{
			BlockPos next = openList.get(0);
			if(!closedList.contains(next)&&isFence(next))
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
		return closedList;
	}

	private boolean isFence(BlockPos pos)
	{
		return Utils.isBlockAt(level, pos, MetalDecoration.STEEL_FENCE.get());
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		EnergyHelper.deserializeFrom(energyStorage, nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		EnergyHelper.serializeTo(energyStorage, nbt);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
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
			Direction facing = getFacing();
			if(posInMultiblock.getY() < 2)
			{
				yMin = -.5f;
				yMax = 1.25f;
				xMin = (facing.getAxis()==Axis.X?(posInMultiblock.getZ() < 2^facing==Direction.EAST): (posInMultiblock.getX()==2^facing==Direction.NORTH))?.8125f: .4375f;
				xMax = (facing.getAxis()==Axis.X?(posInMultiblock.getZ()==2^facing==Direction.EAST): (posInMultiblock.getX()==0^facing==Direction.NORTH))?.1875f: .5625f;
				zMin = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==2^facing==Direction.EAST): (posInMultiblock.getZ()==2^facing==Direction.NORTH))?.8125f: .4375f;
				zMax = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==0^facing==Direction.EAST): (posInMultiblock.getZ() < 2^facing==Direction.NORTH))?.1875f: .5625f;
			}
			else
			{
				yMin = .25f;
				yMax = .75f;
				xMin = (facing.getAxis()==Axis.X?(posInMultiblock.getZ() < 2^facing==Direction.EAST): (posInMultiblock.getX()==2^facing==Direction.NORTH))?1: .625f;
				xMax = (facing.getAxis()==Axis.X?(posInMultiblock.getZ()==2^facing==Direction.EAST): (posInMultiblock.getX()==0^facing==Direction.NORTH))?0: .375f;
				zMin = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==2^facing==Direction.EAST): (posInMultiblock.getZ()==2^facing==Direction.NORTH))?1: .625f;
				zMax = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==0^facing==Direction.EAST): (posInMultiblock.getZ() < 2^facing==Direction.NORTH))?0: .375f;
			}
		}
		else if(posInMultiblock.getY() >= 2)
		{
			yMin = .25f;
			yMax = .75f;
			xMin = offsetToMaster.getX() < 0?.375f: 0;
			xMax = offsetToMaster.getX() > 0?.625f: 1;
			zMin = offsetToMaster.getZ() < 0?.375f: 0;
			zMax = offsetToMaster.getZ() > 0?.625f: 1;
		}
		return Shapes.box(xMin, yMin, zMin, xMax, yMax, zMax);
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return false;
	}

	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityEnergy.ENERGY&&isEnergyPos())
			return energyCap.getAndCast();
		return super.getCapability(capability, facing);
	}

	private boolean isEnergyPos()
	{
		return posInMultiblock.getY()==1&&(posInMultiblock.getX()+posInMultiblock.getZ())%2==1;
	}
}
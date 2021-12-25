/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LightningrodTileEntity extends MultiblockPartTileEntity<LightningrodTileEntity> implements
		IIEInternalFluxHandler, IBlockBounds
{
	FluxStorage energyStorage = new FluxStorage(IEServerConfig.MACHINES.lightning_output.get());

	@Nullable
	private List<BlockPos> fenceNet = null;
	private int height;

	public LightningrodTileEntity()
	{
		super(IEMultiblocks.LIGHTNING_ROD, IETileTypes.LIGHTNING_ROD.get(), false);
	}

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		if(!level.isClientSide&&formed&&new BlockPos(1, 1, 1).equals(posInMultiblock))
		{
			if(energyStorage.getEnergyStored() > 0)
			{
				BlockEntity tileEntity;
				for(Direction f : DirectionUtils.BY_HORIZONTAL_INDEX)
				{
					tileEntity = Utils.getExistingTileEntity(level, getBlockPos().relative(f, 2));
					int output = EnergyHelper.insertFlux(tileEntity, f.getOpposite(), energyStorage.getLimitExtract(), true);
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
					this.energyStorage.setEnergy(IEServerConfig.MACHINES.lightning_output.get());
					BlockPos pos = fenceNet.get(Utils.RAND.nextInt(fenceNet.size()));
					LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(level);
					lightningboltentity.moveTo(Vec3.atBottomCenterOf(pos));
					lightningboltentity.setVisualOnly(true);
					level.addFreshEntity(lightningboltentity);
				}
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
		return Utils.isBlockAt(level, pos, MetalDecoration.steelFence);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		energyStorage.readFromNBT(nbt);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		energyStorage.writeToNBT(nbt);
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
				xMin = (facing.getAxis()==Axis.X?(posInMultiblock.getZ()==2^facing==Direction.EAST): (posInMultiblock.getX()==0^facing==Direction.NORTH))?.1875f: .5625f;
				xMax = (facing.getAxis()==Axis.X?(posInMultiblock.getZ() < 2^facing==Direction.EAST): (posInMultiblock.getX()==2^facing==Direction.NORTH))?.8125f: .4375f;
				zMin = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==0^facing==Direction.EAST): (posInMultiblock.getZ() < 2^facing==Direction.NORTH))?.1875f: .5625f;
				zMax = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==2^facing==Direction.EAST): (posInMultiblock.getZ()==2^facing==Direction.NORTH))?.8125f: .4375f;
			}
			else
			{
				yMin = .25f;
				yMax = .75f;
				xMin = (facing.getAxis()==Axis.X?(posInMultiblock.getZ()==2^facing==Direction.EAST): (posInMultiblock.getX()==0^facing==Direction.NORTH))?0: .375f;
				xMax = (facing.getAxis()==Axis.X?(posInMultiblock.getZ() < 2^facing==Direction.EAST): (posInMultiblock.getX()==2^facing==Direction.NORTH))?1: .625f;
				zMin = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==0^facing==Direction.EAST): (posInMultiblock.getZ() < 2^facing==Direction.NORTH))?0: .375f;
				zMax = (facing.getAxis()==Axis.X?(posInMultiblock.getX()==2^facing==Direction.EAST): (posInMultiblock.getZ()==2^facing==Direction.NORTH))?1: .625f;
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
	public FluxStorage getFluxStorage()
	{
		LightningrodTileEntity master = this.master();
		if(master!=null)
			return master.energyStorage;
		return energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		return this.formed&&this.isEnergyPos()?IOSideConfig.OUTPUT: IOSideConfig.NONE;
	}

	private IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, null);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(this.formed&&this.isEnergyPos())
			return wrapper;
		return null;
	}

	private boolean isEnergyPos()
	{
		return posInMultiblock.getY()==1&&(posInMultiblock.getX()+posInMultiblock.getZ())%2==1;
	}
}
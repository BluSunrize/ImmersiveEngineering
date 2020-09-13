/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHasDummyBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.items.CoresampleItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SampleDrillTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxHandler, IHasDummyBlocks,
		IPlayerInteraction, IModelOffsetProvider
{
	public FluxStorage energyStorage = new FluxStorage(8000);
	public int dummy = 0;
	public int process = 0;
	public boolean active = false;
	@Nonnull
	public ItemStack sample = ItemStack.EMPTY;

	public SampleDrillTileEntity()
	{
		super(IETileTypes.SAMPLE_DRILL.get());
	}

	public static boolean _Immovable()
	{
		return true;
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(dummy!=0||world.isAirBlock(getPos().add(0, -1, 0))||!sample.isEmpty())
			return;
		if(world.isRemote&&active)
		{
			process++;
			return;
		}

		boolean powered = isRSPowered();
		final boolean prevActive = active;
		int totalTime = IEConfig.MACHINES.coredrill_time.get();
		int consumption = IEConfig.MACHINES.coredrill_consumption.get();
		if(!active&&powered)
			active = true;
		else if(active&&!powered&&process >= totalTime)
			active = false;


		if(active&&process < totalTime)
			if(energyStorage.extractEnergy(consumption, false)==consumption)
			{
				process++;
				if(process >= totalTime)
				{
					MineralWorldInfo info = ExcavatorHandler.getMineralWorldInfo(world, getPos());
					this.sample = createCoreSample(info);
				}
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		if(prevActive!=active)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	public float getSampleProgress()
	{
		return process/(float)IEConfig.MACHINES.coredrill_time.get();
	}

	public boolean isSamplingFinished()
	{
		return process >= IEConfig.MACHINES.coredrill_time.get();
	}

	@Nullable
	public MineralMix[] getVein()
	{
		if(sample.isEmpty())
			return null;
		else
			return CoresampleItem.getMineralMixes(sample);
	}

	public int getExpectedVeinYield()
	{
		if(sample.isEmpty())
			return -1;
		return ExcavatorHandler.mineralVeinYield-sample.getOrCreateTag().getInt("depletion");
	}

	@Nonnull
	public ItemStack createCoreSample(@Nullable MineralWorldInfo info)
	{
		ItemStack stack = new ItemStack(Misc.coresample);
		ItemNBTHelper.putLong(stack, "timestamp", world.getGameTime());
		CoresampleItem.setDimenson(stack, world.getDimension().getType());
		CoresampleItem.setCoords(stack, getPos());
		CoresampleItem.setMineralInfo(stack, info, getPos());
		return stack;
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.writeToNBT(nbt);
		nbt.putInt("dummy", dummy);
		nbt.putInt("process", process);
		nbt.putBoolean("active", active);
		if(!sample.isEmpty())
			nbt.put("sample", sample.write(new CompoundNBT()));
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		energyStorage.readFromNBT(nbt);
		dummy = nbt.getInt("dummy");
		process = nbt.getInt("process");
		active = nbt.getBoolean("active");
		if(nbt.contains("sample", NBT.TAG_COMPOUND))
			sample = ItemStack.read(nbt.getCompound("sample"));

	}

	@OnlyIn(Dist.CLIENT)
	private AxisAlignedBB renderAABB;

	@OnlyIn(Dist.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderAABB==null)
			if(dummy==0)
				renderAABB = new AxisAlignedBB(getPos(), getPos().add(1, 3, 1));
			else
				renderAABB = new AxisAlignedBB(getPos(), getPos());
		return renderAABB;
	}


	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(dummy > 0)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(te instanceof SampleDrillTileEntity)
				return ((SampleDrillTileEntity)te).getFluxStorage();
		}
		return energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return dummy==0&&facing!=null&&facing.getAxis()!=Axis.Y?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper[] wrappers = {
			new IEForgeEnergyWrapper(this, Direction.NORTH),
			new IEForgeEnergyWrapper(this, Direction.SOUTH),
			new IEForgeEnergyWrapper(this, Direction.WEST),
			new IEForgeEnergyWrapper(this, Direction.EAST)
	};

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(dummy==0&&facing!=null&&facing.getAxis()!=Axis.Y)
			return wrappers[facing.ordinal()-2];
		return null;
	}

	@Override
	public boolean isDummy()
	{
		return dummy > 0;
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getPos().down(dummy);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockItemUseContext ctx, BlockState state)
	{
		state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		for(int i = 1; i <= 2; i++)
		{
			world.setBlockState(pos.add(0, i, 0), state);
			((SampleDrillTileEntity)world.getTileEntity(pos.add(0, i, 0))).dummy = i;
		}
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 2; i++)
			if(world.getTileEntity(getPos().add(0, -dummy, 0).add(0, i, 0)) instanceof SampleDrillTileEntity)
				world.removeBlock(getPos().add(0, -dummy, 0).add(0, i, 0), false);
	}

	@Override
	public boolean interact(Direction side, PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(dummy!=0)
		{
			TileEntity te = world.getTileEntity(getPos().add(0, -dummy, 0));
			if(te instanceof SampleDrillTileEntity)
				return ((SampleDrillTileEntity)te).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
		}

		if(!this.sample.isEmpty())
		{
			if(!world.isRemote)
				player.entityDropItem(this.sample.copy(), .5f);
			this.sample = ItemStack.EMPTY;
			this.active = false;
			markDirty();
			this.markContainingBlockForUpdate(null);
			return true;
		}
		else if(!this.active)
		{
			if(energyStorage.getEnergyStored() >= IEConfig.MACHINES.coredrill_consumption.get())
			{
				this.active = true;
				markDirty();
				this.markContainingBlockForUpdate(null);
			}
			return true;
		}
		return false;
	}

	@Override
	public BlockPos getModelOffset(BlockState state)
	{
		return new BlockPos(0, dummy, 0);
	}
}
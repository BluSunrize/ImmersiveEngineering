/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeTileEntity.DirectionalFluidOutput;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class FluidPumpTileEntity extends IEBaseTileEntity implements TickableBlockEntity, IBlockBounds, IHasDummyBlocks,
		IConfigurableSides, IFluidPipe, IIEInternalFluxHandler, IBlockOverlayText
{
	public Map<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);

	{
		for(Direction d : DirectionUtils.VALUES)
		{
			if(d==Direction.DOWN)
				sideConfig.put(d, IOSideConfig.INPUT);
			else
				sideConfig.put(d, IOSideConfig.NONE);
		}
	}

	public FluidTank tank = new FluidTank(4*FluidAttributes.BUCKET_VOLUME);
	public FluxStorage energyStorage = new FluxStorage(8000);
	public boolean placeCobble = true;

	boolean checkingArea = false;
	Fluid searchFluid = null;
	ArrayList<BlockPos> openList = new ArrayList<>();
	ArrayList<BlockPos> closedList = new ArrayList<>();
	ArrayList<BlockPos> checked = new ArrayList<>();

	public FluidPumpTileEntity()
	{
		super(IETileTypes.FLUID_PUMP.get());
	}

	private Map<Direction, CapabilityReference<IFluidHandler>> neighborFluids = new EnumMap<>(Direction.class);

	{
		for(Direction neighbor : DirectionUtils.VALUES)
			neighborFluids.put(neighbor,
					CapabilityReference.forNeighbor(this, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, neighbor));
	}

	@Override
	public void tick()
	{
		checkForNeedlessTicking();
		if(isDummy()||level.isClientSide)
			return;
		if(tank.getFluidAmount() > 0)
		{
			int i = outputFluid(tank.getFluid(), FluidAction.EXECUTE);
			tank.drain(i, FluidAction.EXECUTE);
		}

		int consumption = IEServerConfig.MACHINES.pump_consumption.get();
		boolean hasRSSignal = isRSPowered();
		if(!hasRSSignal)
		{
			BlockEntity above = level.getBlockEntity(getBlockPos().above());
			if(above instanceof FluidPumpTileEntity)
				hasRSSignal = ((FluidPumpTileEntity)above).isRSPowered();
		}
		if(isRSPowered())
		{
			for(Direction f : Direction.values())
				if(sideConfig.get(f)==IOSideConfig.INPUT)
				{
					CapabilityReference<IFluidHandler> input = neighborFluids.get(f);
					if(input.isPresent())
					{
						IFluidHandler handler = input.get();
						int drainAmount = IFluidPipe.getTransferableAmount(this.canOutputPressurized(false));
						FluidStack drain = handler.drain(500, FluidAction.SIMULATE);
						if(drain.isEmpty())
							continue;
						int out = this.outputFluid(drain, FluidAction.EXECUTE);
						handler.drain(out, FluidAction.EXECUTE);
					}
					else if(level.getGameTime()%20==((getBlockPos().getX()^getBlockPos().getZ())&19)
							&&level.getFluidState(getBlockPos().relative(f)).getType().is(FluidTags.WATER)
							&&IEServerConfig.MACHINES.pump_infiniteWater.get()
							&&tank.fill(new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME), FluidAction.SIMULATE)==FluidAttributes.BUCKET_VOLUME
							&&this.energyStorage.extractEnergy(consumption, true) >= consumption)
					{
						int connectedSources = 0;
						for(Direction f2 : DirectionUtils.BY_HORIZONTAL_INDEX)
						{
							FluidState waterState = level.getFluidState(getBlockPos().relative(f).relative(f2));
							if(waterState.getType().is(FluidTags.WATER)&&waterState.isSource())
								connectedSources++;
						}
						if(connectedSources > 1)
						{
							this.energyStorage.extractEnergy(consumption, false);
							this.tank.fill(new FluidStack(Fluids.WATER, FluidAttributes.BUCKET_VOLUME), FluidAction.EXECUTE);
						}
					}
				}
			if(level.getGameTime()%40==(((getBlockPos().getX()^getBlockPos().getZ()))%40+40)%40)
			{
				if(closedList.isEmpty())
					prepareAreaCheck();
				else
				{
					int target = closedList.size()-1;
					BlockPos pos = closedList.get(target);
					FluidStack fs = Utils.drainFluidBlock(level, pos, FluidAction.SIMULATE);
					if(fs==null)
						closedList.remove(target);
					else if(tank.fill(fs, FluidAction.SIMULATE)==fs.getAmount()
							&&this.energyStorage.extractEnergy(consumption, true) >= consumption)
					{
						this.energyStorage.extractEnergy(consumption, false);
						fs = Utils.drainFluidBlock(level, pos, FluidAction.EXECUTE);
						if(IEServerConfig.MACHINES.pump_placeCobble.get()&&placeCobble)
							level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState());
						this.tank.fill(fs, FluidAction.EXECUTE);
						closedList.remove(target);
					}
				}
			}
		}

		if(checkingArea)
			checkAreaTick();
	}

	public void prepareAreaCheck()
	{
		openList.clear();
		closedList.clear();
		checked.clear();
		for(Direction f : Direction.values())
			if(sideConfig.get(f)==IOSideConfig.INPUT)
			{
				openList.add(getBlockPos().relative(f));
				checkingArea = true;
			}
	}

	public void checkAreaTick()
	{
		boolean infiniteWater = IEServerConfig.MACHINES.pump_infiniteWater.get();
		BlockPos next = null;
		final int closedListMax = 2048;
		int timeout = 0;
		while(timeout < 64&&closedList.size() < closedListMax&&!openList.isEmpty())
		{
			timeout++;
			next = openList.get(0);
			if(!checked.contains(next))
			{
				Fluid fluid = Utils.getRelatedFluid(getWorldNonnull(), next);
				if(fluid!=Fluids.EMPTY&&(fluid!=Fluids.WATER||!infiniteWater)&&(searchFluid==null||fluid==searchFluid))
				{
					if(searchFluid==null)
						searchFluid = fluid;

					if(!Utils.drainFluidBlock(level, next, FluidAction.SIMULATE).isEmpty())
						closedList.add(next);
					for(Direction f : Direction.values())
					{
						BlockPos pos2 = next.relative(f);
						fluid = Utils.getRelatedFluid(level, pos2);
						if(fluid!=Fluids.EMPTY&&!checked.contains(pos2)&&!closedList.contains(pos2)&&!openList.contains(pos2)&&(fluid!=Fluids.WATER
								||!infiniteWater)&&(searchFluid==null||fluid==searchFluid))
							openList.add(pos2);
					}
				}
				checked.add(next);
			}
			openList.remove(0);
		}
		if(closedList.size() >= closedListMax||openList.isEmpty())
			checkingArea = false;
	}

	public int outputFluid(FluidStack fs, FluidAction action)
	{
		if(fs.isEmpty())
			return 0;

		int canAccept = fs.getAmount();
		if(canAccept <= 0)
			return 0;

		int accelPower = IEServerConfig.MACHINES.pump_consumption_accelerate.get();
		final int fluidForSort = canAccept;
		int sum = 0;
		HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<>();
		for(Direction f : Direction.values())
			if(sideConfig.get(f)==IOSideConfig.OUTPUT)
			{
				CapabilityReference<IFluidHandler> output = neighborFluids.get(f);
				if(output.isPresent())
				{
					BlockEntity tile = getWorldNonnull().getBlockEntity(worldPosition.relative(f));
					IFluidHandler handler = output.get();
					FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, fs.getAmount(), true);
					if(tile instanceof FluidPipeTileEntity&&this.energyStorage.extractEnergy(accelPower, true) >= accelPower)
						insertResource.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true);
					int temp = handler.fill(insertResource, FluidAction.SIMULATE);
					if(temp > 0)
					{
						sorting.put(new DirectionalFluidOutput(handler, tile, f), temp);
						sum += temp;
					}
				}
			}
		if(sum > 0)
		{
			int f = 0;
			int i = 0;
			for(DirectionalFluidOutput output : sorting.keySet())
			{
				float prio = sorting.get(output)/(float)sum;
				int amount = (int)(fluidForSort*prio);
				if(i++==sorting.size()-1)
					amount = canAccept;
				FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, amount, true);
				if(output.containingTile instanceof FluidPipeTileEntity&&this.energyStorage.extractEnergy(accelPower, true) >= accelPower)
				{
					this.energyStorage.extractEnergy(accelPower, false);
					insertResource.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true);
				}
				int r = output.output.fill(insertResource, action);
				f += r;
				canAccept -= r;
				if(canAccept <= 0)
					break;
			}
			return f;
		}
		return 0;
	}


	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int[] sideConfigArray = nbt.getIntArray("sideConfig");
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.VALUES[sideConfigArray[d.ordinal()]]);
		if(nbt.contains("placeCobble", NBT.TAG_BYTE))
			placeCobble = nbt.getBoolean("placeCobble");
		tank.readFromNBT(nbt.getCompound("tank"));
		energyStorage.readFromNBT(nbt);
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		int[] sideConfigArray = new int[6];
		for(Direction d : DirectionUtils.VALUES)
			sideConfigArray[d.ordinal()] = sideConfig.get(d).ordinal();
		nbt.putIntArray("sideConfig", sideConfigArray);
		nbt.putBoolean("placeCobble", placeCobble);
		nbt.put("tank", tank.writeToNBT(new CompoundTag()));
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, Player p)
	{
		if(side!=Direction.UP&&!isDummy())
		{
			sideConfig.put(side, IOSideConfig.next(sideConfig.get(side)));
			this.setChanged();
			this.markContainingBlockForUpdate(null);
			getWorldNonnull().blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
			return true;
		}
		else if(p.isShiftKeyDown())
		{
			FluidPumpTileEntity master = this;
			if(isDummy())
			{
				BlockEntity tmp = level.getBlockEntity(worldPosition.below());
				if(tmp instanceof FluidPumpTileEntity)
					master = (FluidPumpTileEntity)tmp;
			}
			master.placeCobble = !master.placeCobble;
			ChatUtils.sendServerNoSpamMessages(p, new TranslatableComponent(Lib.CHAT_INFO+"pump.placeCobble."+master.placeCobble));
			return true;
		}
		return false;
	}

	private Map<Direction, LazyOptional<IFluidHandler>> sidedFluidHandler = new EnumMap<>(Direction.class);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&facing!=null&&!isDummy())
		{
			if(!sidedFluidHandler.containsKey(facing))
				sidedFluidHandler.put(facing, registerConstantCap(new SidedFluidHandler(this, facing)));
			return sidedFluidHandler.get(facing).cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(hammer&&IEClientConfig.showTextOverlay.get()&&!isDummy()&&mop instanceof BlockHitResult)
		{
			BlockHitResult brtr = (BlockHitResult)mop;
			IOSideConfig i = sideConfig.get(brtr.getDirection());
			IOSideConfig j = sideConfig.get(brtr.getDirection().getOpposite());
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectFluid.", i, j);
		}
		return null;
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}

	public void setDummy(boolean dummy)
	{
		BlockState old = getBlockState();
		BlockState newState = old.setValue(IEProperties.MULTIBLOCKSLAVE, dummy);
		setState(newState);
	}

	static class SidedFluidHandler implements IFluidHandler
	{
		FluidPumpTileEntity pump;
		Direction facing;

		SidedFluidHandler(FluidPumpTileEntity pump, Direction facing)
		{
			this.pump = pump;
			this.facing = facing;
		}

		@Override
		public int getTanks()
		{
			return pump.tank.getTanks();
		}

		@Nonnull
		@Override
		public FluidStack getFluidInTank(int tank)
		{
			return pump.tank.getFluidInTank(tank);
		}

		@Override
		public int getTankCapacity(int tank)
		{
			return pump.tank.getTankCapacity(tank);
		}

		@Override
		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			if(pump.sideConfig.get(facing)!=IOSideConfig.INPUT)
				return false;
			return pump.tank.isFluidValid(tank, stack);
		}

		@Override
		public int fill(FluidStack resource, FluidAction action)
		{
			if(resource.isEmpty()||pump.sideConfig.get(facing)!=IOSideConfig.INPUT)
				return 0;
			return pump.tank.fill(resource, action);
		}

		@Override
		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			return this.drain(resource.getAmount(), action);
		}

		@Override
		public FluidStack drain(int maxDrain, FluidAction action)
		{
			if(pump.sideConfig.get(facing)!=IOSideConfig.OUTPUT)
				return FluidStack.EMPTY;
			return pump.tank.drain(maxDrain, action);
		}
	}

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		if(isDummy())
		{
			BlockEntity te = level.getBlockEntity(getBlockPos().offset(0, -1, 0));
			if(te instanceof FluidPumpTileEntity)
				return ((FluidPumpTileEntity)te).getFluxStorage();
		}
		return energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return isDummy()&&facing==Direction.UP?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	IEForgeEnergyWrapper wrapper = new IEForgeEnergyWrapper(this, Direction.UP);

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(!isDummy()&&facing==Direction.UP)
			return null;
		return wrapper;
	}

	@Override
	public boolean isDummy()
	{
		return getBlockState().getValue(IEProperties.MULTIBLOCKSLAVE);
	}

	@Nullable
	@Override
	public IGeneralMultiblock master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().below();
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return this.getClass().isInstance(te)?(IGeneralMultiblock)te: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		BlockPos dummyPos = worldPosition.above();
		getWorldNonnull().setBlockAndUpdate(dummyPos, IEBaseBlock.applyLocationalWaterlogging(
				state, getWorldNonnull(), dummyPos
		));
		BlockEntity tile = getWorldNonnull().getBlockEntity(dummyPos);
		if(tile instanceof FluidPumpTileEntity)
			((FluidPumpTileEntity)tile).setDummy(true);
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 1; i++)
			if(Utils.isBlockAt(level, getBlockPos().offset(0, isDummy()?-1: 0, 0).offset(0, i, 0),
					MetalDevices.fluidPump))
				level.removeBlock(getBlockPos().offset(0, isDummy()?-1: 0, 0).offset(0, i, 0), false);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		if(!isDummy())
			return Shapes.block();
		return Shapes.box(.1875f, 0, .1875f, .8125f, 1, .8125f);
	}

	@Override
	public boolean canOutputPressurized(boolean consumePower)
	{
		int accelPower = IEServerConfig.MACHINES.pump_consumption_accelerate.get();
		if(energyStorage.extractEnergy(accelPower, true) >= accelPower)
		{
			if(consumePower)
				energyStorage.extractEnergy(accelPower, false);
			return true;
		}
		return false;
	}

	@Override
	public boolean hasOutputConnection(Direction side)
	{
		return side!=null&&this.sideConfig.get(side)==IOSideConfig.OUTPUT;
	}
}

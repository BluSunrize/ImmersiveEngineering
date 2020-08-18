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
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeTileEntity.DirectionalFluidOutput;
import blusunrize.immersiveengineering.common.util.CapabilityReference;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
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

public class FluidPumpTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IBlockBounds, IHasDummyBlocks,
		IConfigurableSides, IFluidPipe, IIEInternalFluxHandler, IBlockOverlayText
{
	public static TileEntityType<FluidPumpTileEntity> TYPE;

	public Map<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);

	{
		for(Direction d : Direction.VALUES)
		{
			if(d==Direction.DOWN)
				sideConfig.put(d, IOSideConfig.INPUT);
			else
				sideConfig.put(d, IOSideConfig.NONE);
		}
	}

	public FluidTank tank = new FluidTank(4000);
	public FluxStorage energyStorage = new FluxStorage(8000);
	public boolean placeCobble = true;

	boolean checkingArea = false;
	Fluid searchFluid = null;
	ArrayList<BlockPos> openList = new ArrayList<>();
	ArrayList<BlockPos> closedList = new ArrayList<>();
	ArrayList<BlockPos> checked = new ArrayList<>();

	public FluidPumpTileEntity()
	{
		super(TYPE);
	}

	private Map<Direction, CapabilityReference<IFluidHandler>> neighborFluids = new EnumMap<>(Direction.class);

	{
		for(Direction neighbor : Direction.VALUES)
			neighborFluids.put(neighbor,
					CapabilityReference.forNeighbor(this, CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, neighbor));
	}

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		if(isDummy()||world.isRemote)
			return;
		if(tank.getFluidAmount() > 0)
		{
			int i = outputFluid(tank.getFluid(), FluidAction.EXECUTE);
			tank.drain(i, FluidAction.EXECUTE);
		}

		int consumption = IEConfig.MACHINES.pump_consumption.get();
		boolean hasRSSignal = isRSPowered();
		if(!hasRSSignal)
		{
			TileEntity above = world.getTileEntity(getPos().up());
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
						FluidStack drain = handler.drain(500, FluidAction.SIMULATE);
						if(drain.isEmpty())
							continue;
						int out = this.outputFluid(drain, FluidAction.EXECUTE);
						handler.drain(out, FluidAction.EXECUTE);
					}
					else if(world.getGameTime()%20==((getPos().getX()^getPos().getZ())&19)
							&&world.getFluidState(getPos().offset(f)).getFluid().isIn(FluidTags.WATER)
							&&IEConfig.MACHINES.pump_infiniteWater.get()
							&&tank.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE)==1000
							&&this.energyStorage.extractEnergy(consumption, true) >= consumption)
					{
						int connectedSources = 0;
						for(Direction f2 : Direction.BY_HORIZONTAL_INDEX)
						{
							IFluidState waterState = world.getFluidState(getPos().offset(f).offset(f2));
							if(waterState.getFluid().isIn(FluidTags.WATER)&&waterState.isSource())
								connectedSources++;
						}
						if(connectedSources > 1)
						{
							this.energyStorage.extractEnergy(consumption, false);
							this.tank.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
						}
					}
				}
			if(world.getGameTime()%40==(((getPos().getX()^getPos().getZ()))%40+40)%40)
			{
				if(closedList.isEmpty())
					prepareAreaCheck();
				else
				{
					int target = closedList.size()-1;
					BlockPos pos = closedList.get(target);
					FluidStack fs = Utils.drainFluidBlock(world, pos, FluidAction.SIMULATE);
					if(fs==null)
						closedList.remove(target);
					else if(tank.fill(fs, FluidAction.SIMULATE)==fs.getAmount()
							&&this.energyStorage.extractEnergy(consumption, true) >= consumption)
					{
						this.energyStorage.extractEnergy(consumption, false);
						fs = Utils.drainFluidBlock(world, pos, FluidAction.EXECUTE);
						if(IEConfig.MACHINES.pump_placeCobble.get()&&placeCobble)
							world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
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
				openList.add(getPos().offset(f));
				checkingArea = true;
			}
	}

	public void checkAreaTick()
	{
		boolean infiniteWater = IEConfig.MACHINES.pump_infiniteWater.get();
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

					if(!Utils.drainFluidBlock(world, next, FluidAction.SIMULATE).isEmpty())
						closedList.add(next);
					for(Direction f : Direction.values())
					{
						BlockPos pos2 = next.offset(f);
						fluid = Utils.getRelatedFluid(world, pos2);
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

		int accelPower = IEConfig.MACHINES.pump_consumption_accelerate.get();
		final int fluidForSort = canAccept;
		int sum = 0;
		HashMap<DirectionalFluidOutput, Integer> sorting = new HashMap<>();
		for(Direction f : Direction.values())
			if(sideConfig.get(f)==IOSideConfig.OUTPUT)
			{
				CapabilityReference<IFluidHandler> output = neighborFluids.get(f);
				if(output.isPresent())
				{
					TileEntity tile = getWorldNonnull().getTileEntity(pos.offset(f));
					IFluidHandler handler = output.get();
					FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, fs.getAmount(), true);
					if(tile instanceof FluidPipeTileEntity&&this.energyStorage.extractEnergy(accelPower, true) >= accelPower)
						insertResource.getOrCreateTag().putBoolean("pressurized", true);
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
					insertResource.getOrCreateTag().putBoolean("pressurized", true);
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
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		int[] sideConfigArray = nbt.getIntArray("sideConfig");
		for(Direction d : Direction.VALUES)
			sideConfig.put(d, IOSideConfig.VALUES[sideConfigArray[d.ordinal()]]);
		if(nbt.contains("placeCobble", NBT.TAG_BYTE))
			placeCobble = nbt.getBoolean("placeCobble");
		tank.readFromNBT(nbt.getCompound("tank"));
		energyStorage.readFromNBT(nbt);
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		int[] sideConfigArray = new int[6];
		for(Direction d : Direction.VALUES)
			sideConfigArray[d.ordinal()] = sideConfig.get(d).ordinal();
		nbt.putIntArray("sideConfig", sideConfigArray);
		nbt.putBoolean("placeCobble", placeCobble);
		nbt.put("tank", tank.writeToNBT(new CompoundNBT()));
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, PlayerEntity p)
	{
		if(side!=Direction.UP&&!isDummy())
		{
			sideConfig.put(side, IOSideConfig.next(sideConfig.get(side)));
			this.markDirty();
			this.markContainingBlockForUpdate(null);
			getWorldNonnull().addBlockEvent(getPos(), this.getBlockState().getBlock(), 0, 0);
			return true;
		}
		else if(p.isSneaking())
		{
			FluidPumpTileEntity master = this;
			if(isDummy())
			{
				TileEntity tmp = world.getTileEntity(pos.down());
				if(tmp instanceof FluidPumpTileEntity)
					master = (FluidPumpTileEntity)tmp;
			}
			master.placeCobble = !master.placeCobble;
			ChatUtils.sendServerNoSpamMessages(p, new TranslationTextComponent(Lib.CHAT_INFO+"pump.placeCobble."+master.placeCobble));
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
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(hammer&&IEConfig.GENERAL.showTextOverlay.get()&&!isDummy()&&mop instanceof BlockRayTraceResult)
		{
			BlockRayTraceResult brtr = (BlockRayTraceResult)mop;
			IOSideConfig i = sideConfig.get(brtr.getFace());
			IOSideConfig j = sideConfig.get(brtr.getFace().getOpposite());
			return new String[]{
					I18n.format(Lib.DESC_INFO+"blockSide.facing")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+i.getName()),
					I18n.format(Lib.DESC_INFO+"blockSide.opposite")
							+": "+I18n.format(Lib.DESC_INFO+"blockSide.connectFluid."+j.getName())
			};
		}
		return null;
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}

	public void setDummy(boolean dummy)
	{
		BlockState old = getBlockState();
		BlockState newState = old.with(IEProperties.MULTIBLOCKSLAVE, dummy);
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
			TileEntity te = world.getTileEntity(getPos().add(0, -1, 0));
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
		return getBlockState().get(IEProperties.MULTIBLOCKSLAVE);
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
		getWorldNonnull().setBlockState(pos.add(0, 1, 0), state);
		TileEntity tile = getWorldNonnull().getTileEntity(pos.add(0, 1, 0));
		if(tile instanceof FluidPumpTileEntity)
			((FluidPumpTileEntity)tile).setDummy(true);
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 1; i++)
			if(Utils.isBlockAt(world, getPos().add(0, isDummy()?-1: 0, 0).add(0, i, 0),
					MetalDevices.fluidPump))
				world.removeBlock(getPos().add(0, isDummy()?-1: 0, 0).add(0, i, 0), false);
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx)
	{
		if(!isDummy())
			return VoxelShapes.fullCube();
		return VoxelShapes.create(.1875f, 0, .1875f, .8125f, 1, .8125f);
	}

	@Override
	public boolean canOutputPressurized(boolean consumePower)
	{
		int accelPower = IEConfig.MACHINES.pump_consumption_accelerate.get();
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
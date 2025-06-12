/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity.DirectionalFluidOutput;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import blusunrize.immersiveengineering.common.util.MultiblockCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.datafixers.util.Unit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class FluidPumpBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IBlockBounds, IHasDummyBlocks,
		IConfigurableSides, IScrewdriverInteraction, IFluidPipe, IBlockOverlayText
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

	private final FluidTank tank = new FluidTank(4*FluidType.BUCKET_VOLUME);
	private final MutableEnergyStorage energyStorage = new MutableEnergyStorage(8000);
	private boolean placeCobble = true;
	private final MultiblockCapability<IEnergyStorage> energyCap = MultiblockCapability.make(
			this, be -> be.energyCap, FluidPumpBlockEntity::master, makeEnergyInput(energyStorage)
	);
	public boolean redstoneControlInverted = false;

	private boolean checkingArea = false;
	private Fluid searchFluid = null;
	private final List<BlockPos> openList = new ArrayList<>();
	private final List<BlockPos> closedList = new ArrayList<>();
	private final Set<BlockPos> checked = new HashSet<>();

	public FluidPumpBlockEntity(BlockEntityType<FluidPumpBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
	}

	private final Map<Direction, IEBlockCapabilityCache<IFluidHandler>> blockFluidHandlers = IEBlockCapabilityCaches.allNeighbors(
			FluidHandler.BLOCK, this
	);

	@Override
	public void tickServer()
	{
		if(isDummy())
			return;
		if(tank.getFluidAmount() > 0)
		{
			int i = outputFluid(tank.getFluid(), FluidAction.EXECUTE);
			tank.drain(i, FluidAction.EXECUTE);
		}

		int consumption = IEServerConfig.MACHINES.pump_consumption.get();
		if(canRun())
		{
			for(Direction f : Direction.values())
				if(sideConfig.get(f)==IOSideConfig.INPUT)
				{
					IFluidHandler input = blockFluidHandlers.get(f).getCapability();
					// attempt to find an adjacent entity fluid handler
					if(input==null)
						input = getEntityFluidHandler(f);
					if(input!=null)
					{
						int drainAmount = IFluidPipe.getTransferableAmount(this.canOutputPressurized(false));
						FluidStack drain = input.drain(drainAmount, FluidAction.SIMULATE);
						if(drain.isEmpty())
							continue;
						int out = this.outputFluid(drain, FluidAction.EXECUTE);
						input.drain(out, FluidAction.EXECUTE);
					}
					else
						gatherInfiniteFluidFromWorld(f);
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

	private boolean canRun()
	{
		boolean isPowered = isRSPowered();
		if(!isPowered)
		{
			BlockEntity above = level.getBlockEntity(getBlockPos().above());
			if(above instanceof FluidPumpBlockEntity dummy)
				isPowered = dummy.isRSPowered();
		}
		return (isPowered^redstoneControlInverted);
	}

	public void prepareAreaCheck()
	{
		openList.clear();
		closedList.clear();
		checked.clear();
		searchFluid = null;
		for(Direction f : Direction.values())
			if(sideConfig.get(f)==IOSideConfig.INPUT)
			{
				openList.add(getBlockPos().relative(f));
				checkingArea = true;
			}
	}

	private void gatherInfiniteFluidFromWorld(Direction gatherFrom)
	{
		if(level.getGameTime()%20!=Mth.positiveModulo(getBlockPos().getX()^getBlockPos().getZ(), 20))
			return;
		int consumption = IEServerConfig.MACHINES.pump_consumption.get();
		if(this.energyStorage.extractEnergy(consumption, true) < consumption)
			return;
		final BlockPos neighborPos = getBlockPos().relative(gatherFrom);
		final FluidState neighborFluidState = level.getFluidState(neighborPos);
		if(!neighborFluidState.isSource()||!neighborFluidState.canConvertToSource(getLevelNonnull(), neighborPos))
			return;
		final Fluid fluid = neighborFluidState.getType();
		final FluidStack gatheredFluid = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
		if(tank.fill(gatheredFluid, FluidAction.SIMULATE)!=gatheredFluid.getAmount())
			return;
		int connectedSources = 0;
		for(Direction sourceNeighbor : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			FluidState neighboringSource = level.getFluidState(neighborPos.relative(sourceNeighbor));
			if(neighboringSource.getType()==fluid&&neighboringSource.isSource())
				connectedSources++;
		}
		if(connectedSources > 1)
		{
			this.energyStorage.extractEnergy(consumption, false);
			this.tank.fill(gatheredFluid, FluidAction.EXECUTE);
		}
	}

	public void checkAreaTick()
	{
		final int closedListMax = 2048;
		int timeout = 0;
		while(timeout < 64&&closedList.size() < closedListMax&&!openList.isEmpty())
		{
			timeout++;
			BlockPos next = openList.remove(0);
			if(!checked.add(next))
				continue;
			final FluidState fluidState = getLevelNonnull().getFluidState(next);
			if(fluidState.isEmpty()||fluidState.canConvertToSource(getLevelNonnull(), next))
				continue;
			Fluid fluid = fluidState.getType();
			if(searchFluid!=null&&fluid!=searchFluid)
				continue;
			if(searchFluid==null)
				searchFluid = fluid;

			if(!Utils.drainFluidBlock(level, next, FluidAction.SIMULATE).isEmpty())
				closedList.add(next);
			for(Direction f : Direction.values())
			{
				BlockPos neighborPos = next.relative(f);
				if(checked.contains(neighborPos))
					continue;
				FluidState neighborFluidState = getLevelNonnull().getFluidState(neighborPos);
				if(neighborFluidState.isEmpty())
					continue;
				Fluid neighborFluid = Utils.getRelatedFluid(level, neighborPos);
				if(!neighborFluidState.canConvertToSource(getLevelNonnull(), neighborPos)&&neighborFluid==searchFluid)
					openList.add(neighborPos);
			}
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
				IFluidHandler handler = blockFluidHandlers.get(f).getCapability();
				if(handler!=null)
				{
					// TODO check if there are bad BE assumptions here
					BlockEntity tile = getLevelNonnull().getBlockEntity(worldPosition.relative(f));
					FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, fs.getAmount(), true);
					if(tile instanceof FluidPipeBlockEntity&&this.energyStorage.extractEnergy(accelPower, true) >= accelPower)
						insertResource.set(IEApiDataComponents.FLUID_PRESSURIZED, Unit.INSTANCE);
					int temp = handler.fill(insertResource, FluidAction.SIMULATE);
					if(temp > 0)
					{
						sorting.put(new DirectionalFluidOutput(handler, f, tile, worldPosition.relative(f)), temp);
						sum += temp;
					}
				}
				else
				{
					handler = getEntityFluidHandler(f);
					if(handler!=null)
					{
						FluidStack insertResource = Utils.copyFluidStackWithAmount(fs, fs.getAmount(), true);
						int temp = handler.fill(insertResource, FluidAction.SIMULATE);
						if(temp > 0)
						{
							sorting.put(new DirectionalFluidOutput(handler, f, null, worldPosition.relative(f)), temp);
							sum += temp;
						}
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
				if(output.containingTile() instanceof FluidPipeBlockEntity&&this.energyStorage.extractEnergy(accelPower, true) >= accelPower)
				{
					this.energyStorage.extractEnergy(accelPower, false);
					insertResource.set(IEApiDataComponents.FLUID_PRESSURIZED, Unit.INSTANCE);
				}
				int r = output.output().fill(insertResource, action);
				f += r;
				canAccept -= r;
				if(canAccept <= 0)
					break;
			}
			return f;
		}
		return 0;
	}

	@Nullable
	private IFluidHandler getEntityFluidHandler(Direction direction)
	{
		return level.getEntities(null, new AABB(getBlockPos().relative(direction))).stream()
				.map(entity -> entity.getCapability(FluidHandler.ENTITY, direction.getOpposite()))
				.filter(Objects::nonNull)
				.findFirst()
				.orElse(null);
	}


	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		int[] sideConfigArray = nbt.getIntArray("sideConfig");
		for(Direction d : DirectionUtils.VALUES)
			sideConfig.put(d, IOSideConfig.VALUES[sideConfigArray[d.ordinal()]]);
		if(nbt.contains("placeCobble", Tag.TAG_BYTE))
			placeCobble = nbt.getBoolean("placeCobble");
		tank.readFromNBT(provider, nbt.getCompound("tank"));
		EnergyHelper.deserializeFrom(energyStorage, nbt, provider);
		redstoneControlInverted = nbt.getBoolean("redstoneInverted");
		if(descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		int[] sideConfigArray = new int[6];
		for(Direction d : DirectionUtils.VALUES)
			sideConfigArray[d.ordinal()] = sideConfig.get(d).ordinal();
		nbt.putIntArray("sideConfig", sideConfigArray);
		nbt.putBoolean("placeCobble", placeCobble);
		nbt.put("tank", tank.writeToNBT(provider, new CompoundTag()));
		EnergyHelper.serializeTo(energyStorage, nbt, provider);
		nbt.putBoolean("redstoneInverted", redstoneControlInverted);
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
			getLevelNonnull().blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
			return true;
		}
		else if(p.isShiftKeyDown())
		{
			FluidPumpBlockEntity master = this;
			if(isDummy())
			{
				BlockEntity tmp = level.getBlockEntity(worldPosition.below());
				if(tmp instanceof FluidPumpBlockEntity)
					master = (FluidPumpBlockEntity)tmp;
			}
			master.placeCobble = !master.placeCobble;
			p.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"pump.placeCobble."+master.placeCobble), true);
			return true;
		}
		return false;
	}

	@Override
	public ItemInteractionResult screwdriverUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec)
	{
		if(isDummy())
		{
			BlockEntity te = level.getBlockEntity(worldPosition.below());
			if(te instanceof FluidPumpBlockEntity master)
				return master.screwdriverUseSide(side, player, hand, hitVec);
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
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

	private final Map<Direction, IFluidHandler> sidedFluidHandler = new EnumMap<>(Direction.class);

	public static void registerCapabilities(BECapabilityRegistrar<FluidPumpBlockEntity> registrar)
	{
		registrar.register(FluidHandler.BLOCK, (be, facing) -> {
			if(facing!=null&&!be.isDummy())
			{
				if(!be.sidedFluidHandler.containsKey(facing))
					be.sidedFluidHandler.put(facing, new SidedFluidHandler(be, facing));
				return be.sidedFluidHandler.get(facing);
			}
			else
				return null;
		});
		registrar.register(
				EnergyStorage.BLOCK,
				(be, facing) -> facing==null||(facing==Direction.UP&&be.isDummy())?be.energyCap.get(): null
		);
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
		FluidPumpBlockEntity pump;
		Direction facing;

		SidedFluidHandler(FluidPumpBlockEntity pump, Direction facing)
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

	@Override
	public boolean isDummy()
	{
		return getBlockState().getValue(IEProperties.MULTIBLOCKSLAVE);
	}

	@Nullable
	@Override
	public FluidPumpBlockEntity master()
	{
		if(!isDummy())
			return this;
		BlockPos masterPos = getBlockPos().below();
		BlockEntity te = Utils.getExistingTileEntity(level, masterPos);
		return te instanceof FluidPumpBlockEntity pump?pump: null;
	}

	@Override
	public void placeDummies(BlockPlaceContext ctx, BlockState state)
	{
		BlockPos dummyPos = worldPosition.above();
		getLevelNonnull().setBlockAndUpdate(dummyPos, IEBaseBlock.applyLocationalWaterlogging(
				state.setValue(IEProperties.MULTIBLOCKSLAVE, true), getLevelNonnull(), dummyPos
		));
	}

	@Override
	public void breakDummies(BlockPos pos, BlockState state)
	{
		for(int i = 0; i <= 1; i++)
			if(Utils.isBlockAt(level, getBlockPos().offset(0, isDummy()?-1: 0, 0).offset(0, i, 0), MetalDevices.FLUID_PUMP.get()))
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
}

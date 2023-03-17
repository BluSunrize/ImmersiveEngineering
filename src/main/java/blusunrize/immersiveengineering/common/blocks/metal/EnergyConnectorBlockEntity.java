/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.wires.IEWireTypes.IEWireType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static blusunrize.immersiveengineering.api.wires.WireType.*;
import static blusunrize.immersiveengineering.common.config.IEServerConfig.getOrDefault;

public class EnergyConnectorBlockEntity extends ImmersiveConnectableBlockEntity implements IStateBasedDirectional,
		IBlockBounds, EnergyConnector, IEServerTickableBE
{
	public static final Map<Pair<String, Boolean>, RegistryObject<BlockEntityType<EnergyConnectorBlockEntity>>>
			SPEC_TO_TYPE = new HashMap<>();
	public static final Map<ResourceLocation, Pair<String, Boolean>> NAME_TO_SPEC = new HashMap<>();

	public static void registerConnectorTEs(DeferredRegister<BlockEntityType<?>> event)
	{
		for(String type : new String[]{LV_CATEGORY, MV_CATEGORY, HV_CATEGORY})
			for(int b = 0; b < 2; ++b)
			{
				boolean relay = b!=0;
				Pair<String, Boolean> key = Pair.of(type, relay);
				String name = type.toLowerCase(Locale.US)+"_"+(relay?"relay": "conn");
				RegistryObject<BlockEntityType<EnergyConnectorBlockEntity>> teType = event.register(
						name, () -> new BlockEntityType<>(
								(pos, state) -> new EnergyConnectorBlockEntity(type, relay, pos, state),
								ImmutableSet.of(Connectors.ENERGY_CONNECTORS.get(key).get()), null)
				);
				SPEC_TO_TYPE.put(key, teType);
				NAME_TO_SPEC.put(ImmersiveEngineering.rl(name), key);
			}
	}

	private final String voltage;
	private final boolean relay;
	public int currentTickToMachine = 0;
	public int currentTickToNet = 0;
	private final MutableEnergyStorage storageToNet;
	private final MutableEnergyStorage storageToMachine;
	private final ResettableCapability<IEnergyStorage> energyCap;

	private final CapabilityReference<IEnergyStorage> output = CapabilityReference.forNeighbor(
			this, ForgeCapabilities.ENERGY, this::getFacing
	);

	public EnergyConnectorBlockEntity(BlockEntityType<? extends EnergyConnectorBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		Pair<String, Boolean> data = NAME_TO_SPEC.get(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type));
		this.voltage = data.getFirst();
		this.relay = data.getSecond();
		this.storageToMachine = new MutableEnergyStorage(getMaxInput(), getMaxInput(), getMaxInput());
		this.storageToNet = new MutableEnergyStorage(getMaxInput(), getMaxInput(), getMaxInput());
		this.energyCap = registerCapability(new ConnectorEnergyStorage());
	}

	public EnergyConnectorBlockEntity(String voltage, boolean relay, BlockPos pos, BlockState state)
	{
		this(SPEC_TO_TYPE.get(Pair.of(voltage, relay)).get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		int maxOut = Math.min(storageToMachine.getEnergyStored(), getMaxOutput()-currentTickToMachine);
		if(maxOut > 0)
		{
			IEnergyStorage target = output.getNullable();
			if(target!=null)
			{
				int inserted = target.receiveEnergy(maxOut, false);
				storageToMachine.extractEnergy(inserted, false);
			}
		}
		currentTickToMachine = 0;
		currentTickToNet = 0;
	}

	@Override
	public Property<Direction> getFacingProperty()
	{
		return ConnectorBlock.DEFAULT_FACING_PROP;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, Vec3 hit, LivingEntity entity)
	{
		return false;
	}

	@Override
	public boolean canConnectCable(WireType cableType, ConnectionPoint target, Vec3i offset)
	{
		if(!relay)
		{
			LocalWireNetwork local = globalNet.getNullableLocalNet(new ConnectionPoint(worldPosition, 0));
			if(local!=null&&!local.getConnections(worldPosition).isEmpty())
				return false;
		}
		return voltage.equals(cableType.getCategory());
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		CompoundTag toNet = new CompoundTag();
		EnergyHelper.serializeTo(storageToNet, toNet);
		nbt.put("toNet", toNet);
		CompoundTag toMachine = new CompoundTag();
		EnergyHelper.serializeTo(storageToMachine, toMachine);
		nbt.put("toMachine", toMachine);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		CompoundTag toMachine = nbt.getCompound("toMachine");
		EnergyHelper.deserializeFrom(storageToMachine, toMachine);
		CompoundTag toNet = nbt.getCompound("toNet");
		EnergyHelper.deserializeFrom(storageToNet, toNet);
	}

	@Override
	public Vec3 getConnectionOffset(ConnectionPoint here, ConnectionPoint other, WireType type)
	{
		Direction side = getFacing().getOpposite();
		double lengthFromHalf = LENGTH.getFloat(Pair.of(voltage, relay))-type.getRenderDiameter()/2-.5;
		return new Vec3(.5+lengthFromHalf*side.getStepX(),
				.5+lengthFromHalf*side.getStepY(),
				.5+lengthFromHalf*side.getStepZ());
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==ForgeCapabilities.ENERGY&&!relay&&(side==null||side==getFacing()))
			return energyCap.cast();
		return super.getCapability(cap, side);
	}

	private IEWireType getWireType()
	{
		if(LV_CATEGORY.equals(voltage))
			return IEWireType.COPPER;
		else if(WireType.MV_CATEGORY.equals(voltage))
			return IEWireType.ELECTRUM;
		else
			return IEWireType.STEEL;
	}

	public int getMaxInput()
	{
		return getOrDefault(IEServerConfig.WIRES.energyWireConfigs.get(getWireType()).connectorRate);
	}

	public int getMaxOutput()
	{
		return getOrDefault(IEServerConfig.WIRES.energyWireConfigs.get(getWireType()).connectorRate);
	}

	private static final Object2FloatMap<Pair<String, Boolean>> LENGTH = new Object2FloatArrayMap<>();

	static
	{
		LENGTH.put(Pair.of("HV", false), 0.75F);
		LENGTH.put(Pair.of("HV", true), 0.875F);
		LENGTH.put(Pair.of("MV", false), 0.5625F);
		LENGTH.defaultReturnValue(0.5F);
	}

	public static VoxelShape getConnectorBounds(Direction facing, float length)
	{
		final float wMin = 0.3125f;
		final float wMax = 1-wMin;
		return switch(facing.getOpposite())
				{
					case UP -> Shapes.box(wMin, 0, wMin, wMax, length, wMax);
					case DOWN -> Shapes.box(wMin, 1-length, wMin, wMax, 1, wMax);
					case SOUTH -> Shapes.box(wMin, wMin, 0, wMax, wMax, length);
					case NORTH -> Shapes.box(wMin, wMin, 1-length, wMax, wMax, 1);
					case EAST -> Shapes.box(0, wMin, wMin, length, wMax, wMax);
					case WEST -> Shapes.box(1-length, wMin, wMin, 1, wMax, wMax);
				};
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		float length = LENGTH.getFloat(Pair.of(voltage, relay));
		return getConnectorBounds(getFacing(), length);
	}

	@Override
	public boolean isSource(ConnectionPoint cp)
	{
		return !relay;
	}

	@Override
	public boolean isSink(ConnectionPoint cp)
	{
		return !relay;
	}

	@Override
	public int getAvailableEnergy()
	{
		return storageToNet.getEnergyStored();
	}

	@Override
	public int getRequestedEnergy()
	{
		return storageToMachine.getMaxEnergyStored()-storageToMachine.getEnergyStored();
	}

	@Override
	public void insertEnergy(int amount)
	{
		storageToMachine.receiveEnergy(amount, false);
	}

	@Override
	public void extractEnergy(int amount)
	{
		storageToNet.extractEnergy(amount, false);
	}

	@Override
	public Collection<ResourceLocation> getRequestedHandlers()
	{
		return ImmutableList.of(EnergyTransferHandler.ID);
	}

	private class ConnectorEnergyStorage implements IEnergyStorage
	{

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			if(level.isClientSide||relay)
				return 0;
			maxReceive = Math.min(getMaxInput()-currentTickToNet, maxReceive);
			if(maxReceive <= 0)
				return 0;

			int accepted = Math.min(Math.min(getMaxOutput(), getMaxInput()), maxReceive);
			accepted = Math.min(getMaxOutput()-storageToNet.getEnergyStored(), accepted);
			if(accepted <= 0)
				return 0;

			if(!simulate)
			{
				storageToNet.modifyEnergyStored(accepted);
				currentTickToNet += accepted;
				setChanged();
			}

			return accepted;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			return storageToMachine.extractEnergy(maxExtract, simulate);
		}

		@Override
		public int getEnergyStored()
		{
			return storageToNet.getEnergyStored();
		}

		@Override
		public int getMaxEnergyStored()
		{
			return storageToNet.getMaxEnergyStored();
		}

		@Override
		public boolean canExtract()
		{
			return true;
		}

		@Override
		public boolean canReceive()
		{
			return true;
		}
	}
}
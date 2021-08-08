/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IStateBasedDirectional;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.wires.IEWireTypes.IEWireType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2FloatAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static blusunrize.immersiveengineering.api.wires.WireType.*;

public class EnergyConnectorBlockEntity extends ImmersiveConnectableBlockEntity implements IStateBasedDirectional,
		IIEInternalFluxHandler, IBlockBounds, EnergyConnector, IETickableBlockEntity
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
				ImmutablePair<String, Boolean> key = new ImmutablePair<>(type, relay);
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
	private FluxStorage storageToNet;
	private FluxStorage storageToMachine;

	private CapabilityReference<IEnergyStorage> output = CapabilityReference.forNeighbor(this, CapabilityEnergy.ENERGY, this::getFacing);

	public EnergyConnectorBlockEntity(BlockEntityType<? extends EnergyConnectorBlockEntity> type, BlockPos pos, BlockState state)
	{
		super(type, pos, state);
		Pair<String, Boolean> data = NAME_TO_SPEC.get(type.getRegistryName());
		this.voltage = data.getKey();
		this.relay = data.getValue();
		this.storageToMachine = new FluxStorage(getMaxInput(), getMaxInput(), getMaxInput());
		this.storageToNet = new FluxStorage(getMaxInput(), getMaxInput(), getMaxInput());
	}

	public EnergyConnectorBlockEntity(String voltage, boolean relay, BlockPos pos, BlockState state)
	{
		this(SPEC_TO_TYPE.get(new ImmutablePair<>(voltage, relay)).get(), pos, state);
	}

	@Override
	public void tickServer()
	{
		int maxOut = Math.min(storageToMachine.getEnergyStored(), getMaxOutput()-currentTickToMachine);
		if(maxOut > 0&&output.isPresent())
		{
			IEnergyStorage target = output.get();
			int inserted = target.receiveEnergy(maxOut, false);
			storageToMachine.extractEnergy(inserted, false);
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
	public boolean canRotate(Direction axis)
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
		storageToNet.writeToNBT(toNet);
		nbt.put("toNet", toNet);
		CompoundTag toMachine = new CompoundTag();
		storageToMachine.writeToNBT(toMachine);
		nbt.put("toMachine", toMachine);
	}

	@Override
	public void readCustomNBT(@Nonnull CompoundTag nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		CompoundTag toMachine = nbt.getCompound("toMachine");
		storageToMachine.readFromNBT(toMachine);
		CompoundTag toNet = nbt.getCompound("toNet");
		storageToNet.readFromNBT(toNet);
	}

	@Override
	public Vec3 getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = getFacing().getOpposite();
		double lengthFromHalf = LENGTH.getFloat(new ImmutablePair<>(voltage, relay))-con.type.getRenderDiameter()/2-.5;
		return new Vec3(.5+lengthFromHalf*side.getStepX(),
				.5+lengthFromHalf*side.getStepY(),
				.5+lengthFromHalf*side.getStepZ());
	}

	IEForgeEnergyWrapper energyWrapper;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing!=this.getFacing()||relay)
			return null;
		if(energyWrapper==null||energyWrapper.side!=this.getFacing())
			energyWrapper = new IEForgeEnergyWrapper(this, this.getFacing());
		return energyWrapper;
	}

	@Override
	public FluxStorage getFluxStorage()
	{
		return storageToNet;
	}

	@Override
	public IOSideConfig getEnergySideConfig(Direction facing)
	{
		return (!relay&&facing==this.getFacing())?IOSideConfig.INPUT: IOSideConfig.NONE;
	}

	@Override
	public boolean canConnectEnergy(Direction from)
	{
		if(relay)
			return false;
		return from==getFacing();
	}

	@Override
	public int receiveEnergy(Direction from, int energy, boolean simulate)
	{
		if(level.isClientSide||relay)
			return 0;
		energy = Math.min(getMaxInput()-currentTickToNet, energy);
		if(energy <= 0)
			return 0;

		int accepted = Math.min(Math.min(getMaxOutput(), getMaxInput()), energy);
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
	public int getEnergyStored(Direction from)
	{
		if(relay)
			return 0;
		return storageToNet.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(Direction from)
	{
		if(relay)
			return 0;
		return getMaxInput();
	}

	@Override
	public int extractEnergy(Direction from, int energy, boolean simulate)
	{
		return 0;
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
		return IEServerConfig.WIRES.energyWireConfigs.get(getWireType()).connectorRate.get();
	}

	public int getMaxOutput()
	{
		return IEServerConfig.WIRES.energyWireConfigs.get(getWireType()).connectorRate.get();
	}

	private static final Object2FloatMap<Pair<String, Boolean>> LENGTH = new Object2FloatAVLTreeMap<>();

	static
	{
		LENGTH.put(new ImmutablePair<>("HV", false), 0.75F);
		LENGTH.put(new ImmutablePair<>("HV", true), 0.875F);
		LENGTH.put(new ImmutablePair<>("MV", false), 0.5625F);
		LENGTH.defaultReturnValue(0.5F);
	}

	public static VoxelShape getConnectorBounds(Direction facing, float wMin, float length)
	{
		float wMax = 1-wMin;
		switch(facing.getOpposite())
		{
			case UP:
				return Shapes.box(wMin, 0, wMin, wMax, length, wMax);
			case DOWN:
				return Shapes.box(wMin, 1-length, wMin, wMax, 1, wMax);
			case SOUTH:
				return Shapes.box(wMin, wMin, 0, wMax, wMax, length);
			case NORTH:
				return Shapes.box(wMin, wMin, 1-length, wMax, wMax, 1);
			case EAST:
				return Shapes.box(0, wMin, wMin, length, wMax, wMax);
			case WEST:
				return Shapes.box(1-length, wMin, wMin, 1, wMax, wMax);
		}
		return Shapes.block();
	}

	@Override
	public VoxelShape getBlockBounds(@Nullable CollisionContext ctx)
	{
		float length = LENGTH.getFloat(new ImmutablePair<>(voltage, relay));
		float wMin = .3125f;
		return getConnectorBounds(getFacing(), wMin, length);
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
}
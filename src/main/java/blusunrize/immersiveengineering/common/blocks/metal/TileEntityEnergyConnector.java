/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.wires.*;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2FloatAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.event.RegistryEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;

import static blusunrize.immersiveengineering.api.energy.wires.WireType.*;


public class TileEntityEnergyConnector extends TileEntityImmersiveConnectable implements IDirectionalTile,
		IIEInternalFluxHandler, IBlockBounds, EnergyConnector, ITickable
{
	public static final BiMap<Pair<String, Boolean>, TileEntityType<TileEntityEnergyConnector>> DATA_TYPE_MAP = HashBiMap.create();

	public static void registerConnectorTEs(RegistryEvent.Register<TileEntityType<?>> event)
	{
		for(String type : new String[]{LV_CATEGORY, MV_CATEGORY, HV_CATEGORY})
			for(int b = 0; b < 2; ++b)
			{
				boolean relay = b!=0;
				TileEntityType<TileEntityEnergyConnector> teType = new TileEntityType<>(() -> new TileEntityEnergyConnector(type, relay),
						null);
				teType.setRegistryName(ImmersiveEngineering.MODID, type.toLowerCase()+"_"+(relay?"relay": "conn"));
				event.getRegistry().register(teType);
				DATA_TYPE_MAP.put(new ImmutablePair<>(type, relay), teType);
			}
	}

	private final String voltage;
	private final boolean relay;
	public Direction facing = Direction.DOWN;
	public int currentTickToMachine = 0;
	public int currentTickToNet = 0;
	public static int[] connectorInputValues = Config.IEConfig.Machines.wireConnectorInput;
	private FluxStorage storageToNet = new FluxStorage(getMaxInput(), getMaxInput(), getMaxInput());
	private FluxStorage storageToMachine = new FluxStorage(getMaxInput(), getMaxInput(), getMaxInput());

	public TileEntityEnergyConnector(TileEntityType<? extends TileEntityEnergyConnector> type)
	{
		super(type);
		Pair<String, Boolean> data = DATA_TYPE_MAP.inverse().get(type);
		voltage = data.getKey();
		relay = data.getValue();
	}

	public TileEntityEnergyConnector(String voltage, boolean relay)
	{
		super(DATA_TYPE_MAP.get(new ImmutablePair<>(voltage, relay)));
		this.voltage = voltage;
		this.relay = relay;
	}

	@Override
	public void tick()
	{
		if(!world.isRemote)
		{
			int maxOut = Math.min(storageToMachine.getEnergyStored(), getMaxOutput()-currentTickToMachine);
			if(maxOut > 0)
			{
				TileEntity target = Utils.getExistingTileEntity(world, pos.offset(facing));
				if(target!=null)
				{
					int inserted = EnergyHelper.insertFlux(target, facing.getOpposite(), maxOut, false);
					storageToMachine.extractEnergy(inserted, false);
				}
			}
			currentTickToMachine = 0;
			currentTickToNet = 0;
		}
	}

	@Override
	public Direction getFacing()
	{
		return this.facing;
	}

	@Override
	public void setFacing(Direction facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction side, float hitX, float hitY, float hitZ, LivingEntity entity)
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
			LocalWireNetwork local = globalNet.getNullableLocalNet(new ConnectionPoint(pos, 0));
			if(local!=null&&!local.getConnections(pos).isEmpty())
				return false;
		}
		return voltage.equals(cableType.getCategory());
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.putInt("facing", facing.ordinal());
		CompoundNBT toNet = new CompoundNBT();
		storageToNet.writeToNBT(toNet);
		nbt.put("toNet", toNet);
		CompoundNBT toMachine = new CompoundNBT();
		storageToMachine.writeToNBT(toMachine);
		nbt.put("toMachine", toMachine);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = Direction.byIndex(nbt.getInt("facing"));
		CompoundNBT toMachine = nbt.getCompound("toMachine");
		storageToMachine.readFromNBT(toMachine);
		CompoundNBT toNet = nbt.getCompound("toNet");
		storageToNet.readFromNBT(toNet);
	}

	@Override
	public Vec3d getConnectionOffset(@Nonnull Connection con, ConnectionPoint here)
	{
		Direction side = facing.getOpposite();
		double conRadius = con.type.getRenderDiameter()/2;
		return new Vec3d(.5-conRadius*side.getXOffset(), .5-conRadius*side.getYOffset(), .5-conRadius*side.getZOffset());
	}

	IEForgeEnergyWrapper energyWrapper;

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing!=this.facing||relay)
			return null;
		if(energyWrapper==null||energyWrapper.side!=this.facing)
			energyWrapper = new IEForgeEnergyWrapper(this, this.facing);
		return energyWrapper;
	}

	@Override
	public FluxStorage getFluxStorage()
	{
		return storageToNet;
	}

	@Override
	public SideConfig getEnergySideConfig(Direction facing)
	{
		return (!relay&&facing==this.facing)?SideConfig.INPUT: SideConfig.NONE;
	}

	@Override
	public boolean canConnectEnergy(Direction from)
	{
		if(relay)
			return false;
		return from==facing;
	}

	@Override
	public int receiveEnergy(Direction from, int energy, boolean simulate)
	{
		if(world.isRemote||relay)
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
			markDirty();
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

	private int getVoltageIndex()
	{
		if(LV_CATEGORY.equals(voltage))
			return 0;
		else if(WireType.MV_CATEGORY.equals(voltage))
			return 1;
		else
			return 2;
	}

	public int getMaxInput()
	{
		return connectorInputValues[getVoltageIndex()];
	}

	public int getMaxOutput()
	{
		return connectorInputValues[getVoltageIndex()];
	}

	private static final Object2FloatMap<Pair<String, Boolean>> LENGTH = new Object2FloatAVLTreeMap<>();

	static
	{
		LENGTH.put(new ImmutablePair<>("HV", false), 0.75F);
		LENGTH.put(new ImmutablePair<>("HV", true), 0.875F);
		LENGTH.put(new ImmutablePair<>("MV", false), 0.5625F);
		LENGTH.defaultReturnValue(0.5F);
	}

	public static float[] getConnectorBounds(Direction facing, float wMin, float length)
	{
		float wMax = 1-wMin;
		switch(facing.getOpposite())
		{
			case UP:
				return new float[]{wMin, 0, wMin, wMax, length, wMax};
			case DOWN:
				return new float[]{wMin, 1-length, wMin, wMax, 1, wMax};
			case SOUTH:
				return new float[]{wMin, wMin, 0, wMax, wMax, length};
			case NORTH:
				return new float[]{wMin, wMin, 1-length, wMax, wMax, 1};
			case EAST:
				return new float[]{0, wMin, wMin, length, wMax, wMax};
			case WEST:
				return new float[]{1-length, wMin, wMin, 1, wMax, wMax};
		}
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public float[] getBlockBounds()
	{
		float length = LENGTH.getFloat(new ImmutablePair<>(voltage, relay));
		float wMin = .3125f;
		return getConnectorBounds(facing, wMin, length);
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
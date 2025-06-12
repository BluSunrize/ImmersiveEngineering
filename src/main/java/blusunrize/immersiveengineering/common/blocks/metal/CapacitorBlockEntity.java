/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.MutableEnergyStorage;
import blusunrize.immersiveengineering.api.energy.NullEnergyStorage;
import blusunrize.immersiveengineering.api.energy.WrappingEnergyStorage;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches;
import blusunrize.immersiveengineering.common.util.IEBlockCapabilityCaches.IEBlockCapabilityCache;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class CapacitorBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IBlockOverlayText,
		IConfigurableSides, IComparatorOverride, IBlockEntityDrop
{
	public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);
	private final CapacitorConfig configValues;
	private final IEnergyStorage energyStorage;
	protected final Map<Direction, IEnergyStorage> energyCaps = new EnumMap<>(Direction.class);
	private final Map<Direction, IEBlockCapabilityCache<IEnergyStorage>> connectedCaps = IEBlockCapabilityCaches.allNeighbors(
			Capabilities.EnergyStorage.BLOCK, this
	);
	protected final IEnergyStorage nullEnergyCap;

	public int comparatorOutput = 0;

	public CapacitorBlockEntity(CapacitorConfig configValues, BlockPos pos, BlockState state)
	{
		super(configValues.tileType.get(), pos, state);
		this.configValues = configValues;
		if(IEServerConfig.CONFIG_SPEC.isLoaded())
			energyStorage = makeMainEnergyStorage();
		else
			energyStorage = NullEnergyStorage.INSTANCE;
		for(Direction f : DirectionUtils.VALUES)
		{
			if(f==Direction.UP)
				sideConfig.put(f, IOSideConfig.INPUT);
			else
				sideConfig.put(f, IOSideConfig.NONE);
			energyCaps.put(f, new CapacitorEnergyHandler(f, this::getSideConfig, energyStorage));
		}
		nullEnergyCap = new WrappingEnergyStorage(energyStorage, false, false);
	}

	@Override
	public void tickServer()
	{
		for(Direction f : DirectionUtils.VALUES)
			this.transferEnergy(f);

		if(level.getGameTime()%32==((getBlockPos().getX()^getBlockPos().getZ())&31))
		{
			int i = scaleStoredEnergyTo(15);
			if(i!=this.comparatorOutput)
			{
				this.comparatorOutput = i;
				level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
			}
		}
	}

	public int scaleStoredEnergyTo(int scale)
	{
		return (int)(scale*(energyStorage.getEnergyStored()/(float)energyStorage.getMaxEnergyStored()));
	}

	protected void transferEnergy(Direction side)
	{
		if(this.sideConfig.get(side)!=IOSideConfig.OUTPUT)
			return;
		int out = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
		IEnergyStorage neighborCap = this.connectedCaps.get(side).getCapability();
		if(neighborCap!=null)
		{
			int inserted = neighborCap.receiveEnergy(out, false);
			this.energyStorage.extractEnergy(inserted, false);
		}
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return this.sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, Player player)
	{
		sideConfig.put(side, IOSideConfig.next(sideConfig.get(side)));
		this.setChanged();
		this.markContainingBlockForUpdate(null);
		level.blockEvent(getBlockPos(), this.getBlockState().getBlock(), 0, 0);
		return true;
	}

	@Override
	public boolean triggerEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return false;
	}

	public final int getMaxStorage()
	{
		return configValues.storage.getAsInt();
	}

	public final int getMaxInput()
	{
		return configValues.input.getAsInt();
	}

	public final int getMaxOutput()
	{
		return configValues.output.getAsInt();
	}

	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		for(Direction f : DirectionUtils.VALUES)
			nbt.putInt("sideConfig_"+f.ordinal(), sideConfig.get(f).ordinal());
		if(energyStorage instanceof EnergyStorage forgeStorage)
			EnergyHelper.serializeTo(forgeStorage, nbt, provider);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		for(Direction f : DirectionUtils.VALUES)
			sideConfig.put(f, IOSideConfig.values()[nbt.getInt("sideConfig_"+f.ordinal())]);
		if(energyStorage instanceof EnergyStorage forgeStorage)
			EnergyHelper.deserializeFrom(forgeStorage, nbt, provider);
	}

	public static void registerCapabilities(BECapabilityRegistrar<? extends CapacitorBlockEntity> registrar)
	{
		registrar.register(
				Capabilities.EnergyStorage.BLOCK,
				(be, side) -> side==null?be.nullEnergyCap: be.energyCaps.get(side)
		);
	}

	@Override
	public Component[] getOverlayText(Player player, HitResult mop, boolean hammer)
	{
		if(hammer&&IEClientConfig.showTextOverlay.get()&&mop instanceof BlockHitResult bmop)
		{
			IOSideConfig here = sideConfig.get(bmop.getDirection());
			IOSideConfig opposite = sideConfig.get(bmop.getDirection().getOpposite());
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectEnergy.", here, opposite);
		}
		return null;
	}

	@Override
	public boolean useNixieFont(Player player, HitResult mop)
	{
		return false;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		stack.set(IEDataComponents.GENERIC_ENERGY, this.energyStorage.getEnergyStored());
		stack.set(IEDataComponents.CAPACITOR_CONFIG, new CapacitorState(this.sideConfig));
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		final Integer stored = stack.get(IEDataComponents.GENERIC_ENERGY);
		if(stored!=null&&this.energyStorage instanceof MutableEnergyStorage mutable)
			mutable.setStoredEnergy(stored);
		final var sideConfig = stack.get(IEDataComponents.CAPACITOR_CONFIG);
		if(sideConfig!=null)
			this.sideConfig.putAll(sideConfig.sideConfig);
	}

	protected IEnergyStorage makeMainEnergyStorage()
	{
		return new MutableEnergyStorage(getMaxStorage(), getMaxInput(), getMaxOutput());
	}

	private record CapacitorEnergyHandler(
			Direction side, Function<Direction, IOSideConfig> sideConfig, IEnergyStorage base
	) implements IEnergyStorage
	{

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate)
		{
			if(canReceive())
				return base.receiveEnergy(maxReceive, simulate);
			return 0;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate)
		{
			if(canExtract())
				return base.extractEnergy(maxExtract, simulate);
			return 0;
		}

		@Override
		public int getEnergyStored()
		{
			return base.getEnergyStored();
		}

		@Override
		public int getMaxEnergyStored()
		{
			return base.getMaxEnergyStored();
		}

		@Override
		public boolean canExtract()
		{
			return sideConfig.apply(side)==IOSideConfig.OUTPUT;
		}

		@Override
		public boolean canReceive()
		{
			return sideConfig.apply(side)==IOSideConfig.INPUT;
		}
	}

	public record CapacitorState(Map<Direction, IOSideConfig> sideConfig)
	{
		public static final DualCodec<ByteBuf, CapacitorState> CODECS = IEDualCodecs.forMap(
				DualCodecs.DIRECTION, IOSideConfig.CODECS
		).map(CapacitorState::new, CapacitorState::sideConfig);

		public CapacitorState
		{
			sideConfig = new EnumMap<>(sideConfig);
		}
	}
}
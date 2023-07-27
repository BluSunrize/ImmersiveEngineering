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
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.ticking.IEServerTickableBE;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ResettableCapability;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class CapacitorBlockEntity extends IEBaseBlockEntity implements IEServerTickableBE, IBlockOverlayText,
		IConfigurableSides, IComparatorOverride, IBlockEntityDrop
{
	public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);
	private final CapacitorConfig configValues;
	private final IEnergyStorage energyStorage;
	private final Map<Direction, ResettableCapability<IEnergyStorage>> energyCaps = new EnumMap<>(Direction.class);
	private final ResettableCapability<IEnergyStorage> nullEnergyCap;

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
			energyCaps.put(f, registerCapability(new CapacitorEnergyHandler(f, sideConfig, energyStorage)));
		}
		nullEnergyCap = registerCapability(new WrappingEnergyStorage(energyStorage, false, false));
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
		BlockPos outPos = getBlockPos().relative(side);
		BlockEntity tileEntity = Utils.getExistingTileEntity(level, outPos);
		int out = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
		this.energyStorage.extractEnergy(EnergyHelper.insertFlux(tileEntity, side.getOpposite(), out, false), false);
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
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		for(Direction f : DirectionUtils.VALUES)
			nbt.putInt("sideConfig_"+f.ordinal(), sideConfig.get(f).ordinal());
		if(energyStorage instanceof EnergyStorage forgeStorage)
			EnergyHelper.serializeTo(forgeStorage, nbt);
	}

	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket)
	{
		for(Direction f : DirectionUtils.VALUES)
			sideConfig.put(f, IOSideConfig.values()[nbt.getInt("sideConfig_"+f.ordinal())]);
		if(energyStorage instanceof EnergyStorage forgeStorage)
			EnergyHelper.deserializeFrom(forgeStorage, nbt);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if(cap==ForgeCapabilities.ENERGY)
			return (side==null?nullEnergyCap: energyCaps.get(side)).cast();
		return super.getCapability(cap, side);
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
		writeCustomNBT(stack.getOrCreateTag(), false);
		drop.accept(stack);
	}

	@Override
	public void onBEPlaced(BlockPlaceContext ctx)
	{
		final ItemStack stack = ctx.getItemInHand();
		if(stack.hasTag())
			readCustomNBT(stack.getOrCreateTag(), false);
	}

	protected IEnergyStorage makeMainEnergyStorage()
	{
		return new MutableEnergyStorage(getMaxStorage(), getMaxInput(), getMaxOutput());
	}

	private record CapacitorEnergyHandler(
			Direction side, Map<Direction, IOSideConfig> sideConfigs, IEnergyStorage base
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
			return sideConfigs.get(side)==IOSideConfig.OUTPUT;
		}

		@Override
		public boolean canReceive()
		{
			return sideConfigs.get(side)==IOSideConfig.INPUT;
		}
	}
}
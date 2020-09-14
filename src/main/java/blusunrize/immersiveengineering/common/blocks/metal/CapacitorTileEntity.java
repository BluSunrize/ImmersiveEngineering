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
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

public class CapacitorTileEntity extends IEBaseTileEntity implements ITickableTileEntity, IIEInternalFluxHandler, IBlockOverlayText,
		IConfigurableSides, IComparatorOverride, ITileDrop
{
	public EnumMap<Direction, IOSideConfig> sideConfig = new EnumMap<>(Direction.class);
	private final CapacitorConfig configValues;

	FluxStorage energyStorage;

	public int comparatorOutput = 0;

	public CapacitorTileEntity(CapacitorConfig configValues)
	{
		super(configValues.tileType.get());
		this.configValues = configValues;
		for(Direction f : Direction.VALUES)
		{
			if(f==Direction.UP)
				sideConfig.put(f, IOSideConfig.INPUT);
			else
				sideConfig.put(f, IOSideConfig.NONE);
		}
		energyStorage = new FluxStorage(getMaxStorage(), getMaxInput(), getMaxOutput());
	}

	@Override
	public void tick()
	{
		if(!world.isRemote)
		{
			for(Direction f : Direction.VALUES)
				this.transferEnergy(f);

			if(world.getGameTime()%32==((getPos().getX()^getPos().getZ())&31))
			{
				int i = scaleStoredEnergyTo(15);
				if(i!=this.comparatorOutput)
				{
					this.comparatorOutput = i;
					world.updateComparatorOutputLevel(getPos(), getBlockState().getBlock());
				}
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
		BlockPos outPos = getPos().offset(side);
		TileEntity tileEntity = Utils.getExistingTileEntity(world, outPos);
		int out = Math.min(getMaxOutput(), this.energyStorage.getEnergyStored());
		this.energyStorage.modifyEnergyStored(-EnergyHelper.insertFlux(tileEntity, side.getOpposite(), out, false));
	}

	@Override
	public IOSideConfig getSideConfig(Direction side)
	{
		return this.sideConfig.get(side);
	}

	@Override
	public boolean toggleSide(Direction side, PlayerEntity player)
	{
		sideConfig.put(side, IOSideConfig.next(sideConfig.get(side)));
		this.markDirty();
		this.markContainingBlockForUpdate(null);
		world.addBlockEvent(getPos(), this.getBlockState().getBlock(), 0, 0);
		return true;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
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
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		for(Direction f : Direction.VALUES)
			nbt.putInt("sideConfig_"+f.ordinal(), sideConfig.get(f).ordinal());
		energyStorage.writeToNBT(nbt);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		for(Direction f : Direction.VALUES)
			sideConfig.put(f, IOSideConfig.values()[nbt.getInt("sideConfig_"+f.ordinal())]);
		energyStorage.readFromNBT(nbt);
	}

	private IEForgeEnergyWrapper[] wrappers = IEForgeEnergyWrapper.getDefaultWrapperArray(this);

	@Nonnull
	@Override
	public FluxStorage getFluxStorage()
	{
		return this.energyStorage;
	}

	@Nonnull
	@Override
	public IOSideConfig getEnergySideConfig(@Nullable Direction facing)
	{
		if(facing==null)
			return IOSideConfig.NONE;
		return this.sideConfig.get(facing);
	}

	@Override
	public IEForgeEnergyWrapper getCapabilityWrapper(Direction facing)
	{
		if(facing==null)
			return null;
		return wrappers[facing.ordinal()];
	}

	@Override
	public ITextComponent[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		if(hammer&&IEClientConfig.showTextOverlay.get()&&mop instanceof BlockRayTraceResult)
		{
			BlockRayTraceResult bmop = (BlockRayTraceResult)mop;
			IOSideConfig here = sideConfig.get(bmop.getFace());
			IOSideConfig opposite = sideConfig.get(bmop.getFace().getOpposite());
			return TextUtils.sideConfigWithOpposite(Lib.DESC_INFO+"blockSide.connectEnergy.", here, opposite);
		}
		return null;
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public int getComparatorInputOverride()
	{
		return this.comparatorOutput;
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		writeCustomNBT(stack.getOrCreateTag(), false);
		return ImmutableList.of(stack);
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
		if(stack.hasTag())
			readCustomNBT(stack.getOrCreateTag(), false);
	}
}
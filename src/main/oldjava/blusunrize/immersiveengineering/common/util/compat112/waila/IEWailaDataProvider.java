/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat112.waila;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilTileEntity;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.WoodenBarrelTileEntity;
import mcp.mobius.waila.api.*;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class IEWailaDataProvider implements IWailaDataProvider
{
	public static void callbackRegister(IWailaRegistrar registrar)
	{
		IEWailaDataProvider dataProvider = new IEWailaDataProvider();
		registrar.registerBodyProvider(dataProvider, HempBlock.class);
		registrar.registerBodyProvider(dataProvider, WoodenBarrelTileEntity.class);
		registrar.registerNBTProvider(dataProvider, WoodenBarrelTileEntity.class);
		registrar.registerStackProvider(dataProvider, MultiblockPartTileEntity.class);

		registrar.registerBodyProvider(dataProvider, IFluxReceiver.class);
		registrar.registerNBTProvider(dataProvider, IFluxReceiver.class);
		registrar.registerBodyProvider(dataProvider, IFluxProvider.class);
		registrar.registerNBTProvider(dataProvider, IFluxProvider.class);
	}


	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		if(accessor.getTileEntity() instanceof MultiblockPartTileEntity)
			return new ItemStack(accessor.getBlock(), 1, accessor.getMetadata());
		return ItemStack.EMPTY;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		Block b = accessor.getBlock();
		TileEntity tile = accessor.getTileEntity();
		if(b instanceof HempBlock)
		{
			int meta = accessor.getMetadata();
			int min = ((HempBlock)b).getMinGrowth(meta);
			int max = ((HempBlock)b).getMaxGrowth(meta);
			if(min==max)
				currenttip.add(String.format("%s : %s", I18n.format("hud.msg.growth"), I18n.format("hud.msg.mature")));
			else
			{
				float growth = ((meta-min)/(float)(max-min))*100f;
				if(growth < 100.0)
					currenttip.add(String.format("%s : %.0f %%", I18n.format("hud.msg.growth"), growth));
				else
					currenttip.add(String.format("%s : %s", I18n.format("hud.msg.growth"), I18n.format("hud.msg.mature")));
			}
			return currenttip;
		}
		else if(tile instanceof WoodenBarrelTileEntity)
		{
			CompoundNBT tank = accessor.getNBTData().getCompound("tank");
			if(!tank.hasKey("Empty")&&!tank.isEmpty())
			{
				FluidStack fluid = FluidStack.loadFluidStackFromNBT(tank);
				currenttip.add(String.format("%s: %d / %d mB", fluid.getLocalizedName(), Integer.valueOf(fluid.amount), 12000));
			}
			else
				currenttip.add(I18n.format("hud.msg.empty"));
		}
		if(accessor.getNBTData().hasKey("Energy"))
		{
			int cur = accessor.getNBTInteger(accessor.getNBTData(), "Energy");
			int max = accessor.getNBTInteger(accessor.getNBTData(), "MaxStorage");
			if(max > 0&&((ITaggedList)currenttip).getEntries("IFEnergyStorage").size()==0)
				((ITaggedList)currenttip).add(String.format("%d / %d IF", cur, max), "IFEnergyStorage");
			if(tile instanceof TeslaCoilTileEntity&&((ITaggedList)currenttip).getEntries("teslaCoil").size()==0)
			{
				boolean rsInv = accessor.getNBTData().getBoolean("redstoneInverted");
				boolean lowPower = accessor.getNBTData().getBoolean("lowPower");
				((ITaggedList)currenttip).add(I18n.format(Lib.CHAT_INFO+"rsControl."+(rsInv?"invertedOn": "invertedOff")), "teslaCoil");
				currenttip.add(I18n.format(Lib.CHAT_INFO+"tesla."+(lowPower?"lowPower": "highPower")));
			}
		}
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}

	@Override
	public CompoundNBT getNBTData(ServerPlayerEntity player, TileEntity te, CompoundNBT tag, World world, BlockPos pos)
	{
		int cur = -1;
		int max = -1;
		if(te instanceof IFluxReceiver)
		{
			cur = ((IFluxReceiver)te).getEnergyStored(null);
			max = ((IFluxReceiver)te).getMaxEnergyStored(null);
		}
		else if(te instanceof IFluxProvider)
		{
			cur = ((IFluxProvider)te).getEnergyStored(null);
			max = ((IFluxProvider)te).getMaxEnergyStored(null);
		}
		if(cur!=-1)
		{
			tag.putInt("Energy", cur);
			tag.putInt("MaxStorage", max);
		}
		if(te instanceof TeslaCoilTileEntity)
		{
			if(((TeslaCoilTileEntity)te).dummy)
				te = te.getWorld().getTileEntity(te.getPos().offset(((TeslaCoilTileEntity)te).facing, -1));
			tag.putBoolean("redstoneInverted", ((TeslaCoilTileEntity)te).redstoneControlInverted);
			tag.putBoolean("lowPower", ((TeslaCoilTileEntity)te).lowPower);
		}
		else if(te instanceof WoodenBarrelTileEntity)
			tag.put("tank", ((WoodenBarrelTileEntity)te).tank.writeToNBT(new CompoundNBT()));
		return tag;
	}
}

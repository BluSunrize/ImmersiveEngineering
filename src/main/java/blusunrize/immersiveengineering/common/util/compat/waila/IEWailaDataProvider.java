package blusunrize.immersiveengineering.common.util.compat.waila;

import java.util.List;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenBarrel;
import mcp.mobius.waila.api.ITaggedList;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class IEWailaDataProvider implements IWailaDataProvider
{
	public static void callbackRegister(IWailaRegistrar registrar)
	{
		IEWailaDataProvider dataProvider = new IEWailaDataProvider();
		registrar.registerBodyProvider(dataProvider, BlockIECrop.class);
		registrar.registerBodyProvider(dataProvider, TileEntityWoodenBarrel.class);
		registrar.registerStackProvider(dataProvider, TileEntityMultiblockPart.class);
		
		registrar.registerBodyProvider(dataProvider, IFluxReceiver.class);
		registrar.registerNBTProvider(dataProvider, IFluxReceiver.class);
		registrar.registerBodyProvider(dataProvider, IFluxProvider.class);
		registrar.registerNBTProvider(dataProvider, IFluxProvider.class);
	}


	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		if(accessor.getTileEntity() instanceof TileEntityMultiblockPart)
			return new ItemStack(accessor.getBlock(), 1, accessor.getMetadata());
		return null;
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
		if(b instanceof BlockIECrop)
		{
			int meta = accessor.getMetadata();
			int min = ((BlockIECrop)b).getMinMeta(meta);
			int max = ((BlockIECrop)b).getMaxMeta(meta);
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
		if(tile instanceof TileEntityWoodenBarrel)
		{
			NBTTagCompound tank = accessor.getNBTData().getCompoundTag("tank");
			if(!tank.hasKey("Empty"))
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
			if(max>0 && ((ITaggedList)currenttip).getEntries("IFEnergyStorage").size()==0)
		        ((ITaggedList)currenttip).add(String.format("%d / %d IF", new Object[] {cur,max}), "IFEnergyStorage");
		}
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}
	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
	{
		int cur = -1;
		int max = -1;
		if(te instanceof IFluxReceiver)
		{
			cur = ((IFluxReceiver)te).getEnergyStored(null);
			max = ((IFluxReceiver)te).getMaxEnergyStored(null);
		}
		else if(te instanceof IFluxReceiver)
		{
			cur = ((IFluxProvider)te).getEnergyStored(null);
			max = ((IFluxProvider)te).getMaxEnergyStored(null);
		}
		tag.setInteger("Energy", cur);
		tag.setInteger("MaxStorage", max);
		return tag;
	}
}

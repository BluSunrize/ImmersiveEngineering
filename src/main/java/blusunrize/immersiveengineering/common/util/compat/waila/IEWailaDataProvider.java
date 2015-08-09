package blusunrize.immersiveengineering.common.util.compat.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.plant.BlockIECrop;

public class IEWailaDataProvider implements IWailaDataProvider
{
	public static void callbackRegister(IWailaRegistrar registrar)
	{
		registrar.registerBodyProvider(new IEWailaDataProvider(), BlockIECrop.class);
	}


	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
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
		if(b instanceof BlockIECrop)
		{
			int meta = accessor.getMetadata();
			int min = ((BlockIECrop)b).getMinMeta(meta);
			int max = ((BlockIECrop)b).getMinMeta(meta);
			float growth = ((meta-min)/(float)(max-min))*100f;
			if(growth < 100.0)
				currenttip.add(String.format("%s : %.0f %%", StatCollector.translateToLocal("hud.msg.growth"), growth));
			else
				currenttip.add(String.format("%s : %s", StatCollector.translateToLocal("hud.msg.growth"), StatCollector.translateToLocal("hud.msg.mature")));

			return currenttip;
		}
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}
	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z)
	{
		return tag;
	}
}

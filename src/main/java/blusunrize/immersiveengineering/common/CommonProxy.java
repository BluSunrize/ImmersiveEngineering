package blusunrize.immersiveengineering.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.gui.ContainerBlastFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerCokeOven;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.Lib;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	public void init(){}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID==Lib.GUIID_CokeOven && world.getTileEntity(x, y, z) instanceof TileEntityCokeOven)
			return new ContainerCokeOven(player.inventory, (TileEntityCokeOven) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_BlastFurnace && world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace)
			return new ContainerBlastFurnace(player.inventory, (TileEntityBlastFurnace) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Revolver && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemRevolver)
			return new ContainerRevolver(player.inventory, world);
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	public void serverStart()
	{
	}
}

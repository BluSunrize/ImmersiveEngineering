package blusunrize.immersiveengineering.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.gui.ContainerArcFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerBlastFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerCokeOven;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import blusunrize.immersiveengineering.common.gui.ContainerFermenter;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.ContainerRefinery;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.gui.ContainerSorter;
import blusunrize.immersiveengineering.common.gui.ContainerSqueezer;
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
		if(ID==Lib.GUIID_WoodenCrate && world.getTileEntity(x, y, z) instanceof TileEntityWoodenCrate)
			return new ContainerCrate(player.inventory, (TileEntityWoodenCrate) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Squeezer && world.getTileEntity(x, y, z) instanceof TileEntitySqueezer)
			return new ContainerSqueezer(player.inventory, (TileEntitySqueezer) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Fermenter && world.getTileEntity(x, y, z) instanceof TileEntityFermenter)
			return new ContainerFermenter(player.inventory, (TileEntityFermenter) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Sorter && world.getTileEntity(x, y, z) instanceof TileEntityConveyorSorter)
			return new ContainerSorter(player.inventory, (TileEntityConveyorSorter) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Refinery && world.getTileEntity(x, y, z) instanceof TileEntityRefinery)
			return new ContainerRefinery(player.inventory, (TileEntityRefinery) world.getTileEntity(x, y, z));
		//		if(ID==Lib.GUIID_Workbench && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemDrill)
		//			return new ContainerDrill(player.inventory, world);
		if(ID==Lib.GUIID_Workbench && world.getTileEntity(x, y, z) instanceof TileEntityModWorkbench)
			return new ContainerModWorkbench(player.inventory, (TileEntityModWorkbench) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_ArcFurnace && world.getTileEntity(x, y, z) instanceof TileEntityArcFurnace)
			return new ContainerArcFurnace(player.inventory, (TileEntityArcFurnace) world.getTileEntity(x, y, z));
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
	}

	public void loadComplete()
	{
	}

	public void handleTileSound(String soundName, TileEntity tile, boolean tileActive, float volume, float pitch)
	{
	}
	public void stopTileSound(String soundName, TileEntity tile)
	{
	}
	public void spawnCrusherFX(TileEntityCrusher tile, ItemStack stack)
	{
	}
	public void spawnBucketWheelFX(TileEntityBucketWheel tile, ItemStack stack)
	{
	}
	public void spawnSparkFX(World world, double x, double y, double z, double mx, double my, double mz)
	{
	}
	public void draw3DBlockCauldron()
	{
	}
	public String[] splitStringOnWidth(String s, int w)
	{
		return new String[]{s};
	}
}

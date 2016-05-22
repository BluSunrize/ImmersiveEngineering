package blusunrize.immersiveengineering.common;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBucketWheel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntitySorter;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.gui.ContainerArcFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerAssembler;
import blusunrize.immersiveengineering.common.gui.ContainerBlastFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerCokeOven;
import blusunrize.immersiveengineering.common.gui.ContainerCrate;
import blusunrize.immersiveengineering.common.gui.ContainerFermenter;
import blusunrize.immersiveengineering.common.gui.ContainerModWorkbench;
import blusunrize.immersiveengineering.common.gui.ContainerRefinery;
import blusunrize.immersiveengineering.common.gui.ContainerRevolver;
import blusunrize.immersiveengineering.common.gui.ContainerSorter;
import blusunrize.immersiveengineering.common.gui.ContainerSqueezer;
import blusunrize.immersiveengineering.common.gui.ContainerToolbox;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemToolbox;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
	public void preInit(){}
	public void init(){}
	public void postInit(){}
	public void serverStarting(){}
	public void onWorldLoad(){}
	
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
		if(ID==Lib.GUIID_CokeOven && te instanceof TileEntityCokeOven)
			return new ContainerCokeOven(player.inventory, (TileEntityCokeOven) te);
		if(ID==Lib.GUIID_BlastFurnace && te instanceof TileEntityBlastFurnace)
			return new ContainerBlastFurnace(player.inventory, (TileEntityBlastFurnace) te);
		if(ID==Lib.GUIID_WoodenCrate && te instanceof TileEntityWoodenCrate)
			return new ContainerCrate(player.inventory, (TileEntityWoodenCrate) te);
		if(ID==Lib.GUIID_Workbench && te instanceof TileEntityModWorkbench)
			return new ContainerModWorkbench(player.inventory, (TileEntityModWorkbench) te);
		if(ID==Lib.GUIID_Revolver && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemRevolver)
			return new ContainerRevolver(player.inventory, world);
		if(ID==Lib.GUIID_Toolbox && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemToolbox)
			return new ContainerToolbox(player.inventory, world);
		if(ID==Lib.GUIID_Sorter && te instanceof TileEntitySorter)
			return new ContainerSorter(player.inventory, (TileEntitySorter) te);
		if(ID==Lib.GUIID_Squeezer && te instanceof TileEntitySqueezer)
			return new ContainerSqueezer(player.inventory, (TileEntitySqueezer) te);
		if(ID==Lib.GUIID_Fermenter && te instanceof TileEntityFermenter)
			return new ContainerFermenter(player.inventory, (TileEntityFermenter) te);
		if(ID==Lib.GUIID_Refinery && te instanceof TileEntityRefinery)
			return new ContainerRefinery(player.inventory, (TileEntityRefinery) te);
		if(ID==Lib.GUIID_ArcFurnace && te instanceof TileEntityArcFurnace)
			return new ContainerArcFurnace(player.inventory, (TileEntityArcFurnace) te);
		if(ID==Lib.GUIID_Assembler && te instanceof TileEntityAssembler)
			return new ContainerAssembler(player.inventory, (TileEntityAssembler) te);
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		return null;
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
	public void spawnRedstoneFX(World world, double x, double y, double z, double mx, double my, double mz, float size, float r, float g, float b)
	{
	}
	public void draw3DBlockCauldron()
	{
	}
	public void drawSpecificFluidPipe(String configuration)
	{
	}
	public String[] splitStringOnWidth(String s, int w)
	{
		return new String[]{s};
	}
	public World getClientWorld()
	{
		return null;
	}
	public String getNameFromUUID(String uuid)
	{
		return MinecraftServer.getServer().getMinecraftSessionService().fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
	}
	public void reInitGui()
	{
	}
}
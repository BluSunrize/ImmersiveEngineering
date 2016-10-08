package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityModWorkbench;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntitySorter;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenCrate;
import blusunrize.immersiveengineering.common.gui.*;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.items.ItemToolbox;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nonnull;
import java.util.UUID;

public abstract class CommonProxy implements IGuiHandler
{
	public void preInit(){}

	public void preInitEnd()
	{
	}
	public void init(){}

	public void initEnd()
	{
	}
	public void postInit(){}

	public void postInitEnd()
	{
	}
	public void serverStarting(){}
	public void onWorldLoad(){}

	public static <T extends TileEntity & IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile)
	{
		player.openGui(ImmersiveEngineering.instance, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}

	public static void openGuiForItem(@Nonnull EntityPlayer player, @Nonnull EntityEquipmentSlot slot)
	{
		ItemStack stack = player.getItemStackFromSlot(slot);
		if(stack==null || !(stack.getItem() instanceof IGuiItem))
			return;
		IGuiItem gui = (IGuiItem)stack.getItem();
		player.openGui(ImmersiveEngineering.instance, 100*slot.ordinal() + gui.getGuiID(stack), player.worldObj, (int)player.posX,(int)player.posY,(int)player.posZ);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID>=Lib.GUIID_Base_Item)
		{
			EntityEquipmentSlot slot = EntityEquipmentSlot.values()[ID/100];
			ID %= 100;//Slot determined, get actual ID
			ItemStack item = player.getItemStackFromSlot(slot);
			if(item!=null && item.getItem() instanceof IGuiItem && ((IGuiItem)item.getItem()).getGuiID(item)==ID)
			{
				if(ID == Lib.GUIID_Revolver && item.getItem() instanceof ItemRevolver)
					return new ContainerRevolver(player.inventory, world, slot, item);
				if(ID == Lib.GUIID_Toolbox && item.getItem() instanceof ItemToolbox)
					return new ContainerToolbox(player.inventory, world, slot, item);
			}
		}
		else
		{
			TileEntity te = world.getTileEntity(new BlockPos(x,y,z));
			if(te instanceof IGuiTile)
			{
				Object gui = null;
				if(ID==Lib.GUIID_CokeOven && te instanceof TileEntityCokeOven)
					gui = new ContainerCokeOven(player.inventory, (TileEntityCokeOven) te);
				if(ID==Lib.GUIID_BlastFurnace && te instanceof TileEntityBlastFurnace)
					gui = new ContainerBlastFurnace(player.inventory, (TileEntityBlastFurnace) te);
				if(ID==Lib.GUIID_WoodenCrate && te instanceof TileEntityWoodenCrate)
					gui = new ContainerCrate(player.inventory, (TileEntityWoodenCrate) te);
				if(ID==Lib.GUIID_Workbench && te instanceof TileEntityModWorkbench)
					gui = new ContainerModWorkbench(player.inventory, (TileEntityModWorkbench) te);
				if(ID==Lib.GUIID_Sorter && te instanceof TileEntitySorter)
					gui = new ContainerSorter(player.inventory, (TileEntitySorter) te);
				if(ID==Lib.GUIID_Squeezer && te instanceof TileEntitySqueezer)
					gui = new ContainerSqueezer(player.inventory, (TileEntitySqueezer) te);
				if(ID==Lib.GUIID_Fermenter && te instanceof TileEntityFermenter)
					gui = new ContainerFermenter(player.inventory, (TileEntityFermenter) te);
				if(ID==Lib.GUIID_Refinery && te instanceof TileEntityRefinery)
					gui = new ContainerRefinery(player.inventory, (TileEntityRefinery) te);
				if(ID==Lib.GUIID_ArcFurnace && te instanceof TileEntityArcFurnace)
					gui = new ContainerArcFurnace(player.inventory, (TileEntityArcFurnace) te);
				if(ID==Lib.GUIID_Assembler && te instanceof TileEntityAssembler)
					gui = new ContainerAssembler(player.inventory, (TileEntityAssembler) te);
				if(gui!=null)
					((IGuiTile)te).onGuiOpened(player, false);
				return gui;
			}
		}
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

	public boolean drawConveyorInGui(String conveyor, EnumFacing facing)
	{
		return false;
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
		return FMLCommonHandler.instance().getMinecraftServerInstance().getMinecraftSessionService().fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
	}
	public void reInitGui()
	{
	}

	public void removeStateFromSmartModelCache(IExtendedBlockState state)
	{
	}
	public void removeStateFromConnectionModelCache(IExtendedBlockState state)
	{
	}
	public void clearConnectionModelCache()
	{
	}
	public void clearRenderCaches()
	{
	}
}
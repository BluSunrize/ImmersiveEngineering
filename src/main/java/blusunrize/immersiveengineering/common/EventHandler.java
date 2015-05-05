package blusunrize.immersiveengineering.common;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityLightningRod;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.relauncher.Side;

public class EventHandler
{
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event)
	{
		if(!event.world.isRemote && event.world.provider.dimensionId==0)
		{
			IESaveData worldData = (IESaveData) event.world.loadItemData(IESaveData.class, IESaveData.dataName);
			if(worldData==null)
			{
				worldData = new IESaveData(IESaveData.dataName);
				event.world.setItemData(IESaveData.dataName, worldData);
			}
			IESaveData.setInstance(worldData);
		}
	}

	@SubscribeEvent
	public void harvestCheck(PlayerEvent.HarvestCheck event)
	{
		if(event.block instanceof BlockIEBase && event.entityPlayer.getCurrentEquippedItem()!=null && event.entityPlayer.getCurrentEquippedItem().getItem().getToolClasses(event.entityPlayer.getCurrentEquippedItem()).contains(Lib.TOOL_HAMMER))
		{
			MovingObjectPosition mop = Utils.getMovingObjectPositionFromPlayer(event.entityPlayer.worldObj, event.entityPlayer, true);
			if(mop!=null && mop.typeOfHit==MovingObjectPosition.MovingObjectType.BLOCK)
				if(((BlockIEBase)event.block).allowHammerHarvest(event.entityPlayer.worldObj.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ)))
					event.success=true;
		}

	}
	@SubscribeEvent
	public void entitySpawn(EntityJoinWorldEvent event)
	{
		if(event.entity instanceof EntityLightningBolt&&!event.world.isRemote)
		{
			for(int xx=-1; xx<=1; xx++)
				for(int zz=-1; zz<=1; zz++)
					if(event.world.getBlock((int)event.entity.posX+xx, (int)event.entity.posY-1, (int)event.entity.posZ+zz).equals(IEContent.blockMetalDecoration) && event.world.getBlockMetadata((int)event.entity.posX+xx, (int)event.entity.posY-1, (int)event.entity.posZ+zz)==0)
					{
						for(int y=(int) event.entity.posY; y>0; y--)
						{
							if( event.world.getTileEntity((int)event.entity.posX+xx, y, (int)event.entity.posZ+zz) instanceof TileEntityLightningRod)
							{
								((TileEntityLightningRod) event.world.getTileEntity((int)event.entity.posX+xx, y, (int)event.entity.posZ+zz)).outputEnergy(Config.getInt("lightning_output"));
								return;
							}
							else if(!(event.world.getBlock((int)event.entity.posX+xx, y, (int)event.entity.posZ+zz).equals(IEContent.blockMetalDecoration) && event.world.getBlockMetadata((int)event.entity.posX+xx, y, (int)event.entity.posZ+zz)==0))
							{
								return;							
							}
						}
					}
		}
	}

	public static HashMap<UUID, TileEntityCrusher> crusherMap = new HashMap<UUID, TileEntityCrusher>();
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled() && Lib.DMG_Grinder.equals(event.source.getDamageType()))
		{
			TileEntityCrusher crusher = crusherMap.get(event.entityLiving.getUniqueID());
			if(crusher!=null)
			{
				for(EntityItem item: event.drops)
					if(item!=null && item.getEntityItem()!=null)
						crusher.outputItem(item.getEntityItem());
				crusherMap.remove(event.entityLiving.getUniqueID());
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
//		for(int oid : OreDictionary.getOreIDs(event.itemStack))
//			event.toolTip.add(OreDictionary.getOreName(oid));
		
		if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT
				&& ClientUtils.mc().currentScreen != null
				&& ClientUtils.mc().currentScreen instanceof GuiBlastFurnace
				&& BlastFurnaceRecipe.isValidBlastFuel(event.itemStack))
			event.toolTip.add(EnumChatFormatting.GRAY+StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.blastFuelTime", BlastFurnaceRecipe.getBlastFuelTime(event.itemStack)));
	}

	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.crafting!=null && event.crafting.getItem().equals(IEContent.itemRevolver) && event.player!=null && eliteGunmen.contains(event.player.getUniqueID().toString()))
			event.crafting.setItemDamage(1);
	}
	static final List<String> eliteGunmen;
	static
	{
		eliteGunmen = ImmutableList.of("f34afdfb-996b-4020-b8a2-b740e2937b29", "07c11943-628b-4671-a331-84899d08e538");
	}
}
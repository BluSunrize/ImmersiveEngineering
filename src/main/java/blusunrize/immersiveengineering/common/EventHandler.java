package blusunrize.immersiveengineering.common;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
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
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
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
		if(!event.isCanceled() && Lib.DMG_Crusher.equals(event.source.getDamageType()))
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
	public void onEntityInteract(EntityInteractEvent event)
	{
		if(event.target instanceof EntityLivingBase && OreDictionary.itemMatches(new ItemStack(IEContent.itemRevolver,1,OreDictionary.WILDCARD_VALUE), event.entityPlayer.getCurrentEquippedItem(), false))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if( OreDictionary.itemMatches(new ItemStack(IEContent.itemRevolver,1,OreDictionary.WILDCARD_VALUE),event.crafting,false) && event.player!=null)	
		{
			String s = ItemNBTHelper.getString(event.crafting, "elite");
			if(event.player.getUniqueID().toString().equals("f34afdfb-996b-4020-b8a2-b740e2937b29"))
			{
				if(s==null||s.isEmpty())
				{
					if(event.crafting.getItemDamage()==0)
						event.crafting.setItemDamage(1);
					else if(event.crafting.getItemDamage()==1)
						ItemNBTHelper.setString(event.crafting, "elite", "fenrir");
				}
				else if(s.equals("fenrir"))
				{
					event.crafting.setItemDamage(3);
					ItemNBTHelper.setString(event.crafting, "elite", "sns");
				}
				else if(s.equals("sns"))
				{
					event.crafting.setItemDamage(1);
					ItemNBTHelper.setString(event.crafting, "elite", "oblivion");
				}
				else if(s.equals("oblivion"))
				{
					event.crafting.setItemDamage(4);
					ItemNBTHelper.setString(event.crafting, "elite", "nerf");
				}
				else if(s.equals("nerf"))
				{
					event.crafting.setItemDamage(0);
					ItemNBTHelper.remove(event.crafting, "elite");
				}
			}
			else if(event.player.getUniqueID().toString().equals("c2024e2a-dd76-4bc9-9ea3-b771f18f23b6"))
			{
				if(s.equals("earthshaker"))
					ItemNBTHelper.setString(event.crafting, "elite", "oblivion");
				else if(s.equals("oblivion"))
					ItemNBTHelper.setString(event.crafting, "elite", "oathkeeper");
				else if(s.equals("oathkeeper"))
					ItemNBTHelper.setString(event.crafting, "elite", "earthshaker");
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
}
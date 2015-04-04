package blusunrize.immersiveengineering.common;

import java.util.List;

import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;

import com.google.common.collect.ImmutableList;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

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
	public void onItemTooltip(ItemTooltipEvent event)
	{
		for(int oid : OreDictionary.getOreIDs(event.itemStack))
			event.toolTip.add(OreDictionary.getOreName(oid));
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
package blusunrize.immersiveengineering.common;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;
import WayofTime.alchemicalWizardry.api.event.TeleposeEvent;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

public class EventHandler
{
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event)
	{
		if(ImmersiveNetHandler.INSTANCE==null)
			ImmersiveNetHandler.INSTANCE = new ImmersiveNetHandler();
		//		if(event.world.provider.dimensionId==0)
		//		{
		/**
		if(ImmersiveNetHandler.INSTANCE==null)
			ImmersiveNetHandler.INSTANCE = new ImmersiveNetHandler();
		if(!event.world.isRemote && !IESaveData.loaded)
		{
			IELogger.info("[ImEng] - world data loading, dimension "+event.world.provider.dimensionId);
			IESaveData worldData = (IESaveData) event.world.loadItemData(IESaveData.class, IESaveData.dataName);
			if(worldData==null)
			{
				IELogger.info("[ImEng] - No World Data Found");
				worldData = new IESaveData(IESaveData.dataName);
				//				worldData.dimension = event.world.provider.dimensionId;
				event.world.setItemData(IESaveData.dataName, worldData);
			}
			else
				IELogger.info("World Data Retrieved");
			IESaveData.setInstance(event.world.provider.dimensionId, worldData);
			IESaveData.loaded = true;
		}
		 */
		//		}
	}

	@SubscribeEvent
	public void onSave(WorldEvent.Save event)
	{
		IESaveData.setDirty(0);
	}
	@SubscribeEvent
	public void onUnload(WorldEvent.Unload event)
	{
		IESaveData.setDirty(0);
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
	public void bloodMagicTeleposer(TeleposeEvent event)
	{
		TileEntity tI = event.initialWorld.getTileEntity(event.initialX, event.initialY, event.initialZ);
		TileEntity tF = event.finalWorld.getTileEntity(event.finalX, event.finalY, event.finalZ);
		if(tI instanceof TileEntityImmersiveConnectable || tF instanceof TileEntityImmersiveConnectable)
			event.setCanceled(true);
		if(tI instanceof TileEntityMultiblockPart || tF instanceof TileEntityMultiblockPart)
			event.setCanceled(true);
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
	}

	@SubscribeEvent()
	public void digSpeedEvent(PlayerEvent.BreakSpeed event)
	{
		ItemStack current = event.entityPlayer.getCurrentEquippedItem();
		//Stop the combustion drill from working underwater
		if(current!=null && current.getItem().equals(IEContent.itemDrill) && current.getItemDamage()==0 && event.entityPlayer.isInsideOfMaterial(Material.water))
			if( ((ItemDrill)IEContent.itemDrill).getUpgrades(current).getBoolean("waterproof"))
				event.newSpeed*=5;
			else
				event.setCanceled(true);
	}
	@SubscribeEvent
	public void onAnvilChange(AnvilUpdateEvent event)
	{
		if(event.left!=null && event.left.getItem() instanceof IDrillHead && ((IDrillHead)event.left.getItem()).getHeadDamage(event.left)>0)
		{
			if(event.right!=null && event.left.getItem().getIsRepairable(event.left, event.right))
			{
				event.output = event.left.copy();
				int repair = Math.min(
						((IDrillHead)event.output.getItem()).getHeadDamage(event.output),
						((IDrillHead)event.output.getItem()).getMaximumHeadDamage(event.output)/4);
				int cost = 0;
				for(;repair>0&&cost<event.right.stackSize; ++cost)
				{
					((IDrillHead)event.output.getItem()).damageHead(event.output, -repair);
					event.cost += Math.max(1, repair/200);
					repair = Math.min(
							((IDrillHead)event.output.getItem()).getHeadDamage(event.output),
							((IDrillHead)event.output.getItem()).getMaximumHeadDamage(event.output)/4);
				}
				event.materialCost = cost;

				if(event.name==null || event.name.isEmpty())
				{
					if(event.left.hasDisplayName())
					{
						event.cost += 5;
						event.output.func_135074_t();
					}
				}
				else if (!event.name.equals(event.left.getDisplayName()))
				{
					event.cost += 5;
					if(event.left.hasDisplayName())
						event.cost += 2;
					event.output.setStackDisplayName(event.name);
				}
			}
		}
	}
}
package blusunrize.immersiveengineering.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;
import WayofTime.alchemicalWizardry.api.event.TeleposeEvent;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

public class EventHandler
{
	public static ArrayList<ISpawnInterdiction> interdictionTiles = new ArrayList<ISpawnInterdiction>();
	public static boolean validateConnsNextTick = false;
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
	//transferPerTick
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
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase==TickEvent.Phase.START && validateConnsNextTick && FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			boolean validateConnections = Config.getBoolean("validateConnections");
			int invalidConnectionsDropped = 0;
			for (int dim:ImmersiveNetHandler.INSTANCE.getRelevantDimensions())
			{
				World world = MinecraftServer.getServer().worldServerForDimension(dim);
				if (world==null) {
					ImmersiveNetHandler.INSTANCE.directConnections.remove(dim);
					continue;
				}
				if (validateConnections)
				{
					for (Connection con:ImmersiveNetHandler.INSTANCE.getAllConnections(world))
					{
						if (!(world.getTileEntity(con.start.posX, con.start.posY,
								con.start.posZ) instanceof IImmersiveConnectable
								&& world.getTileEntity(con.end.posX, con.end.posY,
										con.end.posZ) instanceof IImmersiveConnectable))
						{
							ImmersiveNetHandler.INSTANCE.removeConnection(world, con);
							invalidConnectionsDropped++;
						}
					}
					IELogger.info("removed "+invalidConnectionsDropped+" invalid connections from world");
				}
			}

			validateConnsNextTick = false;
		}
		if(event.phase==TickEvent.Phase.END && FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			for(Map.Entry<Connection, Integer> e : ImmersiveNetHandler.INSTANCE.getTransferedRates(event.world.provider.dimensionId).entrySet())
				if(e.getValue()>e.getKey().cableType.getTransferRate())
				{
					if(event.world instanceof WorldServer)
						for(Vec3 vec : e.getKey().getSubVertices(event.world))
							((WorldServer)event.world).func_147487_a("flame", vec.xCoord,vec.yCoord,vec.zCoord, 0, 0,.02,0, 1);
					ImmersiveNetHandler.INSTANCE.removeConnection(event.world, e.getKey());
				}
			ImmersiveNetHandler.INSTANCE.getTransferedRates(event.world.provider.dimensionId).clear();
		}
	}

	@SubscribeEvent
	public void onLogin(PlayerLoggedInEvent event)
	{
		if(!event.player.worldObj.isRemote)
		{
			HashMap<MineralMix,Integer> packetMap = new HashMap<MineralMix,Integer>(); 
			for(Map.Entry<MineralMix,Integer> e: ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null && e.getValue()!=null)
					packetMap.put(e.getKey(), e.getValue());
			ImmersiveEngineering.packetHandler.sendToAll(new MessageMineralListSync(packetMap));
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

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event)
	{
		if(event.source.isFireDamage() && event.entityLiving.getActivePotionEffect(IEPotions.flammable)!=null)
		{
			int amp = event.entityLiving.getActivePotionEffect(IEPotions.flammable).getAmplifier();
			float mod = 1.5f + ((amp*amp)*.5f);
			event.ammount *= mod;
		}
		if(event.source.getDamageType().equals("flux") && event.entityLiving.getActivePotionEffect(IEPotions.conductive)!=null)
		{
			int amp = event.entityLiving.getActivePotionEffect(IEPotions.conductive).getAmplifier();
			float mod = 1.5f + ((amp*amp)*.5f); 
			event.ammount *= mod;
		}
	}
	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event)
	{
		if(event.entityLiving.getActivePotionEffect(IEPotions.sticky)!=null)
			event.entityLiving.motionY -= (event.entityLiving.getActivePotionEffect(IEPotions.sticky).getAmplifier()+1)*0.3F;
	}
	
	@SubscribeEvent
	public void onEnderTeleport(EnderTeleportEvent event)
	{
		if(event.entityLiving.isCreatureType(EnumCreatureType.monster, false))
		{
			synchronized (interdictionTiles) {
				Iterator<ISpawnInterdiction> it = interdictionTiles.iterator();
				while(it.hasNext())
				{
					ISpawnInterdiction interdictor = it.next();
					if(interdictor instanceof TileEntity)
					{
						if(((TileEntity)interdictor).isInvalid() || ((TileEntity)interdictor).getWorldObj()==null)
						{
							it.remove();
							continue;
						}
						else if( ((TileEntity)interdictor).getWorldObj().provider.dimensionId==event.entity.worldObj.provider.dimensionId && ((TileEntity)interdictor).getDistanceFrom(event.entity.posX, event.entity.posY, event.entity.posZ)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead || ((Entity)interdictor).worldObj==null)
						{
							it.remove();
							continue;
						}
						else if(((Entity)interdictor).worldObj.provider.dimensionId==event.entity.worldObj.provider.dimensionId && ((Entity)interdictor).getDistanceSqToEntity(event.entity)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
				}
			}
		}
	}
	@SubscribeEvent
	public void onEntitySpawnCheck(LivingSpawnEvent.CheckSpawn event)
	{
		if(event.getResult() == Event.Result.ALLOW||event.getResult() == Event.Result.DENY)
			return;
		if(event.entityLiving.isCreatureType(EnumCreatureType.monster, false))
		{
			synchronized (interdictionTiles) {
				Iterator<ISpawnInterdiction> it = interdictionTiles.iterator();
				while(it.hasNext())
				{
					ISpawnInterdiction interdictor = it.next();
					if(interdictor instanceof TileEntity)
					{
						if(((TileEntity)interdictor).isInvalid() || ((TileEntity)interdictor).getWorldObj()==null)
						{
							it.remove();
							continue;
						}
						else if( ((TileEntity)interdictor).getWorldObj().provider.dimensionId==event.entity.worldObj.provider.dimensionId && ((TileEntity)interdictor).getDistanceFrom(event.entity.posX, event.entity.posY, event.entity.posZ)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead || ((Entity)interdictor).worldObj==null)
						{
							it.remove();
							continue;
						}
						else if(((Entity)interdictor).worldObj.provider.dimensionId==event.entity.worldObj.provider.dimensionId && ((Entity)interdictor).getDistanceSqToEntity(event.entity)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
				}
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
		if(event.player!=null && OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,0), event.crafting, true))
			event.player.triggerAchievement(IEAchievements.craftHammer);

		if(event.crafting!=null && ItemNBTHelper.hasKey(event.crafting, "jerrycanFilling"))
		{
			int drain = ItemNBTHelper.getInt(event.crafting, "jerrycanFilling");
			for(int i=0;i<event.craftMatrix.getSizeInventory();i++)
			{
				ItemStack stackInSlot = event.craftMatrix.getStackInSlot(i);
				if(stackInSlot!=null)
					if(IEContent.itemJerrycan.equals(stackInSlot.getItem()) && ItemNBTHelper.hasKey(stackInSlot, "fluid"))
					{
						ItemNBTHelper.setInt(stackInSlot, "jerrycanDrain", drain);
						break;
					}
			}
			ItemNBTHelper.remove(event.crafting, "jerrycanFilling");	
		}
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
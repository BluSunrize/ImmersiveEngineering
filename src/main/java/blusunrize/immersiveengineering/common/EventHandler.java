package blusunrize.immersiveengineering.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import WayofTime.alchemicalWizardry.api.event.TeleposeEvent;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.energy.IICProxy;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.client.models.ModelShaderMinecart;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.entities.EntityPropertiesShaderCart;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.util.IEAchievements;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.compat.computercraft.TileEntityRequest;
import blusunrize.immersiveengineering.common.util.network.MessageDrill;
import blusunrize.immersiveengineering.common.util.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.material.Material;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;

public class EventHandler
{
	public static ArrayList<ISpawnInterdiction> interdictionTiles = new ArrayList<ISpawnInterdiction>();
	public static boolean validateConnsNextTick = false;
	public static Set<TileEntityRequest> ccRequestedTEs = Collections.newSetFromMap(new ConcurrentHashMap<TileEntityRequest, Boolean>());
	public static Set<TileEntityRequest> cachedRequestResults = Collections.newSetFromMap(new ConcurrentHashMap<TileEntityRequest, Boolean>());
	@SubscribeEvent
	public void onLoad(WorldEvent.Load event)
	{
		TileEntityCrusher.recipeCache.clear();
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
		if(event.world.isRemote)
		{
			if(!ModelShaderMinecart.rendersReplaced)
			{
				for(Object render : RenderManager.instance.entityRenderMap.values())
					if(RenderMinecart.class.isAssignableFrom(render.getClass()))
					{
						Object o = ObfuscationReflectionHelper.getPrivateValue(RenderMinecart.class, (RenderMinecart) render, "field_77013_a", "modelMinecart");
						if (o instanceof ModelMinecart)
						{
							ModelMinecart wrapped = (ModelMinecart) o;
							ObfuscationReflectionHelper.setPrivateValue(RenderMinecart.class, (RenderMinecart) render, (ModelMinecart) new ModelShaderMinecart(wrapped), "field_77013_a", "modelMinecart");
						}
					}
				ModelShaderMinecart.rendersReplaced = true;
			}
		}
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
	public void onEntityConstructing(EntityConstructing event)
	{
		if(event.entity instanceof EntityMinecart)
		{
			for(Class<? extends EntityMinecart> invalid : ShaderCaseMinecart.invalidMinecartClasses)
				if(invalid.isAssignableFrom(event.entity.getClass())) return;
			event.entity.registerExtendedProperties(EntityPropertiesShaderCart.PROPERTY_NAME, new EntityPropertiesShaderCart());
		}
	}
	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if(event.entity.worldObj.isRemote && event.entity instanceof EntityMinecart && event.entity.getExtendedProperties(EntityPropertiesShaderCart.PROPERTY_NAME)!=null)
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.entity,null));
	}
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event)
	{
		if(event.target instanceof EntityLivingBase && OreDictionary.itemMatches(new ItemStack(IEContent.itemRevolver,1,OreDictionary.WILDCARD_VALUE), event.entityPlayer.getCurrentEquippedItem(), false))
			event.setCanceled(true);
		if(!event.entityPlayer.worldObj.isRemote && event.target instanceof EntityMinecart && event.entityPlayer.getCurrentEquippedItem()!=null && event.entityPlayer.getCurrentEquippedItem().getItem() instanceof IShaderItem)
		{
			EntityPropertiesShaderCart properties = (EntityPropertiesShaderCart)event.target.getExtendedProperties(EntityPropertiesShaderCart.PROPERTY_NAME);
			if(properties!=null)
			{
				properties.setShader(Utils.copyStackWithAmount(event.entityPlayer.getCurrentEquippedItem(), 1));
				ImmersiveEngineering.packetHandler.sendTo(new MessageMinecartShaderSync(event.target,properties), (EntityPlayerMP)event.entityPlayer);
				event.setCanceled(true);
			}
		}
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
			if (validateConnections)
			{
				int invalidProxies = 0;
				Set<DimensionBlockPos> toRemove = new HashSet<>();
				for (Entry<DimensionBlockPos, IICProxy> e:ImmersiveNetHandler.INSTANCE.proxies.entrySet())
				{
					DimensionBlockPos p = e.getKey();
					World w = MinecraftServer.getServer().worldServerForDimension(p.dim);
					if (w==null)
					{
						invalidProxies++;
						toRemove.add(p);
						continue;
					}
					if (!(w.getTileEntity(p.posX, p.posY, p.posZ) instanceof IImmersiveConnectable))
					{
						invalidProxies++;
						toRemove.add(p);
					}
				}
				IELogger.info("Removed "+invalidProxies+" invalid connector proxies (used to transfer power through unloaded chunks)");
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
			// CC tile entity requests
			Iterator<TileEntityRequest> it;
			it = cachedRequestResults.iterator();
			while (it.hasNext())
			{
				TileEntityRequest req = it.next();
				if (!ccRequestedTEs.contains(req))
				{
					it.remove();
					continue;
				}
				req.te = req.w.getTileEntity(req.x, req.y, req.z);
			}
			it = ccRequestedTEs.iterator();
			int timeout = 100;
			while (it.hasNext() && timeout > 0)
			{
				TileEntityRequest req = it.next();
				synchronized (req)
				{
					req.te = req.w.getTileEntity(req.x, req.y, req.z);
					req.checked = true;
					req.notifyAll();
				}
				it.remove();
				timeout--;
				cachedRequestResults.add(req);
			}
			if (ItemDrill.animationTimer!=null&&event.world.getTotalWorldTime()!=ItemDrill.lastUpdate)
			{
				synchronized (ItemDrill.animationTimer)
				{
					for (String name:((Map<String, Integer>)ItemDrill.animationTimer).keySet())
					{
						Integer timer = ItemDrill.animationTimer.get(name);
						timer--;
						if (timer<=0)
						{
							ItemDrill.animationTimer.remove(name);
							ImmersiveEngineering.packetHandler.sendToAll(new MessageDrill(name, false));
						}
						else
							ItemDrill.animationTimer.put(name, timer);
					}
				}
				ItemDrill.lastUpdate = event.world.getTotalWorldTime();
			}
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
	public static HashSet<Class<? extends EntityLiving>> listOfBoringBosses = new HashSet();
	static{
		listOfBoringBosses.add(EntityWither.class);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLivingDropsLowest(LivingDropsEvent event)
	{
		if(!event.isCanceled() && Lib.DMG_Crusher.equals(event.source.getDamageType()))
		{
			TileEntityCrusher crusher = crusherMap.get(event.entityLiving.getUniqueID());
			if(crusher!=null)
			{
				ArrayList<ItemStack> out = new ArrayList<>();
				for(EntityItem item: event.drops)
					if(item!=null && item.getEntityItem()!=null)
						out.add(item.getEntityItem());
				crusher.outputItems(out);
				crusherMap.remove(event.entityLiving.getUniqueID());
				event.setCanceled(true);
			}
		}
	}
	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled() && event.entityLiving instanceof IBossDisplayData)
		{
			EnumRarity r = EnumRarity.epic;
			for(Class<? extends EntityLiving> boring : listOfBoringBosses)
				if(boring.isAssignableFrom(event.entityLiving.getClass()))
				{
					r = EnumRarity.rare;
					break;
				}
			String entityName = event.entityLiving.getClass().getSimpleName().toLowerCase();
			for(String name : Config.getStringArray("blacklistBosses"))
				if(name.toLowerCase(Locale.US).equals(entityName))
					return;

			ItemStack bag = new ItemStack(IEContent.itemShaderBag);
			ItemNBTHelper.setString(bag, "rarity", r.toString());
			event.drops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX,event.entityLiving.posY,event.entityLiving.posZ, bag));
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
							it.remove();
						else if( ((TileEntity)interdictor).getWorldObj().provider.dimensionId==event.entity.worldObj.provider.dimensionId && ((TileEntity)interdictor).getDistanceFrom(event.entity.posX, event.entity.posY, event.entity.posZ)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead || ((Entity)interdictor).worldObj==null)
							it.remove();
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
							it.remove();
						else if( ((TileEntity)interdictor).getWorldObj().provider.dimensionId==event.entity.worldObj.provider.dimensionId && ((TileEntity)interdictor).getDistanceFrom(event.entity.posX, event.entity.posY, event.entity.posZ)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead || ((Entity)interdictor).worldObj==null)
							it.remove();
						else if(((Entity)interdictor).worldObj.provider.dimensionId==event.entity.worldObj.provider.dimensionId && ((Entity)interdictor).getDistanceSqToEntity(event.entity)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event)
	{
		if(event.player!=null)
			for(IEAchievements.AchievementIE achievement : IEAchievements.normalCraftingAchievements)
			{
				if(achievement.triggerItems!=null && achievement.triggerItems.length>0)
				{
					for(ItemStack trigger : achievement.triggerItems)
						if(OreDictionary.itemMatches(trigger, event.crafting, true))
						{
							event.player.triggerAchievement(achievement);
							break;
						}
				}
				else if(OreDictionary.itemMatches(achievement.theItemStack, event.crafting, true))
					event.player.triggerAchievement(achievement);
			}

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

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onBlockPlaced(BlockEvent.PlaceEvent event)
	{
		if(event.player!=null && !event.isCanceled())
			for(IEAchievements.AchievementIE achievement : IEAchievements.placementAchievements)
			{
				if(achievement.triggerItems!=null && achievement.triggerItems.length>0)
				{
					for(ItemStack trigger : achievement.triggerItems)
						if(OreDictionary.itemMatches(trigger, event.itemInHand, true))
						{
							event.player.triggerAchievement(achievement);
							break;
						}
				}
				else if(OreDictionary.itemMatches(achievement.theItemStack, event.itemInHand, true))
					event.player.triggerAchievement(achievement);
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
	public static TileEntity requestTE(World w, int x, int y, int z)
	{
		TileEntityRequest req = new TileEntityRequest(w, x, y, z);
		TileEntity te = null;
		Iterator<TileEntityRequest> it = cachedRequestResults.iterator();
		while (it.hasNext())
		{
			TileEntityRequest curr = it.next();
			if (req.equals(curr))
				te = curr.te;
		}
		if (te!=null)
			return te;
		synchronized (req)
		{
			ccRequestedTEs.add(req);
			int timeout = 100;
			while (!req.checked&&timeout>0)
			{
				// i don't really know why this is necessary, but the requests sometimes time out without this
				if (!ccRequestedTEs.contains(req))
					ccRequestedTEs.add(req);
				try
				{
					req.wait(50);
				}
				catch (InterruptedException e)
				{}
				timeout--;
			}
			if (!req.checked)
			{
				IELogger.info("Timeout while requesting a TileEntity");
				return w.getTileEntity(x, y, z);
			}
		}
		return req.te;
	}
}
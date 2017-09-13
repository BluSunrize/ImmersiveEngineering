package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.energy.wires.IICProxy;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.crafting.ArcRecyclingThreadHandler;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemIEShield;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources.TeslaDamageSource;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class EventHandler
{
	public static ArrayList<ISpawnInterdiction> interdictionTiles = new ArrayList<ISpawnInterdiction>();
	public static boolean validateConnsNextTick = false;
	public static HashSet<IEExplosion> currentExplosions = new HashSet<IEExplosion>();
	public static final Queue<Pair<Integer, BlockPos>> requestedBlockUpdates = new LinkedList<>();
	public static final Set<TileEntity> REMOVE_FROM_TICKING = new HashSet<>();

	@SubscribeEvent
	public void onLoad(WorldEvent.Load event)
	{
		//		if(event.world.provider.dimensionId==0)
		//		{
		/*
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
		ImmersiveEngineering.proxy.onWorldLoad();
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
	public void onCapabilitiesAttach(AttachCapabilitiesEvent event)
	{
		if(event.getObject() instanceof EntityMinecart)
		{
			EntityMinecart entityMinecart = (EntityMinecart) event.getObject();
			event.addCapability(new ResourceLocation("immersiveengineering:shader"), new ShaderWrapper_Direct("immersiveengineering:minecart"));
		}
	}
	@SubscribeEvent
	public void onMinecartInteraction(MinecartInteractEvent event)
	{
		if(!event.getPlayer().world.isRemote && !event.getItem().isEmpty() && event.getItem().getItem() instanceof IShaderItem)
			if(event.getMinecart().hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
//				if(event.getMinecart().hasCapability(CapabilityHandler_CartShaders.SHADER_CAPABILITY, null))
			{
				ShaderWrapper handler = event.getMinecart().getCapability(CapabilityShader.SHADER_CAPABILITY, null);
				if(handler != null)
				{
					handler.setShaderItem(Utils.copyStackWithAmount(event.getItem(), 1));
					ImmersiveEngineering.packetHandler.sendTo(new MessageMinecartShaderSync(event.getMinecart(), handler), (EntityPlayerMP) event.getPlayer());
					event.setCanceled(true);
				}
			}
	}

	public static List<ResourceLocation> lootInjections = Arrays.asList(new ResourceLocation(ImmersiveEngineering.MODID, "chests/stronghold_library"),new ResourceLocation(ImmersiveEngineering.MODID, "chests/village_blacksmith"));
	static Field f_lootEntries;
	@SubscribeEvent
	public void lootLoad(LootTableLoadEvent event)
	{
		if(event.getName().getResourceDomain().equals("minecraft"))
			for(ResourceLocation inject : lootInjections)
				if(event.getName().getResourcePath().equals(inject.getResourcePath()))
				{
					LootPool injectPool = Utils.loadBuiltinLootTable(inject, event.getLootTableManager()).getPool("immersiveengineering_loot_inject");
					LootPool mainPool = event.getTable().getPool("main");
					if(injectPool!=null && mainPool!=null)
						try
						{
							if(f_lootEntries==null)
							{
								f_lootEntries = LootPool.class.getDeclaredField(ObfuscationReflectionHelper.remapFieldNames(LootPool.class.getName(), "field_186453_a")[0]);//field_186453_a is srg for lootEntries
								f_lootEntries.setAccessible(true);
							}
							if(f_lootEntries!=null)
							{
								List<LootEntry> entryList = (List<LootEntry>) f_lootEntries.get(injectPool);
								for(LootEntry entry : entryList)
									mainPool.addEntry(entry);
							}
						}catch(Exception e){
							e.printStackTrace();
						}
				}
	}

	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if(event.getEntity().world.isRemote && event.getEntity() instanceof EntityMinecart && event.getEntity().hasCapability(CapabilityShader.SHADER_CAPABILITY,null))
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.getEntity(),null));
	}
//	@SubscribeEvent
//	public void onEntityInteract(EntityInteractEvent event)
//	{
//		if(event.target instanceof EntityLivingBase && OreDictionary.itemMatches(new ItemStack(IEContent.itemRevolver,1,OreDictionary.WILDCARD_VALUE), event.entityPlayer.getCurrentEquippedItem(), false))
//			event.setCanceled(true);
//	}



	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if (event.phase==TickEvent.Phase.START && validateConnsNextTick && FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			boolean validateConnections = IEConfig.validateConnections;
			int invalidConnectionsDropped = 0;
			for (int dim:ImmersiveNetHandler.INSTANCE.getRelevantDimensions())
			{
				if (!validateConnections)
				{
					continue;
				}
				World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
				if (world==null) {
					ImmersiveNetHandler.INSTANCE.directConnections.remove(dim);
					continue;
				}
				for (Connection con:ImmersiveNetHandler.INSTANCE.getAllConnections(world))
				{
					if (!(world.getTileEntity(con.start) instanceof IImmersiveConnectable
							&& world.getTileEntity(con.end) instanceof IImmersiveConnectable))
					{
						ImmersiveNetHandler.INSTANCE.removeConnection(world, con);
						invalidConnectionsDropped++;
					}
				}
				IELogger.info("removed "+invalidConnectionsDropped+" invalid connections from world");
			}
			int invalidProxies = 0;
			Set<DimensionBlockPos> toRemove = new HashSet<>();
			for (Entry<DimensionBlockPos, IICProxy> e:ImmersiveNetHandler.INSTANCE.proxies.entrySet())
			{
				if (!validateConnections)
				{
					continue;
				}
				DimensionBlockPos p = e.getKey();
				World w = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(p.dimension);
				if (w!=null&&w.isBlockLoaded(p))
					toRemove.add(p);
				if (validateConnections&&w==null)
				{
					invalidProxies++;
					toRemove.add(p);
					continue;
				}
				if (validateConnections&&!(w.getTileEntity(p) instanceof IImmersiveConnectable))
				{
					invalidProxies++;
					toRemove.add(p);
				}
			}
			if (invalidProxies>0)
				IELogger.info("Removed "+invalidProxies+" invalid connector proxies (used to transfer power through unloaded chunks)");
			validateConnsNextTick = false;
		}
		if (event.phase==TickEvent.Phase.END && ArcRecyclingThreadHandler.recipesToAdd!=null)
		{
			for(ArcFurnaceRecipe recipe : ArcRecyclingThreadHandler.recipesToAdd)
			{
				ArcFurnaceRecipe.recipeList.add(recipe);
				IECompatModule.jeiAddFunc.accept(recipe);
			}
			ArcRecyclingThreadHandler.recipesToAdd = null;
		}
		if(event.phase==TickEvent.Phase.END && FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			int dim = event.world.provider.getDimension();
			for(Entry<Connection, Integer> e : ImmersiveNetHandler.INSTANCE.getTransferedRates(dim).entrySet())
				if(e.getValue()>e.getKey().cableType.getTransferRate())
				{
					if(event.world instanceof WorldServer)
						for(Vec3d vec : e.getKey().getSubVertices(event.world))
							((WorldServer)event.world).spawnParticle(EnumParticleTypes.FLAME, false, vec.x,vec.y,vec.z, 0, 0,.02,0, 1, new int[0]);
					ImmersiveNetHandler.INSTANCE.removeConnection(event.world, e.getKey());
				}
			ImmersiveNetHandler.INSTANCE.getTransferedRates(dim).clear();

			if (!REMOVE_FROM_TICKING.isEmpty())
			{
				event.world.tickableTileEntities.removeAll(REMOVE_FROM_TICKING);
				REMOVE_FROM_TICKING.removeIf((te) -> te.getWorld().provider.getDimension() == dim);
			}
		}
		if(event.phase==TickEvent.Phase.START)
		{
			if(!currentExplosions.isEmpty())
			{
				Iterator<IEExplosion> itExplosion = currentExplosions.iterator();
				while(itExplosion.hasNext())
				{
					IEExplosion ex = itExplosion.next();
					ex.doExplosionTick();
					if(ex.isExplosionFinished)
						itExplosion.remove();
				}
			}
			synchronized (requestedBlockUpdates)
			{
				while (!requestedBlockUpdates.isEmpty())
				{
					Pair<Integer, BlockPos> curr = requestedBlockUpdates.poll();
					if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
					{
						World w = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(curr.getLeft());
						if(w!=null)
						{
							IBlockState state = w.getBlockState(curr.getRight());
							w.notifyBlockUpdate(curr.getRight(), state,state, 3);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLogin(PlayerLoggedInEvent event)
	{
		ExcavatorHandler.allowPackets = true;
		if(!event.player.world.isRemote)
		{
			HashMap<MineralMix,Integer> packetMap = new HashMap<MineralMix,Integer>();
			for(Entry<MineralMix,Integer> e: ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null && e.getValue()!=null)
					packetMap.put(e.getKey(), e.getValue());
			ImmersiveEngineering.packetHandler.sendToAll(new MessageMineralListSync(packetMap));
		}
	}
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLogout(PlayerLoggedOutEvent event)
	{
		ExcavatorHandler.allowPackets = false;
	}

	@SubscribeEvent
	public void harvestCheck(PlayerEvent.HarvestCheck event)
	{
		if(event.getTargetBlock().getBlock() instanceof BlockIEBase)
		{
			if(!event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).isEmpty()&&event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem().getToolClasses(event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND)).contains(Lib.TOOL_HAMMER))
			{
				RayTraceResult mop = Utils.getMovingObjectPositionFromPlayer(event.getEntityPlayer().world, event.getEntityPlayer(), true);
				if(mop!=null&&mop.typeOfHit==RayTraceResult.Type.BLOCK)
					if(((BlockIEBase)event.getTargetBlock().getBlock()).allowHammerHarvest(event.getTargetBlock()))
						event.setCanHarvest(true);
			}
			if(!event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).isEmpty()&&event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem().getToolClasses(event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND)).contains(Lib.TOOL_WIRECUTTER))
			{
				RayTraceResult mop = Utils.getMovingObjectPositionFromPlayer(event.getEntityPlayer().world, event.getEntityPlayer(), true);
				if(mop!=null&&mop.typeOfHit==RayTraceResult.Type.BLOCK)
					if(((BlockIEBase)event.getTargetBlock().getBlock()).allowWirecutterHarvest(event.getTargetBlock()))
						event.setCanHarvest(true);
			}
		}
	}
	//	@SubscribeEvent
	//	public void bloodMagicTeleposer(TeleposeEvent event)
	//	{
	//		TileEntity tI = event.initialWorld.getTileEntity(event.initialX, event.initialY, event.initialZ);
	//		TileEntity tF = event.finalWorld.getTileEntity(event.finalX, event.finalY, event.finalZ);
	//		if(tI instanceof TileEntityImmersiveConnectable || tF instanceof TileEntityImmersiveConnectable)
	//			event.setCanceled(true);
	//		if(tI instanceof TileEntityMultiblockPart || tF instanceof TileEntityMultiblockPart)
	//			event.setCanceled(true);
	//	}

	public static HashMap<UUID, TileEntityCrusher> crusherMap = new HashMap<UUID, TileEntityCrusher>();
	public static HashSet<Class<? extends EntityLiving>> listOfBoringBosses = new HashSet();
	static{
		listOfBoringBosses.add(EntityWither.class);
	}
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLivingDropsLowest(LivingDropsEvent event)
	{
		if(!event.isCanceled() && Lib.DMG_Crusher.equals(event.getSource().getDamageType()))
		{
			TileEntityCrusher crusher = crusherMap.get(event.getEntityLiving().getUniqueID());
			if(crusher!=null)
			{
				for(EntityItem item: event.getDrops())
					if(item!=null && !item.getItem().isEmpty())
						crusher.doProcessOutput(item.getItem());
				crusherMap.remove(event.getEntityLiving().getUniqueID());
				event.setCanceled(true);
			}
		}
	}
	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled() && !event.getEntityLiving().isNonBoss())
		{
			EnumRarity r = EnumRarity.EPIC;
			for(Class<? extends EntityLiving> boring : listOfBoringBosses)
				if(boring.isAssignableFrom(event.getEntityLiving().getClass()))
					return;
			ItemStack bag = new ItemStack(IEContent.itemShaderBag);
			ItemNBTHelper.setString(bag, "rarity", r.toString());
			event.getDrops().add(new EntityItem(event.getEntityLiving().world, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, bag));
		}
	}

	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			ItemStack activeStack = player.getActiveItemStack();
			if(!activeStack.isEmpty() && activeStack.getItem() instanceof ItemIEShield && event.getAmount()>=3 && Utils.canBlockDamageSource(player, event.getSource()))
			{
				float amount = event.getAmount();
				((ItemIEShield)activeStack.getItem()).hitShield(activeStack, player, event.getSource(), amount, event);
			}
		}
	}


	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event)
	{
		if(event.getSource().isFireDamage() && event.getEntityLiving().getActivePotionEffect(IEPotions.flammable)!=null)
		{
			int amp = event.getEntityLiving().getActivePotionEffect(IEPotions.flammable).getAmplifier();
			float mod = 1.5f + ((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(("flux".equals(event.getSource().getDamageType())||IEDamageSources.razorShock.equals(event.getSource())||event.getSource() instanceof TeslaDamageSource) && event.getEntityLiving().getActivePotionEffect(IEPotions.conductive)!=null)
		{
			int amp = event.getEntityLiving().getActivePotionEffect(IEPotions.conductive).getAmplifier();
			float mod = 1.5f + ((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
	}
	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event)
	{
		if(event.getEntityLiving().getActivePotionEffect(IEPotions.sticky)!=null)
			event.getEntityLiving().motionY -= (event.getEntityLiving().getActivePotionEffect(IEPotions.sticky).getAmplifier()+1)*0.3F;
		else if(event.getEntityLiving().getActivePotionEffect(IEPotions.concreteFeet)!=null)
		{
			event.getEntityLiving().motionX = 0;
			event.getEntityLiving().motionY = 0;
			event.getEntityLiving().motionZ = 0;
		}
	}
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.getEntityLiving() instanceof EntityPlayer && !event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() && ItemNBTHelper.hasKey(event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
				powerpack.getItem().onArmorTick(event.getEntityLiving().getEntityWorld(), (EntityPlayer)event.getEntityLiving(), powerpack);
		}
	}


	@SubscribeEvent
	public void onEnderTeleport(EnderTeleportEvent event)
	{
		if(event.getEntityLiving().isCreatureType(EnumCreatureType.MONSTER, false))
		{
			synchronized (interdictionTiles) {
				Iterator<ISpawnInterdiction> it = interdictionTiles.iterator();
				while(it.hasNext())
				{
					ISpawnInterdiction interdictor = it.next();
					if(interdictor instanceof TileEntity)
					{
						if(((TileEntity)interdictor).isInvalid() || ((TileEntity)interdictor).getWorld()==null)
							it.remove();
						else if( ((TileEntity)interdictor).getWorld().provider.getDimension()== event.getEntity().world.provider.getDimension() && ((TileEntity)interdictor).getDistanceSq(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ)<=interdictor.getInterdictionRangeSquared())
							event.setCanceled(true);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead || ((Entity)interdictor).world==null)
							it.remove();
						else if(((Entity)interdictor).world.provider.getDimension()== event.getEntity().world.provider.getDimension() && ((Entity)interdictor).getDistanceSqToEntity(event.getEntity())<=interdictor.getInterdictionRangeSquared())
							event.setCanceled(true);
					}
				}
			}
		}
		if(event.getEntityLiving().getActivePotionEffect(IEPotions.stunned)!=null)
			event.setCanceled(true);
	}
	@SubscribeEvent
	public void onEntitySpawnCheck(LivingSpawnEvent.CheckSpawn event)
	{
		if(event.getResult() == Event.Result.ALLOW||event.getResult() == Event.Result.DENY)
			return;
		if(event.getEntityLiving().isCreatureType(EnumCreatureType.MONSTER, false))
		{
			synchronized (interdictionTiles) {
				Iterator<ISpawnInterdiction> it = interdictionTiles.iterator();
				while(it.hasNext())
				{
					ISpawnInterdiction interdictor = it.next();
					if(interdictor instanceof TileEntity)
					{
						if(((TileEntity)interdictor).isInvalid() || ((TileEntity)interdictor).getWorld()==null)
							it.remove();
						else if( ((TileEntity)interdictor).getWorld().provider.getDimension()== event.getEntity().world.provider.getDimension() && ((TileEntity)interdictor).getDistanceSq(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ)<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead || ((Entity)interdictor).world==null)
							it.remove();
						else if(((Entity)interdictor).world.provider.getDimension()== event.getEntity().world.provider.getDimension() && ((Entity)interdictor).getDistanceSqToEntity(event.getEntity())<=interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void digSpeedEvent(PlayerEvent.BreakSpeed event)
	{
		ItemStack current = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
		//Stop the combustion drill from working underwater
		if(!current.isEmpty() && current.getItem().equals(IEContent.itemDrill) && current.getItemDamage()==0 && event.getEntityPlayer().isInsideOfMaterial(Material.WATER))
			if( ((ItemDrill)IEContent.itemDrill).getUpgrades(current).getBoolean("waterproof"))
				event.setNewSpeed(event.getOriginalSpeed()*5);
			else
				event.setCanceled(true);
		if(event.getState().getBlock()==IEContent.blockMetalDecoration2 && IEContent.blockMetalDecoration2.getMetaFromState(event.getState())==BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta())
			if(!OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,1), current, false))
				event.setCanceled(true);
		TileEntity te = event.getEntityPlayer().getEntityWorld().getTileEntity(event.getPos());
		if(te instanceof IEntityProof && !((IEntityProof)te).canEntityDestroy(event.getEntityPlayer()))
			event.setCanceled(true);
	}
	@SubscribeEvent
	public void onAnvilChange(AnvilUpdateEvent event)
	{
		if(!event.getLeft().isEmpty() && event.getLeft().getItem() instanceof IDrillHead && ((IDrillHead) event.getLeft().getItem()).getHeadDamage(event.getLeft())>0)
		{
			if(!event.getRight().isEmpty() && event.getLeft().getItem().getIsRepairable(event.getLeft(), event.getRight()))
			{
				event.setOutput(event.getLeft().copy());
				int repair = Math.min(
						((IDrillHead)event.getOutput().getItem()).getHeadDamage(event.getOutput()),
						((IDrillHead) event.getOutput().getItem()).getMaximumHeadDamage(event.getOutput())/4);
				int cost = 0;
				for(; repair>0&& cost < event.getRight().getCount(); ++cost)
				{
					((IDrillHead) event.getOutput().getItem()).damageHead(event.getOutput(), -repair);
					event.setCost(Math.max(1, repair/200));
					repair = Math.min(
							((IDrillHead) event.getOutput().getItem()).getHeadDamage(event.getOutput()),
							((IDrillHead) event.getOutput().getItem()).getMaximumHeadDamage(event.getOutput())/4);
				}
				event.setMaterialCost(cost);

				if(event.getName()==null || event.getName().isEmpty())
				{
					if(event.getLeft().hasDisplayName())
					{
						event.setCost(event.getCost()+5);
						event.getOutput().clearCustomName();
					}
				}
				else if (!event.getName().equals(event.getLeft().getDisplayName()))
				{
					event.setCost(event.getCost()+5);
					if(event.getLeft().hasDisplayName())
						event.setCost(event.getCost()+2);
					event.getOutput().setStackDisplayName(event.getName());
				}
			}
		}
	}
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void breakLast(BlockEvent.BreakEvent event) {
		if (event.getState().getBlock() instanceof BlockIEMultiblock) {
			TileEntity te = event.getWorld().getTileEntity(event.getPos());
			if (te instanceof TileEntityMultiblockPart) {
				((TileEntityMultiblockPart) te).onlyLocalDissassembly = event.getWorld().getTotalWorldTime();
			}
		}
	}

	@SubscribeEvent
	public void remap(RegistryEvent.MissingMappings<?> ev)
	{
		NameRemapper.remap(ev);
	}
}
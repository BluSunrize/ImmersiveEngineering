/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.CapabilitySkyhookData.SimpleSkyhookProvider;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.NetHandlerCapability;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.RazorWireTileEntity;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.network.MessageMineralListSync;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.MinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.thread.EffectiveSide;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class EventHandler
{
	//TODO move to a capability
	public static final Map<DimensionType, Set<ISpawnInterdiction>> interdictionTiles = new HashMap<>();
	public static boolean validateConnsNextTick = false;
	public static HashSet<IEExplosion> currentExplosions = new HashSet<IEExplosion>();
	public static final Queue<Pair<DimensionType, BlockPos>> requestedBlockUpdates = new LinkedList<>();
	public static final Set<TileEntity> REMOVE_FROM_TICKING = new HashSet<>();

	@SubscribeEvent
	public void onLoad(WorldEvent.Load event)
	{
		ImmersiveEngineering.proxy.onWorldLoad();
	}

	@SubscribeEvent
	public void onCapabilitiesAttachEntity(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof AbstractMinecartEntity)
			event.addCapability(new ResourceLocation("immersiveengineering:shader"),
					new ShaderWrapper_Direct("immersiveengineering:minecart"));
		if(event.getObject() instanceof PlayerEntity)
			event.addCapability(new ResourceLocation(ImmersiveEngineering.MODID, "skyhook_data"),
					new SimpleSkyhookProvider());
	}

	@SubscribeEvent
	public void onCapabilitiesAttachWorld(AttachCapabilitiesEvent<World> event)
	{
		event.addCapability(new ResourceLocation(ImmersiveEngineering.MODID, "wire_network"),
				new NetHandlerCapability.Provider(event.getObject()));
	}

	@SubscribeEvent
	public void onTagsUpdated(TagsUpdatedEvent tagsChanged)
	{
		if(EffectiveSide.get().isServer())
		{
			IERecipes.addTagBasedRecipes();//TODO does this already have the new tags?
		}
	}

	@SubscribeEvent
	public void onMinecartInteraction(EntityInteractSpecific event)
	{
		PlayerEntity player = event.getEntityPlayer();
		ItemStack stack = event.getItemStack();
		if(!(event.getEntity() instanceof MinecartEntity))
			return;
		MinecartEntity cart = (MinecartEntity)event.getEntity();
		if(!player.world.isRemote&&!stack.isEmpty()&&stack.getItem() instanceof IShaderItem)
			cart.getCapability(CapabilityShader.SHADER_CAPABILITY).ifPresent(wrapper ->
			{
				wrapper.setShaderItem(Utils.copyStackWithAmount(stack, 1));
				ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player),
						new MessageMinecartShaderSync(cart, wrapper));
				event.setCanceled(true);
			});
	}

	/*TODO re-add when the event exists again!
	@SubscribeEvent
	public void onMinecartUpdate(MinecartUpdateEvent event)
	{
		if(event.getMinecart().ticksExisted%3==0 && event.getMinecart().hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
		{
			ShaderWrapper wrapper = event.getMinecart().getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			if(wrapper!=null)
			{
				Vec3d prevPosVec = new Vec3d(event.getMinecart().prevPosX, event.getMinecart().prevPosY, event.getMinecart().prevPosZ);
				Vec3d movVec = prevPosVec.subtract(event.getMinecart().posX, event.getMinecart().posY, event.getMinecart().posZ);
				if(movVec.lengthSquared() > 0.0001)
				{
					movVec = movVec.normalize();
					Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(wrapper);
					if(shader!=null)
						shader.getMiddle().getEffectFunction().execute(event.getMinecart().world, shader.getLeft(), null, shader.getRight().getShaderType(), prevPosVec.add(0, .25, 0).add(movVec), movVec.scale(1.5f), .25f);
				}
			}
		}
	}*/


	public static List<ResourceLocation> lootInjections = Arrays.asList(
			new ResourceLocation(ImmersiveEngineering.MODID, "chests/stronghold_library"),
			new ResourceLocation(ImmersiveEngineering.MODID, "chests/village_blacksmith")
	);
	private static Field f_lootEntries;

	/*TODO I think this can be done data-driven now?
	@SubscribeEvent
	public void lootLoad(LootTableLoadEvent event)
	{
		if(event.getName().getNamespace().equals("minecraft"))
			for(ResourceLocation inject : lootInjections)
				if(event.getName().getPath().equals(inject.getPath()))
				{
					LootPool injectPool = Utils.loadBuiltinLootTable(inject, event.getLootTableManager()).getPool("immersiveengineering_loot_inject");
					LootPool mainPool = event.getTable().getPool("main");
					if(injectPool!=null&&mainPool!=null)
						try
						{
							if(f_lootEntries==null)
							{
								f_lootEntries = LootPool.class.getDeclaredField(ObfuscationReflectionHelper.findField(LootPool.class.getName(), "field_186453_a"));//field_186453_a is srg for lootEntries
								f_lootEntries.setAccessible(true);
							}
							if(f_lootEntries!=null)
							{
								List<ILootGenerator> entryList = (List<ILootGenerator>)f_lootEntries.get(injectPool);
								for(ILootGenerator entry : entryList)
									mainPool.addEntry(entry);
							}
						} catch(Exception e)
						{
							e.printStackTrace();
						}
				}
	}
	 */

	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if(event.getEntity().world.isRemote&&event.getEntity() instanceof AbstractMinecartEntity&&
				event.getEntity().getCapability(CapabilityShader.SHADER_CAPABILITY).isPresent())
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.getEntity(), null));
	}

	private LongList tickTimes = new LongArrayList();
	private long lastTick = -1;
	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if(event.phase==Phase.START&&event.world.dimension.getType()==DimensionType.OVERWORLD&&!event.world.isRemote)
		{
			lastTick = Util.milliTime();
		}
		if(event.phase==Phase.END&&event.world.dimension.getType()==DimensionType.OVERWORLD&&!event.world.isRemote)
		{
			long curr = Util.milliTime();
			tickTimes.add(curr-lastTick);
			if(event.world.getGameTime()%40==0)
			{
				long sum = 0;
				long max = 0, min = Long.MAX_VALUE;
				for(Long i : tickTimes)
				{
					sum += i;
					if(i > max) max = i;
					if(i < min) min = i;
				}
				IELogger.info("Max tick time: {}, min {}, mean {}", max, min, sum/(double)tickTimes.size());
				tickTimes.clear();
			}
		}
		if(event.phase==TickEvent.Phase.START&&validateConnsNextTick&&!event.world.isRemote)
		{
			//TODO implement for the new system
		}
		if(event.phase==TickEvent.Phase.END&&!event.world.isRemote)
		{
			DimensionType dim = event.world.getDimension().getType();
			GlobalWireNetwork.getNetwork(event.world).tick();

			if(!REMOVE_FROM_TICKING.isEmpty())
			{
				event.world.tickableTileEntities.removeAll(REMOVE_FROM_TICKING);
				REMOVE_FROM_TICKING.removeIf((te) -> te.getWorld().getDimension().getType()==dim);
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
			while(!requestedBlockUpdates.isEmpty())
			{
				Pair<DimensionType, BlockPos> curr = requestedBlockUpdates.poll();
				World w = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), curr.getLeft(),
						false, false);
				if(w!=null)
				{
					BlockState state = w.getBlockState(curr.getRight());
					w.notifyBlockUpdate(curr.getRight(), state, state, 3);
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLogin(PlayerLoggedInEvent event)
	{
		ExcavatorHandler.allowPacketsToPlayer.add(event.getPlayer().getUniqueID());
		if(!event.getPlayer().world.isRemote)
		{
			HashMap<MineralMix, Integer> packetMap = new HashMap<MineralMix, Integer>();
			for(Entry<MineralMix, Integer> e : ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null&&e.getValue()!=null)
					packetMap.put(e.getKey(), e.getValue());
			ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)event.getPlayer()),
					new MessageMineralListSync(packetMap));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLogout(PlayerLoggedOutEvent event)
	{
		ExcavatorHandler.allowPacketsToPlayer.remove(event.getPlayer().getUniqueID());
	}

	public static HashMap<UUID, CrusherTileEntity> crusherMap = new HashMap<UUID, CrusherTileEntity>();
	public static HashSet<Class<? extends MobEntity>> listOfBoringBosses = new HashSet();

	static
	{
		listOfBoringBosses.add(WitherEntity.class);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDropsLowest(LivingDropsEvent event)
	{
		if(!event.isCanceled()&&Lib.DMG_Crusher.equals(event.getSource().getDamageType()))
		{
			CrusherTileEntity crusher = crusherMap.get(event.getEntityLiving().getUniqueID());
			if(crusher!=null)
			{
				for(ItemEntity item : event.getDrops())
					if(item!=null&&!item.getItem().isEmpty())
						crusher.doProcessOutput(item.getItem());
				crusherMap.remove(event.getEntityLiving().getUniqueID());
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled()&&!event.getEntityLiving().isNonBoss())
		{
			Rarity r = Rarity.EPIC;
			for(Class<? extends MobEntity> boring : listOfBoringBosses)
				if(boring.isAssignableFrom(event.getEntityLiving().getClass()))
					return;
			ItemStack bag = new ItemStack(Misc.shaderBag.get(r));
			event.getDrops().add(new ItemEntity(event.getEntityLiving().world, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, bag));
		}
	}

	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		if(event.getEntityLiving() instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			ItemStack activeStack = player.getActiveItemStack();
			if(!activeStack.isEmpty()&&activeStack.getItem() instanceof IEShieldItem&&event.getAmount() >= 3&&Utils.canBlockDamageSource(player, event.getSource()))
			{
				float amount = event.getAmount();
				((IEShieldItem)activeStack.getItem()).hitShield(activeStack, player, event.getSource(), amount, event);
			}
		}
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event)
	{
		if(event.getSource().isFireDamage()&&event.getEntityLiving().getActivePotionEffect(IEPotions.flammable)!=null)
		{
			int amp = event.getEntityLiving().getActivePotionEffect(IEPotions.flammable).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(("flux".equals(event.getSource().getDamageType())||IEDamageSources.razorShock.equals(event.getSource())||
				event.getSource() instanceof ElectricDamageSource)&&event.getEntityLiving().getActivePotionEffect(IEPotions.conductive)!=null)
		{
			int amp = event.getEntityLiving().getActivePotionEffect(IEPotions.conductive).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(!event.isCanceled()&&!event.getEntityLiving().isNonBoss()&&event.getAmount() >= event.getEntityLiving().getHealth()&&event.getSource().getTrueSource() instanceof PlayerEntity&&((PlayerEntity)event.getSource().getTrueSource()).getHeldItem(Hand.MAIN_HAND).getItem() instanceof DrillItem)
			Utils.unlockIEAdvancement((PlayerEntity)event.getSource().getTrueSource(), "main/secret_drillbreak");
	}

	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event)
	{
		Vec3d motion = event.getEntity().getMotion();
		if(event.getEntityLiving().getActivePotionEffect(IEPotions.sticky)!=null)
			motion = motion.subtract(0, (event.getEntityLiving().getActivePotionEffect(IEPotions.sticky).getAmplifier()+1)*0.3F, 0);
		else if(event.getEntityLiving().getActivePotionEffect(IEPotions.concreteFeet)!=null)
			motion = Vec3d.ZERO;
		event.getEntity().setMotion(motion);
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.getEntityLiving() instanceof PlayerEntity&&!event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty()&&ItemNBTHelper.hasKey(event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.CHEST), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getEntityLiving().getItemStackFromSlot(EquipmentSlotType.CHEST), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
				powerpack.getItem().onArmorTick(powerpack, event.getEntityLiving().getEntityWorld(), (PlayerEntity)event.getEntityLiving());
		}
	}

	@SubscribeEvent
	public void onEnderTeleport(EnderTeleportEvent event)
	{
		if(event.getEntityLiving().getType().getClassification()==EntityClassification.MONSTER)
		{
			synchronized(interdictionTiles)
			{
				Set<ISpawnInterdiction> dimSet = interdictionTiles.get(event.getEntity().world.getDimension().getType());
				if(dimSet!=null)
				{
					Iterator<ISpawnInterdiction> it = dimSet.iterator();
					while(it.hasNext())
					{
						ISpawnInterdiction interdictor = it.next();
						if(interdictor instanceof TileEntity)
						{
							if(((TileEntity)interdictor).isRemoved()||((TileEntity)interdictor).getWorld()==null)
								it.remove();
							else if(((TileEntity)interdictor).getDistanceSq(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ) <= interdictor.getInterdictionRangeSquared())
								event.setCanceled(true);
						}
						else if(interdictor instanceof Entity)
						{
							if(!((Entity)interdictor).isAlive()||((Entity)interdictor).world==null)
								it.remove();
							else if(((Entity)interdictor).getDistanceSq(event.getEntity()) <= interdictor.getInterdictionRangeSquared())
								event.setCanceled(true);
						}
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
		if(event.getResult()==Event.Result.ALLOW||event.getResult()==Event.Result.DENY
				||event.isSpawner())
			return;
		if(event.getEntityLiving().getType().getClassification()==EntityClassification.MONSTER)
		{
			synchronized(interdictionTiles)
			{
				DimensionType dimension = event.getEntity().world.getDimension().getType();
				if(interdictionTiles.containsKey(dimension))
				{
					Iterator<ISpawnInterdiction> it = interdictionTiles.get(dimension).iterator();
					while(it.hasNext())
					{
						ISpawnInterdiction interdictor = it.next();
						if(interdictor instanceof TileEntity)
						{
							if(((TileEntity)interdictor).isRemoved()||((TileEntity)interdictor).getWorld()==null)
								it.remove();
							else if(((TileEntity)interdictor).getDistanceSq(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ) <= interdictor.getInterdictionRangeSquared())
								event.setResult(Event.Result.DENY);
						}
						else if(interdictor instanceof Entity)
						{
							if(!((Entity)interdictor).isAlive()||((Entity)interdictor).world==null)
								it.remove();
							else if(((Entity)interdictor).getDistanceSq(event.getEntity()) <= interdictor.getInterdictionRangeSquared())
								event.setResult(Event.Result.DENY);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void digSpeedEvent(PlayerEvent.BreakSpeed event)
	{
		ItemStack current = event.getEntityPlayer().getHeldItem(Hand.MAIN_HAND);
		//Stop the combustion drill from working underwater
		if(!current.isEmpty()&&current.getItem()==Tools.drill&&event.getEntityPlayer().isInWater())
			if(((DrillItem)Tools.drill).getUpgrades(current).getBoolean("waterproof"))
				event.setNewSpeed(event.getOriginalSpeed()*5);
			else
				event.setCanceled(true);
		if(event.getState().getBlock()==MetalDevices.razorWire)
			if(current.getItem()!=Tools.wirecutter)
			{
				event.setCanceled(true);
				RazorWireTileEntity.applyDamage(event.getEntityLiving());
			}
		TileEntity te = event.getEntityPlayer().getEntityWorld().getTileEntity(event.getPos());
		if(te instanceof IEntityProof&&!((IEntityProof)te).canEntityDestroy(event.getEntityPlayer()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onAnvilChange(AnvilUpdateEvent event)
	{
		if(!event.getLeft().isEmpty()&&event.getLeft().getItem() instanceof IDrillHead&&((IDrillHead)event.getLeft().getItem()).getHeadDamage(event.getLeft()) > 0)
		{
			if(!event.getRight().isEmpty()&&event.getLeft().getItem().getIsRepairable(event.getLeft(), event.getRight()))
			{
				event.setOutput(event.getLeft().copy());
				int repair = Math.min(
						((IDrillHead)event.getOutput().getItem()).getHeadDamage(event.getOutput()),
						((IDrillHead)event.getOutput().getItem()).getMaximumHeadDamage(event.getOutput())/4);
				int cost = 0;
				for(; repair > 0&&cost < event.getRight().getCount(); ++cost)
				{
					((IDrillHead)event.getOutput().getItem()).damageHead(event.getOutput(), -repair);
					event.setCost(Math.max(1, repair/200));
					repair = Math.min(
							((IDrillHead)event.getOutput().getItem()).getHeadDamage(event.getOutput()),
							((IDrillHead)event.getOutput().getItem()).getMaximumHeadDamage(event.getOutput())/4);
				}
				event.setMaterialCost(cost);

				if(event.getName()==null||event.getName().isEmpty())
				{
					if(event.getLeft().hasDisplayName())
					{
						event.setCost(event.getCost()+5);
						event.getOutput().clearCustomName();
					}
				}
				else if(!event.getName().equals(event.getLeft().getDisplayName()))
				{
					event.setCost(event.getCost()+5);
					if(event.getLeft().hasDisplayName())
						event.setCost(event.getCost()+2);
					event.getOutput().setDisplayName(new StringTextComponent(event.getName()));
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void breakLast(BlockEvent.BreakEvent event)
	{
		if(event.getState().getBlock() instanceof IEMultiblockBlock)
		{
			TileEntity te = event.getWorld().getTileEntity(event.getPos());
			if(te instanceof MultiblockPartTileEntity)
				((MultiblockPartTileEntity)te).onlyLocalDissassembly = event.getWorld().getWorldInfo().getGameTime();
		}
	}
}
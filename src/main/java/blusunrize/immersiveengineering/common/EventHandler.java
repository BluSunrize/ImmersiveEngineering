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
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.IICProxy;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.BlockIEMultiblock;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRazorWire;
import blusunrize.immersiveengineering.common.crafting.MixerPotionHelper;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemIEShield;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
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
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.event.entity.minecart.MinecartUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.village.MerchantTradeOffersEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
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
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

public class EventHandler
{
	public static final ArrayList<ISpawnInterdiction> interdictionTiles = new ArrayList<ISpawnInterdiction>();
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
		if(IEConfig.blocksBreakWires)
			event.getWorld().addEventListener(ImmersiveNetHandler.INSTANCE.LISTENER);
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
	public void onCapabilitiesAttachEntity(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof EntityMinecart)
			event.addCapability(new ResourceLocation("immersiveengineering:shader"),
					new ShaderWrapper_Direct("immersiveengineering:minecart"));
		if(event.getObject() instanceof EntityPlayer)
			event.addCapability(new ResourceLocation(ImmersiveEngineering.MODID, "skyhook_data"),
					new SimpleSkyhookProvider());
	}

	@SubscribeEvent
	public void onCapabilitiesAttachItem(AttachCapabilitiesEvent<ItemStack> event)
	{
		if(event.getObject().getItem() instanceof ItemPotion)
		{
			IFluidHandlerItem potionHandler = new IFluidHandlerItem()
			{
				boolean isEmpty = false;
				FluidStack potion = null;

				void ensurePotionAvailable()
				{
					if(potion==null)
						//Lazy generation since NBT usually isn't wavailable when the event is fired
						potion = MixerPotionHelper.getFluidStackForType(PotionUtils.getPotionFromItem(event.getObject()), 250);
				}

				@Nonnull
				@Override
				public ItemStack getContainer()
				{
					if(isEmpty)
						return new ItemStack(Items.GLASS_BOTTLE);
					else
						return event.getObject();
				}

				@Override
				public IFluidTankProperties[] getTankProperties()
				{
					ensurePotionAvailable();
					if(isEmpty)
						return new IFluidTankProperties[0];
					else
						return new IFluidTankProperties[]{
								new FluidTankProperties(potion, 250, false, true)
						};
				}

				@Override
				public int fill(FluidStack resource, boolean doFill)
				{
					return 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain)
				{
					ensurePotionAvailable();
					if(isEmpty)
						return null;
					if(!resource.isFluidEqual(potion))
						return null;
					return drain(resource.amount, doDrain);
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain)
				{
					ensurePotionAvailable();
					if(maxDrain < 250)
						return null;
					if(doDrain)
						isEmpty = true;
					return new FluidStack(potion, 250);
				}
			};
			event.addCapability(new ResourceLocation(ImmersiveEngineering.MODID, "potions"), new ICapabilityProvider()
			{
				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
				{
					return capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
				}

				@Nullable
				@Override
				public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
				{
					if(capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
						return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(potionHandler);
					return null;
				}
			});
		}
	}

	@SubscribeEvent
	public void onFurnaceBurnTime(FurnaceFuelBurnTimeEvent event)
	{
		if(Utils.isFluidRelatedItemStack(event.getItemStack()))
		{
			FluidStack fs = FluidUtil.getFluidContained(event.getItemStack());
			if(fs!=null&&fs.getFluid()==IEContent.fluidCreosote)
				event.setBurnTime((int)(0.8*fs.amount));
		}
	}

	@SubscribeEvent
	public void onMinecartInteraction(MinecartInteractEvent event)
	{
		if(!event.getPlayer().world.isRemote&&!event.getItem().isEmpty()&&event.getItem().getItem() instanceof IShaderItem)
			if(event.getMinecart().hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
			{
				ShaderWrapper wrapper = event.getMinecart().getCapability(CapabilityShader.SHADER_CAPABILITY, null);
				if(wrapper!=null)
				{
					wrapper.setShaderItem(Utils.copyStackWithAmount(event.getItem(), 1));
					ImmersiveEngineering.packetHandler.sendTo(new MessageMinecartShaderSync(event.getMinecart(), wrapper), (EntityPlayerMP)event.getPlayer());
					event.setCanceled(true);
				}
			}
	}

	@SubscribeEvent
	public void onMinecartUpdate(MinecartUpdateEvent event)
	{
		if(event.getMinecart().ticksExisted%3==0&&event.getMinecart().hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
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
	}


	public static List<ResourceLocation> lootInjections = Arrays.asList(new ResourceLocation(ImmersiveEngineering.MODID, "chests/stronghold_library"), new ResourceLocation(ImmersiveEngineering.MODID, "chests/village_blacksmith"));
	static Field f_lootEntries;

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
								f_lootEntries = LootPool.class.getDeclaredField(ObfuscationReflectionHelper.remapFieldNames(LootPool.class.getName(), "field_186453_a")[0]);//field_186453_a is srg for lootEntries
								f_lootEntries.setAccessible(true);
							}
							if(f_lootEntries!=null)
							{
								List<LootEntry> entryList = (List<LootEntry>)f_lootEntries.get(injectPool);
								for(LootEntry entry : entryList)
									mainPool.addEntry(entry);
							}
						} catch(Exception e)
						{
							e.printStackTrace();
						}
				}
	}

	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if(event.getEntity().world.isRemote&&event.getEntity() instanceof EntityMinecart&&event.getEntity().hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.getEntity(), null));
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
		if(event.phase==TickEvent.Phase.START&&validateConnsNextTick&&FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			boolean validateConnections = IEConfig.validateConnections;
			int invalidConnectionsDropped = 0;
			for(int dim : ImmersiveNetHandler.INSTANCE.getRelevantDimensions())
			{
				if(!validateConnections)
				{
					continue;
				}
				World world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
				if(world==null)
				{
					ImmersiveNetHandler.INSTANCE.directConnections.remove(dim);
					continue;
				}
				for(Connection con : ImmersiveNetHandler.INSTANCE.getAllConnections(world))
				{
					if(!(world.getTileEntity(con.start) instanceof IImmersiveConnectable
							&&world.getTileEntity(con.end) instanceof IImmersiveConnectable))
					{
						ImmersiveNetHandler.INSTANCE.removeConnection(world, con);
						invalidConnectionsDropped++;
					}
				}
				IELogger.info("removed "+invalidConnectionsDropped+" invalid connections from world");
			}
			int invalidProxies = 0;
			Set<DimensionBlockPos> toRemove = new HashSet<>();
			for(Entry<DimensionBlockPos, IICProxy> e : ImmersiveNetHandler.INSTANCE.proxies.entrySet())
			{
				if(!validateConnections)
				{
					continue;
				}
				DimensionBlockPos p = e.getKey();
				World w = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(p.dimension);
				if(w!=null&&w.isBlockLoaded(p))
					toRemove.add(p);
				if(validateConnections&&w==null)
				{
					invalidProxies++;
					toRemove.add(p);
					continue;
				}
				if(validateConnections&&!(w.getTileEntity(p) instanceof IImmersiveConnectable))
				{
					invalidProxies++;
					toRemove.add(p);
				}
			}
			if(invalidProxies > 0)
				IELogger.info("Removed "+invalidProxies+" invalid connector proxies (used to transfer power through unloaded chunks)");
			validateConnsNextTick = false;
		}
		if(event.phase==TickEvent.Phase.END&&FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)
		{
			int dim = event.world.provider.getDimension();
			for(Entry<Connection, Integer> e : ImmersiveNetHandler.INSTANCE.getTransferedRates(dim).entrySet())
				if(e.getValue() > e.getKey().cableType.getTransferRate())
				{
					if(event.world instanceof WorldServer)
					{
						BlockPos start = e.getKey().start;
						for(Vec3d vec : e.getKey().getSubVertices(event.world))
							((WorldServer)event.world).spawnParticle(EnumParticleTypes.FLAME,
									vec.x+start.getX(), vec.y+start.getY(), vec.z+start.getZ(),
									0, 0, .02, 0, 1, new int[0]);
					}
					ImmersiveNetHandler.INSTANCE.removeConnection(event.world, e.getKey());
				}
			ImmersiveNetHandler.INSTANCE.getTransferedRates(dim).clear();

			if(!REMOVE_FROM_TICKING.isEmpty())
			{
				event.world.tickableTileEntities.removeAll(REMOVE_FROM_TICKING);
				REMOVE_FROM_TICKING.removeIf((te) -> te.getWorld().provider.getDimension()==dim);
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
				Pair<Integer, BlockPos> curr = requestedBlockUpdates.poll();
				World w = DimensionManager.getWorld(curr.getLeft());
				if(w!=null)
				{
					IBlockState state = w.getBlockState(curr.getRight());
					w.notifyBlockUpdate(curr.getRight(), state, state, 3);
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
			HashMap<MineralMix, Integer> packetMap = new HashMap<MineralMix, Integer>();
			for(Entry<MineralMix, Integer> e : ExcavatorHandler.mineralList.entrySet())
				if(e.getKey()!=null&&e.getValue()!=null)
					packetMap.put(e.getKey(), e.getValue());
			ImmersiveEngineering.packetHandler.sendToAll(new MessageMineralListSync(packetMap));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onLogout(PlayerLoggedOutEvent event)
	{
		ExcavatorHandler.allowPackets = false;
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

	static
	{
		listOfBoringBosses.add(EntityWither.class);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDropsLowest(LivingDropsEvent event)
	{
		if(!event.isCanceled()&&Lib.DMG_Crusher.equals(event.getSource().getDamageType()))
		{
			TileEntityCrusher crusher = crusherMap.get(event.getEntityLiving().getUniqueID());
			if(crusher!=null)
			{
				for(EntityItem item : event.getDrops())
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
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			ItemStack activeStack = player.getActiveItemStack();
			if(!activeStack.isEmpty()&&activeStack.getItem() instanceof ItemIEShield&&event.getAmount() >= 3&&Utils.canBlockDamageSource(player, event.getSource()))
			{
				float amount = event.getAmount();
				((ItemIEShield)activeStack.getItem()).hitShield(activeStack, player, event.getSource(), amount, event);
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
		if(!event.isCanceled()&&!event.getEntityLiving().isNonBoss()&&event.getAmount() >= event.getEntityLiving().getHealth()&&event.getSource().getTrueSource() instanceof EntityPlayer&&((EntityPlayer)event.getSource().getTrueSource()).getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemDrill)
			Utils.unlockIEAdvancement((EntityPlayer)event.getSource().getTrueSource(), "main/secret_drillbreak");
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
		if(event.getEntityLiving() instanceof EntityPlayer&&!event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty()&&ItemNBTHelper.hasKey(event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
				powerpack.getItem().onArmorTick(event.getEntityLiving().getEntityWorld(), (EntityPlayer)event.getEntityLiving(), powerpack);
		}
	}

	@SubscribeEvent
	public void onMerchantTrade(MerchantTradeOffersEvent event)
	{
		if(event.getMerchant() instanceof EntityVillager&&((EntityVillager)event.getMerchant()).getProfessionForge()==IEVillagerHandler.PROF_ENGINEER&&event.getList()!=null)
		{
			Iterator<MerchantRecipe> iterator = event.getList().iterator();
			while(iterator.hasNext())
			{
				MerchantRecipe recipe = iterator.next();
				ItemStack output = recipe.getItemToSell();
				if(output.getItem()==IEContent.itemMaterial&&ItemNBTHelper.hasKey(output, "generatePerks"))
				{
					EntityPlayer player = event.getPlayer();
					Random random = player.getRNG();
					ItemNBTHelper.remove(output, "generatePerks");
					NBTTagCompound perksTag = ItemRevolver.RevolverPerk.generatePerkSet(random, player.getLuck());
					ItemNBTHelper.setTagCompound(output, "perks", perksTag);
					int tier = Math.max(1, ItemRevolver.RevolverPerk.calculateTier(perksTag));
					recipe.getItemToBuy().setCount(5*tier+random.nextInt(5));
				}
				//Make recipe Unusable
				else if(output.getItem()==IEContent.itemMaterial&&ItemNBTHelper.hasKey(output, "perks")&&recipe.getToolUses() >= 1&&recipe.getMaxTradeUses() > 0)
					recipe.increaseMaxTradeUses(Integer.MIN_VALUE);
			}
		}
	}

	@SubscribeEvent
	public void onEnderTeleport(EnderTeleportEvent event)
	{
		if(event.getEntityLiving().isCreatureType(EnumCreatureType.MONSTER, false))
		{
			synchronized(interdictionTiles)
			{
				Iterator<ISpawnInterdiction> it = interdictionTiles.iterator();
				while(it.hasNext())
				{
					ISpawnInterdiction interdictor = it.next();
					if(interdictor instanceof TileEntity)
					{
						if(((TileEntity)interdictor).isInvalid()||((TileEntity)interdictor).getWorld()==null)
							it.remove();
						else if(((TileEntity)interdictor).getWorld().provider.getDimension()==event.getEntity().world.provider.getDimension()&&((TileEntity)interdictor).getDistanceSq(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ) <= interdictor.getInterdictionRangeSquared())
							event.setCanceled(true);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead||((Entity)interdictor).world==null)
							it.remove();
						else if(((Entity)interdictor).world.provider.getDimension()==event.getEntity().world.provider.getDimension()&&((Entity)interdictor).getDistanceSq(event.getEntity()) <= interdictor.getInterdictionRangeSquared())
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
		if(event.getResult()==Event.Result.ALLOW||event.getResult()==Event.Result.DENY
				||event.isSpawner())
			return;
		if(event.getEntityLiving().isCreatureType(EnumCreatureType.MONSTER, false))
		{
			synchronized(interdictionTiles)
			{
				Iterator<ISpawnInterdiction> it = interdictionTiles.iterator();
				while(it.hasNext())
				{
					ISpawnInterdiction interdictor = it.next();
					if(interdictor instanceof TileEntity)
					{
						if(((TileEntity)interdictor).isInvalid()||((TileEntity)interdictor).getWorld()==null)
							it.remove();
						else if(((TileEntity)interdictor).getWorld().provider.getDimension()==event.getEntity().world.provider.getDimension()&&((TileEntity)interdictor).getDistanceSq(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ) <= interdictor.getInterdictionRangeSquared())
							event.setResult(Event.Result.DENY);
					}
					else if(interdictor instanceof Entity)
					{
						if(((Entity)interdictor).isDead||((Entity)interdictor).world==null)
							it.remove();
						else if(((Entity)interdictor).world.provider.getDimension()==event.getEntity().world.provider.getDimension()&&((Entity)interdictor).getDistanceSq(event.getEntity()) <= interdictor.getInterdictionRangeSquared())
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
		if(!current.isEmpty()&&current.getItem().equals(IEContent.itemDrill)&&current.getItemDamage()==0&&event.getEntityPlayer().isInsideOfMaterial(Material.WATER))
			if(((ItemDrill)IEContent.itemDrill).getUpgrades(current).getBoolean("waterproof"))
				event.setNewSpeed(event.getOriginalSpeed()*5);
			else
				event.setCanceled(true);
		if(event.getState().getBlock()==IEContent.blockMetalDecoration2&&IEContent.blockMetalDecoration2.getMetaFromState(event.getState())==BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta())
			if(!OreDictionary.itemMatches(new ItemStack(IEContent.itemTool, 1, 1), current, false))
			{
				event.setCanceled(true);
				TileEntityRazorWire.applyDamage(event.getEntityLiving());
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
					event.getOutput().setStackDisplayName(event.getName());
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void breakLast(BlockEvent.BreakEvent event)
	{
		if(event.getState().getBlock() instanceof BlockIEMultiblock)
		{
			TileEntity te = event.getWorld().getTileEntity(event.getPos());
			if(te instanceof TileEntityMultiblockPart)
			{
				((TileEntityMultiblockPart)te).onlyLocalDissassembly = event.getWorld().getTotalWorldTime();
			}
		}
	}

	@SubscribeEvent
	public void remap(RegistryEvent.MissingMappings<?> ev)
	{
		NameRemapper.remap(ev);
	}
}
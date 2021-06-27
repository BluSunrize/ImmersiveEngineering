/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.RazorWireTileEntity;
import blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SimpleSkyhookProvider;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.ManualItem;
import blusunrize.immersiveengineering.common.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.wires.CapabilityInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.LecternBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class EventHandler
{
	public static HashSet<IEExplosion> currentExplosions = new HashSet<IEExplosion>();
	public static final Queue<Pair<RegistryKey<World>, BlockPos>> requestedBlockUpdates = new LinkedList<>();
	public static final Set<TileEntity> REMOVE_FROM_TICKING = new HashSet<>();
	public static final Queue<Runnable> SERVER_TASKS = new ArrayDeque<>();

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
					new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "minecart")));
		if(event.getObject() instanceof PlayerEntity)
			event.addCapability(new ResourceLocation(ImmersiveEngineering.MODID, "skyhook_data"),
					new SimpleSkyhookProvider());
	}

	@SubscribeEvent
	public void onCapabilitiesAttachWorld(AttachCapabilitiesEvent<World> event)
	{
		event.addCapability(new ResourceLocation(ImmersiveEngineering.MODID, "wire_network"),
				new CapabilityInit.Provider(event.getObject()));
	}

	@SubscribeEvent
	public void onMinecartInteraction(EntityInteractSpecific event)
	{
		PlayerEntity player = event.getPlayer();
		ItemStack stack = event.getItemStack();
		if(!(event.getTarget() instanceof AbstractMinecartEntity))
			return;
		AbstractMinecartEntity cart = (AbstractMinecartEntity)event.getTarget();
		if(!stack.isEmpty()&&stack.getItem() instanceof IShaderItem)
		{
			cart.getCapability(CapabilityShader.SHADER_CAPABILITY).ifPresent(wrapper ->
			{
				wrapper.setShaderItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
				if(!player.world.isRemote)
					ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player),
							new MessageMinecartShaderSync(cart, wrapper));
			});
			event.setCanceled(true);
			event.setCancellationResult(ActionResultType.SUCCESS);
		}
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


	@SubscribeEvent
	public void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if(event.getEntity().world.isRemote&&event.getEntity() instanceof AbstractMinecartEntity&&
				event.getEntity().getCapability(CapabilityShader.SHADER_CAPABILITY).isPresent())
			ImmersiveEngineering.packetHandler.sendToServer(new MessageMinecartShaderSync(event.getEntity(), null));
	}

	@SubscribeEvent
	public void onWorldTick(WorldTickEvent event)
	{
		if(event.phase==TickEvent.Phase.START&&!event.world.isRemote)
		{
			GlobalWireNetwork.getNetwork(event.world).update(event.world);

			if(!REMOVE_FROM_TICKING.isEmpty())
			{
				event.world.tickableTileEntities.removeAll(REMOVE_FROM_TICKING);
				REMOVE_FROM_TICKING.removeIf((te) -> te.getWorld()==event.world);
			}
			// Explicitly support tasks adding more tasks to be delayed
			int numToRun = SERVER_TASKS.size();
			for(int i = 0; i < numToRun; ++i)
			{
				Runnable next = SERVER_TASKS.poll();
				if(next!=null)
					next.run();
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
			Iterator<Pair<RegistryKey<World>, BlockPos>> it = requestedBlockUpdates.iterator();
			while(it.hasNext())
			{
				Pair<RegistryKey<World>, BlockPos> curr = it.next();
				if(curr.getLeft().equals(event.world.getDimensionKey()))
				{
					Chunk chunk = event.world.getChunkAt(curr.getRight());
					int sectionId = SectionPos.from(curr.getRight()).getSectionY();
					ChunkSection[] sections = chunk.getSections();
					if(sectionId >= 0&&sectionId < sections.length&&sections[sectionId]!=null)
					{
						BlockState state = event.world.getBlockState(curr.getRight());
						event.world.notifyBlockUpdate(curr.getRight(), state, state, 3);
					}
					it.remove();
				}
			}
		}
	}

	public static HashMap<UUID, CrusherTileEntity> crusherMap = new HashMap<>();
	public static HashSet<Class<? extends MobEntity>> listOfBoringBosses = new HashSet<>();

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
		if(!event.isCanceled()&&!event.getEntityLiving().canChangeDimension())
		{
			Rarity r = Rarity.EPIC;
			for(Class<? extends MobEntity> boring : listOfBoringBosses)
				if(boring.isAssignableFrom(event.getEntityLiving().getClass()))
					return;
			ItemStack bag = new ItemStack(Misc.shaderBag.get(r));
			event.getDrops().add(new ItemEntity(event.getEntityLiving().world, event.getEntityLiving().getPosX(), event.getEntityLiving().getPosY(), event.getEntityLiving().getPosZ(), bag));
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
		if(event.getSource().isFireDamage()&&event.getEntityLiving().getActivePotionEffect(IEPotions.flammable.get())!=null)
		{
			int amp = event.getEntityLiving().getActivePotionEffect(IEPotions.flammable.get()).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(("flux".equals(event.getSource().getDamageType())||IEDamageSources.razorShock.equals(event.getSource())||
				event.getSource() instanceof ElectricDamageSource)&&event.getEntityLiving().getActivePotionEffect(IEPotions.conductive.get())!=null)
		{
			int amp = event.getEntityLiving().getActivePotionEffect(IEPotions.conductive.get()).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(!event.isCanceled()&&!event.getEntityLiving().canChangeDimension()&&event.getAmount() >= event.getEntityLiving().getHealth()&&event.getSource().getTrueSource() instanceof PlayerEntity&&((PlayerEntity)event.getSource().getTrueSource()).getHeldItem(Hand.MAIN_HAND).getItem() instanceof DrillItem)
			Utils.unlockIEAdvancement((PlayerEntity)event.getSource().getTrueSource(), "main/secret_drillbreak");
	}

	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event)
	{
		Vector3d motion = event.getEntity().getMotion();
		if(event.getEntityLiving().getActivePotionEffect(IEPotions.sticky.get())!=null)
			motion = motion.subtract(0, (event.getEntityLiving().getActivePotionEffect(IEPotions.sticky.get()).getAmplifier()+1)*0.3F, 0);
		else if(event.getEntityLiving().getActivePotionEffect(IEPotions.concreteFeet.get())!=null)
			motion = Vector3d.ZERO;
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

	@SubscribeEvent()
	public void digSpeedEvent(PlayerEvent.BreakSpeed event)
	{
		ItemStack current = event.getPlayer().getHeldItem(Hand.MAIN_HAND);
		//Stop the combustion drill from working underwater
		if(!current.isEmpty()&&current.getItem()==Tools.drill.get()&&event.getPlayer().areEyesInFluid(FluidTags.WATER))
			if(Tools.drill.get().getUpgrades(current).getBoolean("waterproof"))
				event.setNewSpeed(event.getOriginalSpeed()*5);
			else
				event.setCanceled(true);
		if(event.getState().getBlock()==MetalDevices.razorWire.get())
			if(current.getItem()!=Tools.wirecutter.get())
			{
				event.setCanceled(true);
				RazorWireTileEntity.applyDamage(event.getEntityLiving());
			}
		if(event.getPos()!=null) // Avoid a potential NPE for invalid positions passed
		{
			TileEntity te = event.getPlayer().getEntityWorld().getTileEntity(event.getPos());
			if(te instanceof IEntityProof&&!((IEntityProof)te).canEntityDestroy(event.getPlayer()))
				event.setCanceled(true);
		}
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
				else if(!event.getName().equals(event.getLeft().getDisplayName().getString()))
				{
					event.setCost(event.getCost()+5);
					if(event.getLeft().hasDisplayName())
						event.setCost(event.getCost()+2);
					event.getOutput().setDisplayName(new StringTextComponent(event.getName()));
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockRightclick(RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		BlockState state = event.getWorld().getBlockState(pos);
		if(!(state.getBlock() instanceof LecternBlock)||event.getPlayer()==null)
			return;
		TileEntity tile = event.getWorld().getTileEntity(pos);
		if(tile instanceof LecternTileEntity&&((LecternTileEntity)tile).getBook().getItem() instanceof ManualItem)
		{
			if(!event.getPlayer().isSneaking())
			{
				ImmersiveEngineering.proxy.openManual();
				event.setCanceled(true);
			}
			else if(!event.getWorld().isRemote)
			{
				Direction direction = state.get(LecternBlock.FACING);
				ItemStack itemstack = ((LecternTileEntity)tile).getBook().copy();
				float f = 0.25F*(float)direction.getXOffset();
				float f1 = 0.25F*(float)direction.getZOffset();
				ItemEntity itementity = new ItemEntity(event.getWorld(), pos.getX()+0.5D+f, pos.getY()+1, pos.getZ()+0.5D+f1, itemstack);
				itementity.setDefaultPickupDelay();
				event.getWorld().addEntity(itementity);
				((LecternTileEntity)tile).clear();
				LecternBlock.setHasBook(event.getWorld(), pos, state, false);
				event.setCanceled(true);
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
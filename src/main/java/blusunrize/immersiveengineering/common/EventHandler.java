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
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.RazorWireBlockEntity;
import blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SimpleSkyhookProvider;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.ManualItem;
import blusunrize.immersiveengineering.common.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.register.IEStats;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.wires.GlobalNetProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class EventHandler
{
	public static HashSet<IEExplosion> currentExplosions = new HashSet<IEExplosion>();
	public static final Queue<Runnable> SERVER_TASKS = new ArrayDeque<>();

	@SubscribeEvent
	public void onLoad(LevelEvent.Load event)
	{
		ImmersiveEngineering.proxy.onWorldLoad();
	}

	@SubscribeEvent
	public void onCapabilitiesAttachEntity(AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof AbstractMinecart)
			event.addCapability(new ResourceLocation("immersiveengineering:shader"),
					new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "minecart")));
		if(event.getObject() instanceof Player)
			event.addCapability(new ResourceLocation(ImmersiveEngineering.MODID, "skyhook_data"),
					new SimpleSkyhookProvider());
	}

	@SubscribeEvent
	public void onCapabilitiesAttachWorld(AttachCapabilitiesEvent<Level> event)
	{
		event.addCapability(ImmersiveEngineering.rl("wire_network"), new GlobalNetProvider(event.getObject()));
	}

	@SubscribeEvent
	public void onCapabilitiesAttachBlockEntity(AttachCapabilitiesEvent<BlockEntity> event)
	{
		if(event.getObject() instanceof FurnaceBlockEntity furnace)
			event.addCapability(ImmersiveEngineering.rl("vanilla_furnace_heater"), new SimpleCapProvider<>(
					() -> ExternalHeaterHandler.CAPABILITY, new VanillaFurnaceHeater(furnace)
			));
	}

	@SubscribeEvent
	public void onMinecartInteraction(EntityInteractSpecific event)
	{
		Player player = event.getEntity();
		ItemStack stack = event.getItemStack();
		if(!(event.getTarget() instanceof AbstractMinecart cart))
			return;
		if(stack.getItem() instanceof IShaderItem)
		{
			cart.getCapability(CapabilityShader.SHADER_CAPABILITY).ifPresent(wrapper ->
			{
				wrapper.setShaderItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
				if(!player.level.isClientSide)
					ImmersiveEngineering.packetHandler.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
							new MessageMinecartShaderSync(cart, wrapper));
			});
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	@SubscribeEvent
	public void onItemPickup(ItemPickupEvent event)
	{
		Player player = event.getEntity();
		ItemStack stack = event.getStack();
		if(!stack.isEmpty()&&stack.getItem() instanceof IShaderItem)
		{
			ResourceLocation shader = ((IShaderItem)stack.getItem()).getShaderName(stack);
			ShaderRegistry.markShaderReceived(player.getUUID(), shader);
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
						shader.getMiddle().getEffectFunction().execute(event.getMinecart().world, shader.getFirst(), null, shader.getSecond().getShaderType(), prevPosVec.add(0, .25, 0).add(movVec), movVec.scale(1.5f), .25f);
				}
			}
		}
	}*/


	@SubscribeEvent
	public void onWorldTick(LevelTickEvent event)
	{
		if(event.phase==TickEvent.Phase.START&&!event.level.isClientSide)
		{
			GlobalWireNetwork.getNetwork(event.level).update(event.level);

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
		}
	}

	public static Map<UUID, CrusherBlockEntity> crusherMap = new HashMap<>();
	public static Set<Class<? extends Mob>> listOfBoringBosses = new HashSet<>();

	static
	{
		listOfBoringBosses.add(WitherBoss.class);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDropsLowest(LivingDropsEvent event)
	{
		if(!event.isCanceled()&&Lib.DMG_Crusher.equals(event.getSource().getMsgId()))
		{
			CrusherBlockEntity crusher = crusherMap.get(event.getEntity().getUUID());
			if(crusher!=null)
			{
				for(ItemEntity item : event.getDrops())
					if(item!=null&&!item.getItem().isEmpty())
						crusher.doProcessOutput(item.getItem());
				crusherMap.remove(event.getEntity().getUUID());
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled()&&!event.getEntity().canChangeDimensions())
		{
			Rarity r = Rarity.EPIC;
			for(Class<? extends Mob> boring : listOfBoringBosses)
				if(boring.isAssignableFrom(event.getEntity().getClass()))
					return;
			ItemStack bag = new ItemStack(Misc.SHADER_BAG.get(r));
			event.getDrops().add(new ItemEntity(event.getEntity().level, event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), bag));
		}
	}

	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player player = (Player)event.getEntity();
			ItemStack activeStack = player.getUseItem();
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
		if(event.getSource().isFire()&&event.getEntity().getEffect(IEPotions.FLAMMABLE.get())!=null)
		{
			int amp = event.getEntity().getEffect(IEPotions.FLAMMABLE.get()).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(("flux".equals(event.getSource().getMsgId())||IEDamageSources.razorShock.equals(event.getSource())||
				event.getSource() instanceof ElectricDamageSource)&&event.getEntity().getEffect(IEPotions.CONDUCTIVE.get())!=null)
		{
			int amp = event.getEntity().getEffect(IEPotions.CONDUCTIVE.get()).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(!event.isCanceled()&&!event.getEntity().canChangeDimensions()&&event.getAmount() >= event.getEntity().getHealth()&&event.getSource().getEntity() instanceof Player&&((Player)event.getSource().getEntity()).getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof DrillItem)
			Utils.unlockIEAdvancement((Player)event.getSource().getEntity(), "main/secret_drillbreak");
	}

	@SubscribeEvent
	public void onLivingJump(LivingJumpEvent event)
	{
		Vec3 motion = event.getEntity().getDeltaMovement();
		if(event.getEntity().getEffect(IEPotions.STICKY.get())!=null)
			motion = motion.subtract(0, (event.getEntity().getEffect(IEPotions.STICKY.get()).getAmplifier()+1)*0.2F, 0);
		else if(event.getEntity().getEffect(IEPotions.CONCRETE_FEET.get())!=null)
			motion = Vec3.ZERO;
		event.getEntity().setDeltaMovement(motion);
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingTickEvent event)
	{
		if(event.getEntity() instanceof Player&&!event.getEntity().getItemBySlot(EquipmentSlot.CHEST).isEmpty()&&ItemNBTHelper.hasKey(event.getEntity().getItemBySlot(EquipmentSlot.CHEST), Lib.NBT_Powerpack))
		{
			ItemStack powerpack = ItemNBTHelper.getItemStack(event.getEntity().getItemBySlot(EquipmentSlot.CHEST), Lib.NBT_Powerpack);
			if(!powerpack.isEmpty())
				powerpack.getItem().onArmorTick(powerpack, event.getEntity().getCommandSenderWorld(), (Player)event.getEntity());
		}
	}

	@SubscribeEvent()
	public void digSpeedEvent(PlayerEvent.BreakSpeed event)
	{
		ItemStack current = event.getEntity().getItemInHand(InteractionHand.MAIN_HAND);
		//Stop the combustion drill from working underwater
		if(!current.isEmpty()&&current.getItem()==Tools.DRILL.get()&&event.getEntity().isEyeInFluid(FluidTags.WATER))
			if(Tools.DRILL.get().getUpgrades(current).getBoolean("waterproof"))
				event.setNewSpeed(event.getOriginalSpeed()*5);
			else
				event.setCanceled(true);
		if(event.getState().getBlock()==MetalDevices.RAZOR_WIRE.get())
			if(current.getItem()!=Tools.WIRECUTTER.get())
			{
				event.setCanceled(true);
				RazorWireBlockEntity.applyDamage(event.getEntity());
			}
		if(event.getPos()!=null) // Avoid a potential NPE for invalid positions passed
		{
			BlockEntity te = event.getEntity().getCommandSenderWorld().getBlockEntity(event.getPos());
			if(te instanceof IEntityProof&&!((IEntityProof)te).canEntityDestroy(event.getEntity()))
				event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onAnvilChange(AnvilUpdateEvent event)
	{
		if(!event.getLeft().isEmpty()&&event.getLeft().getItem() instanceof IDrillHead&&((IDrillHead)event.getLeft().getItem()).getHeadDamage(event.getLeft()) > 0)
		{
			if(!event.getRight().isEmpty()&&event.getLeft().getItem().isValidRepairItem(event.getLeft(), event.getRight()))
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
					if(event.getLeft().hasCustomHoverName())
					{
						event.setCost(event.getCost()+5);
						event.getOutput().resetHoverName();
					}
				}
				else if(!event.getName().equals(event.getLeft().getHoverName().getString()))
				{
					event.setCost(event.getCost()+5);
					if(event.getLeft().hasCustomHoverName())
						event.setCost(event.getCost()+2);
					event.getOutput().setHoverName(Component.literal(event.getName()));
				}
			}
		}
	}

	@SubscribeEvent
	public void onBlockRightclick(RightClickBlock event)
	{
		BlockPos pos = event.getPos();
		BlockState state = event.getLevel().getBlockState(pos);
		if(!(state.getBlock() instanceof LecternBlock)||event.getEntity()==null)
			return;
		BlockEntity tile = event.getLevel().getBlockEntity(pos);
		if(tile instanceof LecternBlockEntity&&((LecternBlockEntity)tile).getBook().getItem() instanceof ManualItem)
		{
			if(!event.getEntity().isShiftKeyDown())
			{
				if(event.getLevel().isClientSide)
					ImmersiveEngineering.proxy.openManual();
				event.setCanceled(true);
			}
			else if(!event.getLevel().isClientSide)
			{
				Direction direction = state.getValue(LecternBlock.FACING);
				ItemStack itemstack = ((LecternBlockEntity)tile).getBook().copy();
				float f = 0.25F*(float)direction.getStepX();
				float f1 = 0.25F*(float)direction.getStepZ();
				ItemEntity itementity = new ItemEntity(event.getLevel(), pos.getX()+0.5D+f, pos.getY()+1, pos.getZ()+0.5D+f1, itemstack);
				itementity.setDefaultPickUpDelay();
				event.getLevel().addFreshEntity(itementity);
				((LecternBlockEntity)tile).clearContent();
				LecternBlock.resetBookState(event.getLevel(), pos, state, false);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void breakLast(BlockEvent.BreakEvent event)
	{
		if(event.getState().getBlock() instanceof IEMultiblockBlock)
		{
			BlockEntity te = event.getLevel().getBlockEntity(event.getPos());
			if(te instanceof MultiblockPartBlockEntity<?> multiblockBE)
				multiblockBE.onlyLocalDissassembly = event.getLevel().getLevelData().getGameTime();
		}
	}

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(!(event.getSource() instanceof ElectricDamageSource))
			return;
		if(!(event.getEntity() instanceof ServerPlayer serverPlayer))
			return;
		serverPlayer.awardStat(IEStats.WIRE_DEATHS.get());
	}
}
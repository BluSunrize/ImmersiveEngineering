/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.Lib.DamageTypes;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.metal.RazorWireBlockEntity;
import blusunrize.immersiveengineering.common.entities.CapabilitySkyhookData.SimpleSkyhookProvider;
import blusunrize.immersiveengineering.common.entities.illager.EngineerIllager;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.ManualItem;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import blusunrize.immersiveengineering.common.network.MessageMinecartShaderSync;
import blusunrize.immersiveengineering.common.network.MessageOpenManual;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.register.IEStats;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.wires.GlobalNetProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
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
import net.minecraftforge.common.Tags.EntityTypes;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EventHandler
{
	public static Map<Level, Set<IEExplosion>> currentExplosions = new WeakHashMap<>();
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
		ItemStack stack = event.getItemStack();
		if(!(event.getTarget() instanceof AbstractMinecart cart))
			return;
		if(stack.getItem() instanceof IShaderItem)
		{
			final ShaderWrapper wrapper = CapabilityUtils.getCapability(cart, CapabilityShader.SHADER_CAPABILITY);
			if(wrapper!=null&&!event.getLevel().isClientSide)
			{
				wrapper.setShaderItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
				ImmersiveEngineering.packetHandler.send(
						PacketDistributor.TRACKING_ENTITY.with(() -> cart),
						new MessageMinecartShaderSync(cart, wrapper)
				);
			}
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
		if(event.level.isClientSide||event.phase!=TickEvent.Phase.START)
			return;
		GlobalWireNetwork.getNetwork(event.level).update(event.level);

		// Explicitly support tasks adding more tasks to be delayed
		int numToRun = SERVER_TASKS.size();
		for(int i = 0; i < numToRun; ++i)
		{
			Runnable next = SERVER_TASKS.poll();
			if(next!=null)
				next.run();
		}

		final Set<IEExplosion> explosionsInLevel = currentExplosions.get(event.level);
		if(explosionsInLevel!=null)
		{
			Iterator<IEExplosion> itExplosion = explosionsInLevel.iterator();
			while(itExplosion.hasNext())
			{
				IEExplosion ex = itExplosion.next();
				ex.doExplosionTick();
				if(ex.isExplosionFinished)
					itExplosion.remove();
			}
		}
	}

	public static Map<UUID, Consumer<ItemStack>> crusherMap = new HashMap<>();

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingDropsLowest(LivingDropsEvent event)
	{
		if(!event.isCanceled()&&event.getSource().is(DamageTypes.CRUSHER))
		{
			final Consumer<ItemStack> crusher = crusherMap.get(event.getEntity().getUUID());
			if(crusher!=null)
			{
				for(ItemEntity item : event.getDrops())
					if(item!=null&&!item.getItem().isEmpty())
						crusher.accept(item.getItem());
				crusherMap.remove(event.getEntity().getUUID());
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onLivingDrops(LivingDropsEvent event)
	{
		if(!event.isCanceled())
		{
			boolean isBoss = event.getEntity().getType().is(EntityTypes.BOSSES);
			if(!isBoss||event.getEntity().getType().is(IETags.shaderbagBlacklist))
				return;
			ItemStack bag = new ItemStack(Misc.SHADER_BAG.get(Rarity.EPIC));
			event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), bag));
		}
	}

	@SubscribeEvent
	public void onLivingAttacked(LivingAttackEvent event)
	{
		if(event.getEntity() instanceof Player player)
		{
			ItemStack activeStack = player.getUseItem();
			if(!activeStack.isEmpty()&&activeStack.getItem() instanceof IEShieldItem&&event.getAmount() >= 3&&Utils.canBlockDamageSource(player, event.getSource()))
			{
				float amount = event.getAmount();
				((IEShieldItem)activeStack.getItem()).hitShield(activeStack, player, event.getSource(), amount, event);
			}

			RandomSource rng = player.getRandom();
			ItemStack powerpack = PowerpackItem.POWERPACK_GETTER.getFrom(player);
			// 33% chance of zapping the attacker, provided they are living and within ~2 blocks
			if(!powerpack.isEmpty()&&PowerpackItem.getUpgradesStatic(powerpack).getBoolean("tesla"))
				if(event.getSource().getEntity() instanceof LivingEntity attacker&&attacker.distanceToSqr(player) < 4)
					if(EnergyHelper.extractFlux(powerpack, PowerpackItem.TESLA_CONSUMPTION, true)==PowerpackItem.TESLA_CONSUMPTION)
					{
						EnergyHelper.extractFlux(powerpack, PowerpackItem.TESLA_CONSUMPTION, false);
						ElectricDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(player.level(), 2+player.getRandom().nextInt(4), true);
						if(dmgsrc.apply(attacker))
							attacker.addEffect(new MobEffectInstance(IEPotions.STUNNED.get(), 60));
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.spark.get(),
								SoundSource.BLOCKS, 2.5F, 0.5F+rng.nextFloat());
					}
		}
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingHurtEvent event)
	{
		if(event.getSource().is(DamageTypeTags.IS_FIRE)&&event.getEntity().getEffect(IEPotions.FLAMMABLE.get())!=null)
		{
			int amp = event.getEntity().getEffect(IEPotions.FLAMMABLE.get()).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(("flux".equals(event.getSource().getMsgId())||event.getSource().is(DamageTypes.RAZOR_SHOCK)||
				event.getSource() instanceof ElectricDamageSource)&&event.getEntity().getEffect(IEPotions.CONDUCTIVE.get())!=null)
		{
			int amp = event.getEntity().getEffect(IEPotions.CONDUCTIVE.get()).getAmplifier();
			float mod = 1.5f+((amp*amp)*.5f);
			event.setAmount(event.getAmount()*mod);
		}
		if(!event.isCanceled()&&!event.getEntity().canChangeDimensions()&&event.getAmount() >= event.getEntity().getHealth()&&event.getSource().getEntity() instanceof Player&&((Player)event.getSource().getEntity()).getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof DrillItem)
			Utils.unlockIEAdvancement((Player)event.getSource().getEntity(), "tools/secret_drillbreak");
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
		// Certain blocks require a wirecutter to break or else they hurt
		if(event.getState().is(IETags.wirecutterHarvestable)&&!current.canPerformAction(Lib.WIRECUTTER_DIG))
		{
			event.setCanceled(true);
			if(event.getEntity().getRandom().nextInt(4)==0)
				RazorWireBlockEntity.applyDamage(event.getEntity());
		}
		if(event.getPosition().isPresent())
		{
			BlockEntity te = event.getEntity().getCommandSenderWorld().getBlockEntity(event.getPosition().get());
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
		if(event.getLevel().isClientSide)
			return;
		BlockPos pos = event.getPos();
		BlockState state = event.getLevel().getBlockState(pos);
		if(!(state.getBlock() instanceof LecternBlock)||event.getEntity()==null)
			return;
		BlockEntity tile = event.getLevel().getBlockEntity(pos);
		if(tile instanceof LecternBlockEntity lectern&&lectern.getBook().getItem() instanceof ManualItem)
		{
			if(!event.getEntity().isShiftKeyDown())
			{
				if(event.getEntity() instanceof ServerPlayer serverPlayer)
					ImmersiveEngineering.packetHandler.send(
							PacketDistributor.PLAYER.with(() -> serverPlayer), new MessageOpenManual()
					);
			}
			else
			{
				Direction direction = state.getValue(LecternBlock.FACING);
				ItemStack itemstack = ((LecternBlockEntity)tile).getBook().copy();
				float f = 0.25F*(float)direction.getStepX();
				float f1 = 0.25F*(float)direction.getStepZ();
				ItemEntity itementity = new ItemEntity(event.getLevel(), pos.getX()+0.5D+f, pos.getY()+1, pos.getZ()+0.5D+f1, itemstack);
				itementity.setDefaultPickUpDelay();
				event.getLevel().addFreshEntity(itementity);
				lectern.clearContent();
				LecternBlock.resetBookState(null, event.getLevel(), pos, state, false);
			}
			event.setCanceled(true);
		}
	}

	// TODO test if multiblocks are still bad when broken with a drill or similar
	/*@SubscribeEvent(priority = EventPriority.LOWEST)
	public void breakLast(BlockEvent.BreakEvent event)
	{
		if(event.getState().getBlock() instanceof IEMultiblockBlock)
		{
			BlockEntity te = event.getLevel().getBlockEntity(event.getPos());
			if(te instanceof MultiblockPartBlockEntity<?> multiblockBE)
				multiblockBE.onlyLocalDissassembly = event.getLevel().getLevelData().getGameTime();
		}
	}*/

	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		if(!(event.getSource() instanceof ElectricDamageSource))
			return;
		if(!(event.getEntity() instanceof ServerPlayer serverPlayer))
			return;
		serverPlayer.awardStat(IEStats.WIRE_DEATHS.get());
		if(serverPlayer.getAbilities().flying||serverPlayer.isFallFlying())
			Utils.unlockIEAdvancement(serverPlayer, "main/secret_friedbird");
	}

	private static final List<IllagerUpgrade> ILLAGER_UPGRADES = List.of(
			new IllagerUpgrade(EntityType.PILLAGER, IEEntityTypes.FUSILIER.get(), .5f),
			new IllagerUpgrade(EntityType.VINDICATOR, IEEntityTypes.BULWARK.get(), .1f),
			new IllagerUpgrade(EntityType.VINDICATOR, IEEntityTypes.COMMANDO.get(), .33f),
			new IllagerUpgrade(EntityType.RAVAGER, IEEntityTypes.BULWARK.get(), .2f),
			new IllagerUpgrade(EntityType.EVOKER, IEEntityTypes.COMMANDO.get(), .33f)
	);

	@SubscribeEvent
	public void onMobSpawn(EntityJoinLevelEvent event)
	{
		if(event.isCanceled()||event.loadedFromDisk())
			return;
		if(event.getEntity() instanceof Raider raider&&raider.hasActiveRaid()&&event.getLevel() instanceof ServerLevel level)
		{
			// can't upgrade our own Illagers
			if(raider instanceof EngineerIllager)
				return;
			Raid raid = raider.getCurrentRaid();
			// check if there are any players in the raid with the advancement
			if(level.players().stream().anyMatch(canTriggerEngineerRaid.apply(raid)))
				for(IllagerUpgrade upgrade : ILLAGER_UPGRADES)
					if(upgrade.shouldUpgrade(raider))
					{
						// data to keep
						int wave = raider.getWave();
						BlockPos pos = raider.blockPosition();
						// configure new Illager
						Raider replacement = upgrade.replacement.create(level);
						if(raider.isPatrolLeader()&&replacement.canBeLeader())
						{
							replacement.setPatrolLeader(true);
							raid.setLeader(wave, replacement);
						}
						raid.joinRaid(wave, replacement, pos, false);
						// prevent original spawn
						raid.removeFromRaid(raider, true);
						event.setCanceled(true);
					}
		}
	}

	private static final Function<Raid, Predicate<ServerPlayer>> canTriggerEngineerRaid = raid -> serverPlayer -> {
		ServerLevel level = serverPlayer.serverLevel();
		ServerAdvancementManager manager = level.getServer().getAdvancements();
		Advancement advancement = manager.getAdvancement(new ResourceLocation(ImmersiveEngineering.MODID, "main/kill_illager"));
		return level.getRaidAt(serverPlayer.blockPosition())==raid&&advancement!=null&&serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
	};

	record IllagerUpgrade(EntityType<? extends Raider> type, EntityType<? extends Raider> replacement, float chance)
	{
		boolean shouldUpgrade(Raider target)
		{
			return this.type==target.getType()&&target.getRandom().nextFloat() <= this.chance;
		}
	}
}
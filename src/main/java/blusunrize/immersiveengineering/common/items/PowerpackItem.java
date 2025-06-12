/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.WrappingEnergyStorage;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireCollisionData;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler.IShockingWire;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.network.MessagePowerpackAntenna;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemGetterList;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.util.EnergyHelper.insertFlux;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class PowerpackItem extends UpgradeableToolItem
{
	public static final String TYPE = "POWERPACK";
	public static final int CHEST_SLOT = Inventory.INVENTORY_SIZE+EquipmentSlot.CHEST.getIndex();
	private static final Supplier<Map<Item, CapacitorConfig>> capacitorConfigMap = Suppliers.memoize(() -> {
		Map<Item, CapacitorConfig> capacitorConfigMap = new HashMap<>();
		capacitorConfigMap.put(MetalDevices.CAPACITOR_LV.asItem(), IEServerConfig.MACHINES.lvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_MV.asItem(), IEServerConfig.MACHINES.mvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_HV.asItem(), IEServerConfig.MACHINES.hvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_CREATIVE.asItem(), IEServerConfig.Machines.CapacitorConfig.CREATIVE);
		return capacitorConfigMap;
	});

	private static final Map<UUID, Connection> PLAYER_ATTACHED_TO = new HashMap<>();

	public static final ItemGetterList POWERPACK_GETTER = new ItemGetterList(player -> {
		ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
		if(chest.getItem() instanceof PowerpackItem)
			return chest;
		var attachedPack = chest.get(IEDataComponents.CONTAINED_POWERPACK);
		if(attachedPack!=null)
			return attachedPack.attached();
		else
			return ItemStack.EMPTY;
	});

	public static final int ITEM_CHARGE_RATE = 256;
	public static final int INDUCTION_CHARGE_RATE = ITEM_CHARGE_RATE/32;
	public static final int ANTENNA_CHARGE_RATE = 32;
	public static final int TESLA_CONSUMPTION = 1024;
	public static final int MAGNET_CONSUMPTION = 8;

	public PowerpackItem()
	{
		super(new Properties().stacksTo(1), TYPE, 4);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		IEnergyStorage energy = stack.getCapability(EnergyStorage.ITEM);
		if(energy!=null)
		{
			String stored = energy.getEnergyStored()+"/"+getMaxEnergyStored(stack);
			list.add(Component.translatable(Lib.DESC+"info.energyStored", stored).withStyle(ChatFormatting.GRAY));
		}
	}

	@Nullable
	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack)
	{
		return EquipmentSlot.CHEST;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack heldItem = player.getItemInHand(hand);
		EquipmentSlot slot = player.getEquipmentSlotForItem(heldItem);
		if(!player.getItemBySlot(slot).isEmpty())
			return InteractionResultHolder.fail(heldItem);
		player.setItemSlot(slot, heldItem.copy());
		if(!world.isClientSide())
			player.awardStat(Stats.ITEM_USED.get(this));
		heldItem.setCount(0);
		return InteractionResultHolder.sidedSuccess(heldItem, world.isClientSide());
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
	{
		// We'll just have to assume that's Curios which sets the slot of -1
		if(entity instanceof Player player&&(itemSlot==CHEST_SLOT||itemSlot==-1))
			tickWornPack(stack, world, player);
	}

	public static void tickWornPack(ItemStack itemStack, Level world, Player player)
	{
		IEnergyStorage packEnergy = itemStack.getCapability(EnergyStorage.ITEM);
		if(packEnergy==null)
			return;
		var upgrades = getUpgradesStatic(itemStack);
		int energy = packEnergy.getEnergyStored();
		if(energy > 0)
		{
			final int pre = energy;
			for(EquipmentSlot slot : EquipmentSlot.values())
				energy -= insertInto(player.getItemBySlot(slot), energy);
			// induction charging only happens every 4 ticks
			if(upgrades.has(UpgradeEffect.INDUCTION)&&player.tickCount%4==0)
			{
				NonNullList<ItemStack> allItems = player.getInventory().items;
				final int selected = player.getInventory().selected;
				for(int i = 0; i < allItems.size(); i++)
				{
					if(i==selected) // ignore equipped item
						continue;
					energy -= insertInto(allItems.get(i), energy);
					if(energy <= 0) // this is a long loop, so breaking early is good
						break;
				}
			}
			if(pre!=energy)
				packEnergy.extractEnergy(pre-energy, false);
		}
		if(upgrades.has(UpgradeEffect.ANTENNA))
			handleAntennaTick(itemStack, world, player);
		if(upgrades.has(UpgradeEffect.MAGNET)&&energy >= MAGNET_CONSUMPTION)
			handleMagnetTick(itemStack, world, player);
	}

	private static int insertInto(ItemStack insertInto, int maxAmount)
	{
		IEnergyStorage equippedEnergy = insertInto.getCapability(Capabilities.EnergyStorage.ITEM);
		Item insertItem = insertInto.getItem();
		if(equippedEnergy!=null&&!(insertItem instanceof PowerpackItem)&&!(insertItem instanceof BlockItem))
			return equippedEnergy.receiveEnergy(Math.min(maxAmount, ITEM_CHARGE_RATE), false);
		else
			return 0;
	}

	private static void handleAntennaTick(ItemStack itemStack, Level world, Player player)
	{
		// attachment only works when grounded
		boolean grounded = player.getRootVehicle().onGround();
		if(!grounded&&player.getRootVehicle() instanceof AbstractMinecart minecart)
		{
			BlockPos railPos = minecart.getCurrentRailPosition();
			if(world.getBlockState(railPos).is(BlockTags.RAILS))
				grounded = true;
		}
		if(!grounded||world.isClientSide())
			return;

		GlobalWireNetwork global = GlobalWireNetwork.getNetwork(world);
		tooFar:
		if(PLAYER_ATTACHED_TO.containsKey(player.getUUID()))
		{
			Connection conn = PLAYER_ATTACHED_TO.get(player.getUUID());
			// Check for staying connected
			CatenaryData catenary = conn.getCatenaryData();
			Vec3 antennaTip = player.position().add(0, 2.5, 0);
			Vec3 ap = antennaTip.subtract(Vec3.atLowerCornerOf(conn.getEndA().position()).add(conn.getEndAOffset()));
			double orthLength = ap.dot(catenary.delta())/catenary.delta().dot(catenary.delta());
			Vec3 catPoint = conn.getPoint(orthLength, conn.getEndA()).add(Vec3.atLowerCornerOf(conn.getEndA().position()));
			double dist = antennaTip.subtract(catPoint).lengthSqr();
			if(orthLength < 0||orthLength > 1||dist >= 8)
			{
				PLAYER_ATTACHED_TO.remove(player.getUUID());
				PacketDistributor.sendToAllPlayers(new MessagePowerpackAntenna(
						player.getUUID(), false, conn.getEndA().position(), conn.getEndB().position()
				));
				break tooFar;
			}

			// Transfer energy
			if(!world.isClientSide())
			{
				findBestSource(global, conn).ifPresent(e -> {
					int charge = Math.min(e.getAvailableEnergy(), ANTENNA_CHARGE_RATE);
					charge = insertFlux(itemStack, charge, false);
					e.extractEnergy(charge);
				});
			}
		}
		else
		{
			WireCollisionData wireData = global.getCollisionData();
			Collection<CollisionInfo> atBlock = wireData.getCollisionInfo(player.getOnPos().above(4));
			Optional<Connection> connection = atBlock.stream()
					.filter(CollisionInfo::isInBlock)
					.map(CollisionInfo::connection)
					.filter(c -> c.type instanceof IShockingWire)
					.filter(c -> {
						Vec3 delta = c.getCatenaryData().delta();
						double angle = c.getCatenaryData().isVertical()?1: delta.y()/Math.sqrt(delta.x()*delta.x()+delta.z()*delta.z());
						return Math.abs(angle) < 0.5;
					})
					.findAny();
			connection.ifPresent(conn -> {
				PLAYER_ATTACHED_TO.put(player.getUUID(), conn);
				PacketDistributor.sendToAllPlayers(new MessagePowerpackAntenna(player.getUUID(), false, conn.getEndA().position(), conn.getEndB().position()));
				if(player.getVehicle() instanceof AbstractMinecart minecart&&minecart.getDeltaMovement().lengthSqr() > 4)
					findBestSource(global, conn).ifPresent(e -> {
						if(e.getAvailableEnergy() >= 4096)
						{
							e.extractEnergy(4096);
							LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(world);
							lightningbolt.moveTo(player.getX(), player.getY(), player.getZ());
							world.addFreshEntity(lightningbolt);
							Vec3 dir = minecart.getDeltaMovement().normalize();
							Vec3 orth = dir.yRot((float)Math.toRadians(90));
							Vec3 off = dir.scale(0.125);
							Vec3 left = minecart.position().add(orth.scale(0.625));
							Vec3 right = minecart.position().add(orth.scale(-0.625));
							for(int i = 0; i < 80; i++)
							{
								left = left.add(off);
								right = right.add(off);
								((ServerLevel)world).sendParticles(ParticleTypes.FLAME, left.x, left.y, left.z, 0, 0, 0, 0, 1);
								((ServerLevel)world).sendParticles(ParticleTypes.FLAME, right.x, right.y, right.z, 0, 0, 0, 0, 1);
							}
							Utils.unlockIEAdvancement(player, "tools/secret_bttf");
						}
					});
			});
		}
	}

	private static Optional<EnergyConnector> findBestSource(GlobalWireNetwork globalNetwork, Connection connection)
	{
		EnergyTransferHandler energyHandler = connection.getContainingNet(globalNetwork).getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
		if(energyHandler==null)
			return Optional.empty();
		return energyHandler.getSources().values().stream()
				.filter(o -> o.getAvailableEnergy() > 0)
				.max(Comparator.comparingInt(EnergyConnector::getAvailableEnergy));
	}

	private static void handleMagnetTick(ItemStack itemStack, Level world, Player player)
	{
		if(world.isClientSide())
			return;

		final int radius = 6;
		List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius, radius, radius), itemEntity -> {
			if(itemEntity.hasPickUpDelay())
				return false;
			// check if already being pulled by a different magnet
			String magnetSource = itemEntity.getPersistentData().getString(Lib.MAGNET_SOURCE_NBT);
			if(!magnetSource.isEmpty()&&!magnetSource.equals(player.getStringUUID()))
				return false;
			// check if NBT blacklisted (e.g.: on a conveyor)
			return !itemEntity.getPersistentData().contains(Lib.MAGNET_PREVENT_NBT);
		});
		IEnergyStorage magnetEnergy = itemStack.getCapability(EnergyStorage.ITEM);
		for(ItemEntity itemEntity : items)
			if(itemEntity.distanceTo(player) > 0.001&&magnetEnergy.extractEnergy(MAGNET_CONSUMPTION, false) >= MAGNET_CONSUMPTION)
			{
				if(!itemEntity.getPersistentData().contains(Lib.MAGNET_SOURCE_NBT))
				{
					// play sound when being initially moved
					itemEntity.playSound(IESounds.electromagnet.value(), (float)(.125+player.getRandom().nextDouble()*.25), 1);
					// mark the source of magnetism
					itemEntity.getPersistentData().putString(Lib.MAGNET_SOURCE_NBT, player.getStringUUID());
				}
				// get distance to player, then figure out relative movement needed to move it to the player
				// Inspired by similar code in Mekanism
				Vec3 dist = player.position().subtract(itemEntity.position());
				Vec3 diffToPlayer = new Vec3(Math.min(dist.x, 1), Math.min(dist.y, 1), Math.min(dist.z, 1)).subtract(player.getDeltaMovement());
				itemEntity.setDeltaMovement(diffToPlayer.scale(0.2));
			}
	}

	private static void setCapacitorStatic(ItemStack container, ItemStack capacitor)
	{
		if(!capacitorConfigMap.get().containsKey(capacitor.getItem()))
			return;
		IItemHandler cap = container.getCapability(ItemHandler.ITEM);
		if(cap instanceof IItemHandlerModifiable modifiable)
			modifiable.setStackInSlot(0, capacitor);
	}

	public static ItemStack getCapacitorStatic(ItemStack container)
	{
		IItemHandler cap = container.getCapability(ItemHandler.ITEM);
		if(cap!=null)
		{
			ItemStack capacitor = cap.getStackInSlot(0);
			return capacitorConfigMap.get().containsKey(capacitor.getItem())?capacitor: ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getBannerStatic(ItemStack container)
	{
		IItemHandler cap = container.getCapability(ItemHandler.ITEM);
		if(cap!=null)
		{
			ItemStack banner = cap.getStackInSlot(1);
			if(banner.getItem() instanceof BannerItem)
				return banner;
			if(banner.getItem() instanceof IShaderItem)
				return banner;
		}
		return ItemStack.EMPTY;
	}

	public static int getMaxEnergyStored(ItemStack container)
	{
		ItemStack capacitor = getCapacitorStatic(container);
		if(!capacitor.isEmpty())
		{
			CapacitorConfig cfg = capacitorConfigMap.get().get(capacitor.getItem());
			if(cfg!=null)
				return cfg.storage.getAsInt();
		}
		return 0;
	}

	public static void registerCapabilities(ItemCapabilityRegistrar registrar)
	{
		registerCapabilitiesISI(registrar);
		registrar.register(EnergyStorage.ITEM, stack -> {
			final ItemStack capacitor = getCapacitorStatic(stack);
			final IEnergyStorage capacitorStorage = capacitor.getCapability(EnergyStorage.ITEM);
			if(capacitorStorage==null)
				return null;
			return new WrappingEnergyStorage(capacitorStorage, true, true, () -> setCapacitorStatic(stack, capacitor));
		});
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]{
				new IESlot.WithPredicate(toolInventory, 0, 98, 22, (itemStack) -> capacitorConfigMap.get().containsKey(itemStack.getItem())),
				new IESlot.WithPredicate(toolInventory, 1, 134, 22,
						(itemStack) -> itemStack.getItem() instanceof BannerItem||itemStack.getItem() instanceof IShaderItem
				),
				new IESlot.Upgrades(container, toolInventory, 2, 79, 52, TYPE, stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 3, 117, 52, TYPE, stack, true, level, getPlayer)
		};
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(ItemHandler.ITEM);
		if(inv!=null&&!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(2).isEmpty()&&!inv.getStackInSlot(3).isEmpty())
			Utils.unlockIEAdvancement(player, "tools/upgrade_powerpack");
	}
}
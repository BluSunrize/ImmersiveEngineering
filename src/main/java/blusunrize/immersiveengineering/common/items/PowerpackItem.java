/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
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
import blusunrize.immersiveengineering.common.network.MessagePowerpackAntenna;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.base.Suppliers;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.util.EnergyHelper.*;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class PowerpackItem extends UpgradeableToolItem
{
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
		else if(ItemNBTHelper.hasKey(chest, Lib.NBT_Powerpack))
			return ItemNBTHelper.getItemStack(chest, Lib.NBT_Powerpack);
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
		super(new Properties().stacksTo(1), "POWERPACK");
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		IEnergyStorage energy = CapabilityUtils.getCapability(stack, ForgeCapabilities.ENERGY);
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
		EquipmentSlot slot = Mob.getEquipmentSlotForItem(heldItem);
		if(!player.getItemBySlot(slot).isEmpty())
			return InteractionResultHolder.fail(heldItem);
		player.setItemSlot(slot, heldItem.copy());
		if(!world.isClientSide())
			player.awardStat(Stats.ITEM_USED.get(this));
		heldItem.setCount(0);
		return InteractionResultHolder.sidedSuccess(heldItem, world.isClientSide());
	}

	@Override
	public void onArmorTick(ItemStack itemStack, Level world, Player player)
	{
		int energy = getEnergyStored(itemStack);
		if(energy > 0)
		{
			int pre = energy;
			for(EquipmentSlot slot : EquipmentSlot.values())
			{
				ItemStack equipped = player.getItemBySlot(slot);
				if(isFluxReceiver(equipped)&&!(equipped.getItem() instanceof PowerpackItem)&&!(equipped.getItem() instanceof BlockItem))
					energy -= insertFlux(equipped, Math.min(energy, ITEM_CHARGE_RATE), false);
			}
			// induction charging only happens every 4 ticks
			if(getUpgrades(itemStack).getBoolean("induction")&&player.tickCount%4==0)
			{
				NonNullList<ItemStack> allItems = player.getInventory().items;
				final int selected = player.getInventory().selected;
				for(int i = 0; i < allItems.size(); i++)
				{
					if(i==selected) // ignore equipped item
						continue;
					ItemStack inventoryItem = allItems.get(i);
					if(isFluxReceiver(inventoryItem)&&!(inventoryItem.getItem() instanceof PowerpackItem)&&!(inventoryItem.getItem() instanceof BlockItem))
						energy -= insertFlux(inventoryItem, Math.min(energy, INDUCTION_CHARGE_RATE), false);
					if(energy <= 0) // this is a long loop, so breaking early is good
						break;
				}
			}
			if(pre!=energy)
				extractFlux(itemStack, pre-energy, false);
		}
		if(getUpgrades(itemStack).getBoolean("antenna"))
			handleAntennaTick(itemStack, world, player);
		if(getUpgrades(itemStack).getBoolean("magnet")&&energy >= MAGNET_CONSUMPTION)
			handleMagnetTick(itemStack, world, player);
	}

	private void handleAntennaTick(ItemStack itemStack, Level world, Player player)
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
				ImmersiveEngineering.packetHandler.send(PacketDistributor.ALL.noArg(), new MessagePowerpackAntenna(player, null));
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
				ImmersiveEngineering.packetHandler.send(PacketDistributor.ALL.noArg(), new MessagePowerpackAntenna(player, conn));
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

	private Optional<EnergyConnector> findBestSource(GlobalWireNetwork globalNetwork, Connection connection)
	{
		EnergyTransferHandler energyHandler = connection.getContainingNet(globalNetwork).getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
		if(energyHandler==null)
			return Optional.empty();
		return energyHandler.getSources().values().stream()
				.filter(o -> o.getAvailableEnergy() > 0)
				.max(Comparator.comparingInt(EnergyConnector::getAvailableEnergy));
	}

	private void handleMagnetTick(ItemStack itemStack, Level world, Player player)
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
		for(ItemEntity itemEntity : items)
			if(itemEntity.distanceTo(player) > 0.001&&extractFlux(itemStack, MAGNET_CONSUMPTION, false) >= MAGNET_CONSUMPTION)
			{
				if(!itemEntity.getPersistentData().contains(Lib.MAGNET_SOURCE_NBT))
				{
					// play sound when being initially moved
					itemEntity.playSound(IESounds.electromagnet.get(), (float)(.125+player.getRandom().nextDouble()*.25), 1);
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

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
	{
		// Migration for older powerpacks before they had item storage
		if(ItemNBTHelper.hasKey(stack, "energy"))
		{
			int previousEnergy = ItemNBTHelper.getInt(stack, "energy");
			IItemHandler inv = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(RuntimeException::new);
			ItemStack newCapacitor = new ItemStack(IEBlocks.MetalDevices.CAPACITOR_LV);
			ItemNBTHelper.putInt(newCapacitor, EnergyHelper.ENERGY_KEY, previousEnergy);
			((IItemHandlerModifiable)inv).setStackInSlot(0, newCapacitor);
			ItemNBTHelper.remove(stack, "energy");
		}

		// We'll just have to assume that's Curios which sets the slot of -1
		if(itemSlot==-1&&entity instanceof Player)
			onArmorTick(stack, world, (Player)entity);
	}

	public static ItemStack getCapacitorStatic(ItemStack container)
	{
		if(ForgeCapabilities.ITEM_HANDLER==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = container.getCapability(ForgeCapabilities.ITEM_HANDLER);
		if(cap.isPresent())
		{
			ItemStack capacitor = cap.map(handler -> handler.getStackInSlot(0)).orElse(ItemStack.EMPTY);
			return capacitorConfigMap.get().containsKey(capacitor.getItem())?capacitor: ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getBannerStatic(ItemStack container)
	{
		if(ForgeCapabilities.ITEM_HANDLER==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = container.getCapability(ForgeCapabilities.ITEM_HANDLER);
		if(cap.isPresent())
		{
			ItemStack banner = cap.map(handler -> handler.getStackInSlot(1)).orElse(ItemStack.EMPTY);
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

	@Override
	public int getSlotCount()
	{
		return 4;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==ForgeCapabilities.ENERGY)
						return getCapacitorStatic(stack).getCapability(capability, facing);
					return super.getCapability(capability, facing);
				}
			};
		return super.initCapabilities(stack, nbt);
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
				new IESlot.Upgrades(container, toolInventory, 2, 79, 52, "POWERPACK", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 3, 117, 52, "POWERPACK", stack, true, level, getPlayer)
		};
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
		invCap.ifPresent(inv -> {
			if(!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(2).isEmpty()&&!inv.getStackInSlot(3).isEmpty())
				Utils.unlockIEAdvancement(player, "tools/upgrade_powerpack");
		});
	}
}
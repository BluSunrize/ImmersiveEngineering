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
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireCollisionData;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.EnergyConnector;
import blusunrize.immersiveengineering.api.wires.localhandlers.EnergyTransferHandler.Path;
import blusunrize.immersiveengineering.api.wires.localhandlers.WireDamageHandler.IShockingWire;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.config.IEServerConfig.Machines.CapacitorConfig;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.network.MessageBirthdayParty;
import blusunrize.immersiveengineering.common.network.MessagePowerpackAntenna;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemGetterList;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.common.util.EnergyHelper.*;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class PowerpackItem extends UpgradeableToolItem
{
	private static final Map<Item, CapacitorConfig> capacitorConfigMap = new HashMap<>();

	static
	{
		capacitorConfigMap.put(MetalDevices.CAPACITOR_LV.asItem(), IEServerConfig.MACHINES.lvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_MV.asItem(), IEServerConfig.MACHINES.mvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_HV.asItem(), IEServerConfig.MACHINES.hvCapConfig);
		capacitorConfigMap.put(MetalDevices.CAPACITOR_CREATIVE.asItem(), IEServerConfig.Machines.CapacitorConfig.CREATIVE);
	}

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

	public PowerpackItem()
	{
		super(new Properties().stacksTo(1), "POWERPACK");
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		IEnergyStorage energy = CapabilityUtils.getCapability(stack, CapabilityEnergy.ENERGY);
		if(energy!=null)
		{
			String stored = energy.getEnergyStored()+"/"+getMaxEnergyStored(stack);
			list.add(new TranslatableComponent(Lib.DESC+"info.energyStored", stored).withStyle(ChatFormatting.GRAY));
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
					energy -= insertFlux(equipped, Math.min(energy, 256), false);
			}
			if(pre!=energy)
				extractFlux(itemStack, pre-energy, false);
		}
		if(getUpgrades(itemStack).getBoolean("antenna"))
			handleAntennaTick(itemStack, world, player);
	}

	private void handleAntennaTick(ItemStack itemStack, Level world, Player player)
	{
		// attachment only works when grounded
		if(!player.isOnGround()||world.isClientSide())
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
				EnergyTransferHandler energyHandler = conn.getContainingNet(global).getHandler(EnergyTransferHandler.ID, EnergyTransferHandler.class);
				var sources = energyHandler.getSources().values();
				Optional<EnergyConnector> source = sources.stream()
						.filter(o -> o.getAvailableEnergy() > 0)
						.max(Comparator.comparingInt(EnergyConnector::getAvailableEnergy));
				source.ifPresent(e -> {
					int charge = Math.min(e.getAvailableEnergy(), 256);
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
			});
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
	{
		// Migration for older powerpacks before they had item storage
		if(ItemNBTHelper.hasKey(stack, "energy"))
		{
			int previousEnergy = ItemNBTHelper.getInt(stack, "energy");
			IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(RuntimeException::new);
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
		if(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = container.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
		if(cap.isPresent())
		{
			ItemStack capacitor = cap.map(handler -> handler.getStackInSlot(0)).orElse(ItemStack.EMPTY);
			return capacitorConfigMap.containsKey(capacitor.getItem())?capacitor: ItemStack.EMPTY;
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getBannerStatic(ItemStack container)
	{
		if(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY==null)
			return ItemStack.EMPTY;
		LazyOptional<IItemHandler> cap = container.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
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
			CapacitorConfig cfg = capacitorConfigMap.get(capacitor.getItem());
			if(cfg!=null)
				return cfg.storage.getAsInt();
		}
		return 0;
	}

	@Override
	public int getSlotCount()
	{
		return 3;
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
					if(capability==CapabilityEnergy.ENERGY)
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
				new IESlot.WithPredicate(toolInventory, 0, 98, 22, (itemStack) -> capacitorConfigMap.containsKey(itemStack.getItem())),
				new IESlot.WithPredicate(toolInventory, 1, 134, 22,
						(itemStack) -> itemStack.getItem() instanceof BannerItem||itemStack.getItem() instanceof IShaderItem
				),
				new IESlot.Upgrades(container, toolInventory, 2, 79, 52, "POWERPACK", stack, true, level, getPlayer)
		};
	}
}
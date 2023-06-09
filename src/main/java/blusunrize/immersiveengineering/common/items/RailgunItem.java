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
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderAndCase;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.IRailgunProjectile;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RailgunItem extends UpgradeableToolItem implements IZoomTool, IScrollwheel
{
	public RailgunItem()
	{
		super(new Properties().stacksTo(1), "RAILGUN");
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(ItemCallback.USE_IEOBJ_RENDER);
	}

	@Override
	public int getSlotCount()
	{
		return 2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]{
				new IESlot.Upgrades(container, toolInventory, 0, 80, 32, "RAILGUN", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 1, 100, 32, "RAILGUN", stack, true, level, getPlayer)
		};
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void recalculateUpgrades(ItemStack stack, Level w, Player player)
	{
		super.recalculateUpgrades(stack, w, player);
		IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
		if(energy.getEnergyStored() > this.getMaxEnergyStored(stack))
			ItemNBTHelper.putInt(stack, "energy", this.getMaxEnergyStored(stack));
	}

	@Override
	public void clearUpgrades(ItemStack stack)
	{
		super.clearUpgrades(stack);
		IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
		if(energy.getEnergyStored() > this.getMaxEnergyStored(stack))
			ItemNBTHelper.putInt(stack, "energy", this.getMaxEnergyStored(stack));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;
		LazyOptional<ShaderWrapper> wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		Optional<Boolean> sameShader = wrapperOld.map(wOld -> {
			LazyOptional<ShaderWrapper> wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
			return wrapperNew.map(w -> ItemStack.matches(wOld.getShaderItem(), w.getShaderItem()))
					.orElse(true);
		});
		if(!sameShader.orElse(true))
			return true;
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<EnergyHelper.ItemEnergyStorage> energyStorage = CapabilityUtils.constantOptional(
						new EnergyHelper.ItemEnergyStorage(stack, RailgunItem::getMaxEnergyStored)
				);
				final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(
						new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "railgun"), stack)
				);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==ForgeCapabilities.ENERGY)
						return energyStorage.cast();
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return shaders.cast();
					return super.getCapability(capability, facing);
				}
			};
		return null;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
		String stored = energy.getEnergyStored()+"/"+getMaxEnergyStored(stack);
		list.add(Component.translatable(Lib.DESC+"info.energyStored", stored).withStyle(ChatFormatting.GRAY));
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.NONE;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		int consumption = IEServerConfig.TOOLS.railgun_consumption.get();
		float energyMod = 1+this.getUpgrades(stack).getFloat("consumption");
		consumption = (int)(consumption*energyMod);
		IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
		if(energy.extractEnergy(consumption, true)==consumption&&!findAmmo(stack, player).isEmpty())
		{
			player.startUsingItem(hand);
			playChargeSound(player, stack);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}
		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

	public static void playChargeSound(LivingEntity living, ItemStack railgun)
	{
		living.level().playSound(null,
				living.getX(), living.getY(), living.getZ(),
				getChargeTime(railgun) <= 20?IESounds.chargeFast.get(): IESounds.chargeSlow.get(), SoundSource.PLAYERS,
				1.5f, 1f
		);

	}

	@Override
	public void onUseTick(Level level, LivingEntity user, ItemStack stack, int count)
	{
		int inUse = this.getUseDuration(stack)-count;
		if(inUse > getChargeTime(stack)&&inUse%20==user.getRandom().nextInt(20))
		{
			user.level().playSound(null, user.getX(), user.getY(), user.getZ(), IESounds.spark.get(), SoundSource.PLAYERS, .8f+(.2f*user.getRandom().nextFloat()), .5f+(.5f*user.getRandom().nextFloat()));
			ShaderAndCase shader = ShaderRegistry.getStoredShaderAndCase(stack);
			if(shader!=null)
			{
				Vec3 pos = Utils.getLivingFrontPos(user, .4375, user.getBbHeight()*.75, ItemUtils.getLivingHand(user, user.getUsedItemHand()), false, 1);
				shader.registryEntry().getEffectFunction().execute(user.level(), shader.shader(), stack, shader.sCase().getShaderType().toString(), pos, null, .0625f);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int timeLeft)
	{
		if(!world.isClientSide()&&user instanceof Player player)
		{
			int inUse = this.getUseDuration(stack)-timeLeft;
			ItemNBTHelper.remove(stack, "inUse");
			if(inUse < getChargeTime(stack))
				return;
			int consumption = IEServerConfig.TOOLS.railgun_consumption.get();
			float energyMod = 1+this.getUpgrades(stack).getFloat("consumption");
			consumption = (int)(consumption*energyMod);
			IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
			if(energy.extractEnergy(consumption, true)==consumption)
			{
				ItemStack ammo = findAmmo(stack, player);
				if(!ammo.isEmpty())
				{
					ItemStack ammoConsumed = ammo.split(1);
					fireProjectile(stack, world, user, ammoConsumed);
					energy.extractEnergy(consumption, false);
				}
			}
		}
	}

	public static Entity fireProjectile(ItemStack railgun, Level world, LivingEntity user, ItemStack ammo)
	{
		IRailgunProjectile projectileProperties = RailgunHandler.getProjectile(ammo);
		float speed = 20;
		Entity shot = new RailgunShotEntity(user.level(), user, speed, 0, ammo);
		shot = projectileProperties.getProjectile(user instanceof Player player?player: null, ammo, shot);
		user.level().playSound(null, user.getX(), user.getY(), user.getZ(), IESounds.railgunFire.get(), SoundSource.PLAYERS, 1, .5f+(.5f*user.getRandom().nextFloat()));
		if(!world.isClientSide)
			user.level().addFreshEntity(shot);

		ShaderAndCase shader = ShaderRegistry.getStoredShaderAndCase(railgun);
		if(shader!=null)
		{
			HumanoidArm handside = user.getMainArm();
			if(user.getUsedItemHand()!=InteractionHand.MAIN_HAND)
				handside = handside==HumanoidArm.LEFT?HumanoidArm.RIGHT: HumanoidArm.LEFT;
			Vec3 pos = Utils.getLivingFrontPos(user, .75, user.getBbHeight()*.75, handside, false, 1);
			shader.registryEntry().getEffectFunction().execute(world, shader.shader(), railgun,
					shader.sCase().getShaderType().toString(), pos,
					Vec3.directionFromRotation(user.getRotationVector()), .125f);
		}
		return shot;
	}

	public static ItemStack findAmmo(ItemStack railgun, Player player)
	{
		// Check for cached slot
		if(ItemNBTHelper.hasKey(railgun, "ammo_slot"))
		{
			int slot = ItemNBTHelper.getInt(railgun, "ammo_slot");
			ItemStack ammo = findAmmoInSlot(player, slot);
			if(!ammo.isEmpty())
				return ammo;
		}

		// Find it otherwise
		if(isAmmo(player.getItemInHand(InteractionHand.OFF_HAND)))
		{
			ItemNBTHelper.putInt(railgun, "ammo_slot", 0);
			return player.getItemInHand(InteractionHand.OFF_HAND);
		}
		else if(isAmmo(player.getItemInHand(InteractionHand.MAIN_HAND)))
		{
			ItemNBTHelper.putInt(railgun, "ammo_slot", 1);
			return player.getItemInHand(InteractionHand.MAIN_HAND);
		}
		else
			for(int i = 0; i < player.getInventory().getContainerSize(); i++)
			{
				ItemStack itemstack = player.getInventory().getItem(i);
				if(isAmmo(itemstack))
				{
					ItemNBTHelper.putInt(railgun, "ammo_slot", 2+i);
					return itemstack;
				}
			}
		return ItemStack.EMPTY;
	}

	public static ItemStack findAmmoInSlot(Player player, int slot)
	{
		ItemStack ammo = ItemStack.EMPTY;
		if(slot==0||slot==1)
			ammo = player.getItemInHand(slot==0?InteractionHand.MAIN_HAND: InteractionHand.OFF_HAND);
		else if(slot > 1&&slot-2 < player.getInventory().getContainerSize())
			ammo = player.getInventory().getItem(slot-2);
		if(isAmmo(ammo))
			return ammo;
		return ItemStack.EMPTY;
	}

	public static boolean isAmmo(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		RailgunHandler.IRailgunProjectile prop = RailgunHandler.getProjectile(stack);
		return prop!=null;
	}

	private boolean checkAmmoSlot(ItemStack stack, Player player, int actualSlot)
	{
		if(!findAmmoInSlot(player, actualSlot).isEmpty())
		{
			ItemNBTHelper.putInt(stack, "ammo_slot", actualSlot);
			player.getInventory().setChanged();
			return true;
		}
		return false;
	}

	@Override
	public void onScrollwheel(ItemStack stack, Player player, boolean forward)
	{
		int slot = ItemNBTHelper.getInt(stack, "ammo_slot");
		int count = player.getInventory().getContainerSize()+2;
		if(forward)
		{
			for(int i = 1; i < count; i++)
				if(checkAmmoSlot(stack, player, (slot+i)%count))
					return;
		}
		else
		{
			for(int i = count-1; i >= 1; i--)
				if(checkAmmoSlot(stack, player, (slot+i)%count))
					return;
		}
	}

	public static int getChargeTime(ItemStack railgun)
	{
		return (int)(40/(1+getUpgradesStatic(railgun).getFloat("speed")));
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
		invCap.ifPresent(inv -> {
			if(!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty())
				Utils.unlockIEAdvancement(player, "tools/upgrade_railgun");
		});
	}

	public static int getMaxEnergyStored(ItemStack container)
	{
		return 8000;
	}


	@Override
	public boolean canZoom(ItemStack stack, Player player)
	{
		return this.getUpgrades(stack).getBoolean("scope");
	}

	float[] zoomSteps = new float[]{.1f, .15625f, .2f, .25f, .3125f, .4f, .5f, .625f};

	@Override
	public float[] getZoomSteps(ItemStack stack, Player player)
	{
		return zoomSteps;
	}
}
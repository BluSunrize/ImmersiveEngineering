/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IEShieldItem extends UpgradeableToolItem
{
	public IEShieldItem()
	{
		super(new Properties().defaultDurability(1024), "SHIELD");
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(ItemCallback.USE_IEOBJ_RENDER);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<EnergyHelper.ItemEnergyStorage> energyStorage = CapabilityUtils.constantOptional(
						new EnergyHelper.ItemEnergyStorage(stack, IEShieldItem::getMaxEnergyStored)
				);
				final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(
						new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "shield"), stack)
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
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		if(getMaxEnergyStored(stack) > 0)
		{
			IEnergyStorage energyStorage = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
			String stored = energyStorage.getEnergyStored()+"/"+getMaxEnergyStored(stack);
			list.add(Component.translatable(Lib.DESC+"info.energyStored", stored));
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
		if(world.isClientSide)
			return;

		if(ent instanceof LivingEntity)
			inHand |= ((LivingEntity)ent).getItemInHand(InteractionHand.OFF_HAND)==stack;

		boolean blocking = ent instanceof LivingEntity&&((LivingEntity)ent).isBlocking();
		IEnergyStorage energy = CapabilityUtils.getPresentCapability(stack, ForgeCapabilities.ENERGY);
		if(!inHand||!blocking)//Don't recharge if in use, to avoid flickering
		{
			if(getUpgrades(stack).contains("flash_cooldown", Tag.TAG_INT)&&energy.extractEnergy(10, true)==10)
			{
				energy.extractEnergy(20, false);
				int cooldown = getUpgrades(stack).getInt("flash_cooldown");
				if(--cooldown <= 0)
					getUpgrades(stack).remove("flash_cooldown");
				else
					getUpgrades(stack).putInt("flash_cooldown", cooldown);
			}
			if(getUpgrades(stack).contains("shock_cooldown", Tag.TAG_INT)&&energy.extractEnergy(10, true)==10)
			{
				energy.extractEnergy(20, false);
				int cooldown = getUpgrades(stack).getInt("shock_cooldown");
				if(--cooldown <= 0)
					getUpgrades(stack).remove("shock_cooldown");
				else
					getUpgrades(stack).putInt("shock_cooldown", cooldown);
			}
		}
	}

	@Override
	public boolean canPerformAction(ItemStack stack, ToolAction toolAction)
	{
		return ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
	}

	public void hitShield(ItemStack stack, Player player, DamageSource source, float amount, LivingAttackEvent event)
	{
		if(getUpgrades(stack).getBoolean("flash")&&getUpgrades(stack).getInt("flash_cooldown") <= 0)
		{
			Vec3 look = player.getLookAngle();
			//Offsets Player position by look backwards, then truncates cone at 1
			List<LivingEntity> targets = Utils.getTargetsInCone(player.getCommandSenderWorld(), player.position().subtract(look), player.getLookAngle().scale(9), 1.57079f, .5f);
			for(LivingEntity t : targets)
				if(!player.equals(t))
				{
					t.addEffect(new MobEffectInstance(IEPotions.FLASHED.get(), 100, 1));
					if(t instanceof Mob)
						((Mob)t).setTarget(null);
				}
			getUpgrades(stack).putInt("flash_cooldown", 40);
		}
		if(getUpgrades(stack).getBoolean("shock")&&getUpgrades(stack).getInt("shock_cooldown") <= 0)
		{
			boolean b = false;
			if(event.getSource().is(DamageTypeTags.IS_PROJECTILE)&&event.getSource().getDirectEntity()!=null)
			{
				Entity projectile = event.getSource().getDirectEntity();
				projectile.discard();
				event.setCanceled(true);
				b = true;
			}
			if(event.getSource().getEntity()!=null&&event.getSource().getEntity() instanceof LivingEntity&&event.getSource().getEntity().distanceToSqr(player) < 4)
			{
				ElectricDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(event.getEntity().level(), 1, true);
				dmgsrc.apply(event.getSource().getEntity());
				b = true;
			}
			if(b)
			{
				getUpgrades(stack).putInt("shock_cooldown", 40);
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.spark.get(),
						SoundSource.BLOCKS, 2.5F, 0.5F+ApiUtils.RANDOM.nextFloat());
			}
		}
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack material)
	{
		return material.is(IETags.getTagsFor(EnumMetals.STEEL).ingot);
	}

	public static int getMaxEnergyStored(ItemStack container)
	{
		return (getUpgradesStatic(container).getBoolean("flash")||getUpgradesStatic(container).getBoolean("shock"))?3200: 0;
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, @Nonnull InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		playerIn.startUsingItem(handIn);
		return InteractionResultHolder.consume(itemstack);
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.BLOCK;
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
				new IESlot.Upgrades(container, toolInventory, 0, 80, 32, "SHIELD", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 1, 100, 32, "SHIELD", stack, true, level, getPlayer)
		};
	}

	@Override
	public int getSlotCount()
	{
		return 2;
	}

	@Override
	public boolean isFoil(ItemStack stack)
	{
		return false;//Remove glint effect since it doesn't work that well with models, see #2944
	}
}
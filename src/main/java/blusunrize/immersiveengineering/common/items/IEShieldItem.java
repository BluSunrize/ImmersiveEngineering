/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.tool.upgrade.Cooldown;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IEShieldItem extends UpgradeableToolItem
{
	public static final String TYPE = "SHIELD";

	public IEShieldItem()
	{
		super(new Properties().durability(1024), TYPE, 2);
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(ItemCallback.USE_IEOBJ_RENDER);
	}

	public static void registerCapabilities(ItemCapabilityRegistration.ItemCapabilityRegistrar registrar)
	{
		registerCapabilitiesISI(registrar);
		registrar.register(
				EnergyStorage.ITEM,
				stack -> new ComponentEnergyStorage(stack, IEDataComponents.GENERIC_ENERGY.get(), getMaxEnergyStored(stack))
		);
		registrar.register(
				CapabilityShader.ITEM,
				stack -> new ShaderWrapper_Item(IEApi.ieLoc("shield"), stack)
		);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged||CapabilityShader.shouldReequipDueToShader(oldStack, newStack))
			return true;
		else
			return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		if(getMaxEnergyStored(stack) > 0)
		{
			IEnergyStorage energyStorage = stack.getCapability(EnergyStorage.ITEM);
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
		IEnergyStorage energy = stack.getCapability(EnergyStorage.ITEM);
		if(!inHand||!blocking)//Don't recharge if in use, to avoid flickering
		{
			for(var cooldownKey : List.of(UpgradeEffect.FLASH, UpgradeEffect.SHOCK))
			{
				var upgrades = getUpgrades(stack);
				var cooldown = upgrades.get(cooldownKey);
				if(cooldown.isOnCooldown()&&energy.extractEnergy(10, true)==10)
				{
					energy.extractEnergy(10, false);
					if(cooldown.isOnCooldown())
						stack.set(IEDataComponents.UPGRADE_DATA, upgrades.with(cooldownKey, cooldown.tick()));
				}
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
		var upgrades = getUpgrades(stack);
		if(upgrades.has(UpgradeEffect.FLASH)&&!upgrades.get(UpgradeEffect.FLASH).isOnCooldown())
		{
			Vec3 look = player.getLookAngle();
			//Offsets Player position by look backwards, then truncates cone at 1
			List<LivingEntity> targets = Utils.getTargetsInCone(player.getCommandSenderWorld(), player.position().subtract(look), player.getLookAngle().scale(9), 1.57079f, .5f);
			for(LivingEntity t : targets)
				if(!player.equals(t))
				{
					t.addEffect(new MobEffectInstance(IEPotions.FLASHED, 100, 1));
					if(t instanceof Mob)
						((Mob)t).setTarget(null);
				}
			upgrades = upgrades.with(UpgradeEffect.FLASH, new Cooldown(40));
		}
		if(upgrades.has(UpgradeEffect.SHOCK)&&!upgrades.get(UpgradeEffect.SHOCK).isOnCooldown())
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
				upgrades = upgrades.with(UpgradeEffect.SHOCK, new Cooldown(40));
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.spark.value(),
						SoundSource.BLOCKS, 2.5F, 0.5F+ApiUtils.RANDOM.nextFloat());
			}
		}
		stack.set(IEDataComponents.UPGRADE_DATA, upgrades);
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack material)
	{
		return material.is(IETags.getTagsFor(EnumMetals.STEEL).ingot);
	}

	public static int getMaxEnergyStored(ItemStack container)
	{
		var upgrades = getUpgradesStatic(container);
		return (upgrades.has(UpgradeEffect.FLASH)||upgrades.has(UpgradeEffect.SHOCK))?3200: 0;
	}

	@Override
	public int getUseDuration(ItemStack p_41454_, LivingEntity p_344979_)
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
				new IESlot.Upgrades(container, toolInventory, 0, 80, 32, TYPE, stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 1, 100, 32, TYPE, stack, true, level, getPlayer)
		};
	}

	@Override
	public boolean isFoil(ItemStack stack)
	{
		return false;//Remove glint effect since it doesn't work that well with models, see #2944
	}
}
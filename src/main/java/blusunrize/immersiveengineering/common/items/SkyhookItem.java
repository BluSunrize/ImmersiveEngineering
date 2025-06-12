/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.Lib.DamageTypes;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.entities.SkyhookUserData;
import blusunrize.immersiveengineering.common.entities.SkyhookUserData.SkyhookStatus;
import blusunrize.immersiveengineering.common.entities.SkylineHookEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.register.IEDataAttachments;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.IEApi.ieLoc;

public class SkyhookItem extends UpgradeableToolItem implements IElectricEquipment
{
	public static final String TYPE = "SKYHOOK";

	public SkyhookItem()
	{
		super(new Properties().stacksTo(1).component(IEDataComponents.SKYHOOK_SPEED_LIMIT, false), TYPE, 2);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook").withStyle(ChatFormatting.GRAY));
		if(shouldLimitSpeed(stack))
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook.speedLimit").withStyle(ChatFormatting.GRAY));
		else
			list.add(Component.translatable(Lib.DESC_FLAVOUR+"skyhook.noLimit").withStyle(ChatFormatting.GRAY));
	}

	public static boolean shouldLimitSpeed(ItemStack stack)
	{
		return stack.getOrDefault(IEDataComponents.SKYHOOK_SPEED_LIMIT, false);
	}

	public static void setLimitSpeed(ItemStack stack, boolean doLimit)
	{
		stack.set(IEDataComponents.SKYHOOK_SPEED_LIMIT, doLimit);
	}

	public static boolean toggleSpeedLimit(ItemStack stack)
	{
		boolean wasActive = shouldLimitSpeed(stack);
		setLimitSpeed(stack, !wasActive);
		return !wasActive;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
	{
		var builder = ItemAttributeModifiers.builder();
		builder.add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(ieLoc("weapon_modifier_damage"), 5, Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
		);
		builder.add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(ieLoc("weapon_modifier_speed"), -2.4, Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
		);
		return builder.build();
	}

	private static boolean canSmash(LivingEntity entity)
	{
		return entity.fallDistance > 1.5F&&!entity.isFallFlying()&&getUpgradesStatic(entity.getMainHandItem()).has(UpgradeEffect.MACE_ATTACK);
	}

	@Override
	public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource)
	{
		Entity attacker = damageSource.getDirectEntity();
		if(attacker instanceof LivingEntity livingentity)
			if(canSmash(livingentity))
			{
				float fallDistance = livingentity.fallDistance;
				float damageBonus;
				if(fallDistance <= 3)
					damageBonus = 4f*fallDistance;
				else if(fallDistance <= 8)
					damageBonus = 12f+2f*(fallDistance-3f);
				else
					damageBonus = 22f+fallDistance-8f;
				return damageBonus;
			}
		return 0;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
	{
		// need this so that postHurtEnemy triggers
		return true;
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker)
	{
		// reset fall damage on a successful attack
		if(canSmash(attacker))
			attacker.resetFallDistance();
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{

		ItemStack stack = player.getItemInHand(hand);
		if(player.getCooldowns().isOnCooldown(this))
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
		if(player.isShiftKeyDown())
		{
			boolean limitSpeed = toggleSpeedLimit(stack);
			if(limitSpeed)
				player.displayClientMessage(Component.translatable("chat.immersiveengineering.info.skyhookLimited"), true);
			else
				player.displayClientMessage(Component.translatable("chat.immersiveengineering.info.skyhookUnlimited"), true);
		}
		else
		{
			SkyhookUserData data = player.getData(IEDataAttachments.SKYHOOK_USER.get());
			if(data.hook!=null&&!world.isClientSide)
			{
				data.dismount();
				IELogger.logger.info("Player left voluntarily");
			}
			else
			{
				data.startHolding();
				player.startUsingItem(hand);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count)
	{
		super.onUseTick(level, player, stack, count);
		SkyhookUserData data = player.getData(IEDataAttachments.SKYHOOK_USER.get());
		if(data.getStatus()!=SkyhookStatus.HOLDING_CONNECTING)
			return;
		Connection con = WireUtils.getConnectionMovedThrough(level, player);
		if(con!=null)
			SkylineHelper.spawnHook(player, con, player.getUsedItemHand(), shouldLimitSpeed(stack), getSlopeModifier(stack));
	}

	@Override
	public void releaseUsing(ItemStack stack, Level worldIn, LivingEntity player, int timeLeft)
	{
		super.releaseUsing(stack, worldIn, player, timeLeft);
		if(!worldIn.isClientSide)
			player.getData(IEDataAttachments.SKYHOOK_USER.get()).release();
	}

	public float getSlopeModifier(ItemStack stack)
	{
		var upgrades = this.getUpgrades(stack);
		return Math.max(upgrades.get(UpgradeEffect.SLOPE_MODIFIER), 0.5f);
	}

	@Override
	public void onStrike(ItemStack equipped, EquipmentSlot eqSlot, LivingEntity owner, Map<String, Object> cache, @Nullable DamageSource dmg, ElectricSource desc)
	{
		if(dmg instanceof ElectricDamageSource eds&&dmg.is(DamageTypes.WIRE_SHOCK)&&this.getUpgrades(equipped).has(UpgradeEffect.INSULATED)
				&&(owner.getVehicle() instanceof SkylineHookEntity||owner.isUsingItem())) // either on a wire or trying to attach
		{
			eds.dmg = 0;
		}
	}

	@Override
	public int getUseDuration(ItemStack p_41454_, LivingEntity p_344979_)
	{
		return 72000;
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
				new IESlot.Upgrades(container, toolInventory, 0, 102, 42, "SKYHOOK", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 1, 102, 22, "SKYHOOK", stack, true, level, getPlayer),
		};
	}
}
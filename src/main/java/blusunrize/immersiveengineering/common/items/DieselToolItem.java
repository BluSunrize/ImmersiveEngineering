/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderAndCase;
import blusunrize.immersiveengineering.api.tool.upgrade.UpgradeEffect;
import blusunrize.immersiveengineering.api.tool.INoisyTool;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemAttributeModifiers.Entry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class DieselToolItem extends UpgradeableToolItem implements IAdvancedFluidItem, INoisyTool
{
	protected static final int CAPACITY = 2*FluidType.BUCKET_VOLUME;

	public DieselToolItem(Item.Properties props, String upgradeType, int slotCount)
	{
		super(props, upgradeType, slotCount);
	}

	@Override
	public int getEnchantmentValue()
	{
		return 0;
	}

	public static void registerCapabilities(ItemCapabilityRegistration.ItemCapabilityRegistrar registrar)
	{
		registerCapabilitiesISI(registrar);
		registrar.register(FluidHandler.ITEM, (stack, $) -> new IEItemFluidHandler(stack, CAPACITY));
		registrar.register(
				CapabilityShader.ITEM,
				stack -> new ShaderWrapper_Item(BuiltInRegistries.ITEM.getKey(stack.getItem()), stack)
		);
	}

	@Override
	public int getBarWidth(@Nonnull ItemStack stack)
	{
		return Math.round(MAX_BAR_WIDTH*(1-(float)getHeadDamage(stack)/(float)getMaxHeadDamage(stack)));
	}

	@Override
	public boolean isBarVisible(@Nonnull ItemStack stack)
	{
		return getHeadDamage(stack) > 0;
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack, RegistryAccess registries)
	{
		super.finishUpgradeRecalculation(stack, registries);
		FluidStack fs = getFluid(stack);
		if(fs!=null&&fs.getAmount() > getCapacity(stack, CAPACITY))
		{
			fs.setAmount(getCapacity(stack, CAPACITY));
			stack.set(IEDataComponents.GENERIC_FLUID, SimpleFluidContent.copyOf(fs));
		}
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity player)
	{
		consumeDurability(stack, target.getCommandSenderWorld(), null, null, player);
		return true;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.BOW;
	}

	/* ------------- FLUID ------------- */

	@Override
	public int getCapacity(ItemStack container, int baseCapacity)
	{
		return baseCapacity+getUpgrades(container).get(UpgradeEffect.CAPACITY);
	}

	@Override
	public boolean allowFluid(ItemStack container, FluidStack fluid)
	{
		return fluid!=null&&fluid.getFluid().is(IETags.drillFuel);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state)
	{
		//TODO fix drill air tank
		Tier tier = getHarvestLevel(stack, null);
		if(tier==null)
			return false;
		return isEffective(stack, state)&&canToolBeUsed(stack)&&!state.is(tier.getIncorrectBlocksForDrops());
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)
	{
		List<Entry> modifiers = new ArrayList<>();
		ItemStack head = getHead(stack);
		if(!head.isEmpty()&&canToolBeUsed(stack))
		{
			modifiers.add(new Entry(Attributes.ATTACK_DAMAGE, new AttributeModifier(
					BASE_ATTACK_DAMAGE_ID, getAttackDamage(stack, head), Operation.ADD_VALUE
			), EquipmentSlotGroup.MAINHAND));
			modifiers.add(new Entry(Attributes.ATTACK_SPEED, new AttributeModifier(
					BASE_ATTACK_SPEED_ID, -2.5D, Operation.ADD_VALUE
			), EquipmentSlotGroup.MAINHAND));
		}
		return new ItemAttributeModifiers(modifiers, false);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged||CapabilityShader.shouldReequipDueToShader(oldStack, newStack))
			return true;
		else
			return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	private final Map<UUID, Integer> animationTimer = new HashMap<>();

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand)
	{
		if(canToolBeUsed(stack))
		{
			if(!animationTimer.containsKey(entity.getUUID()))
				animationTimer.put(entity.getUUID(), 40);
			else if(animationTimer.get(entity.getUUID()) < 20)
				animationTimer.put(entity.getUUID(), 20);
		}
		return true;
	}

	protected int getToolDamageFromBlock(ItemStack stack, @Nullable BlockState state)
	{
		return state==null||isEffective(stack, state)?1: 3;
	}

	protected void consumeDurability(
			ItemStack stack, Level world, @Nullable BlockState state, @Nullable BlockPos pos, LivingEntity living
	)
	{
		Preconditions.checkArgument((pos==null)==(state==null));
		if(state==null||state.getDestroySpeed(world, pos)!=0.0f)
		{
			int dmg = getToolDamageFromBlock(stack, state);
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				if(!getUpgrades(stack).has(UpgradeEffect.OILED)||ApiUtils.RANDOM.nextInt(4)==0)
					damageHead(head, dmg, living);
				this.setHead(stack, head);
				IFluidHandler handler = FluidUtil.getFluidHandler(stack).orElseThrow(RuntimeException::new);
				handler.drain(1, FluidAction.EXECUTE);

				ShaderAndCase shader = ShaderRegistry.getStoredShaderAndCase(stack);
				if(shader!=null)
				{
					Vec3 particlePos;
					if(pos!=null)
						particlePos = Vec3.atCenterOf(pos);
					else
						particlePos = living.position();
					shader.registryEntry().getEffectFunction().execute(
							world, stack, shader.sCase().getShaderType().toString(),
							particlePos, null, .375f
					);
				}
			}
		}
	}

	protected abstract void damageHead(ItemStack head, int amount, LivingEntity living);

	protected abstract double getAttackDamage(ItemStack stack, ItemStack head);

	public abstract boolean isEffective(ItemStack stack, BlockState state);

	public abstract Tier getHarvestLevel(ItemStack stack, @Nullable Player player);

	public abstract boolean canToolBeUsed(ItemStack stack);

	protected abstract ItemStack getHead(ItemStack tool);

	protected abstract void setHead(ItemStack tool, ItemStack newHead);

	public abstract int getMaxHeadDamage(ItemStack stack);

	public abstract int getHeadDamage(ItemStack stack);

	@Override
	public abstract Holder<SoundEvent> getIdleSound(ItemStack stack);

	@Override
	public abstract Holder<SoundEvent> getBusySound(ItemStack stack);

	@Override
	public abstract Holder<SoundEvent> getFadingSound(ItemStack stack);

	@Override
	public abstract Holder<SoundEvent> getAttackSound(ItemStack stack);

	@Override
	public abstract Holder<SoundEvent> getHarvestSound(ItemStack stack);

	@Override
	public abstract boolean ableToMakeNoise(ItemStack stack);
}

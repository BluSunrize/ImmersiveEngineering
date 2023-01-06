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
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderAndCase;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class DieselToolItem extends UpgradeableToolItem implements IAdvancedFluidItem
{
	protected static final int CAPACITY = 2*FluidType.BUCKET_VOLUME;

	public DieselToolItem(Item.Properties props, String upgradeType)
	{
		super(props, upgradeType);
	}

	@Override
	public int getEnchantmentValue()
	{
		return 0;
	}

	@Nullable
	@Override
	public CompoundTag getShareTag(ItemStack stack)
	{
		CompoundTag ret = super.getShareTag(stack);
		if(ret==null)
			ret = new CompoundTag();
		else
			ret = ret.copy();
		ItemStack head = getHead(stack);
		if(!head.isEmpty())
			ret.put("head", head.save(new CompoundTag()));
		return ret;
	}

	@Override
	public void readShareTag(ItemStack stack, @Nullable CompoundTag nbt)
	{
		if(nbt!=null)
		{
			setHead(stack, ItemStack.of(nbt.getCompound("head")));
			nbt.remove("head");
		}
		super.readShareTag(stack, nbt);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				private final LazyOptional<IEItemFluidHandler> fluids = CapabilityUtils.constantOptional(new IEItemFluidHandler(stack, CAPACITY));
				private final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(
						new ShaderWrapper_Item(BuiltInRegistries.ITEM.getKey(DieselToolItem.this), stack)
				);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==ForgeCapabilities.FLUID_HANDLER_ITEM)
						return fluids.cast();
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return shaders.cast();
					return super.getCapability(capability, facing);
				}
			};
		return null;
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
	public void finishUpgradeRecalculation(ItemStack stack)
	{
		super.finishUpgradeRecalculation(stack);
		FluidStack fs = getFluid(stack);
		if(fs!=null&&fs.getAmount() > getCapacity(stack, CAPACITY))
		{
			fs.setAmount(getCapacity(stack, CAPACITY));
			ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
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
		return baseCapacity+getUpgrades(container).getInt("capacity");
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
		return isEffective(stack, state)&&canToolBeUsed(stack)&&TierSortingRegistry.isCorrectTierForDrops(tier, state);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
	{
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		if(slot==EquipmentSlot.MAINHAND)
		{
			ItemStack head = getHead(stack);
			if(!head.isEmpty()&&canToolBeUsed(stack))
			{
				builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
						BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(stack, head), Operation.ADDITION
				));
				builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(
						BASE_ATTACK_SPEED_UUID, "Tool modifier", -2.5D, Operation.ADDITION
				));
			}
		}
		return builder.build();
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

	private final Map<UUID, Integer> animationTimer = new HashMap<>();

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity)
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
				if(!getUpgrades(stack).getBoolean("oiled")||ApiUtils.RANDOM.nextInt(4)==0)
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
							world, shader.shader(), stack, shader.sCase().getShaderType().toString(),
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
}

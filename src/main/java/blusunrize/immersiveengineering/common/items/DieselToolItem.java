package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class DieselToolItem extends UpgradeableToolItem
		implements IAdvancedFluidItem, IOBJModelCallback<ItemStack>, ITool
{
	protected static final int CAPACITY = 2*FluidAttributes.BUCKET_VOLUME;

	public DieselToolItem(String name, Item.Properties props, String upgradeType)
	{
		super(name, props, upgradeType);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
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
				private final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(new ShaderWrapper_Item(getRegistryName(), stack));

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
						return fluids.cast();
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return shaders.cast();
					return super.getCapability(capability, facing);
				}
			};
		return null;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double)getHeadDamage(stack)/(double)getMaxHeadDamage(stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
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
		return fluid!=null&&DieselHandler.isValidDrillFuel(fluid.getFluid());
	}

	@Override
	public boolean canHarvestBlock(ItemStack stack, BlockState state)
	{
		return isEffective(stack, state.getMaterial())&&canToolBeUsed(stack, null);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
	{
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		if(slot==EquipmentSlot.MAINHAND)
		{
			ItemStack head = getHead(stack);
			if(!head.isEmpty()&&canToolBeUsed(stack, null))
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
		if(canToolBeUsed(stack, entity))
		{
			if(!animationTimer.containsKey(entity.getUUID()))
				animationTimer.put(entity.getUUID(), 40);
			else if(animationTimer.get(entity.getUUID()) < 20)
				animationTimer.put(entity.getUUID(), 20);
		}
		return true;
	}

	protected void consumeDurability(
			ItemStack stack, Level world, @Nullable BlockState state, @Nullable BlockPos pos, LivingEntity living
	)
	{
		Preconditions.checkArgument((pos==null)==(state==null));
		if(state==null||state.getDestroySpeed(world, pos)!=0.0f)
		{
			int dmg = state==null||ForgeHooks.isToolEffective(world, pos, stack)||isEffective(stack, state.getMaterial())?1: 3;
			ItemStack head = getHead(stack);
			if(!head.isEmpty())
			{
				if(!getUpgrades(stack).getBoolean("oiled")||Utils.RAND.nextInt(4)==0)
					damageHead(head, dmg, living);
				this.setHead(stack, head);
				IFluidHandler handler = FluidUtil.getFluidHandler(stack).orElseThrow(RuntimeException::new);
				handler.drain(1, FluidAction.EXECUTE);

				Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
				if(shader!=null)
				{
					Vec3 particlePos;
					if(pos!=null)
						particlePos = Vec3.atCenterOf(pos);
					else
						particlePos = living.position();
					shader.getMiddle().getEffectFunction().execute(
							world, shader.getLeft(), stack, shader.getRight().getShaderType().toString(),
							particlePos, null, .375f
					);
				}
			}
		}
	}

	protected boolean shouldRotate(LivingEntity entity, ItemStack stack, TransformType transform)
	{
		return entity!=null&&canToolBeUsed(stack, entity)&&
				(entity.getItemInHand(InteractionHand.MAIN_HAND)==stack||entity.getItemInHand(InteractionHand.OFF_HAND)==stack)&&
				(transform==TransformType.FIRST_PERSON_RIGHT_HAND||transform==TransformType.FIRST_PERSON_LEFT_HAND||
						transform==TransformType.THIRD_PERSON_RIGHT_HAND||transform==TransformType.THIRD_PERSON_LEFT_HAND);
	}

	protected abstract void damageHead(ItemStack head, int amount, LivingEntity living);

	protected abstract double getAttackDamage(ItemStack stack, ItemStack head);

	public abstract boolean isEffective(ItemStack stack, Material mat);

	protected abstract boolean canToolBeUsed(ItemStack stack, @Nullable LivingEntity player);

	protected abstract ItemStack getHead(ItemStack tool);

	protected abstract void setHead(ItemStack tool, ItemStack newHead);

	public abstract int getMaxHeadDamage(ItemStack stack);

	public abstract int getHeadDamage(ItemStack stack);
}

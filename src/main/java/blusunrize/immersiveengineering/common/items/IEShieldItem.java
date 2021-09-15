/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IEShieldItem extends UpgradeableToolItem implements IIEEnergyItem, IOBJModelCallback<ItemStack>
{
	public IEShieldItem()
	{
		super(new Properties().defaultDurability(1024), "SHIELD");
		GenericDeferredWork.registerDispenseBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IItemRenderProperties> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(IEOBJItemRenderer.USE_IEOBJ_RENDER);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<EnergyHelper.ItemEnergyStorage> energyStorage = CapabilityUtils.constantOptional(
						new EnergyHelper.ItemEnergyStorage(stack)
				);
				final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(
						new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "shield"), stack)
				);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
				{
					if(capability==CapabilityEnergy.ENERGY)
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		if(this.getMaxEnergyStored(stack) > 0)
		{
			String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
			list.add(new TranslatableComponent(Lib.DESC+"info.energyStored", stored));
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
		if(!inHand||!blocking)//Don't recharge if in use, to avoid flickering
		{
			if(getUpgrades(stack).contains("flash_cooldown", NBT.TAG_INT)&&this.extractEnergy(stack, 10, true)==10)
			{
				this.extractEnergy(stack, 20, false);
				int cooldown = getUpgrades(stack).getInt("flash_cooldown");
				if(--cooldown <= 0)
					getUpgrades(stack).remove("flash_cooldown");
				else
					getUpgrades(stack).putInt("flash_cooldown", cooldown);
			}
			if(getUpgrades(stack).contains("shock_cooldown", NBT.TAG_INT)&&this.extractEnergy(stack, 10, true)==10)
			{
				this.extractEnergy(stack, 20, false);
				int cooldown = getUpgrades(stack).getInt("shock_cooldown");
				if(--cooldown <= 0)
					getUpgrades(stack).remove("shock_cooldown");
				else
					getUpgrades(stack).putInt("shock_cooldown", cooldown);
			}
		}
	}

	@Override
	public boolean isShield(ItemStack stack, @Nullable LivingEntity entity)
	{
		return true;
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
			if(event.getSource().isProjectile()&&event.getSource().getDirectEntity()!=null)
			{
				Entity projectile = event.getSource().getDirectEntity();
				projectile.discard();
				event.setCanceled(true);
				b = true;
			}
			if(event.getSource().getEntity()!=null&&event.getSource().getEntity() instanceof LivingEntity&&event.getSource().getEntity().distanceToSqr(player) < 4)
			{
				ElectricDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(1, true);
				dmgsrc.apply(event.getSource().getEntity());
				b = true;
			}
			if(b)
			{
				getUpgrades(stack).putInt("shock_cooldown", 40);
				player.level.playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.spark,
						SoundSource.BLOCKS, 2.5F, 0.5F+Utils.RAND.nextFloat());
			}
		}
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack material)
	{
		return IETags.getTagsFor(EnumMetals.STEEL).ingot.contains(material.getItem());
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return (getUpgrades(container).getBoolean("flash")||getUpgrades(container).getBoolean("shock"))?3200: 0;
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
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.BLOCK;
	}

	@Override
	public boolean shouldRenderGroup(ItemStack object, String group)
	{
		if("flash".equals(group))
			return getUpgrades(object).getBoolean("flash");
		else if("shock".equals(group))
			return getUpgrades(object).getBoolean("shock");
		return true;
	}

	@Override
	public void handlePerspective(ItemStack Object, TransformType cameraTransformType, PoseStack mat, LivingEntity entity)
	{
		if(entity!=null&&entity.isUsingItem())
			if((entity.getUsedItemHand()==InteractionHand.MAIN_HAND)==(entity.getMainArm()==HumanoidArm.RIGHT))
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
				{
					mat.mulPose(new Quaternion(-.15F, 0, 0, false));
					mat.translate(-.25, .5, -.4375);
				}
				else if(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)
				{
					mat.mulPose(new Quaternion(0.52359F, 0, 0, false));
					mat.mulPose(new Quaternion(0, 0.78539F, 0, false));
					mat.translate(.40625, -.125, -.125);
				}
			}
			else
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND)
				{
					mat.mulPose(new Quaternion(.15F, 0, 0, false));
					mat.translate(.25, .375, .4375);
				}
				else if(cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND)
				{
					mat.mulPose(new Quaternion(-0.52359F, 1, 0, false));
					mat.translate(.1875, .3125, .75);
				}
			}
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
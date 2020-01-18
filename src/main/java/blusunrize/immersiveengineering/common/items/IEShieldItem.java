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
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class IEShieldItem extends UpgradeableToolItem implements IIEEnergyItem, IOBJModelCallback<ItemStack>
{
	public IEShieldItem()
	{
		super("shield", new Properties().maxStackSize(1).defaultMaxDamage(1024).setTEISR(() -> () -> IEOBJItemRenderer.INSTANCE), "SHIELD");
		DispenserBlock.registerDispenseBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final LazyOptional<EnergyHelper.ItemEnergyStorage> energyStorage = ApiUtils.constantOptional(
						new EnergyHelper.ItemEnergyStorage(stack)
				);
				final LazyOptional<ShaderWrapper_Item> shaders = ApiUtils.constantOptional(
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
		LazyOptional<Boolean> sameShader = wrapperOld.map(wOld->{
			LazyOptional<ShaderWrapper> wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
			return wrapperNew.map(w->ItemStack.areItemStacksEqual(wOld.getShaderItem(), w.getShaderItem()))
					.orElse(true);
		});
		if (!sameShader.orElse(true))
			return true;
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		if(this.getMaxEnergyStored(stack) > 0)
		{
			String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
			list.add(new TranslationTextComponent(Lib.DESC+"info.energyStored", stored));
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
		if(world.isRemote)
			return;

		if(ent instanceof LivingEntity)
			inHand |= ((LivingEntity)ent).getHeldItem(Hand.OFF_HAND)==stack;

		boolean blocking = ent instanceof LivingEntity&&((LivingEntity)ent).isActiveItemStackBlocking();
		if(!inHand||!blocking)//Don't recharge if in use, to avoid flickering
		{
			if(getUpgrades(stack).contains("flash_cooldown", NBT.TAG_INT)&&this.extractEnergy(stack, 20, true)==20)
			{
				this.extractEnergy(stack, 20, false);
				int cooldown = getUpgrades(stack).getInt("flash_cooldown");
				if(--cooldown <= 0)
					getUpgrades(stack).remove("flash_cooldown");
				else
					getUpgrades(stack).putInt("flash_cooldown", cooldown);
			}
			if(getUpgrades(stack).contains("shock_cooldown", NBT.TAG_INT)&&this.extractEnergy(stack, 20, true)==20)
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

	public void hitShield(ItemStack stack, PlayerEntity player, DamageSource source, float amount, LivingAttackEvent event)
	{
		if(getUpgrades(stack).getBoolean("flash")&&getUpgrades(stack).getInt("flash_cooldown") <= 0)
		{
			Vec3d look = player.getLookVec();
			//Offsets Player position by look backwards, then truncates cone at 1
			List<LivingEntity> targets = Utils.getTargetsInCone(player.getEntityWorld(), player.getPositionVector().subtract(look), player.getLookVec().scale(9), 1.57079f, .5f);
			for(LivingEntity t : targets)
				if(!player.equals(t))
				{
					t.addPotionEffect(new EffectInstance(IEPotions.flashed, 100, 1));
					if(t instanceof MobEntity)
						((MobEntity)t).setAttackTarget(null);
				}
			getUpgrades(stack).putInt("flash_cooldown", 40);
		}
		if(getUpgrades(stack).getBoolean("shock")&&getUpgrades(stack).getInt("shock_cooldown") <= 0)
		{
			boolean b = false;
			if(event.getSource().isProjectile()&&event.getSource().getImmediateSource()!=null)
			{
				Entity projectile = event.getSource().getImmediateSource();
				projectile.remove();
				event.setCanceled(true);
				b = true;
			}
			if(event.getSource().getTrueSource()!=null&&event.getSource().getTrueSource() instanceof LivingEntity&&event.getSource().getTrueSource().getDistanceSq(player) < 4)
			{
				ElectricDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(1, true);
				dmgsrc.apply(event.getSource().getTrueSource());
				b = true;
			}
			if(b)
			{
				getUpgrades(stack).putInt("shock_cooldown", 40);
				player.world.playSound(null, player.posX, player.posY, player.posZ, IESounds.spark, SoundCategory.BLOCKS, 2.5F, 0.5F+Utils.RAND.nextFloat());
			}
		}
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack material)
	{
		return Utils.isInTag(material, new ResourceLocation(ImmersiveEngineering.MODID, "ingot_steel"));
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return (getUpgrades(container).getBoolean("flash")||getUpgrades(container).getBoolean("shock"))?1600: 0;
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, @Nonnull Hand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		playerIn.setActiveHand(handIn);
		return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
	}

	@Nonnull
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.BLOCK;
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
	public Matrix4 handlePerspective(ItemStack Object, TransformType cameraTransformType, Matrix4 perspective, LivingEntity entity)
	{
		if(entity!=null&&entity.isHandActive())
			if((entity.getActiveHand()==Hand.MAIN_HAND)==(entity.getPrimaryHand()==HandSide.RIGHT))
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
					perspective.rotate(-.15, 1, 0, 0).translate(-.25, .5, -.4375);
				else if(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)
					perspective.rotate(0.52359, 1, 0, 0).rotate(0.78539, 0, 1, 0).translate(.40625, -.125, -.125);
			}
			else
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND)
					perspective.rotate(.15, 1, 0, 0).translate(.25, .375, .4375);
				else if(cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND)
					perspective.rotate(-0.52359, 1, 0, 0).rotate(0.78539, 0, 1, 0).translate(.1875, .3125, .5625);
			}
		return perspective;
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, Supplier<World> getWorld)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(RuntimeException::new);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv, 0, 80, 32, "SHIELD", stack, true, getWorld),
						new IESlot.Upgrades(container, inv, 1, 100, 32, "SHIELD", stack, true, getWorld)
//						new IESlot.Upgrades(container, invItem,2,100,32, "SHIELD", stack, true)
				};

	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 2;
	}

	@Override
	public boolean hasEffect(ItemStack stack)
	{
		return false;//Remove glint effect since it doesn't work that well with models, see #2944
	}
}
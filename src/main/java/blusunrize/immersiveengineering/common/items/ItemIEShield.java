/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.*;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemIEShield extends ItemUpgradeableTool implements IIEEnergyItem, IOBJModelCallback<ItemStack>
{
	public ItemIEShield()
	{
		super("shield", 1, "SHIELD");
		this.setMaxDamage(1024);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, ItemArmor.DISPENSER_BEHAVIOR);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				final EnergyHelper.ItemEnergyStorage energyStorage = new EnergyHelper.ItemEnergyStorage(stack);
				final ShaderWrapper_Item shaders = new ShaderWrapper_Item("immersiveengineering:shield", stack);

				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
				{
					return capability==CapabilityEnergy.ENERGY||
							capability==CapabilityShader.SHADER_CAPABILITY||
							super.hasCapability(capability, facing);
				}

				@Override
				public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
				{
					if(capability==CapabilityEnergy.ENERGY)
						return (T)energyStorage;
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return (T)shaders;
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
		if(oldStack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null)&&newStack.hasCapability(CapabilityShader.SHADER_CAPABILITY, null))
		{
			ShaderWrapper wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			ShaderWrapper wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY, null);
			if(!ItemStack.areItemStacksEqual(wrapperOld.getShaderItem(), wrapperNew.getShaderItem()))
				return true;
		}
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(this.getMaxEnergyStored(stack) > 0)
		{
			String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
			list.add(I18n.format(Lib.DESC+"info.energyStored", stored));
		}
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		super.onUpdate(stack, world, ent, slot, inHand);
		if(world.isRemote)
			return;

		if(ent instanceof EntityLivingBase)
			inHand |= ((EntityLivingBase)ent).getHeldItem(EnumHand.OFF_HAND)==stack;

		boolean blocking = ent instanceof EntityLivingBase&&((EntityLivingBase)ent).isActiveItemStackBlocking();
		if(!inHand||!blocking)//Don't recharge if in use, to avoid flickering
		{
			if(getUpgrades(stack).hasKey("flash_cooldown")&&this.extractEnergy(stack, 20, true)==20)
			{
				this.extractEnergy(stack, 20, false);
				int cooldown = getUpgrades(stack).getInteger("flash_cooldown");
				if(--cooldown <= 0)
					getUpgrades(stack).removeTag("flash_cooldown");
				else
					getUpgrades(stack).setInteger("flash_cooldown", cooldown);
			}
			if(getUpgrades(stack).hasKey("shock_cooldown")&&this.extractEnergy(stack, 20, true)==20)
			{
				this.extractEnergy(stack, 20, false);
				int cooldown = getUpgrades(stack).getInteger("shock_cooldown");
				if(--cooldown <= 0)
					getUpgrades(stack).removeTag("shock_cooldown");
				else
					getUpgrades(stack).setInteger("shock_cooldown", cooldown);
			}
		}
	}

	@Override
	public boolean isShield(ItemStack stack, @Nullable EntityLivingBase entity)
	{
		return true;
	}

	public void hitShield(ItemStack stack, EntityPlayer player, DamageSource source, float amount, LivingAttackEvent event)
	{
		if(getUpgrades(stack).getBoolean("flash")&&getUpgrades(stack).getInteger("flash_cooldown") <= 0)
		{
			Vec3d look = player.getLookVec();
			//Offsets Player position by look backwards, then truncates cone at 1
			List<EntityLivingBase> targets = Utils.getTargetsInCone(player.getEntityWorld(), player.getPositionVector().subtract(look), player.getLookVec().scale(9), 1.57079f, .5f);
			for(EntityLivingBase t : targets)
				if(!player.equals(t))
				{
					t.addPotionEffect(new PotionEffect(IEPotions.flashed, 100, 1));
					if(t instanceof EntityLiving)
						((EntityLiving)t).setAttackTarget(null);
				}
			getUpgrades(stack).setInteger("flash_cooldown", 40);
		}
		if(getUpgrades(stack).getBoolean("shock")&&getUpgrades(stack).getInteger("shock_cooldown") <= 0)
		{
			boolean b = false;
			if(event.getSource().isProjectile()&&event.getSource().getImmediateSource()!=null)
			{
				Entity projectile = event.getSource().getImmediateSource();
				projectile.setDead();
				event.setCanceled(true);
				b = true;
			}
			if(event.getSource().getTrueSource()!=null&&event.getSource().getTrueSource() instanceof EntityLivingBase&&event.getSource().getTrueSource().getDistanceSq(player) < 4)
			{
				ElectricDamageSource dmgsrc = IEDamageSources.causeTeslaDamage(1, true);
				dmgsrc.apply(event.getSource().getTrueSource());
				b = true;
			}
			if(b)
			{
				getUpgrades(stack).setInteger("shock_cooldown", 40);
				player.world.playSound(null, player.posX, player.posY, player.posZ, IESounds.spark, SoundCategory.BLOCKS, 2.5F, 0.5F+Utils.RAND.nextFloat());
			}
		}
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack material)
	{
		return Utils.compareToOreName(material, "ingotSteel");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return (getUpgrades(container).getBoolean("flash")||getUpgrades(container).getBoolean("shock"))?1600: 0;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn)
	{
		ItemStack itemstack = playerIn.getHeldItem(handIn);
		playerIn.setActiveHand(handIn);
		return new ActionResult(EnumActionResult.SUCCESS, itemstack);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)
	{
		return EnumAction.BLOCK;
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
	public Matrix4 handlePerspective(ItemStack Object, TransformType cameraTransformType, Matrix4 perspective, EntityLivingBase entity)
	{
		if(entity!=null&&entity.isHandActive())
			if((entity.getActiveHand()==EnumHand.MAIN_HAND)==(entity.getPrimaryHand()==EnumHandSide.RIGHT))
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
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv, 0, 80, 32, "SHIELD", stack, true),
						new IESlot.Upgrades(container, inv, 1, 100, 32, "SHIELD", stack, true)
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
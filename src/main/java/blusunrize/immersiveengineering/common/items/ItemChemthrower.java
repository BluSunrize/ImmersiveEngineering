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
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.entities.EntityChemthrowerShot;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.util.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemChemthrower extends ItemUpgradeableTool implements IAdvancedFluidItem, IOBJModelCallback<ItemStack>, ITool
{
	public ItemChemthrower()
	{
		super("chemthrower", 1, "CHEMTHROWER");
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		int cap = getCapacity(stack, 2000);
		if(!getUpgrades(stack).getBoolean("multitank"))
			list.add(formatFluidStack(getFluid(stack), cap));
		else
			for(int i = 0; i < 3; i++)
				list.add((i > 0?"  ": "")+formatFluidStack(ItemNBTHelper.getFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY+(i > 0?i: "")), cap));
	}

	private String formatFluidStack(FluidStack fs, int capacity)
	{
		if(fs!=null)
		{
			TextFormatting rarity = fs.getFluid().getRarity()==EnumRarity.COMMON?TextFormatting.GRAY: fs.getFluid().getRarity().color;
			return rarity+fs.getLocalizedName()+TextFormatting.GRAY+": "+fs.amount+"/"+capacity+"mB";
		}
		else
			return I18n.format(Lib.DESC_FLAVOUR+"drill.empty");

	}

	@Override
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack p_77661_1_)
	{
		return EnumAction.NONE;
	}

	@Override
	public void removeFromWorkbench(EntityPlayer player, ItemStack stack)
	{
//		ToDo: Make an Upgrade Advancement?
//		if(contents[0]!=null&&contents[1]!=null&&contents[2]!=null&&contents[3]!=null)
//			Utils.unlockIEAdvancement(player, "upgrade_chemthrower");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking())
		{
			if(!world.isRemote)
				ItemNBTHelper.setBoolean(stack, "ignite", !ItemNBTHelper.getBoolean(stack, "ignite"));
		}
		else
			player.setActiveHand(hand);
		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count)
	{
		FluidStack fs = this.getFluid(stack);
		if(fs!=null&&fs.getFluid()!=null)
		{
			int duration = getMaxItemUseDuration(stack)-count;
			int consumed = IEConfig.Tools.chemthrower_consumption;
			if(consumed*duration <= fs.amount)
			{
				Vec3d v = player.getLookVec();
				int split = 8;
				boolean isGas = fs.getFluid().isGaseous()||ChemthrowerHandler.isGas(fs.getFluid());

				float scatter = isGas?.15f: .05f;
				float range = isGas?.5f: 1f;
				if(getUpgrades(stack).getBoolean("focus"))
				{
					range += .25f;
					scatter -= .025f;
				}

				boolean ignite = ChemthrowerHandler.isFlammable(fs.getFluid())&&ItemNBTHelper.getBoolean(stack, "ignite");
				for(int i = 0; i < split; i++)
				{
					Vec3d vecDir = v.add(player.getRNG().nextGaussian()*scatter, player.getRNG().nextGaussian()*scatter, player.getRNG().nextGaussian()*scatter);
					EntityChemthrowerShot chem = new EntityChemthrowerShot(player.world, player, vecDir.x*0.25, vecDir.y*0.25, vecDir.z*0.25, fs);

					// Apply momentum from the player.
					chem.motionX = player.motionX+vecDir.x*range;
					chem.motionY = player.motionY+vecDir.y*range;
					chem.motionZ = player.motionZ+vecDir.z*range;

					// Apply a small amount of backforce.
					if(!player.onGround)
					{
						player.motionX -= vecDir.x*0.0025*range;
						player.motionY -= vecDir.y*0.0025*range;
						player.motionZ -= vecDir.z*0.0025*range;
					}
					if(ignite)
						chem.setFire(10);
					if(!player.world.isRemote)
						player.world.spawnEntity(chem);
				}
				if(count%4==0)
				{
					if(ignite)
						player.world.playSound(null, player.posX, player.posY, player.posZ, IESounds.sprayFire, SoundCategory.PLAYERS, .5f, 1.5f);
					else
						player.world.playSound(null, player.posX, player.posY, player.posZ, IESounds.spray, SoundCategory.PLAYERS, .5f, .75f);
				}
			}
			else
				player.stopActiveHand();
		}
		else
			player.stopActiveHand();
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft)
	{
		FluidStack fs = this.getFluid(stack);
		if(fs!=null)
		{
			int duration = getMaxItemUseDuration(stack)-timeLeft;
			fs.amount -= IEConfig.Tools.chemthrower_consumption*duration;
			if(fs.amount <= 0)
				ItemNBTHelper.remove(stack, FluidHandlerItemStack.FLUID_NBT_KEY);
			else
				ItemNBTHelper.setFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY, fs);
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
	}

	public void switchTank(ItemStack stack, boolean forward)
	{
		if(getUpgrades(stack).getBoolean("multitank"))
		{
			NBTTagCompound fluidTag = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY);
			NBTTagCompound fluidTag1 = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"1");
			NBTTagCompound fluidTag2 = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"2");

			if(forward)
			{
				ItemNBTHelper.setTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY, fluidTag2);
				ItemNBTHelper.setTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"1", fluidTag);
				ItemNBTHelper.setTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"2", fluidTag1);
			}
			else
			{
				ItemNBTHelper.setTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY, fluidTag1);
				ItemNBTHelper.setTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"1", fluidTag2);
				ItemNBTHelper.setTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"2", fluidTag);
			}
		}
	}

	@Override
	public void finishUpgradeRecalculation(ItemStack stack)
	{
		FluidStack fs = getFluid(stack);
		if(fs!=null&&fs.amount > getCapacity(stack, 2000))
		{
			fs.amount = getCapacity(stack, 2000);
			ItemNBTHelper.setFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY, fs);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if("base".equals(group)||"grip".equals(group)||"cage".equals(group)||"tanks".equals(group))
			return true;
		NBTTagCompound upgrades = this.getUpgrades(stack);
		if("large_tank".equals(group)&&upgrades.getInteger("capacity") > 0)
			return true;
		else if("multi_tank".equals(group)&&upgrades.getBoolean("multitank"))
			return true;
		else
			return "tank".equals(group);
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
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				IEItemFluidHandler fluids = new IEItemFluidHandler(stack, 2000);
				ShaderWrapper_Item shaders = new ShaderWrapper_Item("immersiveengineering:chemthrower", stack);

				@Override
				public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing)
				{
					return capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY||
							capability==CapabilityShader.SHADER_CAPABILITY||
							super.hasCapability(capability, facing);
				}

				@Override
				public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing)
				{
					if(capability==CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
						return (T)fluids;
					if(capability==CapabilityShader.SHADER_CAPABILITY)
						return (T)shaders;
					return super.getCapability(capability, facing);
				}

			};
		return null;
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 4;
	}

	@Override
	public int getCapacity(ItemStack stack, int baseCapacity)
	{
		return baseCapacity+getUpgrades(stack).getInteger("capacity");
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
						new IESlot.Upgrades(container, inv, 0, 80, 32, "CHEMTHROWER", stack, true),
						new IESlot.Upgrades(container, inv, 1, 100, 32, "CHEMTHROWER", stack, true)
				};
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}
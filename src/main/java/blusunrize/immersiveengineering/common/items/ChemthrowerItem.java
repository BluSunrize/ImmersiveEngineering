/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ChemthrowerItem extends UpgradeableToolItem implements IAdvancedFluidItem, IOBJModelCallback<ItemStack>, ITool
{
	public ChemthrowerItem()
	{
		super("chemthrower", new Item.Properties().setTEISR(() -> () -> IEOBJItemRenderer.INSTANCE), "CHEMTHROWER");
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		int cap = getCapacity(stack, 2000);

		int numberOfTanks = getUpgrades(stack).getBoolean("multitank")?3: 1;

		for(int i = 0; i < numberOfTanks; i++)
		{
			ITextComponent add = IEItemFluidHandler.fluidItemInfoFlavor(ItemNBTHelper.getFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY+(i > 0?i: "")), cap);
			if(i > 0)
				add.setStyle(new Style().setColor(TextFormatting.GRAY));
			list.add(add);
		}
	}

	private ITextComponent formatFluidStack(FluidStack fs, int capacity)
	{
		if(fs!=null)
		{
			FluidAttributes attr = fs.getFluid().getAttributes();
			TextFormatting rarity = attr.getRarity()==Rarity.COMMON?TextFormatting.GRAY:
					attr.getRarity().color;
			return new TranslationTextComponent(Lib.DESC_FLAVOUR+"fluidStack", attr.getDisplayName(fs),
					fs.getAmount(), capacity).setStyle(new Style().setColor(rarity));
		}
		else
			return new TranslationTextComponent(Lib.DESC_FLAVOUR+"drill.empty");

	}

	@Nonnull
	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.NONE;
	}

	@Override
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack)
	{
//		ToDo: Make an Upgrade Advancement?
//		if(contents[0]!=null&&contents[1]!=null&&contents[2]!=null&&contents[3]!=null)
//			Utils.unlockIEAdvancement(player, "upgrade_chemthrower");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking())
		{
			if(!world.isRemote)
				ItemNBTHelper.putBoolean(stack, "ignite", !ItemNBTHelper.getBoolean(stack, "ignite"));
		}
		else
			player.setActiveHand(hand);
		return new ActionResult(ActionResultType.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count)
	{
		FluidStack fs = this.getFluid(stack);
		if(!fs.isEmpty())
		{
			int duration = getUseDuration(stack)-count;
			int consumed = IEConfig.TOOLS.chemthrower_consumption.get();
			if(consumed*duration <= fs.getAmount())
			{
				Vec3d v = player.getLookVec();
				int split = 8;
				boolean isGas = fs.getFluid().getAttributes().isGaseous()||ChemthrowerHandler.isGas(fs.getFluid());

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
					ChemthrowerShotEntity chem = new ChemthrowerShotEntity(player.world, player, vecDir.x*0.25, vecDir.y*0.25, vecDir.z*0.25, fs);

					// Apply momentum from the player.
					chem.setMotion(player.getMotion().add(vecDir.scale(range)));

					// Apply a small amount of backforce.
					if(!player.onGround)
						player.setMotion(player.getMotion().subtract(vecDir.scale(0.0025*range)));
					if(ignite)
						chem.setFire(10);
					if(!player.world.isRemote)
						player.world.addEntity(chem);
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
	public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity player, int timeLeft)
	{
		FluidStack fs = this.getFluid(stack);
		if(!fs.isEmpty())
		{
			int duration = getUseDuration(stack)-timeLeft;
			fs.shrink(IEConfig.TOOLS.chemthrower_consumption.get()*duration);
			if(fs.getAmount() <= 0)
				ItemNBTHelper.remove(stack, FluidHandlerItemStack.FLUID_NBT_KEY);
			else
				ItemNBTHelper.setFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY, fs);
		}
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 72000;
	}

	public void switchTank(ItemStack stack, boolean forward)
	{
		if(getUpgrades(stack).getBoolean("multitank"))
		{
			CompoundNBT fluidTag = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY);
			CompoundNBT fluidTag1 = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"1");
			CompoundNBT fluidTag2 = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"2");

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
		if(!fs.isEmpty()&&fs.getAmount() > getCapacity(stack, 2000))
		{
			fs.setAmount(getCapacity(stack, 2000));
			ItemNBTHelper.setFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY, fs);
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if("base".equals(group)||"grip".equals(group)||"cage".equals(group)||"tanks".equals(group))
			return true;
		CompoundNBT upgrades = this.getUpgrades(stack);
		if("large_tank".equals(group)&&upgrades.getInt("capacity") > 0)
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
		LazyOptional<ShaderWrapper> wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		LazyOptional<Boolean> sameShader = wrapperOld.map(wOld -> {
			LazyOptional<ShaderWrapper> wrapperNew = newStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
			return wrapperNew.map(w -> ItemStack.areItemStacksEqual(wOld.getShaderItem(), w.getShaderItem()))
					.orElse(true);
		});
		if(!sameShader.orElse(true))
			return true;
		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				LazyOptional<IEItemFluidHandler> fluids = ApiUtils.constantOptional(new IEItemFluidHandler(stack, 2000));
				LazyOptional<ShaderWrapper_Item> shaders = ApiUtils.constantOptional(new ShaderWrapper_Item(new ResourceLocation("immersiveengineering", "chemthrower"), stack));

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
	public int getSlotCount(ItemStack stack)
	{
		return 4;
	}

	@Override
	public int getCapacity(ItemStack stack, int baseCapacity)
	{
		return baseCapacity+getUpgrades(stack).getInt("capacity");
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, Supplier<World> getWorld)
	{
		LazyOptional<IItemHandler> inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv.orElseThrow(RuntimeException::new), 0, 80, 32, "CHEMTHROWER", stack, true, getWorld),
						new IESlot.Upgrades(container, inv.orElseThrow(RuntimeException::new), 1, 100, 32, "CHEMTHROWER", stack, true, getWorld)
				};
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}
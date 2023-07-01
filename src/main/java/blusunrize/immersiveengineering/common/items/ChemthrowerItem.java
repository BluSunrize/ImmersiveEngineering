/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.ChemthrowerShotEntity;
import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IAdvancedFluidItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChemthrowerItem extends UpgradeableToolItem implements IAdvancedFluidItem, IScrollwheel
{
	private static final int CAPACITY = 2*FluidType.BUCKET_VOLUME;

	public ChemthrowerItem()
	{
		super(new Properties().stacksTo(1), "CHEMTHROWER");
	}

	@Override
	public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer)
	{
		super.initializeClient(consumer);
		consumer.accept(ItemCallback.USE_IEOBJ_RENDER);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		int cap = getCapacity(stack, CAPACITY);

		int numberOfTanks = getUpgrades(stack).getBoolean("multitank")?3: 1;

		for(int i = 0; i < numberOfTanks; i++)
		{
			Component add = IEItemFluidHandler.fluidItemInfoFlavor(ItemNBTHelper.getFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY+(i > 0?i: "")), cap);
			if(i > 0)
				TextUtils.applyFormat(
						add,
						ChatFormatting.GRAY
				);
			list.add(add);
		}
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.NONE;
	}

	@Override
	public void removeFromWorkbench(Player player, ItemStack stack)
	{
//		ToDo: Make an Upgrade Advancement?
//		if(contents[0]!=null&&contents[1]!=null&&contents[2]!=null&&contents[3]!=null)
//			Utils.unlockIEAdvancement(player, "upgrade_chemthrower");
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		if(player.isShiftKeyDown())
		{
			if(!world.isClientSide)
				setIgniteEnable(stack, !isIgniteEnable(stack));
		}
		else
			player.startUsingItem(hand);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	@Override
	public void onUseTick(Level level, LivingEntity player, ItemStack stack, int remainingUseDuration)
	{
		FluidStack fs = this.getFluid(stack);
		if(!fs.isEmpty())
		{
			int duration = getUseDuration(stack)-remainingUseDuration;
			int consumed = IEServerConfig.TOOLS.chemthrower_consumption.get();
			if(consumed*duration <= fs.getAmount())
			{
				Vec3 v = player.getLookAngle();
				int split = 8;
				boolean isGas = fs.getFluid().is(Tags.Fluids.GASEOUS);

				float scatter = isGas?.25f: .15f;
				float range = isGas?.5f: 1f;
				if(getUpgrades(stack).getBoolean("focus"))
				{
					range += .25f;
					scatter = .025f;
				}

				boolean ignite = ChemthrowerHandler.isFlammable(fs.getFluid())&&isIgniteEnable(stack);
				for(int i = 0; i < split; i++)
				{
					Vec3 vecDir = v.add(player.getRandom().nextGaussian()*scatter, player.getRandom().nextGaussian()*scatter, player.getRandom().nextGaussian()*scatter);
					ChemthrowerShotEntity chem = new ChemthrowerShotEntity(player.level(), player, vecDir.x*0.25, vecDir.y*0.25, vecDir.z*0.25, fs);

					// Apply momentum from the player.
					chem.setDeltaMovement(player.getDeltaMovement().add(vecDir.scale(range)));

					// Apply a small amount of backforce.
					if(!player.onGround())
						player.setDeltaMovement(player.getDeltaMovement().subtract(vecDir.scale(0.0025*range)));
					if(ignite)
						chem.setSecondsOnFire(10);
					if(!player.level().isClientSide)
						player.level().addFreshEntity(chem);
				}
				if(remainingUseDuration%4==0)
				{
					if(ignite)
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.sprayFire.get(), SoundSource.PLAYERS, .5f, 1.5f);
					else
						player.level().playSound(null, player.getX(), player.getY(), player.getZ(), IESounds.spray.get(), SoundSource.PLAYERS, .5f, .75f);
				}
			}
			else
				player.releaseUsingItem();
		}
		else
			player.releaseUsingItem();
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity player, int timeLeft)
	{
		FluidStack fs = this.getFluid(stack);
		if(!fs.isEmpty())
		{
			int duration = getUseDuration(stack)-timeLeft;
			fs.shrink(IEServerConfig.TOOLS.chemthrower_consumption.get()*duration);
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

	@Override
	public void onScrollwheel(ItemStack stack, Player playerEntity, boolean forward)
	{
		if(getUpgrades(stack).getBoolean("multitank"))
		{
			CompoundTag fluidTag = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY);
			CompoundTag fluidTag1 = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"1");
			CompoundTag fluidTag2 = ItemNBTHelper.getTagCompound(stack, FluidHandlerItemStack.FLUID_NBT_KEY+"2");

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
		if(!fs.isEmpty()&&fs.getAmount() > getCapacity(stack, CAPACITY))
		{
			fs.setAmount(getCapacity(stack, CAPACITY));
			ItemNBTHelper.setFluidStack(stack, FluidHandlerItemStack.FLUID_NBT_KEY, fs);
		}
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
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack)
			{
				LazyOptional<IEItemFluidHandler> fluids = CapabilityUtils.constantOptional(new IEItemFluidHandler(stack, CAPACITY));
				LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "chemthrower"), stack));

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
	public int getSlotCount()
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
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level level, Supplier<Player> getPlayer, IItemHandler toolInventory)
	{
		return new Slot[]{
				new IESlot.Upgrades(container, toolInventory, 0, 80, 32, "CHEMTHROWER", stack, true, level, getPlayer),
				new IESlot.Upgrades(container, toolInventory, 1, 100, 32, "CHEMTHROWER", stack, true, level, getPlayer)
		};
	}

	public static void setIgniteEnable(ItemStack chemthrower, boolean enabled)
	{
		chemthrower.getOrCreateTag().putBoolean("ignite", enabled);
	}

	public static boolean isIgniteEnable(ItemStack chemthrower)
	{
		return ItemNBTHelper.getBoolean(chemthrower, "ignite");
	}
}
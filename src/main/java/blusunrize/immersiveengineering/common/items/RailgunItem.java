/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Item;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.IRailgunProjectile;
import blusunrize.immersiveengineering.api.tool.ZoomHandler.IZoomTool;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IScrollwheel;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEEnergyItem;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.tuple.Triple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class RailgunItem extends UpgradeableToolItem implements IIEEnergyItem, IZoomTool, IScrollwheel, ITool, IOBJModelCallback<ItemStack>
{
	public RailgunItem()
	{
		super("railgun", new Properties().maxStackSize(1).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE), "RAILGUN");
	}

	@Override
	public int getSlotCount()
	{
		return 2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(Container container, ItemStack stack, Supplier<World> getWorld, Supplier<PlayerEntity> getPlayer)
	{
		IItemHandler inv = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.orElseThrow(RuntimeException::new);
		return new Slot[]
				{
						new IESlot.Upgrades(container, inv, 0, 80, 32, "RAILGUN", stack, true, getWorld, getPlayer),
						new IESlot.Upgrades(container, inv, 1, 100, 32, "RAILGUN", stack, true, getWorld, getPlayer)
				};
	}

	@Override
	public boolean canModify(ItemStack stack)
	{
		return true;
	}

	@Override
	public void recalculateUpgrades(ItemStack stack, World w, PlayerEntity player)
	{
		super.recalculateUpgrades(stack, w, player);
		if(this.getEnergyStored(stack) > this.getMaxEnergyStored(stack))
			ItemNBTHelper.putInt(stack, "energy", this.getMaxEnergyStored(stack));
	}

	@Override
	public void clearUpgrades(ItemStack stack)
	{
		super.clearUpgrades(stack);
		if(this.getEnergyStored(stack) > this.getMaxEnergyStored(stack))
			ItemNBTHelper.putInt(stack, "energy", this.getMaxEnergyStored(stack));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		if(slotChanged)
			return true;
		LazyOptional<ShaderWrapper> wrapperOld = oldStack.getCapability(CapabilityShader.SHADER_CAPABILITY);
		Optional<Boolean> sameShader = wrapperOld.map(wOld -> {
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
				final LazyOptional<EnergyHelper.ItemEnergyStorage> energyStorage = CapabilityUtils.constantOptional(
						new EnergyHelper.ItemEnergyStorage(stack)
				);
				final LazyOptional<ShaderWrapper_Item> shaders = CapabilityUtils.constantOptional(
						new ShaderWrapper_Item(new ResourceLocation(ImmersiveEngineering.MODID, "railgun"), stack)
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
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
		list.add(new TranslationTextComponent(Lib.DESC+"info.energyStored", stored));
	}

	@Nonnull
	@Override
	public String getTranslationKey(ItemStack stack)
	{
		//		if(stack.getItemDamage()!=1)
		//		{
		//			String tag = getRevolverDisplayTag(stack);
		//			if(!tag.isEmpty())
		//				return this.getTranslationKey()+"."+tag;
		//		}
		return super.getTranslationKey(stack);
	}

	@Nonnull
	@Override
	public UseAction getUseAction(ItemStack p_77661_1_)
	{
		return UseAction.NONE;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		int energy = IEServerConfig.TOOLS.railgun_consumption.get();
		float energyMod = 1+this.getUpgrades(stack).getFloat("consumption");
		energy = (int)(energy*energyMod);
		if(this.extractEnergy(stack, energy, true)==energy&&!findAmmo(stack, player).isEmpty())
		{
			player.setActiveHand(hand);
			player.world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), getChargeTime(stack) <= 20?IESounds.chargeFast: IESounds.chargeSlow, SoundCategory.PLAYERS, 1.5f, 1f);
			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}
		return new ActionResult<>(ActionResultType.PASS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity user, int count)
	{
		int inUse = this.getUseDuration(stack)-count;
		if(inUse > getChargeTime(stack)&&inUse%20==user.getRNG().nextInt(20))
		{
			user.world.playSound(null, user.getPosX(), user.getPosY(), user.getPosZ(), IESounds.spark, SoundCategory.PLAYERS, .8f+(.2f*user.getRNG().nextFloat()), .5f+(.5f*user.getRNG().nextFloat()));
			Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
			if(shader!=null)
			{
				Vector3d pos = Utils.getLivingFrontPos(user, .4375, user.getHeight()*.75, user.getActiveHand()==Hand.MAIN_HAND?user.getPrimaryHand(): user.getPrimaryHand().opposite(), false, 1);
				shader.getMiddle().getEffectFunction().execute(user.world, shader.getLeft(), stack, shader.getRight().getShaderType().toString(), pos, null, .0625f);
			}
		}
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity user, int timeLeft)
	{
		if(user instanceof PlayerEntity)
		{
			int inUse = this.getUseDuration(stack)-timeLeft;
			ItemNBTHelper.remove(stack, "inUse");
			if(inUse < getChargeTime(stack))
				return;
			int energy = IEServerConfig.TOOLS.railgun_consumption.get();
			float energyMod = 1+this.getUpgrades(stack).getFloat("consumption");
			energy = (int)(energy*energyMod);
			if(this.extractEnergy(stack, energy, true)==energy)
			{
				ItemStack ammo = findAmmo(stack, (PlayerEntity)user);
				if(!ammo.isEmpty())
				{
					ItemStack ammoConsumed = ammo.split(1);
					IRailgunProjectile projectileProperties = RailgunHandler.getProjectile(ammoConsumed);
					Vector3d vec = user.getLookVec();
					float speed = 20;
					Entity shot = new RailgunShotEntity(user.world, user, vec.x*speed, vec.y*speed, vec.z*speed, ammoConsumed);
					shot = projectileProperties.getProjectile((PlayerEntity)user, ammoConsumed, shot);
					user.world.playSound(null, user.getPosX(), user.getPosY(), user.getPosZ(), IESounds.railgunFire, SoundCategory.PLAYERS, 1, .5f+(.5f*user.getRNG().nextFloat()));
					this.extractEnergy(stack, energy, false);
					if(!world.isRemote)
						user.world.addEntity(shot);

					Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
					if(shader!=null)
					{
						Vector3d pos = Utils.getLivingFrontPos(user, .75, user.getHeight()*.75, user.getActiveHand()==Hand.MAIN_HAND?user.getPrimaryHand(): user.getPrimaryHand().opposite(), false, 1);
						shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), stack,
								shader.getRight().getShaderType().toString(), pos,
								Vector3d.fromPitchYaw(user.getPitchYaw()), .125f);
					}
				}
			}
		}
	}

	public static ItemStack findAmmo(ItemStack railgun, PlayerEntity player)
	{
		// Check for cached slot
		if(ItemNBTHelper.hasKey(railgun, "ammo_slot"))
		{
			int slot = ItemNBTHelper.getInt(railgun, "ammo_slot");
			ItemStack ammo = findAmmoInSlot(player, slot);
			if(!ammo.isEmpty())
				return ammo;
		}

		// Find it otherwise
		if(isAmmo(player.getHeldItem(Hand.OFF_HAND)))
		{
			ItemNBTHelper.putInt(railgun, "ammo_slot", 0);
			return player.getHeldItem(Hand.OFF_HAND);
		}
		else if(isAmmo(player.getHeldItem(Hand.MAIN_HAND)))
		{
			ItemNBTHelper.putInt(railgun, "ammo_slot", 1);
			return player.getHeldItem(Hand.MAIN_HAND);
		}
		else
			for(int i = 0; i < player.inventory.getSizeInventory(); i++)
			{
				ItemStack itemstack = player.inventory.getStackInSlot(i);
				if(isAmmo(itemstack))
				{
					ItemNBTHelper.putInt(railgun, "ammo_slot", 2+i);
					return itemstack;
				}
			}
		return ItemStack.EMPTY;
	}

	public static ItemStack findAmmoInSlot(PlayerEntity player, int slot)
	{
		ItemStack ammo = ItemStack.EMPTY;
		if(slot==0||slot==1)
			ammo = player.getHeldItem(slot==0?Hand.MAIN_HAND: Hand.OFF_HAND);
		else if(slot > 1&&slot-2 < player.inventory.getSizeInventory())
			ammo = player.inventory.getStackInSlot(slot-2);
		if(isAmmo(ammo))
			return ammo;
		return ItemStack.EMPTY;
	}

	public static boolean isAmmo(ItemStack stack)
	{
		if(stack.isEmpty())
			return false;
		RailgunHandler.IRailgunProjectile prop = RailgunHandler.getProjectile(stack);
		return prop!=null;
	}

	private boolean checkAmmoSlot(ItemStack stack, PlayerEntity player, int actualSlot)
	{
		if(!findAmmoInSlot(player, actualSlot).isEmpty())
		{
			ItemNBTHelper.putInt(stack, "ammo_slot", actualSlot);
			player.inventory.markDirty();
			return true;
		}
		return false;
	}

	@Override
	public void onScrollwheel(ItemStack stack, PlayerEntity player, boolean forward)
	{
		int slot = ItemNBTHelper.getInt(stack, "ammo_slot");
		int count = player.inventory.getSizeInventory()+2;
		if(forward)
		{
			for(int i = 1; i < count; i++)
				if(checkAmmoSlot(stack, player, (slot+i)%count))
					return;
		}
		else
		{
			for(int i = count-1; i >= 1; i--)
				if(checkAmmoSlot(stack, player, (slot+i)%count))
					return;
		}
	}

	public int getChargeTime(ItemStack railgun)
	{
		return (int)(40/(1+this.getUpgrades(railgun).getFloat("speed")));
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 72000;
	}

	@Override
	public void removeFromWorkbench(PlayerEntity player, ItemStack stack)
	{
		LazyOptional<IItemHandler> invCap = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		invCap.ifPresent(inv -> {
			if(!inv.getStackInSlot(0).isEmpty()&&!inv.getStackInSlot(1).isEmpty())
				Utils.unlockIEAdvancement(player, "main/upgrade_railgun");
		});
	}

	@Override
	public int getMaxEnergyStored(ItemStack container)
	{
		return 8000;
	}


	public String[] compileRender(ItemStack stack)
	{
		HashSet<String> render = new HashSet<String>();
		render.add("frame");
		render.add("barrel");
		render.add("grip");
		render.add("capacitors");
		render.add("sled");
		render.add("wires");
		CompoundNBT upgrades = this.getUpgrades(stack);
		if(upgrades.getDouble("speed") > 0)
			render.add("upgrade_speed");
		if(upgrades.getBoolean("scope"))
			render.add("upgrade_scope");
		return render.toArray(new String[render.size()]);
	}

	@Override
	public boolean canZoom(ItemStack stack, PlayerEntity player)
	{
		return this.getUpgrades(stack).getBoolean("scope");
	}

	float[] zoomSteps = new float[]{.1f, .15625f, .2f, .25f, .3125f, .4f, .5f, .625f};

	@Override
	public float[] getZoomSteps(ItemStack stack, PlayerEntity player)
	{
		return zoomSteps;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderGroup(ItemStack stack, String group)
	{
		if(group.equals("upgrade_scope"))
			return getUpgrades(stack).getBoolean("scope");
		if(group.equals("upgrade_speed"))
			return getUpgrades(stack).getDouble("speed") > 0;
		if(group.equals("barrel_top"))
			return getUpgrades(stack).getDouble("speed") <= 0;
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public TransformationMatrix applyTransformations(ItemStack stack, String group, TransformationMatrix transform)
	{
		return transform;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void handlePerspective(ItemStack stack, TransformType cameraTransformType, MatrixStack mat, LivingEntity entity)
	{
	}
}
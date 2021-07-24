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
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
		super(withIEOBJRender().stacksTo(1), "RAILGUN");
	}

	@Override
	public int getSlotCount()
	{
		return 2+1;
	}

	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Supplier<Level> getWorld, Supplier<Player> getPlayer)
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
	public void recalculateUpgrades(ItemStack stack, Level w, Player player)
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		String stored = this.getEnergyStored(stack)+"/"+this.getMaxEnergyStored(stack);
		list.add(new TranslatableComponent(Lib.DESC+"info.energyStored", stored));
	}

	@Nonnull
	@Override
	public String getDescriptionId(ItemStack stack)
	{
		//		if(stack.getItemDamage()!=1)
		//		{
		//			String tag = getRevolverDisplayTag(stack);
		//			if(!tag.isEmpty())
		//				return this.getTranslationKey()+"."+tag;
		//		}
		return super.getDescriptionId(stack);
	}

	@Nonnull
	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.NONE;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand)
	{
		ItemStack stack = player.getItemInHand(hand);
		int energy = IEServerConfig.TOOLS.railgun_consumption.get();
		float energyMod = 1+this.getUpgrades(stack).getFloat("consumption");
		energy = (int)(energy*energyMod);
		if(this.extractEnergy(stack, energy, true)==energy&&!findAmmo(stack, player).isEmpty())
		{
			player.startUsingItem(hand);
			player.level.playSound(null, player.getX(), player.getY(), player.getZ(), getChargeTime(stack) <= 20?IESounds.chargeFast: IESounds.chargeSlow, SoundSource.PLAYERS, 1.5f, 1f);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		}
		return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity user, int count)
	{
		int inUse = this.getUseDuration(stack)-count;
		if(inUse > getChargeTime(stack)&&inUse%20==user.getRandom().nextInt(20))
		{
			user.level.playSound(null, user.getX(), user.getY(), user.getZ(), IESounds.spark, SoundSource.PLAYERS, .8f+(.2f*user.getRandom().nextFloat()), .5f+(.5f*user.getRandom().nextFloat()));
			Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
			if(shader!=null)
			{
				Vec3 pos = Utils.getLivingFrontPos(user, .4375, user.getBbHeight()*.75, ItemUtils.getLivingHand(user, user.getUsedItemHand()), false, 1);
				shader.getMiddle().getEffectFunction().execute(user.level, shader.getLeft(), stack, shader.getRight().getShaderType().toString(), pos, null, .0625f);
			}
		}
	}

	@Override
	public void releaseUsing(ItemStack stack, Level world, LivingEntity user, int timeLeft)
	{
		if(user instanceof Player)
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
				ItemStack ammo = findAmmo(stack, (Player)user);
				if(!ammo.isEmpty())
				{
					ItemStack ammoConsumed = ammo.split(1);
					IRailgunProjectile projectileProperties = RailgunHandler.getProjectile(ammoConsumed);
					Vec3 vec = user.getLookAngle();
					float speed = 20;
					Entity shot = new RailgunShotEntity(user.level, user, vec.x*speed, vec.y*speed, vec.z*speed, ammoConsumed);
					shot = projectileProperties.getProjectile((Player)user, ammoConsumed, shot);
					user.level.playSound(null, user.getX(), user.getY(), user.getZ(), IESounds.railgunFire, SoundSource.PLAYERS, 1, .5f+(.5f*user.getRandom().nextFloat()));
					this.extractEnergy(stack, energy, false);
					if(!world.isClientSide)
						user.level.addFreshEntity(shot);

					Triple<ItemStack, ShaderRegistryEntry, ShaderCase> shader = ShaderRegistry.getStoredShaderAndCase(stack);
					if(shader!=null)
					{
						HumanoidArm handside = user.getMainArm();
						if(user.getUsedItemHand()!=InteractionHand.MAIN_HAND)
							handside = handside==HumanoidArm.LEFT?HumanoidArm.RIGHT: HumanoidArm.LEFT;
						Vec3 pos = Utils.getLivingFrontPos(user, .75, user.getBbHeight()*.75, handside, false, 1);
						shader.getMiddle().getEffectFunction().execute(world, shader.getLeft(), stack,
								shader.getRight().getShaderType().toString(), pos,
								Vec3.directionFromRotation(user.getRotationVector()), .125f);
					}
				}
			}
		}
	}

	public static ItemStack findAmmo(ItemStack railgun, Player player)
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
		if(isAmmo(player.getItemInHand(InteractionHand.OFF_HAND)))
		{
			ItemNBTHelper.putInt(railgun, "ammo_slot", 0);
			return player.getItemInHand(InteractionHand.OFF_HAND);
		}
		else if(isAmmo(player.getItemInHand(InteractionHand.MAIN_HAND)))
		{
			ItemNBTHelper.putInt(railgun, "ammo_slot", 1);
			return player.getItemInHand(InteractionHand.MAIN_HAND);
		}
		else
			for(int i = 0; i < player.getInventory().getContainerSize(); i++)
			{
				ItemStack itemstack = player.getInventory().getItem(i);
				if(isAmmo(itemstack))
				{
					ItemNBTHelper.putInt(railgun, "ammo_slot", 2+i);
					return itemstack;
				}
			}
		return ItemStack.EMPTY;
	}

	public static ItemStack findAmmoInSlot(Player player, int slot)
	{
		ItemStack ammo = ItemStack.EMPTY;
		if(slot==0||slot==1)
			ammo = player.getItemInHand(slot==0?InteractionHand.MAIN_HAND: InteractionHand.OFF_HAND);
		else if(slot > 1&&slot-2 < player.getInventory().getContainerSize())
			ammo = player.getInventory().getItem(slot-2);
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

	private boolean checkAmmoSlot(ItemStack stack, Player player, int actualSlot)
	{
		if(!findAmmoInSlot(player, actualSlot).isEmpty())
		{
			ItemNBTHelper.putInt(stack, "ammo_slot", actualSlot);
			player.getInventory().setChanged();
			return true;
		}
		return false;
	}

	@Override
	public void onScrollwheel(ItemStack stack, Player player, boolean forward)
	{
		int slot = ItemNBTHelper.getInt(stack, "ammo_slot");
		int count = player.getInventory().getContainerSize()+2;
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
	public void removeFromWorkbench(Player player, ItemStack stack)
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
		CompoundTag upgrades = this.getUpgrades(stack);
		if(upgrades.getDouble("speed") > 0)
			render.add("upgrade_speed");
		if(upgrades.getBoolean("scope"))
			render.add("upgrade_scope");
		return render.toArray(new String[render.size()]);
	}

	@Override
	public boolean canZoom(ItemStack stack, Player player)
	{
		return this.getUpgrades(stack).getBoolean("scope");
	}

	float[] zoomSteps = new float[]{.1f, .15625f, .2f, .25f, .3125f, .4f, .5f, .625f};

	@Override
	public float[] getZoomSteps(ItemStack stack, Player player)
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
	public Transformation applyTransformations(ItemStack stack, String group, Transformation transform)
	{
		return transform;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void handlePerspective(ItemStack stack, TransformType cameraTransformType, PoseStack mat, LivingEntity entity)
	{
	}
}
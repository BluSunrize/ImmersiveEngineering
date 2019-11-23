/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.CapabilitySkyhookData.SkyhookStatus;
import blusunrize.immersiveengineering.api.CapabilitySkyhookData.SkyhookUserData;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class SkyhookItem extends UpgradeableToolItem implements ITool
{
	public SkyhookItem()
	{
		super("skyhook", new Properties().maxStackSize(1), "SKYHOOK");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		if(shouldLimitSpeed(stack))
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"skyhook.speedLimit"));
		else
			list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"skyhook.noLimit"));
		list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"skyhook"));
	}

	private static final String LIMIT_SPEED = "limitSpeed";

	public static boolean shouldLimitSpeed(ItemStack stack)
	{
		return ItemNBTHelper.getBoolean(stack, LIMIT_SPEED);
	}

	public static void setLimitSpeed(ItemStack stack, boolean doLimit)
	{
		ItemNBTHelper.putBoolean(stack, LIMIT_SPEED, doLimit);
	}

	public static boolean toggleSpeedLimit(ItemStack stack)
	{
		CompoundNBT nbt = stack.getOrCreateTag();
		boolean wasActive = nbt.getBoolean(LIMIT_SPEED);
		nbt.putBoolean(LIMIT_SPEED, !wasActive);
		return !wasActive;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		super.inventoryTick(stack, world, ent, slot, inHand);
		if(getUpgrades(stack).getBoolean("fallBoost"))
		{
			float dmg = (float)Math.ceil(ent.fallDistance/5);
			ItemNBTHelper.putFloat(stack, "fallDamageBoost", dmg);
		}
	}
	/*@Override
	public Multimap getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack)
	{
		Multimap multimap = super.getAttributeModifiers(slot, stack);
		if(slot == EntityEquipmentSlot.MAINHAND)
		{
			float dmg = 5 + ItemNBTHelper.getFloat(stack, "fallDamageBoost");
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", dmg, 0));
		}
		return multimap;
	}*/


	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand)
	{

		ItemStack stack = player.getHeldItem(hand);
		if(player.getCooldownTracker().hasCooldown(this))
			return new ActionResult<>(ActionResultType.PASS, stack);
		if(player.isSneaking())
		{
			boolean limitSpeed = toggleSpeedLimit(stack);
			if(limitSpeed)
				player.sendStatusMessage(new TranslationTextComponent("chat.immersiveengineering.info.skyhookLimited"), true);
			else
				player.sendStatusMessage(new TranslationTextComponent("chat.immersiveengineering.info.skyhookUnlimited"), true);
		}
		else
		{
			SkyhookUserData data = player.getCapability(SKYHOOK_USER_DATA, Direction.UP).orElseThrow(RuntimeException::new);
			if(data.hook!=null&&!world.isRemote)
			{
				data.dismount();
				IELogger.logger.info("Player left voluntarily");
			}
			else
			{
				data.startHolding();
				player.setActiveHand(hand);
			}
		}
		return new ActionResult<>(ActionResultType.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count)
	{
		super.onUsingTick(stack, player, count);
		SkyhookUserData data = player.getCapability(SKYHOOK_USER_DATA, Direction.UP).orElseThrow(RuntimeException::new);
		if(data.getStatus()!=SkyhookStatus.HOLDING_CONNECTING)
			return;
		World world = player.world;
		Connection con = ApiUtils.getConnectionMovedThrough(world, player);
		if(con!=null)
			SkylineHelper.spawnHook(player, con, player.getActiveHand(), shouldLimitSpeed(stack));
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity player, int timeLeft)
	{
		super.onPlayerStoppedUsing(stack, worldIn, player, timeLeft);
		if(!worldIn.isRemote)
			player.getCapability(SKYHOOK_USER_DATA, Direction.UP).ifPresent(SkyhookUserData::release);
	}

	public float getSkylineSpeed(ItemStack stack)
	{
		return 3f+this.getUpgrades(stack).getFloat("speed");
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 72000;
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
						new IESlot.Upgrades(container, inv, 0, 102, 42, "SKYHOOK", stack, true, getWorld),
						new IESlot.Upgrades(container, inv, 1, 102, 22, "SKYHOOK", stack, true, getWorld),
				};
	}

	@Override
	public int getSlotCount(ItemStack stack)
	{
		return 2;
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}
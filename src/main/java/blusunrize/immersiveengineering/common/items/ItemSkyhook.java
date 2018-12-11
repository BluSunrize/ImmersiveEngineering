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
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.SkylineHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

import static blusunrize.immersiveengineering.api.CapabilitySkyhookData.SKYHOOK_USER_DATA;

public class ItemSkyhook extends ItemUpgradeableTool implements ITool
{
	public ItemSkyhook()
	{
		super("skyhook", 1, "SKYHOOK");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(shouldLimitSpeed(stack))
			list.add(I18n.format(Lib.DESC_FLAVOUR+"skyhook.speedLimit"));
		else
			list.add(I18n.format(Lib.DESC_FLAVOUR+"skyhook.noLimit"));
		list.add(I18n.format(Lib.DESC_FLAVOUR+"skyhook"));
	}

	private static final String LIMIT_SPEED = "limitSpeed";

	public static boolean shouldLimitSpeed(ItemStack stack)
	{
		return ItemNBTHelper.getBoolean(stack, LIMIT_SPEED);
	}

	public static void setLimitSpeed(ItemStack stack, boolean doLimit)
	{
		ItemNBTHelper.setBoolean(stack, LIMIT_SPEED, doLimit);
	}

	public static boolean toggleSpeedLimit(ItemStack stack)
	{
		NBTTagCompound nbt = ItemNBTHelper.getTag(stack);
		boolean wasActive = nbt.getBoolean(LIMIT_SPEED);
		nbt.setBoolean(LIMIT_SPEED, !wasActive);
		return !wasActive;
	}


	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean inHand)
	{
		super.onUpdate(stack, world, ent, slot, inHand);
		if(getUpgrades(stack).getBoolean("fallBoost"))
		{
			float dmg = (float)Math.ceil(ent.fallDistance/5);
			ItemNBTHelper.setFloat(stack, "fallDamageBoost", dmg);
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
	{

		ItemStack stack = player.getHeldItem(hand);
		if(player.getCooldownTracker().hasCooldown(this))
			return new ActionResult<>(EnumActionResult.PASS, stack);
		if(player.isSneaking())
		{
			boolean limitSpeed = toggleSpeedLimit(stack);
			if(limitSpeed)
				player.sendStatusMessage(new TextComponentTranslation("chat.immersiveengineering.info.skyhookLimited"), true);
			else
				player.sendStatusMessage(new TextComponentTranslation("chat.immersiveengineering.info.skyhookUnlimited"), true);
		}
		else
		{
			SkyhookUserData data = player.getCapability(SKYHOOK_USER_DATA, EnumFacing.UP);
			assert data!=null;
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
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count)
	{
		super.onUsingTick(stack, player, count);
		SkyhookUserData data = player.getCapability(SKYHOOK_USER_DATA, EnumFacing.UP);
		assert data!=null;
		if(data.getStatus()!=SkyhookStatus.HOLDING_CONNECTING)
			return;
		World world = player.world;
		TileEntity connector = null;
		Connection line = null;
		Connection con = ApiUtils.getConnectionMovedThrough(world, player);
		if(con!=null)
		{
			connector = world.getTileEntity(con.start);
			line = con;
		}
		if(line!=null&&connector!=null)
			SkylineHelper.spawnHook(player, connector, line, player.getActiveHand(), shouldLimitSpeed(stack));
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase player, int timeLeft)
	{
		super.onPlayerStoppedUsing(stack, worldIn, player, timeLeft);
		if(!worldIn.isRemote)
		{
			Objects.requireNonNull(player.getCapability(SKYHOOK_USER_DATA, EnumFacing.UP))
					.release();
		}
	}

	public float getSkylineSpeed(ItemStack stack)
	{
		return 3f+this.getUpgrades(stack).getFloat("speed");
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack)
	{
		return 72000;
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
						new IESlot.Upgrades(container, inv, 0, 102, 42, "SKYHOOK", stack, true),
						new IESlot.Upgrades(container, inv, 1, 102, 22, "SKYHOOK", stack, true),
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
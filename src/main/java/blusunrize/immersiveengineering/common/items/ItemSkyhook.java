/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.entities.EntitySkylineHook;
import blusunrize.immersiveengineering.common.gui.IESlot;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class ItemSkyhook extends ItemUpgradeableTool implements ITool
{
	public ItemSkyhook()
	{
		super("skyhook", 1, "SKYHOOK");
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		list.add(I18n.format(Lib.DESC_FLAVOUR+"skyhook"));
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

	public static HashMap<String, EntitySkylineHook> existingHooks = new HashMap<String, EntitySkylineHook>();

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		TileEntity connector = null;
		Connection line = null;
		Connection con = SkylineHelper.getTargetConnection(world, player, null);
		if (con != null)
		{
			connector = world.getTileEntity(con.start);
			line = con;
		}
		ItemStack stack = player.getHeldItem(hand);
		if (line != null && connector != null)
		{
			SkylineHelper.spawnHook(player, connector, line);
			player.setActiveHand(hand);
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS, stack);
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
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int ticks)
	{
		if(existingHooks.containsKey(player.getName()))
		{
			EntitySkylineHook hook = existingHooks.get(player.getName());
			//			player.motionX = hook.motionX;
			//			player.motionY = hook.motionY;
			//			player.motionZ = hook.motionZ;
			//			IELogger.debug("player motion: "+player.motionX+","+player.motionY+","+player.motionZ);
			hook.setDead();
			existingHooks.remove(player.getName());
		}
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
package blusunrize.immersiveengineering.common.util;

import java.util.HashMap;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxContainerItem;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.entities.EntityGrapplingHook;
import blusunrize.immersiveengineering.common.items.ItemManeuverGear;
import blusunrize.immersiveengineering.common.util.compat.BaublesHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.oredict.OreDictionary;

public class ManeuverGearHelper
{
	public static enum ManeuverGearOperator
	{
		PRESS_0,
		PRESS_1,
		RELEASE_0,
		RELEASE_1,
		RETRACT_0,
		RETRACT_1,
		RETRACT_ALL,
		PRESS_SPACE;
	}
	public static enum HookMode
	{
		LAUNCHING,
		REELING,
		RETURNING;
	}

	private static HashMap<String, EntityGrapplingHook[]> hookMap = new HashMap();
	public static EntityGrapplingHook[] getHooks(EntityPlayer player)
	{
		EntityGrapplingHook[] hooks = hookMap.get(player.getName());
		if(hooks==null || hooks.length<2)
		{
			hooks = new EntityGrapplingHook[2];
			hookMap.put(player.getName(), hooks);
		}
		return hooks;
	}

	public static void pressHookButton(EntityPlayer player, int hook)
	{
		EntityGrapplingHook[] hooks = getHooks(player);
		if((hooks[hook]==null || hooks[hook].isDead) && doHookRFCheck(player, true))
		{
			doHookRFCheck(player, false);
			Vec3 vec = player.getLookVec();
			EntityGrapplingHook entityHook = new EntityGrapplingHook(player.worldObj, player, vec.xCoord,vec.yCoord,vec.zCoord);
			entityHook.setHookNr(hook);
			entityHook.setHookMode(HookMode.LAUNCHING);
			entityHook.setHookNrSynced();
			entityHook.setHookModeSynced();
			hooks[hook] = entityHook;
			if(!player.worldObj.isRemote)
				player.worldObj.spawnEntityInWorld(entityHook);
		}
		else if(hooks[hook]!=null && !hooks[hook].isDead && hooks[hook].getHookMode()!=HookMode.RETURNING && hooks[hook].inGround)
		{
			hooks[hook].setHookMode(HookMode.REELING);
			hooks[hook].setHookModeSynced();
		}
	}

	public static void releaseHookButton(EntityPlayer player, int hook)
	{
		EntityGrapplingHook[] hooks = getHooks(player);
		if(hooks[hook]!=null && !hooks[hook].isDead && hooks[hook].getHookMode()!=HookMode.RETURNING && hooks[hook].inGround)
		{
			hooks[hook].setHookMode(HookMode.LAUNCHING);
			hooks[hook].setHookModeSynced();
		}
	}

	public static void returnHook(EntityPlayer player, int hook)
	{
		EntityGrapplingHook[] hooks = getHooks(player);
		if(hooks[hook]!=null)
		{
			hooks[hook].setHookMode(HookMode.RETURNING);
			hooks[hook].noClip = true;
			hooks[hook].setHookModeSynced();
		}
	}

	public static void doGasJump(EntityPlayer player)
	{
		if(doGasJumpCheck(player, true))
		{
			doGasJumpCheck(player, false);
			player.setSprinting(true);
			float increase = .4f;
			if (increase > 0.56f)
				increase = 0.56f;
			float speed = 2f;
			//			if(speed > 0.925f)
			//				speed = 0.925f;
			Vec3 vec = player.getLookVec();
			player.motionX = vec.xCoord*speed;
			player.motionY = vec.yCoord*.375*speed;
			player.motionZ = vec.zCoord*speed;
			player.motionY += increase;
			//			player.motionX = (double) (-MathHelper.sin(player.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float) Math.PI) * speed);
			//			player.motionZ = (double) (MathHelper.cos(player.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(player.rotationPitch / 180.0F * (float) Math.PI) * speed);
		}
	}

	static ItemStack gearStack;
	public static boolean isPlayerWearing3DMG(EntityPlayer player)
	{
		return getPlayer3DMG(player)!=null;
	}
	public static ItemStack getPlayer3DMG(EntityPlayer player)
	{
		if(gearStack==null)
			gearStack = new ItemStack(IEContent.itemManeuverGear,1,OreDictionary.WILDCARD_VALUE);
		if(Lib.BAUBLES)
		{
			ItemStack belt = BaublesHelper.getBauble(player, 3);
			if(OreDictionary.itemMatches(gearStack, belt, false))
				return belt;
		}
		ItemStack leggings = player.getCurrentArmor(1);
		if(OreDictionary.itemMatches(gearStack, leggings, false))
			return leggings;
		return null;
	}
	public static void updatePlayer3DMG(EntityPlayer player, ItemStack gear)
	{
		if(gearStack==null)
			gearStack = new ItemStack(IEContent.itemManeuverGear,1,OreDictionary.WILDCARD_VALUE);
		if(Lib.BAUBLES)
		{
			ItemStack belt = BaublesHelper.getBauble(player, 3);
			if(OreDictionary.itemMatches(gearStack, belt, false))
				BaublesHelper.setBauble(player, 3, gear);
		}
		ItemStack leggings = player.getCurrentArmor(1);
		if(OreDictionary.itemMatches(gearStack, leggings, false))
			player.setCurrentItemOrArmor(2, gear);
	}
	public static boolean doHookRFCheck(EntityPlayer player, boolean simulate)
	{
		ItemStack gear = getPlayer3DMG(player);
		if(gear==null)
			return false;
		return ((IFluxContainerItem)gear.getItem()).extractEnergy(gear, ItemManeuverGear.hookCost, simulate)==ItemManeuverGear.hookCost;
	}
	public static boolean doGasJumpCheck(EntityPlayer player, boolean simulate)
	{
		ItemStack gear = getPlayer3DMG(player);
		if(gear==null)
			return false;
		float gas = ItemNBTHelper.getFloat(gear, "gas");
		if(gas<ItemManeuverGear.jumpCost)
			return false;
		if(!simulate)
		{
			gas -= ItemManeuverGear.jumpCost;
			ItemNBTHelper.setFloat(gear, "gas", gas);
			ItemNBTHelper.setInt(gear, "cooldown", ItemManeuverGear.rechargeCooldown);
			updatePlayer3DMG(player, gear);
		}
		return true;
	}
}
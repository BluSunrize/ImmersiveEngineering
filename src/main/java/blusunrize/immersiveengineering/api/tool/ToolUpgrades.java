/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public enum ToolUpgrades
{
	DRILL_WATERPROOF(ImmutableSet.of("DRILL"), (upgrade, modifications)-> modifications.setBoolean("waterproof", true)),
	DRILL_LUBE(ImmutableSet.of("DRILL"), (upgrade, modifications)-> modifications.setBoolean("oiled", true)),
	DRILL_DAMAGE(ImmutableSet.of("DRILL"), 3, (upgrade, modifications)-> {
		ItemNBTHelper.modifyFloat(modifications, "speed", upgrade.getCount()*2f);
		ItemNBTHelper.modifyInt(modifications, "damage", upgrade.getCount());
	}),
	DRILL_CAPACITY(ImmutableSet.of("DRILL","CHEMTHROWER"), 1, (target, upgrade)->!((IUpgradeableTool)target.getItem()).getUpgrades(target).hasKey("multitank"), (upgrade, modifications)-> ItemNBTHelper.modifyInt(modifications, "capacity", 2000)),
	REVOLVER_BAYONET(ImmutableSet.of("REVOLVER"), (upgrade, modifications)-> ItemNBTHelper.modifyFloat(modifications, "melee", 6f)),
	REVOLVER_MAGAZINE(ImmutableSet.of("REVOLVER"), 1, (target, upgrade)->!((IUpgradeableTool)target.getItem()).getUpgrades(target).hasKey("bullets"), (upgrade, modifications)-> ItemNBTHelper.modifyInt(modifications, "bullets", 6)),
	REVOLVER_ELECTRO(ImmutableSet.of("REVOLVER"), (upgrade, modifications)-> modifications.setBoolean("electro",true)),
	CHEMTHROWER_FOCUS(ImmutableSet.of("CHEMTHROWER"), (upgrade, modifications)-> modifications.setBoolean("focus",true)),
	RAILGUN_SCOPE(ImmutableSet.of("RAILGUN"), (upgrade, modifications)-> modifications.setBoolean("scope",true)),
	RAILGUN_CAPACITORS(ImmutableSet.of("RAILGUN"), (upgrade, modifications)-> modifications.setFloat("speed",1f)),
	SHIELD_FLASH(ImmutableSet.of("SHIELD"), (upgrade, modifications)-> modifications.setBoolean("flash",true)),
	SHIELD_SHOCK(ImmutableSet.of("SHIELD"), (upgrade, modifications)-> modifications.setBoolean("shock",true)),
	SHIELD_MAGNET(ImmutableSet.of("SHIELD"), (upgrade, modifications)-> modifications.setBoolean("magnet",true)),
	CHEMTHROWER_MULTITANK(ImmutableSet.of("CHEMTHROWER"), 1, (target, upgrade)->!((IUpgradeableTool)target.getItem()).getUpgrades(target).hasKey("capacity"), (upgrade, modifications)-> modifications.setBoolean("multitank",true));

	public ImmutableSet<String> toolset;
	public int stackSize=1;
	public BiPredicate<ItemStack, ItemStack> applyCheck;
	public BiConsumer<ItemStack, NBTTagCompound> function;
	ToolUpgrades(ImmutableSet<String> toolset, BiConsumer<ItemStack, NBTTagCompound> function)
	{
		this(toolset, 1, function);
	}
	ToolUpgrades(ImmutableSet<String> toolset, int stackSize, BiConsumer<ItemStack, NBTTagCompound> function)
	{
		this(toolset, stackSize, null, function);
	}
	ToolUpgrades(ImmutableSet<String> toolset, int stackSize, BiPredicate<ItemStack, ItemStack> applyCheck, BiConsumer<ItemStack, NBTTagCompound> function)
	{
		this.toolset = toolset;
		this.stackSize = stackSize;
		this.applyCheck = applyCheck;
		this.function = function;
	}

	public static String[] parse()
	{
		String[] ret = new String[values().length];
		for(int i=0; i<ret.length; i++)
			ret[i] = values()[i].toString().toLowerCase(Locale.US);
		return ret;
	}
	public static ToolUpgrades get(int meta)
	{
		if(meta>=0&&meta<values().length)
			return values()[meta];
		return DRILL_WATERPROOF;
	}
}
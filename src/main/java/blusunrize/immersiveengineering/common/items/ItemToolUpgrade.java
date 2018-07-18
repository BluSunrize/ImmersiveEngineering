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
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class ItemToolUpgrade extends ItemIEBase implements IUpgrade
{

	public enum ToolUpgrades
	{
		DRILL_WATERPROOF(ImmutableSet.of("DRILL"), (upgrade, modifications) -> modifications.setBoolean("waterproof", true)),
		DRILL_LUBE(ImmutableSet.of("DRILL"), (upgrade, modifications) -> modifications.setBoolean("oiled", true)),
		DRILL_DAMAGE(ImmutableSet.of("DRILL"), 3, (upgrade, modifications) -> {
			ItemNBTHelper.modifyFloat(modifications, "speed", upgrade.getCount()*2f);
			ItemNBTHelper.modifyInt(modifications, "damage", upgrade.getCount());
		}),
		DRILL_CAPACITY(ImmutableSet.of("DRILL", "CHEMTHROWER"), 1, (target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).hasKey("multitank"), (upgrade, modifications) -> ItemNBTHelper.modifyInt(modifications, "capacity", 2000)),
		REVOLVER_BAYONET(ImmutableSet.of("REVOLVER"), (upgrade, modifications) -> ItemNBTHelper.modifyFloat(modifications, "melee", 6f)),
		REVOLVER_MAGAZINE(ImmutableSet.of("REVOLVER"), 1, (target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).hasKey("bullets"), (upgrade, modifications) -> ItemNBTHelper.modifyInt(modifications, "bullets", 6)),
		REVOLVER_ELECTRO(ImmutableSet.of("REVOLVER"), (upgrade, modifications) -> modifications.setBoolean("electro", true)),
		CHEMTHROWER_FOCUS(ImmutableSet.of("CHEMTHROWER"), (upgrade, modifications) -> modifications.setBoolean("focus", true)),
		RAILGUN_SCOPE(ImmutableSet.of("RAILGUN"), (upgrade, modifications) -> modifications.setBoolean("scope", true)),
		RAILGUN_CAPACITORS(ImmutableSet.of("RAILGUN"), (upgrade, modifications) -> modifications.setFloat("speed", 1f)),
		SHIELD_FLASH(ImmutableSet.of("SHIELD"), (upgrade, modifications) -> modifications.setBoolean("flash", true)),
		SHIELD_SHOCK(ImmutableSet.of("SHIELD"), (upgrade, modifications) -> modifications.setBoolean("shock", true)),
		SHIELD_MAGNET(ImmutableSet.of("SHIELD"), (upgrade, modifications) -> modifications.setBoolean("magnet", true)),
		CHEMTHROWER_MULTITANK(ImmutableSet.of("CHEMTHROWER"), 1, (target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).hasKey("capacity"), (upgrade, modifications) -> modifications.setBoolean("multitank", true));

		private ImmutableSet<String> toolset;
		private int stackSize = 1;
		private BiPredicate<ItemStack, ItemStack> applyCheck;
		private BiConsumer<ItemStack, NBTTagCompound> function;

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

		static String[] parse()
		{
			String[] ret = new String[values().length];
			for(int i = 0; i < ret.length; i++)
				ret[i] = values()[i].toString().toLowerCase(Locale.US);
			return ret;
		}

		static ToolUpgrades get(int meta)
		{
			if(meta >= 0&&meta < values().length)
				return values()[meta];
			return DRILL_WATERPROOF;
		}
	}

	public ItemToolUpgrade()
	{
		super("toolupgrade", 1, ToolUpgrades.parse());
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(stack.getItemDamage() < getSubNames().length)
		{
			String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(I18n.format(Lib.DESC_FLAVOUR+"toolupgrade."+this.getSubNames()[stack.getItemDamage()]), 200);
			for(String s : flavour)
				list.add(s);
		}
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return ToolUpgrades.get(stack.getMetadata()).stackSize;
	}

	@Override
	public Set<String> getUpgradeTypes(ItemStack upgrade)
	{
		return ToolUpgrades.get(upgrade.getMetadata()).toolset;
	}

	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade)
	{
		BiPredicate<ItemStack, ItemStack> check = ToolUpgrades.get(upgrade.getMetadata()).applyCheck;
		if(check!=null&&target.getItem() instanceof IUpgradeableTool)
			return check.test(target, upgrade);
		return true;
	}

	@Override
	public void applyUpgrades(ItemStack target, ItemStack upgrade, NBTTagCompound modifications)
	{
		ToolUpgrades.get(upgrade.getMetadata()).function.accept(upgrade, modifications);
	}

}

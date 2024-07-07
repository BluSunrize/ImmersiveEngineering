/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.upgrades;

import blusunrize.immersiveengineering.api.tool.upgrade.*;
import blusunrize.immersiveengineering.common.items.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public enum ToolUpgrade
{
	DRILL_WATERPROOF(ImmutableSet.of(DrillItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.WATERPROOF)),
	DRILL_LUBE(ImmutableSet.of(DrillItem.TYPE, BuzzsawItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.OILED)),
	DRILL_DAMAGE(
			ImmutableSet.of(DrillItem.TYPE),
			3,
			(upgrade, modifications) -> modifications.add(UpgradeEffect.SPEED, upgrade.getCount()*2f)
					.add(UpgradeEffect.DAMAGE, upgrade.getCount())
	),
	DRILL_FORTUNE(ImmutableSet.of(DrillItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.FORTUNE)),
	DRILL_CAPACITY(
			ImmutableSet.of(DrillItem.TYPE, ChemthrowerItem.TYPE, BuzzsawItem.TYPE),
			1,
			(target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).has(UpgradeEffect.MULTITANK),
			(upgrade, modifications) -> modifications.add(UpgradeEffect.CAPACITY, 2*FluidType.BUCKET_VOLUME)
	),
	REVOLVER_BAYONET(ImmutableSet.of(RevolverItem.TYPE), (upgrade, modifications) -> modifications.add(UpgradeEffect.MELEE, 6)),
	REVOLVER_MAGAZINE(
			ImmutableSet.of(RevolverItem.TYPE),
			1,
			(target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).has(UpgradeEffect.BULLETS),
			(upgrade, modifications) -> modifications.add(UpgradeEffect.BULLETS, 6)
	),
	REVOLVER_ELECTRO(ImmutableSet.of(RevolverItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.ELECTRO)),
	CHEMTHROWER_FOCUS(ImmutableSet.of(ChemthrowerItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.FOCUS)),
	RAILGUN_SCOPE(ImmutableSet.of(RailgunItem.TYPE, RevolverItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.SCOPE)),
	RAILGUN_CAPACITORS(ImmutableSet.of(RailgunItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.SPEED, 1f)),
	SHIELD_FLASH(ImmutableSet.of(IEShieldItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.FLASH, Cooldown.IDLE)),
	SHIELD_SHOCK(ImmutableSet.of(IEShieldItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.SHOCK, Cooldown.IDLE)),
	SHIELD_MAGNET(ImmutableSet.of(IEShieldItem.TYPE), (upgrade, modifications) -> modifications.with(UpgradeEffect.MAGNET, PrevSlot.NONE)),
	CHEMTHROWER_MULTITANK(
			ImmutableSet.of(ChemthrowerItem.TYPE),
			1,
			(target, upgrade) -> !((IUpgradeableTool)target.getItem()).getUpgrades(target).has(UpgradeEffect.CAPACITY),
			(upgrade, modifications) -> modifications.with(UpgradeEffect.MULTITANK)
	),
	BUZZSAW_SPAREBLADES(ImmutableSet.of(BuzzsawItem.TYPE), 1, (upgrade, modifications) -> modifications.with(UpgradeEffect.SPAREBLADES)),
	POWERPACK_ANTENNA(
			ImmutableSet.of(PowerpackItem.TYPE),
			1,
			(target, upgrade) -> !PowerpackItem.getUpgradesStatic(target).has(UpgradeEffect.TESLA),
			(upgrade, modifications) -> modifications.with(UpgradeEffect.ANTENNA)
	),
	POWERPACK_INDUCTION(ImmutableSet.of(PowerpackItem.TYPE), 1, (upgrade, modifications) -> modifications.with(UpgradeEffect.INDUCTION)),
	POWERPACK_TESLA(
			ImmutableSet.of(PowerpackItem.TYPE),
			1,
			(target, upgrade) -> !PowerpackItem.getUpgradesStatic(target).has(UpgradeEffect.ANTENNA),
			(upgrade, modifications) -> modifications.with(UpgradeEffect.TESLA)
	),
	POWERPACK_MAGNET(ImmutableSet.of(PowerpackItem.TYPE), 1, (upgrade, modifications) -> modifications.with(UpgradeEffect.MAGNET, PrevSlot.NONE));

	public final ImmutableSet<String> toolset;
	public final int stackSize;
	public final BiPredicate<ItemStack, UpgradeData> applyCheck;
	public final BiFunction<ItemStack, UpgradeData, UpgradeData> function;

	ToolUpgrade(ImmutableSet<String> toolset, BiFunction<ItemStack, UpgradeData, UpgradeData> function)
	{
		this(toolset, 1, function);
	}

	ToolUpgrade(ImmutableSet<String> toolset, int stackSize, BiFunction<ItemStack, UpgradeData, UpgradeData> function)
	{
		this(toolset, stackSize, null, function);
	}

	ToolUpgrade(ImmutableSet<String> toolset, int stackSize, BiPredicate<ItemStack, UpgradeData> applyCheck, BiFunction<ItemStack, UpgradeData, UpgradeData> function)
	{
		this.toolset = toolset;
		this.stackSize = stackSize;
		this.applyCheck = applyCheck;
		this.function = function;
	}
}

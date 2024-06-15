/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments.Mutable;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;

import java.util.Set;
import java.util.function.Predicate;

public class RockcutterItem extends SawbladeItem
{
	private static final Set<ToolAction> TOOL_ACTIONS = ImmutableSet.of(
			ToolActions.PICKAXE_DIG
	);
	public static final ResourceLocation TEXTURE = ImmersiveEngineering.rl("item/rockcutter_blade");

	public RockcutterItem(int maxDamage, float sawbladeSpeed, float sawbladeDamage)
	{
		super(maxDamage, sawbladeSpeed, sawbladeDamage, TEXTURE);
	}

	@Override
	public boolean canSawbladeFellTree()
	{
		return false;
	}

	@Override
	public void modifyEnchants(Mutable baseEnchants, RegistryLookup<Enchantment> lookup)
	{
		baseEnchants.set(lookup.getOrThrow(Enchantments.SILK_TOUCH), 1);
	}

	@Override
	public Predicate<BlockState> getSawbladeMaterials()
	{
		return s -> s.is(IETags.rockcutterHarvestable);
	}

	@Override
	public Set<ToolAction> getToolActions()
	{
		return TOOL_ACTIONS;
	}
}

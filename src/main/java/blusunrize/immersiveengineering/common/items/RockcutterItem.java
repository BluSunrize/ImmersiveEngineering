/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class RockcutterItem extends SawbladeItem
{
	private static final Set<Material> silktouchMaterials = ImmutableSet.of(
			Material.PLANT, Material.REPLACEABLE_PLANT, Material.GRASS,
			Material.STONE, Material.GLASS, Material.ICE, Material.ICE_SOLID
	);
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
	public void modifyEnchants(Map<Enchantment, Integer> baseEnchants)
	{
		baseEnchants.put(Enchantments.SILK_TOUCH, 1);
	}

	@Override
	public Predicate<BlockState> getSawbladeMaterials()
	{
		return s -> silktouchMaterials.contains(s.getMaterial());
	}

	@Override
	public Set<ToolAction> getToolActions()
	{
		return TOOL_ACTIONS;
	}
}

/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class GrindingDiskItem extends SawbladeItem
{
	private static final Set<ToolAction> TOOL_ACTIONS = ImmutableSet.of(
			ToolActions.PICKAXE_DIG, ToolActions.AXE_STRIP, ToolActions.AXE_SCRAPE, ToolActions.AXE_WAX_OFF, Lib.WIRECUTTER_DIG
	);
	private static final ListTag ENCHANTS = new ListTag();
	public static final ResourceLocation TEXTURE = ImmersiveEngineering.rl("item/grindingdisk_blade");

	static
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "silk_touch");
		tag.putInt("lvl", 1);
		ENCHANTS.add(tag);
	}

	public GrindingDiskItem(int maxDamage, float sawbladeSpeed, float sawbladeDamage)
	{
		super(maxDamage, sawbladeSpeed, sawbladeDamage, TEXTURE);
	}

	@Override
	public int getSawbladeDamageFromBlock(boolean effective)
	{
		return effective?1: 10;
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
		return s -> s.is(IETags.grindingDiskHarvestable);
	}

	@Override
	public Set<ToolAction> getToolActions()
	{
		return TOOL_ACTIONS;
	}
}

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
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import java.util.Set;
import java.util.function.Predicate;

public class GrindingDiskItem extends SawbladeItem
{
	private static final Set<Material> materials = ImmutableSet.of(
			Material.METAL, Material.HEAVY_METAL
	);
	private static final Set<ToolAction> TOOL_ACTIONS = ImmutableSet.of(
			ToolActions.PICKAXE_DIG, ToolActions.AXE_STRIP, ToolActions.AXE_SCRAPE, ToolActions.AXE_WAX_OFF
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
	public boolean canSawbladeFellTree()
	{
		return false;
	}

	@Override
	public ListTag getSawbladeEnchants()
	{
		return ENCHANTS.copy();
	}

	@Override
	public Predicate<BlockState> getSawbladeMaterials()
	{
		return s -> materials.contains(s.getMaterial())||s.is(IETags.wirecutterHarvestable);
	}

	@Override
	public boolean canPerformAction(ToolAction toolAction)
	{
		return TOOL_ACTIONS.contains(toolAction);
	}
}

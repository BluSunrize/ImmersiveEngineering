/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.Set;
import java.util.function.Predicate;

public class RockcutterItem extends SawbladeItem
{
	private static final Set<Material> silktouchMaterials = ImmutableSet.of(
			Material.PLANT, Material.REPLACEABLE_PLANT, Material.GRASS,
			Material.STONE, Material.GLASS, Material.ICE, Material.ICE_SOLID
	);
	private static final ListTag ENCHANTS = new ListTag();
	public static final ResourceLocation TEXTURE = ImmersiveEngineering.rl("item/rockcutter_blade");

	static
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "silk_touch");
		tag.putInt("lvl", 1);
		ENCHANTS.add(tag);
	}

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
	public ListTag getSawbladeEnchants()
	{
		return ENCHANTS.copy();
	}

	@Override
	public Predicate<BlockState> getSawbladeMaterials()
	{
		return s -> silktouchMaterials.contains(s.getMaterial());
	}
}

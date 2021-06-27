/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.material.Material;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

public class RockcutterItem extends SawbladeItem
{
	private static Material[] silktouchMaterials = {Material.PLANTS, Material.TALL_PLANTS, Material.ORGANIC,
			Material.ROCK, Material.GLASS, Material.ICE, Material.PACKED_ICE};
	private static ListNBT enchants = new ListNBT();
	public static ResourceLocation texture = new ResourceLocation("immersiveengineering:item/rockcutter_blade");

	static
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putString("id", "silk_touch");
		tag.putInt("lvl", 1);
		enchants.add(tag);
	}

	public RockcutterItem(int maxDamage, float sawbladeSpeed, float sawbladeDamage)
	{
		super(maxDamage, sawbladeSpeed, sawbladeDamage);
	}

	@Override
	public boolean canSawbladeFellTree()
	{
		return false;
	}

	@Override
	public ListNBT getSawbladeEnchants()
	{
		return enchants.copy();
	}

	@Override
	public Material[] getSawbladeMaterials()
	{
		return silktouchMaterials;
	}

	@Override
	public ResourceLocation getSawbladeTexture()
	{
		return texture;
	}
}
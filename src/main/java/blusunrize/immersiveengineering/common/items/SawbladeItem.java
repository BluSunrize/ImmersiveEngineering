/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

public class SawbladeItem extends IEBaseItem
{
	protected static Material[] validMaterials = {Material.WOOD, Material.PLANTS, Material.TALL_PLANTS, Material.BAMBOO};
	private final float sawbladeSpeed;
	private final float sawbladeDamage;

	public SawbladeItem(String name, int maxDamage, float sawbladeSpeed, float sawbladeDamage)
	{
		super(name, new Properties().defaultMaxDamage(maxDamage).setNoRepair());
		this.sawbladeSpeed = sawbladeSpeed;
		this.sawbladeDamage = sawbladeDamage;
		BuzzsawItem.sawblades.add(this);
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	public ResourceLocation getSawbladeTexture()
	{
		return null;
	}

	public float getSawbladeSpeed()
	{
		return sawbladeSpeed;
	}

	public float getSawbladeDamage()
	{
		return sawbladeDamage;
	}

	public boolean canSawbladeFellTree()
	{
		return true;
	}

	public ListNBT getSawbladeEnchants()
	{
		return null;
	}

	public Material[] getSawbladeMaterials()
	{
		return validMaterials;
	}
}
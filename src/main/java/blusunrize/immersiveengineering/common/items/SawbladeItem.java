/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Material;

public class SawbladeItem extends IEBaseItem
{
	protected static Material[] validMaterials = {Material.WOOD, Material.PLANT, Material.REPLACEABLE_PLANT, Material.BAMBOO};
	private final float sawbladeSpeed;
	private final float sawbladeDamage;

	public SawbladeItem(int maxDamage, float sawbladeSpeed, float sawbladeDamage)
	{
		super(new Properties().defaultDurability(maxDamage).setNoRepair());
		this.sawbladeSpeed = sawbladeSpeed;
		this.sawbladeDamage = sawbladeDamage;
		BuzzsawItem.SAWBLADES.add(this);
	}

	@Override
	public boolean canBeDepleted()
	{
		return true;
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
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

	public ListTag getSawbladeEnchants()
	{
		return null;
	}

	public Material[] getSawbladeMaterials()
	{
		return validMaterials;
	}
}
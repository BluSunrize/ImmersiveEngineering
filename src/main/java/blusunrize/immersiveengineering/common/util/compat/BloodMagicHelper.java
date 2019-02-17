/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemBullet.HomingBullet;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class BloodMagicHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
		BulletHandler.registerBullet("crystalwill", new HomingBullet(IEConfig.Tools.bulletDamage_Homing, new ResourceLocation("immersiveengineering:items/bullet_crystalwill")));
		BulletHandler.homingCartridges.add("crystalwill");
	}

	@Override
	public void registerRecipes()
	{
		Item crystal = Item.REGISTRY.getObject(new ResourceLocation("bloodmagic:item_demon_crystal"));
		if(crystal!=null)
			BlueprintCraftingRecipe.addRecipe("specialBullet", BulletHandler.getBulletStack("crystalwill"), new ItemStack(IEContent.itemBullet, 1, 0), Items.GUNPOWDER, crystal);
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
	}
}
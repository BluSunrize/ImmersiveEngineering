package blusunrize.immersiveengineering.common.util.compat;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.BotaniaAPI;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.util.Utils;

public class BotaniaHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		if(Config.getBoolean("hardmodeBulletRecipes"))
			BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,7), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"ingotTerrasteel");
		else
			BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,7), new ItemStack(IEContent.itemBullet,1,0),Items.gunpowder,"nuggetTerrasteel","nuggetTerrasteel");
		BlueprintCraftingRecipe.addRecipe("specialBullet", new ItemStack(IEContent.itemBullet,1,8), new ItemStack(IEContent.itemBullet,1,1),Items.gunpowder, new ItemStack(IEContent.itemBullet,4,7));
		Config.setBoolean("botaniaBullets", true);

		if(Utils.getModVersion("Botania").startsWith("r1.8"))
		{
			BotaniaAPI.blacklistBlockFromMagnet(IEContent.blockMetalDevice, BlockMetalDevices.META_conveyorBelt);
			BotaniaAPI.blacklistBlockFromMagnet(IEContent.blockMetalDevice, BlockMetalDevices.META_conveyorDropper);
		}
	}

	@Override
	public void postInit()
	{
	}
}
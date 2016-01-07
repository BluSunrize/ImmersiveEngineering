package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.IERecipes;
import blusunrize.immersiveengineering.common.util.IEPotions;
import net.minecraft.item.ItemStack;

public class TConstructHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		IERecipes.addOreDictAlloyingRecipe("ingotAluminumBrass",4, "Copper", 100,512, "dustAluminum","dustAluminum","dustAluminum");

		IERecipes.addOredictRecipe(new ItemStack(IEContent.blockClothDevice, 2,0), " F ","FTF"," S ", 'F',"fabricHemp", 'T',"torchStone", 'S',"slabTreatedWood");

		ChemthrowerHandler.registerEffect("glue", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		ChemthrowerHandler.registerEffect("slime.blue", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
		
		RailgunHandler.registerProjectileProperties(new ComparableItemStack("rodIron"), 7, 1.25).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		RailgunHandler.registerProjectileProperties(new ComparableItemStack("rodSteel"), 9, 1.25).setColourMap(new int[][]{{0xb4b4b4,0xb4b4b4,0xb4b4b4,0x7a7a7a,0x555555,0x555555}});
		RailgunHandler.registerProjectileProperties(new ComparableItemStack("ironRod"), 7, 1.25).setColourMap(new int[][]{{0xd8d8d8,0xd8d8d8,0xd8d8d8,0xa8a8a8,0x686868,0x686868}});
		RailgunHandler.registerProjectileProperties(new ComparableItemStack("steelRod"), 9, 1.25).setColourMap(new int[][]{{0xb4b4b4,0xb4b4b4,0xb4b4b4,0x7a7a7a,0x555555,0x555555}});
	}

	@Override
	public void postInit()
	{
	}

}

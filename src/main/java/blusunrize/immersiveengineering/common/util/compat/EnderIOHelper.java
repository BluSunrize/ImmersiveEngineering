package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.IERecipes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;

import java.util.function.BiConsumer;

/**
 * @author BluSunrize - 22.08.2016
 */
public class EnderIOHelper extends IECompatModule
{
	public static final String EIO_MAGNET_NBT = "EIOpuller";

	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		IERecipes.addOreDictAlloyingRecipe("ingotElectricalSteel", 1, "Iron", 400, 512, "dustCoal", "itemSilicon");
		IERecipes.addOreDictAlloyingRecipe("ingotEnergeticAlloy", 1, "Gold", 200, 512, "dustRedstone", "dustGlowstone");
		IERecipes.addOreDictAlloyingRecipe("ingotPhaseGold", 1, "EnergeticAlloy", 200, 512, Items.ENDER_PEARL);
		IERecipes.addOreDictAlloyingRecipe("ingotPhasedIron", 1, "Iron", 200, 512, Items.ENDER_PEARL);
		IERecipes.addOreDictAlloyingRecipe("ingotConductiveIron", 1, "Iron", 100, 512, "dustRedstone");
		IERecipes.addOreDictAlloyingRecipe("ingotDarkSteel", 1, "Iron", 400, 512, "dustCoal", "obsidian");
		IERecipes.addOreDictAlloyingRecipe("ingotSoularium", 1, "Gold", 200, 512, Blocks.SOUL_SAND);

		ChemthrowerHandler.registerEffect("nutrient_distillation", new ChemthrowerEffect_Potion(null, 0, Potion.getPotionFromResourceLocation("nausea"), 80, 1));
		ChemthrowerHandler.registerEffect("liquid_sunshine", new ChemthrowerEffect_Potion(null, 0, Potion.getPotionFromResourceLocation("glowing"), 200, 0));

		ConveyorHandler.registerMagnetSupression(new BiConsumer<Entity, IConveyorTile>()
		{
			@Override
			public void accept(Entity entity, IConveyorTile iConveyorTile)
			{
				if(entity instanceof EntityItem)
				{
					NBTTagCompound data = entity.getEntityData();
					long pos = ((TileEntity) iConveyorTile).getPos().toLong();
					if(!data.hasKey(EIO_MAGNET_NBT) || data.getLong(EIO_MAGNET_NBT) != pos)
						data.setLong(EIO_MAGNET_NBT, pos);
				}
			}
		}, new BiConsumer<Entity, IConveyorTile>()
		{
			@Override
			public void accept(Entity entity, IConveyorTile iConveyorTile)
			{
				if(entity instanceof EntityItem)
					entity.getEntityData().removeTag(EIO_MAGNET_NBT);
			}
		});
	}

	@Override
	public void postInit()
	{
	}
}
package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorTile;
import blusunrize.immersiveengineering.common.IERecipes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
		System.out.println("init EIO");
		IERecipes.addOreDictAlloyingRecipe("ingotElectricalSteel", 1, "Iron", 400, 512, "dustCoal", "itemSilicon");
		IERecipes.addOreDictAlloyingRecipe("ingotEnergeticAlloy", 1, "Gold", 200, 512, "dustRedstone", "dustGlowstone");
		Item itemPowder = GameRegistry.findItem("EnderIO", "itemPowderIngot");
		Object dustEnderPearl = ApiUtils.isExistingOreName("dustEnderPearl") ? "dustEnderPearl" : new ItemStack(itemPowder, 1, 5);
		IERecipes.addOreDictAlloyingRecipe("ingotPhasedGold", 1, "EnergeticAlloy", 200, 512, dustEnderPearl);
		IERecipes.addOreDictAlloyingRecipe("ingotPhasedIron", 1, "Iron", 200, 512, dustEnderPearl);
		IERecipes.addOreDictAlloyingRecipe("ingotConductiveIron", 1, "Iron", 100, 512, "dustRedstone");
		IERecipes.addOreDictAlloyingRecipe("ingotDarkSteel", 1, "Iron", 400, 512, "dustCoal", "dustObsidian");

		ChemthrowerHandler.registerEffect("nutrient_distillation", new ChemthrowerEffect_Potion(null, 0, Potion.getPotionFromResourceLocation("nausea"), 80, 1));

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
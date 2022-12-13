package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class TRSRItemModelProvider extends ModelProvider<TRSRModelBuilder>
{
	public TRSRItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)
	{
		super(output, Lib.MODID, ITEM_FOLDER, TRSRModelBuilder::new, existingFileHelper);
	}
}

package blusunrize.immersiveengineering.common.data.models;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class TRSRItemModelProvider extends ModelProvider<TRSRModelBuilder>
{
	public TRSRItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper)
	{
		super(generator, Lib.MODID, ITEM_FOLDER, TRSRModelBuilder::new, existingFileHelper);
	}
}

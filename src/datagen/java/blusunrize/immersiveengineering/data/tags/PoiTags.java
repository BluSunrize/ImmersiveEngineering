package blusunrize.immersiveengineering.data.tags;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.world.Villages.Registers;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class PoiTags extends TagsProvider<PoiType>
{
	public PoiTags(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(pGenerator, Registry.POINT_OF_INTEREST_TYPE, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags()
	{
		TagAppender<PoiType> builder = tag(PoiTypeTags.ACQUIRABLE_JOB_SITE);
		for(var entry : Registers.POINTS_OF_INTEREST.getEntries())
			builder.add(entry.getKey());
	}
}

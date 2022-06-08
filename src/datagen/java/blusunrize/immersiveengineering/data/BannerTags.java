package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns.BannerEntry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BannerTags extends TagsProvider<BannerPattern>
{
	public BannerTags(DataGenerator p_126546_, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(p_126546_, Registry.BANNER_PATTERN, Lib.MODID, existingFileHelper);
	}

	@Override
	protected void addTags()
	{
		for(BannerEntry entry : IEBannerPatterns.ALL_BANNERS)
			tag(entry.tag()).add(entry.pattern().get());
	}
}

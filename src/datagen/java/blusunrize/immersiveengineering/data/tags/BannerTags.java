package blusunrize.immersiveengineering.data.tags;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns;
import blusunrize.immersiveengineering.common.register.IEBannerPatterns.BannerEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BannerTags extends IntrinsicHolderTagsProvider<BannerPattern>
{
	public BannerTags(PackOutput output, CompletableFuture<Provider> provider, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(
				output, Registries.BANNER_PATTERN, provider,
				bet -> BuiltInRegistries.BANNER_PATTERN.getResourceKey(bet).orElseThrow(),
				Lib.MODID, existingFileHelper
		);
	}

	@Override
	protected void addTags(Provider p_256380_)
	{
		for(BannerEntry entry : IEBannerPatterns.ALL_BANNERS)
			tag(entry.tag()).add(entry.pattern().get());
	}
}

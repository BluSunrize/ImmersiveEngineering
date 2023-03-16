/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.energy;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.FastEither;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class WindmillBiome extends IESerializableRecipe
{
	public static RegistryObject<IERecipeSerializer<WindmillBiome>> SERIALIZER;

	public static final CachedRecipeList<WindmillBiome> ALL_BIOMES = new CachedRecipeList<>(IERecipeTypes.WINDMILL_BIOME);

	public final FastEither<TagKey<Biome>, List<Biome>> biomes;
	public final float modifier;

	public WindmillBiome(ResourceLocation id, TagKey<Biome> biomes, float modifier)
	{
		this(id, FastEither.left(biomes), modifier);
	}

	public WindmillBiome(ResourceLocation id, List<Biome> biomes, float modifier)
	{
		this(id, FastEither.right(biomes), modifier);
	}

	private WindmillBiome(ResourceLocation id, FastEither<TagKey<Biome>, List<Biome>> biomes, float modifier)
	{
		super(LAZY_EMPTY, IERecipeTypes.WINDMILL_BIOME, id);
		this.biomes = biomes;
		this.modifier = modifier;
	}

	@Override
	protected IERecipeSerializer<?> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(RegistryAccess access)
	{
		return ItemStack.EMPTY;
	}

	public float getModifier()
	{
		return modifier;
	}

	public boolean matches(Holder<Biome> biome)
	{
		if(biomes.isLeft())
			return biome.is(biomes.leftNonnull());
		else
			return biomes.rightNonnull().contains(biome.value());
	}

	@Nullable
	public static WindmillBiome getBiome(Level level, Holder<Biome> biome, @Nullable WindmillBiome hint)
	{
		if(hint!=null&&hint.matches(biome))
			return hint;
		for(WindmillBiome entry : ALL_BIOMES.getRecipes(level))
			if(entry.matches(biome))
				return entry;
		return null;
	}
}

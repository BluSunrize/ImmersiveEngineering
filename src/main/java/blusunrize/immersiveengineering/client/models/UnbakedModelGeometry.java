package blusunrize.immersiveengineering.client.models;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class UnbakedModelGeometry implements IModelGeometry<UnbakedModelGeometry>
{
	private final IUnbakedModel base;

	public UnbakedModelGeometry(IUnbakedModel base)
	{
		this.base = base;
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation)
	{
		return base.bakeModel(bakery, spriteGetter, modelTransform, modelLocation);
	}

	@Override
	public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
	{
		return base.getTextures(modelGetter, missingTextureErrors);
	}
}

/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.client.models.obj.GeneralIEOBJModel.GroupKey;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.item.ItemCallback;
import blusunrize.immersiveengineering.mixin.accessors.client.obj.ModelMeshAccess;
import blusunrize.immersiveengineering.mixin.accessors.client.obj.ModelObjectAccess;
import blusunrize.immersiveengineering.mixin.accessors.client.obj.OBJModelAccess;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.obj.MaterialLibrary;
import net.minecraftforge.client.model.obj.OBJModel.ModelGroup;
import net.minecraftforge.client.model.obj.OBJModel.ModelObject;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class SpecificIEOBJModel<T> implements BakedModel
{
	private final GeneralIEOBJModel<T> baseModel;
	@Nonnull
	private final IEOBJCallback<T> callback;
	private final T key;
	@Nullable
	private final ShaderCase shader;
	private final IEObjState state;
	@Nullable
	private final RenderType layer;
	private List<BakedQuad> quads;

	public SpecificIEOBJModel(
			GeneralIEOBJModel<T> baseModel, T key, @Nullable ShaderCase shader, @Nullable RenderType layer
	)
	{
		this.baseModel = baseModel;
		this.callback = baseModel.getCallback();
		this.key = key;
		this.shader = shader;
		this.state = callback.getIEOBJState(key);
		this.layer = layer;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull Random pRand)
	{
		if(quads==null)
			quads = buildQuads();
		return quads;
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return baseModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return baseModel.isGui3d();
	}

	@Override
	public boolean usesBlockLight()
	{
		return baseModel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer()
	{
		GlobalTempData.setActiveModel(this);
		return baseModel.isCustomRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return baseModel.getParticleIcon();
	}

	@Nonnull
	@Override
	public ItemOverrides getOverrides()
	{
		return baseModel.getOverrides();
	}

	@Override
	public boolean doesHandlePerspectives()
	{
		return true;
	}

	@Override
	public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack transforms)
	{
		Transformation matrix = PerspectiveMapWrapper.getTransforms(baseModel.getOwner().getCombinedTransform())
				.getOrDefault(cameraTransformType, Transformation.identity());

		matrix.push(transforms);
		ItemCallback.castOrDefault(callback).handlePerspective(
				key, GlobalTempData.getActiveHolder(), cameraTransformType, transforms
		);
		return this;
	}

	private List<BakedQuad> buildQuads()
	{
		List<BakedQuad> quads = Lists.newArrayList();

		for(String groupName : baseModel.getGroups().keySet())
		{
			List<ShadedQuads> temp = addQuadsForGroup(groupName, true);
			quads.addAll(
					temp.stream()
							.map(ShadedQuads::quadsInLayer)
							.flatMap(List::stream)
							.filter(Objects::nonNull)
							.collect(Collectors.toList())
			);
		}

		quads = callback.modifyQuads(key, quads);
		return ImmutableList.copyOf(quads);
	}

	public List<ShadedQuads> addQuadsForGroup(String groupName, boolean allowCaching)
	{
		GroupKey<T> cacheKey = new GroupKey<>(key, shader, layer, groupName);
		if(allowCaching)
		{
			List<ShadedQuads> cached = baseModel.getGroupCache().getIfPresent(cacheKey);
			if(cached!=null)
				return cached;
		}
		final int numPasses;
		if(shader!=null)
			numPasses = shader.getLayers().length;
		else
			numPasses = 1;
		ModelGroup g = baseModel.getGroups().get(groupName);
		List<ShadedQuads> ret = new ArrayList<>();
		Transformation optionalTransform = baseModel.getSprite().getRotation();
		optionalTransform = callback.applyTransformations(key, groupName, optionalTransform);
		Transformation transform = state.transform();

		final MaterialSpriteGetter<T> spriteGetter = new MaterialSpriteGetter<>(
				baseModel.getSpriteGetter(), groupName, callback, key, shader
		);
		final MaterialColorGetter<T> colorGetter = new MaterialColorGetter<>(groupName, callback, key, shader);
		final TextureCoordinateRemapper coordinateRemapper = new TextureCoordinateRemapper(
				this.baseModel.getBaseModel(), shader
		);

		if(state.visibility().isVisible(groupName)&&callback.shouldRenderGroup(key, groupName, layer))
			for(int pass = 0; pass < numPasses; ++pass)
				if(shader==null||shader.shouldRenderGroupForPass(groupName, pass))
				{
					List<BakedQuad> quads = new ArrayList<>();
					spriteGetter.setRenderPass(pass);
					colorGetter.setRenderPass(pass);
					coordinateRemapper.setRenderPass(pass);
					IModelBuilder<?> modelBuilder = new QuadListAdder(quads::add, transform);
					addModelObjectQuads(
							g, baseModel.getOwner(), modelBuilder, spriteGetter, colorGetter, coordinateRemapper, optionalTransform
					);
					final Transformation finalTransform = optionalTransform;
					g.getParts().stream()
							.filter(part -> baseModel.getOwner().getPartVisibility(part)&&part instanceof ModelObject)
							.forEach(part -> addModelObjectQuads(
									(ModelObject)part, baseModel.getOwner(), modelBuilder, spriteGetter, colorGetter, coordinateRemapper, finalTransform
							));
					ShaderLayer layer = shader!=null?shader.getLayers()[pass]: new ShaderLayer(new ResourceLocation("missing/no"), -1)
					{
						@Override
						public RenderType getRenderType(RenderType baseType)
						{
							return baseType;
						}
					};
					ret.add(new ShadedQuads(layer, quads));
				}
		if(allowCaching)
			baseModel.getGroupCache().put(cacheKey, ret);
		return ret;
	}

	/**
	 * Yep, this is 90% a copy of ModelObject.addQuads. We need custom hooks in there, so we copy the rest around it.
	 */
	private void addModelObjectQuads(ModelObject modelObject, IModelConfiguration owner, IModelBuilder<?> modelBuilder,
									 MaterialSpriteGetter<?> spriteGetter, MaterialColorGetter<?> colorGetter,
									 TextureCoordinateRemapper coordinateRemapper,
									 Transformation transform)
	{
		List<ModelMeshAccess> meshes = ((ModelObjectAccess)modelObject).getMeshes();
		for(ModelMeshAccess mesh : meshes)
		{
			MaterialLibrary.Material mat = mesh.getMat();
			if(mat==null)
				continue;
			TextureAtlasSprite texture = spriteGetter.apply(
					mat.name, ModelLoaderRegistry.resolveTexture(mat.diffuseColorMap, owner)
			);
			int tintIndex = mat.diffuseTintIndex;
			Vector4f colorTint = colorGetter.apply(mat.name, mat.diffuseColor);

			for(int[][] face : mesh.getFaces())
			{
				boolean drawFace = coordinateRemapper.remapCoord(face);
				if(drawFace)
				{
					Pair<BakedQuad, Direction> quad = ((OBJModelAccess)baseModel.getBaseModel()).invokeMakeQuad(
							face, tintIndex, colorTint, mat.ambientColor, texture, transform
					);
					if(quad.getRight()==null)
						modelBuilder.addGeneralQuad(quad.getLeft());
					else
						modelBuilder.addFaceQuad(quad.getRight(), quad.getLeft());
				}
				coordinateRemapper.resetCoords();
			}
		}
	}

	public Map<String, ModelGroup> getGroups()
	{
		return baseModel.getGroups();
	}

	@Nonnull
	public IEOBJCallback<T> getCallback()
	{
		return callback;
	}

	public T getKey()
	{
		return key;
	}

	public record ShadedQuads(ShaderLayer layer, List<BakedQuad> quadsInLayer)
	{
	}
}

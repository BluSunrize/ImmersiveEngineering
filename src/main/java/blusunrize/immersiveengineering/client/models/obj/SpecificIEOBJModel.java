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
import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.models.mirror.MirroredModelLoader;
import blusunrize.immersiveengineering.client.models.obj.GeneralIEOBJModel.GroupKey;
import blusunrize.immersiveengineering.client.models.split.PolygonUtils;
import blusunrize.immersiveengineering.client.models.split.PolygonUtils.ExtraQuadData;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import malte0811.modelsplitter.model.Group;
import malte0811.modelsplitter.model.MaterialLibrary.OBJMaterial;
import malte0811.modelsplitter.model.Polygon;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.UnbakedGeometryHelper;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SpecificIEOBJModel<T> implements BakedModel
{
	private static final Map<Direction, List<BakedQuad>> EMPTY_ALL_SIDES = Util.make(
			new EnumMap<>(Direction.class), map -> {
				for(Direction d : DirectionUtils.VALUES)
					map.put(d, List.of());
			}
	);

	private final GeneralIEOBJModel<T> baseModel;
	@Nonnull
	private final IEOBJCallback<T> callback;
	private final T key;
	@Nullable
	private final ShaderCase shader;
	private final IEObjState state;
	@Nullable
	private final RenderType layer;
	private final List<BakedQuad> quads;
	private final Supplier<BakedModel> inverted;

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
		this.quads = buildQuads();
		this.inverted = Suppliers.memoize(() -> new SimpleBakedModel(
				MirroredModelLoader.reversedQuads(quads), EMPTY_ALL_SIDES,
				baseModel.useAmbientOcclusion(), baseModel.usesBlockLight(), baseModel.isGui3d(),
				baseModel.getParticleIcon(), ItemTransforms.NO_TRANSFORMS, baseModel.getOverrides()
		)
		{
			@Override
			public boolean isCustomRenderer()
			{
				return SpecificIEOBJModel.this.isCustomRenderer();
			}
		});
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull RandomSource pRand)
	{
		if(pSide!=null)
			return List.of();
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

	@Nonnull
	@Override
	public List<RenderType> getRenderTypes(@Nonnull ItemStack itemStack, boolean fabulous)
	{
		if(layer!=null)
			return List.of(layer);
		else
			return baseModel.getRenderTypes(itemStack, fabulous);
	}

	public ItemTransform getBaseTransforms(@Nonnull ItemDisplayContext transformType){
		return baseModel.getOwner().getTransforms().getTransform(transformType);
	}

	private static final Matrix4f INVERT = new Matrix4f().scale(-1, -1, -1);
	private static final Matrix3f INVERT_NORMAL = new Matrix3f(INVERT);

	@Nonnull
	@Override
	public BakedModel applyTransform(
			@Nonnull ItemDisplayContext transformType, @Nonnull PoseStack transforms, boolean applyLeftHandTransform
	)
	{
		BakedModel result = this;
		ItemTransform baseItemTransform = getBaseTransforms(transformType);
		Vector3f scale = baseItemTransform.scale;
		if(scale.x()*scale.y()*scale.z() < 0)
		{
			Vector3f newScale = new Vector3f(scale);
			newScale.mul(-1);
			new ItemTransform(
					baseItemTransform.rotation, baseItemTransform.translation, newScale, baseItemTransform.rightRotation
			).apply(applyLeftHandTransform, transforms);
			transforms.last().pose().mul(INVERT);
			transforms.last().normal().mul(INVERT_NORMAL);
			// The custom renderer handles inversion on its own, for the default renderer we need to invert the quads
			if(!isCustomRenderer())
				result = this.inverted.get();
		}
		else
			baseItemTransform.apply(applyLeftHandTransform, transforms);
		ItemCallback.castOrDefault(callback).handlePerspective(
				key, GlobalTempData.getActiveHolder(), transformType, transforms
		);
		return result;
	}

	private List<BakedQuad> buildQuads()
	{
		List<BakedQuad> quads = Lists.newArrayList();

		for(Entry<String, Group<OBJMaterial>> groupName : baseModel.getGroups().entrySet())
		{
			List<ShadedQuads> temp = addQuadsForGroup(groupName.getKey(), groupName.getValue(), true);
			quads.addAll(
					temp.stream()
							.map(ShadedQuads::quadsInLayer)
							.flatMap(List::stream)
							.filter(Objects::nonNull)
							.toList()
			);
		}

		quads = callback.modifyQuads(key, quads);
		return ImmutableList.copyOf(quads);
	}

	public List<ShadedQuads> addQuadsForGroup(String groupName, Group<OBJMaterial> group, boolean allowCaching)
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
		List<ShadedQuads> ret = new ArrayList<>();
		Transformation optionalTransform = baseModel.getSprite().getRotation();
		optionalTransform = callback.applyTransformations(key, groupName, optionalTransform);

		final MaterialSpriteGetter<T> spriteGetter = new MaterialSpriteGetter<>(
				baseModel.getSpriteGetter(), groupName, callback, key, shader
		);
		final MaterialColorGetter<T> colorGetter = new MaterialColorGetter<>(groupName, callback, key, shader);
		final TextureCoordinateRemapper coordinateRemapper = new TextureCoordinateRemapper(shader);

		if(state.visibility().isVisible(groupName)&&callback.shouldRenderGroup(key, groupName, layer))
			for(int pass = 0; pass < numPasses; ++pass)
				if(shader==null||shader.shouldRenderGroupForPass(groupName, pass))
				{
					List<BakedQuad> quads = new ArrayList<>();
					spriteGetter.setRenderPass(pass);
					colorGetter.setRenderPass(pass);
					coordinateRemapper.setRenderPass(pass);
					addGroupQuads(
							group, baseModel.getOwner(), quads::add, spriteGetter, colorGetter,
							coordinateRemapper, state.transform().compose(optionalTransform.blockCenterToCorner())
					);
					ShaderLayer layer = shader!=null?shader.getLayers()[pass]: new ShaderLayer(ResourceLocation.withDefaultNamespace("missing/no"), -1)
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
	private void addGroupQuads(Group<OBJMaterial> group, IGeometryBakingContext owner, Consumer<BakedQuad> out,
							   MaterialSpriteGetter<?> spriteGetter, MaterialColorGetter<?> colorGetter,
							   TextureCoordinateRemapper coordinateRemapper,
							   Transformation transform)
	{
		for(Polygon<OBJMaterial> face : group.getFaces())
		{
			OBJMaterial mat = face.getTexture();
			if(mat==null)
				continue;
			TextureAtlasSprite texture = spriteGetter.apply(
					mat.name(), UnbakedGeometryHelper.resolveDirtyMaterial(mat.map_Kd(), owner)
			);
			Color4 colorTint = colorGetter.apply(mat.name(), Color4.WHITE);

			Polygon<OBJMaterial> remappedFace = coordinateRemapper.remapCoord(face);
			if(remappedFace!=null)
				out.accept(PolygonUtils.toBakedQuad(
						remappedFace.getPoints(), new ExtraQuadData(texture, colorTint), transform,
						callback.useAbsoluteUV(key, mat.name()), callback.shadeQuads(key, mat.name())
				));
		}
	}

	public Map<String, Group<OBJMaterial>> getGroups()
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

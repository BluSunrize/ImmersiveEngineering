/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.client.models.CompositeBakedModel;
import blusunrize.immersiveengineering.client.utils.CombinedModelData;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import malte0811.modelsplitter.ClumpedModel;
import malte0811.modelsplitter.SplitModel;
import malte0811.modelsplitter.math.ModelSplitterVec3i;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BakedBasicSplitModel extends CompositeBakedModel<IBakedModel>
{
	private final Lazy<Map<Vector3i, List<BakedQuad>>> splitModels;

	public BakedBasicSplitModel(IBakedModel base, Set<Vector3i> parts, IModelTransform transform)
	{
		super(base);
		this.splitModels = Lazy.concurrentOf(() -> {
			List<BakedQuad> quads = base.getQuads(null, null, Utils.RAND, EmptyModelData.INSTANCE);
			return split(quads, parts, transform);
		});
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand,
									@Nonnull IModelData extraData)
	{
		BlockPos offset = extraData.getData(Model.SUBMODEL_OFFSET);
		if(offset!=null)
			return splitModels.get().getOrDefault(offset, ImmutableList.of());
		else
			return base.getQuads(state, side, rand, extraData);
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state,
								   @Nonnull IModelData tileData)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IModelOffsetProvider)
			return new CombinedModelData(
					new SinglePropertyModelData<>(
							((IModelOffsetProvider)te).getModelOffset(state),
							Model.SUBMODEL_OFFSET
					),
					tileData
			);
		else
			return tileData;
	}

	public static Map<Vector3i, List<BakedQuad>> split(List<BakedQuad> in, Set<Vector3i> parts, IModelTransform transform)
	{
		List<Polygon<TextureAtlasSprite>> polys = in.stream()
				.map(PolygonUtils::toPolygon)
				.collect(Collectors.toList());
		SplitModel<TextureAtlasSprite> splitData = new SplitModel<>(new OBJModel<>(polys));
		Set<ModelSplitterVec3i> partsBMS = parts.stream()
				.map(v -> new ModelSplitterVec3i(v.getX(), v.getY(), v.getZ()))
				.collect(Collectors.toSet());
		ClumpedModel<TextureAtlasSprite> clumpedModel = new ClumpedModel<>(splitData, partsBMS);

		Map<Vector3i, List<BakedQuad>> map = new HashMap<>();
		for(Entry<ModelSplitterVec3i, OBJModel<TextureAtlasSprite>> e : clumpedModel.getClumpedParts().entrySet())
		{
			List<BakedQuad> subModelFaces = new ArrayList<>(e.getValue().getFaces().size());
			for(Polygon<TextureAtlasSprite> p : e.getValue().getFaces())
				subModelFaces.add(PolygonUtils.toBakedQuad(p, transform, DefaultVertexFormats.BLOCK));
			Vector3i mcKey = new Vector3i(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ());
			map.put(mcKey, subModelFaces);
		}
		return map;
	}
}

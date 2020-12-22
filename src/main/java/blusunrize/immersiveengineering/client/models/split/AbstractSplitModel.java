/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.models.CompositeBakedModel;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public abstract class AbstractSplitModel<T extends IBakedModel> extends CompositeBakedModel<T>
{
	private final Vector3i size;

	public AbstractSplitModel(T base, Vector3i size)
	{
		super(base);
		this.size = size;
	}

	@Nonnull
	@Override
	public IModelData getModelData(
			@Nonnull IBlockDisplayReader world,
			@Nonnull BlockPos pos,
			@Nonnull BlockState state,
			@Nonnull IModelData tileData
	)
	{
		IModelData baseData = super.getModelData(world, pos, state, tileData);
		TileEntity te = world.getTileEntity(pos);
		BlockPos offset = null;
		if(te instanceof IModelOffsetProvider)
			offset = ((IModelOffsetProvider)te).getModelOffset(state, size);
		else if(state.getBlock() instanceof IModelOffsetProvider)
			offset = ((IModelOffsetProvider)state.getBlock()).getModelOffset(state, size);
		if(offset!=null)
			return CombinedModelData.combine(new SinglePropertyModelData<>(offset, Model.SUBMODEL_OFFSET), baseData);
		else
			return baseData;
	}

	protected Map<Vector3i, List<BakedQuad>> split(List<BakedQuad> in, Set<Vector3i> parts, IModelTransform transform)
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

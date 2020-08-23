/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import malte0811.modelsplitter.SplitModel;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import malte0811.modelsplitter.util.BakedQuadUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class BakedSplitModel implements IBakedModel
{
	private final IBakedModel base;
	private final List<Vec3i> parts;
	private final IModelTransform transform;
	private Lazy<Map<Vec3i, List<BakedQuad>>> splitModels;

	public BakedSplitModel(IBakedModel base, List<Vec3i> parts, IModelTransform transform)
	{
		this.base = base;
		this.parts = parts;
		this.transform = transform;
		init();
	}

	private void init()
	{
		this.splitModels = Lazy.concurrentOf(() -> {
			List<BakedQuad> quads = base.getQuads(null, null, Utils.RAND, EmptyModelData.INSTANCE);
			List<Polygon<TextureAtlasSprite>> polys = quads.stream()
					.map(BakedQuadUtil::toPolygon)
					.collect(Collectors.toList());
			SplitModel<TextureAtlasSprite> splitData = new SplitModel<>(new OBJModel<>(polys));

			// Clump parts outside the multiblock into the real part
			Set<Vec3i> multiblockBlocks = new HashSet<>(parts);
			Map<Vec3i, OBJModel<TextureAtlasSprite>> clumped = new HashMap<>();
			for(Entry<Vec3i, OBJModel<TextureAtlasSprite>> e : splitData.getParts().entrySet())
			{
				BlockPos posToAdd = new BlockPos(e.getKey());
				OBJModel<TextureAtlasSprite> model = e.getValue();
				if(!multiblockBlocks.contains(posToAdd))
				{
					for(Direction d : Direction.VALUES)
					{
						if(multiblockBlocks.contains(posToAdd.offset(d)))
						{
							posToAdd = posToAdd.offset(d);
							model = model.translate(d.getAxis().ordinal(), -d.getAxisDirection().getOffset());
							break;
						}
					}
				}
				if(multiblockBlocks.contains(posToAdd))
					clumped.merge(posToAdd, model, OBJModel::union);
			}

			Map<Vec3i, List<BakedQuad>> map = new HashMap<>();
			for(Entry<Vec3i, OBJModel<TextureAtlasSprite>> e : clumped.entrySet())
			{
				List<BakedQuad> subModelFaces = new ArrayList<>(e.getValue().getFaces().size());
				for(Polygon<TextureAtlasSprite> p : e.getValue().getFaces())
					subModelFaces.add(BakedQuadUtil.toBakedQuad(p, transform, DefaultVertexFormats.BLOCK));
				map.put(e.getKey(), subModelFaces);
			}
			return map;
		});
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		BlockPos offset = extraData.getData(Model.SUBMODEL_OFFSET);
		if(offset!=null)
			return splitModels.get().getOrDefault(
					offset,
					ImmutableList.of()
			);
		else
			return base.getQuads(state, side, rand, extraData);
	}

	@Nonnull
	@Override
	public IModelData getModelData(
			@Nonnull ILightReader world,
			@Nonnull BlockPos pos,
			@Nonnull BlockState state,
			@Nonnull IModelData tileData
	)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IGeneralMultiblock)
			return new SinglePropertyModelData<>(
					((IGeneralMultiblock)te).getModelOffset(),
					Model.SUBMODEL_OFFSET
			);
		else
			return tileData;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand)
	{
		return base.getQuads(state, side, rand, EmptyModelData.INSTANCE);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return base.isGui3d();
	}

	@Override
	public boolean func_230044_c_()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return base.getParticleTexture(EmptyModelData.INSTANCE);
	}

	@Override
	public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data)
	{
		return base.getParticleTexture(data);
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return base.getOverrides();
	}

	@Override
	public boolean doesHandlePerspectives()
	{
		return true;
	}
}

/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BakedBasicSplitModel extends AbstractSplitModel<IBakedModel>
{
	private final Lazy<Map<Vector3i, List<BakedQuad>>> splitModels;

	public BakedBasicSplitModel(IBakedModel base, Set<Vector3i> parts, IModelTransform transform, Vector3i size)
	{
		super(base, size);
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
}

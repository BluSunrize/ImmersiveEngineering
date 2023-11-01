/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BakedBasicSplitModel extends AbstractSplitModel<BakedModel>
{
	private static final Set<BakedBasicSplitModel> WEAK_INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());

	static
	{
		IEApi.renderCacheClearers.add(() -> WEAK_INSTANCES.forEach(b -> b.splitModels.reset()));
	}

	private final ResettableLazy<Map<Vec3i, List<BakedQuad>>> splitModels;
	private final ItemTransforms itemTransforms;

	public BakedBasicSplitModel(
			BakedModel base, Set<Vec3i> parts, ModelState transform, Vec3i size, ItemTransforms itemTransforms
	)
	{
		super(base, size);
		this.itemTransforms = itemTransforms;
		this.splitModels = new ResettableLazy<>(() -> {
			List<BakedQuad> quads = base.getQuads(null, null, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
			return split(quads, parts, transform);
		});
		WEAK_INSTANCES.add(this);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(
			@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand,
			@Nonnull ModelData extraData, @Nullable RenderType layer
	)
	{
		BlockPos offset = extraData.get(Model.SUBMODEL_OFFSET);
		if(offset!=null)
			return splitModels.get().getOrDefault(offset, ImmutableList.of());
		else
			return base.getQuads(state, side, rand, extraData, layer);
	}

	@Nonnull
	@Override
	public ItemTransforms getTransforms()
	{
		return itemTransforms;
	}
}

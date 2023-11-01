/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.mirror;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.ExtendedBlockModelDeserializer;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MirroredModelLoader implements IGeometryLoader<MirroredGeometry>
{
	public static final String INNER_MODEL = "inner_model";
	public static final ResourceLocation ID = ImmersiveEngineering.rl("mirror");

	@Override
	public MirroredGeometry read(
			JsonObject modelContents, JsonDeserializationContext deserializationContext
	) throws JsonParseException
	{
		JsonElement innerJson = modelContents.get(INNER_MODEL);
		BlockModel baseModel = ExtendedBlockModelDeserializer.INSTANCE.fromJson(innerJson, BlockModel.class);
		return new MirroredGeometry(baseModel);
	}

	public static List<BakedQuad> reversedQuads(List<BakedQuad> quads)
	{
		return quads.stream()
				.map(ModelUtils::reverseOrder)
				.toList();
	}

	public static List<BakedQuad> getReversedQuads(
			BakedModel model,
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	)
	{
		return reversedQuads(model.getQuads(state, side, rand, extraData, layer));
	}

	public static List<BakedQuad> getReversedQuads(SimpleBakedModel model, @Nullable Direction face)
	{
		return getReversedQuads(model, null, face, ApiUtils.RANDOM_SOURCE, ModelData.EMPTY, null);
	}
}

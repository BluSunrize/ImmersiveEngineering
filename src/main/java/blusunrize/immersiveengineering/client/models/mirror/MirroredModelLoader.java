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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry.ExpandedBlockModelDeserializer;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class MirroredModelLoader implements IModelLoader<MirroredGeometry>
{
	public static final String INNER_MODEL = "inner_model";
	public static final ResourceLocation ID = ImmersiveEngineering.rl("mirror");

	@Nonnull
	@Override
	public MirroredGeometry read(@Nonnull JsonDeserializationContext deserializationContext, @Nonnull JsonObject modelContents)
	{
		JsonElement innerJson = modelContents.get(INNER_MODEL);
		BlockModel baseModel = ExpandedBlockModelDeserializer.INSTANCE.fromJson(innerJson, BlockModel.class);
		return new MirroredGeometry(baseModel);
	}

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager pResourceManager)
	{
	}

	public static List<BakedQuad> reversedQuads(List<BakedQuad> quads)
	{
		return quads.stream()
				.map(ModelUtils::reverseOrder)
				.toList();
	}

	public static List<BakedQuad> getReversedQuads(
			BakedModel model, @Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData
	)
	{
		return reversedQuads(model.getQuads(state, side, rand, extraData));
	}

	public static List<BakedQuad> getReversedQuads(SimpleBakedModel model, @Nullable Direction face)
	{
		return getReversedQuads(model, null, face, ApiUtils.RANDOM, EmptyModelData.INSTANCE);
	}
}

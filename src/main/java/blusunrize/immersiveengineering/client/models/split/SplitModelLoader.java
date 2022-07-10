/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SplitModelLoader implements IGeometryLoader<UnbakedSplitModel>
{
	public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "basic_split");
	public static final String PARTS = "split_parts";
	public static final String INNER_MODEL = "inner_model";
	public static final String DYNAMIC = "dynamic";

	@Nonnull
	@Override
	public UnbakedSplitModel read(JsonObject modelContents, @Nonnull JsonDeserializationContext deserializationContext)
	{
		UnbakedModel baseModel;
		JsonElement innerJson = modelContents.get(INNER_MODEL);
		baseModel = ExtendedBlockModelDeserializer.INSTANCE.fromJson(innerJson, BlockModel.class);
		JsonArray partsJson = modelContents.getAsJsonArray(PARTS);
		List<Vec3i> parts = new ArrayList<>(partsJson.size());
		for(JsonElement e : partsJson)
			parts.add(fromJson(e.getAsJsonArray()));
		BoundingBox box = pointBB(parts.get(0));
		for(Vec3i v : parts)
			box.encapsulate(pointBB(v));
		Vec3i size = new Vec3i(box.getXSpan(), box.getYSpan(), box.getZSpan());
		return new UnbakedSplitModel(baseModel, parts, modelContents.get(DYNAMIC).getAsBoolean(), size);
	}

	private Vec3i fromJson(JsonArray a)
	{
		return new Vec3i(a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt());
	}

	private BoundingBox pointBB(Vec3i point)
	{
		return new BoundingBox(new BlockPos(point));
	}
}

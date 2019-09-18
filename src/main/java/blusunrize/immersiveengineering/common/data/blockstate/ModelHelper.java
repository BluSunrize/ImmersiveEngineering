/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.blockstate;

import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;

public class ModelHelper
{
	private ModelHelper()
	{
	}

	public static JsonObject createBasicCube(ResourceLocation texture)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/cube_all");
		JsonObject textures = new JsonObject();
		textures.addProperty("all", texture.toString());
		model.add("textures", textures);
		return model;
	}

	public static JsonObject createBasicCube(ResourceLocation sides, ResourceLocation top, ResourceLocation bottom)
	{
		assertTextureExists(sides);
		assertTextureExists(top);
		assertTextureExists(bottom);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "immersiveengineering:block/ie_pillar");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", top.toString());
		textures.addProperty("bottom", bottom.toString());
		textures.addProperty("side", sides.toString());
		model.add("textures", textures);
		return model;
	}

	public static JsonObject createFencePost(ResourceLocation texture)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/fence_post");
		JsonObject textures = new JsonObject();
		textures.addProperty("texture", texture.toString());
		model.add("textures", textures);
		return model;
	}

	public static JsonObject createFenceSide(ResourceLocation texture)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/fence_side");
		JsonObject textures = new JsonObject();
		textures.addProperty("texture", texture.toString());
		model.add("textures", textures);
		return model;
	}

	private static void assertTextureExists(ResourceLocation name)
	{
		//TODO implement
	}
}

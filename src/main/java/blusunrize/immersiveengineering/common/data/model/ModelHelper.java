/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.model;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.data.model.ModelFile.GeneratedModelFile;
import com.google.gson.JsonObject;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.ResourceLocation;

public class ModelHelper
{
	private ModelHelper()
	{
	}

	public static GeneratedModelFile createBasicCube(ResourceLocation texture)
	{
		return createBasicCube(texture, texture);
	}

	public static GeneratedModelFile createBasicCube(ResourceLocation texture, ResourceLocation modelName)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/cube_all");
		JsonObject textures = new JsonObject();
		textures.addProperty("all", texture.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(modelName, model);
	}

	public static GeneratedModelFile createBasicCube(ResourceLocation sides, ResourceLocation top,
													 ResourceLocation bottom, ResourceLocation modelName)
	{
		assertTextureExists(sides);
		assertTextureExists(top);
		assertTextureExists(bottom);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/cube_bottom_top");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", top.toString());
		textures.addProperty("bottom", bottom.toString());
		textures.addProperty("side", sides.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(modelName, model);
	}

	public static GeneratedModelFile createSlab(ResourceLocation sides, ResourceLocation top,
													 ResourceLocation bottom, ResourceLocation modelName)
	{
		assertTextureExists(sides);
		assertTextureExists(top);
		assertTextureExists(bottom);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/slab");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", top.toString());
		textures.addProperty("bottom", bottom.toString());
		textures.addProperty("side", sides.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(modelName, model);
	}

	public static GeneratedModelFile createStairs(BasicStairsShape s, ResourceLocation sides, ResourceLocation top, ResourceLocation bottom, ResourceLocation modelName)
	{
		assertTextureExists(sides);
		assertTextureExists(top);
		assertTextureExists(bottom);
		JsonObject model = new JsonObject();
		switch(s)
		{
			case STRAIGHT:
				model.addProperty("parent", "block/stairs");
				break;
			case INNER:
				model.addProperty("parent", "block/inner_stairs");
				break;
			case OUTER:
				model.addProperty("parent", "block/outer_stairs");
				break;
		}
		JsonObject textures = new JsonObject();
		textures.addProperty("side", sides.toString());
		textures.addProperty("top", top.toString());
		textures.addProperty("bottom", bottom.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(modelName, model);
	}

	public static GeneratedModelFile createFencePost(ResourceLocation texture, ResourceLocation modelName)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/fence_post");
		JsonObject textures = new JsonObject();
		textures.addProperty("texture", texture.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(modelName, model);
	}

	public static GeneratedModelFile createFenceSide(ResourceLocation texture, ResourceLocation modelName)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/fence_side");
		JsonObject textures = new JsonObject();
		textures.addProperty("texture", texture.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(modelName, model);
	}

	private static void assertTextureExists(ResourceLocation name)
	{
		//TODO implement
	}

	public static GeneratedModelFile createScaffolding(ResourceLocation side, ResourceLocation top, ResourceLocation fileName)
	{
		assertTextureExists(side);
		assertTextureExists(top);
		JsonObject model = new JsonObject();
		model.addProperty("parent", ImmersiveEngineering.MODID+":block/ie_scaffolding");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", top.toString());
		textures.addProperty("side", side.toString());
		textures.addProperty("bottom", side.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(fileName, model);
	}

	public enum BasicStairsShape
	{
		STRAIGHT,
		INNER,
		OUTER;

		public StairsShape getExample()
		{
			switch(this)
			{
				case STRAIGHT:
					return StairsShape.STRAIGHT;
				case INNER:
					return StairsShape.INNER_LEFT;
				case OUTER:
					return StairsShape.OUTER_LEFT;
			}
			throw new NullPointerException();
		}

		public static BasicStairsShape toBasicShape(StairsShape exact)
		{
			switch(exact)
			{
				case STRAIGHT:
					return STRAIGHT;
				case INNER_LEFT:
				case INNER_RIGHT:
					return INNER;
				case OUTER_LEFT:
				case OUTER_RIGHT:
					return OUTER;
			}
			throw new NullPointerException();
		}
	}
}
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
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Locale;

import static net.minecraft.util.Direction.NORTH;

public class ModelHelper
{
	private ModelHelper()
	{
	}

	public static GeneratedModelFile createBasicCube(ResourceLocation texture)
	{
		return createBasicCube(texture, texture);
	}

	public static GeneratedModelFile createBasicItem(ResourceLocation texture, ResourceLocation modelName)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "item/generated");
		JsonObject textures = new JsonObject();
		textures.addProperty("layer0", texture.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(modelName, model);
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

	public static GeneratedModelFile createSlab(SlabType type, ResourceLocation sides, ResourceLocation top,
												ResourceLocation bottom, ResourceLocation modelName)
	{
		assertTextureExists(sides);
		assertTextureExists(top);
		assertTextureExists(bottom);
		JsonObject model = new JsonObject();
		model.addProperty("parent", type==SlabType.TOP?"block/slab_top":"block/slab");
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

	public static GeneratedModelFile createInventoryFence(ResourceLocation texture, ResourceLocation modelName)
	{
		assertTextureExists(texture);
		JsonObject model = new JsonObject();
		model.addProperty("parent", "block/fence_inventory");
		JsonObject textures = new JsonObject();
		textures.addProperty("texture", texture.toString());
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

	public static GeneratedModelFile createThreeCubed(ResourceLocation outName, ResourceLocation nonFront, ResourceLocation front)
	{
		assertTextureExists(nonFront);
		assertTextureExists(front);
		JsonObject model = new JsonObject();
		model.addProperty("parent", ImmersiveEngineering.MODID+":block/ie_three_cubed");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", nonFront.toString());
		textures.addProperty("bottom", nonFront.toString());
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			if(d!=NORTH)
				textures.addProperty(d.getName(), nonFront.toString());
			else
				textures.addProperty(d.getName(), front.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(outName, model);
	}

	public static GeneratedModelFile createTwoCubed(ResourceLocation out, ResourceLocation bottom, ResourceLocation top, ResourceLocation sides, ResourceLocation front)
	{
		assertTextureExists(bottom);
		assertTextureExists(top);
		assertTextureExists(sides);
		assertTextureExists(front);
		JsonObject model = new JsonObject();
		model.addProperty("parent", ImmersiveEngineering.MODID+":block/ie_two_cubed");
		JsonObject textures = new JsonObject();
		textures.addProperty("top", top.toString());
		textures.addProperty("bottom", bottom.toString());
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			if(d!=NORTH)
				textures.addProperty(d.getName(), sides.toString());
			else
				textures.addProperty(d.getName(), front.toString());
		model.add("textures", textures);
		return new GeneratedModelFile(out, model);
	}

	public static GeneratedModelFile createVariants(ResourceLocation fileName, Enum[] types, GeneratedModelFile... models)
	{
		assert types.length == models.length;
		JsonObject model = new JsonObject();
		JsonObject variants = new JsonObject();
		for(int i=0; i<types.length; i++)
			variants.addProperty(types[i].name().toLowerCase(Locale.US), models[i].getUncheckedLocation().toString());
		model.add("variants", variants);
		return new GeneratedModelFile(fileName, model);
	}

	public static GeneratedModelFile createMetalLadder(ResourceLocation out, @Nullable ResourceLocation bottomTop, @Nullable ResourceLocation sides)
	{
		JsonObject model = new JsonObject();
		JsonObject textures = new JsonObject();
		if(bottomTop!=null)
		{
			Preconditions.checkNotNull(sides);
			assertTextureExists(bottomTop);
			assertTextureExists(sides);
			model.addProperty("parent", ImmersiveEngineering.MODID+":block/ie_scaffoldladder");
			textures.addProperty("top", bottomTop.toString());
			textures.addProperty("bottom", bottomTop.toString());
			textures.addProperty("side", sides.toString());
		}
		else
		{
			Preconditions.checkArgument(sides==null);
			model.addProperty("parent", ImmersiveEngineering.MODID+":block/ie_ladder");
		}
		textures.addProperty("ladder", ImmersiveEngineering.MODID+":block/metal_decoration/metal_ladder");
		model.add("textures", textures);
		return new GeneratedModelFile(out, model);
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
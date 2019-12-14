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
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static net.minecraft.util.Direction.NORTH;

public class ModelHelper
{
	public static ExistingFileHelper EXISTING_FILE_HELPER;

	private ModelHelper()
	{
	}

	public static GeneratedModelFile createBasicCube(ResourceLocation texture)
	{
		return createBasicCube(texture, texture);
	}

	public static GeneratedModelFile createWithModel(ResourceLocation model, ResourceLocation outLoc)
	{
		return create(outLoc, model, ImmutableMap.of(), true);
	}

	public static GeneratedModelFile createWithDynamicModel(ResourceLocation model, ResourceLocation outLoc)
	{
		return create(outLoc, model, ImmutableMap.of(), false);
	}

	public static GeneratedModelFile createBasicItem(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation("item/generated"),
				ImmutableMap.of("layer0", texture), true);
	}

	public static GeneratedModelFile createBasicCube(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation("block/cube_all"),
				ImmutableMap.of("all", texture), true);
	}

	public static GeneratedModelFile createBasicCube(ResourceLocation sides, ResourceLocation top,
													 ResourceLocation bottom, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation("block/cube_bottom_top"), ImmutableMap.of(
				"top", top,
				"bottom", bottom,
				"side", sides
		), true);
	}

	public static GeneratedModelFile create(ResourceLocation outName, ResourceLocation parent,
											Map<String, ResourceLocation> textures, boolean existingModel)
	{
		for(ResourceLocation rl : textures.values())
			assertTextureExists(rl);
		if(existingModel)
			assertModelExists(parent);
		JsonObject model = new JsonObject();
		model.addProperty("parent", parent.toString());
		if(!textures.isEmpty())
		{
			JsonObject textureJson = new JsonObject();
			for(Entry<String, ResourceLocation> e : textures.entrySet())
				textureJson.addProperty(e.getKey(), e.getValue().toString());
			model.add("textures", textureJson);
		}
		return new GeneratedModelFile(outName, model);
	}

	public static GeneratedModelFile createCarpetBlock(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation("block/carpet"),
				ImmutableMap.of("wool", texture, "particle", texture), true);
	}

	public static GeneratedModelFile createThreeQuarterBlock(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_three_quarter_block"),
				ImmutableMap.of("texture", texture), true);
	}

	public static GeneratedModelFile createQuarterBlock(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_quarter_block"),
				ImmutableMap.of("texture", texture), true);
	}

	public static GeneratedModelFile createSlab(SlabType type, ResourceLocation sides, ResourceLocation top,
												ResourceLocation bottom, ResourceLocation modelName)
	{
		ResourceLocation parent;
		if(type==SlabType.TOP)
			parent = new ResourceLocation("block/slab_top");
		else
			parent = new ResourceLocation("block/slab");
		return create(modelName, parent, ImmutableMap.of(
				"top", top,
				"bottom", bottom,
				"side", sides
		), true);
	}

	public static GeneratedModelFile createStairs(BasicStairsShape s, ResourceLocation sides, ResourceLocation top, ResourceLocation bottom, ResourceLocation modelName)
	{
		ResourceLocation parent;
		switch(s)
		{
			case STRAIGHT:
			default:
				parent = new ResourceLocation("block/stairs");
				break;
			case INNER:
				parent = new ResourceLocation("block/inner_stairs");
				break;
			case OUTER:
				parent = new ResourceLocation("block/outer_stairs");
				break;
		}
		return create(modelName, parent, ImmutableMap.of(
				"top", top,
				"bottom", bottom,
				"side", sides
		), true);
	}

	public static GeneratedModelFile createInventoryFence(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation("block/fence_inventory"),
				ImmutableMap.of("texture", texture), true);
	}

	public static GeneratedModelFile createFencePost(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation("block/fence_post"),
				ImmutableMap.of("texture", texture), true);
	}

	public static GeneratedModelFile createFenceSide(ResourceLocation texture, ResourceLocation modelName)
	{
		return create(modelName, new ResourceLocation("block/fence_side"),
				ImmutableMap.of("texture", texture), true);
	}

	private static void assertModelExists(ResourceLocation name)
	{
		if(EXISTING_FILE_HELPER!=null)
		{
			String suffix = name.getPath().contains(".")?"": ".json";
			Preconditions.checkState(
					EXISTING_FILE_HELPER.exists(name, ResourcePackType.CLIENT_RESOURCES, suffix, "models"),
					"Model \""+name+"\" does not exist");
		}
	}

	private static void assertTextureExists(ResourceLocation name)
	{
		if(EXISTING_FILE_HELPER!=null)
			Preconditions.checkState(
					EXISTING_FILE_HELPER.exists(name, ResourcePackType.CLIENT_RESOURCES, ".png", "textures"),
					"Texture \""+name+"\" does not exist");
	}

	public static GeneratedModelFile createScaffolding(ResourceLocation side, ResourceLocation top, ResourceLocation fileName)
	{
		return create(fileName, new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_scaffolding"),
				ImmutableMap.of(
						"top", top,
						"side", side,
						"bottom", side
				), true);
	}

	public static GeneratedModelFile createThreeCubed(ResourceLocation outName, ResourceLocation nonFront, ResourceLocation front)
	{
		Map<String, ResourceLocation> textures = new HashMap<>();
		textures.put("top", nonFront);
		textures.put("bottom", nonFront);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			if(d!=NORTH)
				textures.put(d.getName(), nonFront);
			else
				textures.put(d.getName(), front);
		return create(outName, new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_three_cubed"),
				textures, true);
	}

	public static GeneratedModelFile createTwoCubed(ResourceLocation out, ResourceLocation bottom, ResourceLocation top,
													ResourceLocation sides, ResourceLocation front)
	{
		Map<String, ResourceLocation> textures = new HashMap<>();
		textures.put("top", top);
		textures.put("bottom", bottom);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			if(d!=NORTH)
				textures.put(d.getName(), sides);
			else
				textures.put(d.getName(), front);
		return create(out, new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_two_cubed"),
				textures, true);
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
		Map<String, ResourceLocation> textures = new HashMap<>();
		ResourceLocation parent;
		if(bottomTop!=null)
		{
			Preconditions.checkNotNull(sides);
			parent = new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_scaffoldladder");
			textures.put("top", bottomTop);
			textures.put("bottom", bottomTop);
			textures.put("side", sides);
		}
		else
			parent = new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_ladder");
		textures.put("ladder", new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_decoration/metal_ladder"));
		return create(out, parent, textures, true);
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
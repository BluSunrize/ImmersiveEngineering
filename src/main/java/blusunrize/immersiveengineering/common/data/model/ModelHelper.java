/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.model;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelFile.GeneratedModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelFile.UncheckedModelFile;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.CharStreams;
import com.google.gson.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.fluid.Fluid;
import net.minecraft.resources.IResource;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ForgeBlockStateV1.TRSRDeserializer;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nullable;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;
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

	public static GeneratedModelFile createBasicCube(Function<Direction, ResourceLocation> getTexture, ResourceLocation modelName)
	{
		ImmutableMap.Builder<String, ResourceLocation> textures = ImmutableMap.builder();
		for(Direction d : Direction.VALUES)
			textures.put(d.getName().toLowerCase(Locale.ENGLISH), getTexture.apply(d));
		return create(modelName, new ResourceLocation("block/cube"), textures.build(), true);
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

	public static JsonObject createJson(ModelFile parent, Map<String, ResourceLocation> textures,
										@Nullable ResourceLocation transforms, boolean texturesExist)
	{
		if(texturesExist)
			for(ResourceLocation rl : textures.values())
				assertTextureExists(rl);
		JsonObject model = new JsonObject();
		model.addProperty("parent", parent.getLocation().toString());
		if(!textures.isEmpty())
		{
			JsonObject textureJson = new JsonObject();
			for(Entry<String, ResourceLocation> e : textures.entrySet())
				textureJson.addProperty(e.getKey(), e.getValue().toString());
			model.add("textures", textureJson);
		}
		if(transforms!=null)
			try
			{
				IResource transformFile = EXISTING_FILE_HELPER.getResource(
						transforms, ResourcePackType.CLIENT_RESOURCES, ".json", "transformations"
				);
				String jsonString = CharStreams.toString(new InputStreamReader(transformFile.getInputStream()));
				TransformationMap transformMap = new TransformationMap();
				transformMap.addFromJson(jsonString);
				model.add("display-trsr", transformMap.toJson());
			} catch(IOException e)
			{
				throw new RuntimeException(e);
			}
		return model;
	}

	public static GeneratedModelFile create(ResourceLocation outName, ResourceLocation parent,
											Map<String, ResourceLocation> textures, ResourceLocation transforms,
											boolean existingModel)
	{
		ModelFile parentFile;
		if(existingModel)
			parentFile = new ExistingModelFile(parent);
		else
			parentFile = new UncheckedModelFile(parent);
		return create(outName, parentFile, textures, transforms);
	}

	public static GeneratedModelFile create(ResourceLocation outName, ModelFile parent,
											Map<String, ResourceLocation> textures, ResourceLocation transforms)
	{
		return new GeneratedModelFile(outName, createJson(parent, textures, transforms, true));
	}

	public static GeneratedModelFile create(ResourceLocation outName, ResourceLocation parent,
											Map<String, ResourceLocation> textures, boolean existingModel)
	{
		return create(outName, parent, textures, null, existingModel);
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
		assert types.length==models.length;
		JsonObject model = new JsonObject();
		JsonObject variants = new JsonObject();
		for(int i = 0; i < types.length; i++)
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

	public static GeneratedModelFile createConnectorModel(ResourceLocation out, Map<String, ResourceLocation> retexture, ResourceLocation model)
	{
		return create(out, model, retexture, rl("item/connector"), true);
	}

	public static GeneratedModelFile createFluid(ResourceLocation outName, ResourceLocation stillTexture)
	{
		JsonObject ret = new JsonObject();
		assertTextureExists(stillTexture);
		JsonObject textures = new JsonObject();
		textures.addProperty("particle", stillTexture.toString());
		ret.add("textures", textures);
		return new GeneratedModelFile(outName, ret);
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

	public static GeneratedModelFile createMultilayer(ResourceLocation output,
													  ImmutableMap<BlockRenderLayer, ModelFile> models,
													  @Nullable ResourceLocation transforms)
	{
		JsonObject ret = createJson(
				new UncheckedModelFile(rl("multilayer")),
				ImmutableMap.of(),
				transforms,
				true
		);
		for(BlockRenderLayer layer : models.keySet())
		{
			JsonObject forLayer = new JsonObject();
			forLayer.addProperty("model", models.get(layer).getLocation().toString());
			ret.add(layer.name(), forLayer);
		}
		return new GeneratedModelFile(output, ret);
	}

	public static GeneratedModelFile createBucket(ResourceLocation output, Fluid fluid)
	{
		JsonObject ret = createJson(
				new UncheckedModelFile(new ResourceLocation("forge:dynbucket")),
				ImmutableMap.of(
						"base", new ResourceLocation("item/bucket"),
						"fluid", new ResourceLocation("forge", "items/bucket_fluid")
				),
				null,
				false//TODO provide correct "existing" path for forge textures
		);
		ret.addProperty("fluid", fluid.getRegistryName().toString());
		return new GeneratedModelFile(output, ret);
	}

	public static GeneratedModelFile createTEIR_IEOBJ(ResourceLocation output, ResourceLocation ieobj, ResourceLocation transforms)
	{
		JsonObject ret = createJson(
				new ExistingModelFile(ieobj),
				ImmutableMap.of(),
				transforms,
				false
		);
		ret.addProperty("dynamic", true);
		return new GeneratedModelFile(output, ret);
	}

	public static class TransformationMap
	{
		private final Map<TransformType, TRSRTransformation> transforms = new TreeMap<>();

		public TransformationMap setTransformations(TransformType t, Matrix4 mat)
		{
			transforms.put(t, new TRSRTransformation(mat.toMatrix4f()));
			return this;
		}

		public void addFromJson(String json)
		{
			Gson GSON = new GsonBuilder()
					.registerTypeAdapter(TRSRTransformation.class, TRSRDeserializer.INSTANCE)
					.registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer())
					.create();
			JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
			Map<TransformType, TRSRTransformation> transforms = new HashMap<>();
			boolean vanilla = obj.has("type")&&"vanilla".equals(obj.remove("type").getAsString());
			for(TransformType type : TransformType.values())
			{
				String key = type.name().toLowerCase();
				JsonObject forType = obj.getAsJsonObject(key);
				obj.remove(key);
				if(forType==null)
				{
					key = key.replace("_person", "person").replace("_hand", "hand");
					forType = obj.getAsJsonObject(key);
					obj.remove(key);
				}
				TRSRTransformation transform;
				if(forType!=null)
				{
					if(vanilla)
					{
						ItemTransformVec3f vanillaTransform = GSON.fromJson(forType, ItemTransformVec3f.class);
						transform = TRSRTransformation.from(vanillaTransform);
					}
					else
						transform = GSON.fromJson(forType, TRSRTransformation.class);
				}
				else
					transform = TRSRTransformation.identity();
				transforms.put(type, transform);
			}
			TRSRTransformation baseTransform;
			if(obj.size() > 0)
				baseTransform = GSON.fromJson(obj, TRSRTransformation.class);
			else
				baseTransform = TRSRTransformation.identity();
			for(Entry<TransformType, TRSRTransformation> e : transforms.entrySet())
				this.transforms.put(e.getKey(), TRSRTransformation.blockCenterToCorner(e.getValue().compose(baseTransform)));
		}

		public JsonObject toJson()
		{
			JsonObject ret = new JsonObject();
			for(Entry<TransformType, TRSRTransformation> entry : transforms.entrySet())
				add(ret, entry.getKey(), entry.getValue());
			return ret;
		}

		private void add(JsonObject main, TransformType type, TRSRTransformation trsr)
		{
			JsonObject result = new JsonObject();
			result.add("translation", toJson(trsr.getTranslation()));
			result.add("rotation", toJson(trsr.getLeftRot()));
			result.add("scale", toJson(trsr.getScale()));
			result.add("post-rotation", toJson(trsr.getRightRot()));
			main.add(type.name().toLowerCase(), result);
		}

		private static JsonArray toJson(Quat4f v)
		{
			JsonArray ret = new JsonArray();
			ret.add(v.x);
			ret.add(v.y);
			ret.add(v.z);
			ret.add(v.w);
			return ret;
		}

		private static JsonArray toJson(Vector3f v)
		{
			JsonArray ret = new JsonArray();
			ret.add(v.x);
			ret.add(v.y);
			ret.add(v.z);
			return ret;
		}

		private static Vector3f fromJson(JsonElement ele)
		{
			JsonArray arr = ele.getAsJsonArray();
			return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
		}
	}
}
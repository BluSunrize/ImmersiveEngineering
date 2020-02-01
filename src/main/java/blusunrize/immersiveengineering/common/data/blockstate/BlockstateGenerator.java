/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.blockstate;

import blusunrize.immersiveengineering.common.data.model.ModelFile;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public abstract class BlockstateGenerator implements IDataProvider
{
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	private final DataGenerator gen;

	public BlockstateGenerator(DataGenerator gen)
	{
		this.gen = gen;
	}

	private Set<ResourceLocation> generatedStates;
	private DirectoryCache cache;

	@Override
	public void act(@Nonnull DirectoryCache cache) throws IOException
	{
		generatedStates = new HashSet<>();
		this.cache = cache;
		registerStates(this::createVariantModel, this::createMultipartModel);
	}

	private void createVariantModel(Block block, IVariantModelGenerator out)
	{
		ResourceLocation blockName = Preconditions.checkNotNull(block.getRegistryName());
		Preconditions.checkArgument(generatedStates.add(blockName));
		JsonObject variants = new JsonObject();
		for(BlockState b : block.getStateContainer().getValidStates())
		{
			ConfiguredModel model = out.getModel(b);
			Preconditions.checkNotNull(model);
			StringBuilder name = new StringBuilder();
			for(IProperty<?> prop : block.getStateContainer().getProperties())
			{
				if(name.length() > 0)
					name.append(",");
				name.append(prop.getName())
						.append("=")
						//TODO surely there's a better way to do this
						.append(((IProperty)prop).getName(b.get(prop)));
			}
			variants.add(name.toString(), model.toJSON());
		}
		JsonObject main = new JsonObject();
		main.add("variants", variants);
		BlockstateGenerator.this.saveBlockState(main, block);
	}

	private void createMultipartModel(Block block, List<MultiPart> parts)
	{
		JsonArray variants = new JsonArray();
		for(MultiPart part : parts)
		{
			Preconditions.checkArgument(part.canApplyTo(block));
			variants.add(part.toJson());
		}
		JsonObject main = new JsonObject();
		main.add("multipart", variants);
		saveBlockState(main, block);
	}

	protected abstract void registerStates(BiConsumer<Block, IVariantModelGenerator> variantBased, BiConsumer<Block, List<MultiPart>> multipartBased);

	private void saveBlockState(JsonObject stateJson, Block owner)
	{
		ResourceLocation blockName = Preconditions.checkNotNull(owner.getRegistryName());
		Path mainOutput = gen.getOutputFolder();
		String pathSuffix = "assets/"+blockName.getNamespace()+"/blockstates/"+blockName.getPath()+".json";
		Path outputPath = mainOutput.resolve(pathSuffix);
		saveJSON(cache, stateJson, outputPath);
	}

	//TODO move somewhere else
	public static void saveJSON(DirectoryCache cache, JsonObject data, Path target)
	{
		try
		{
			String jsonString = GSON.toJson(data);
			String hash = HASH_FUNCTION.hashUnencodedChars(jsonString).toString();
			if(!Objects.equals(cache.getPreviousHash(target), hash)||!Files.exists(target))
			{
				Files.createDirectories(target.getParent());

				try(BufferedWriter writer = Files.newBufferedWriter(target))
				{
					writer.write(jsonString);
				}
			}

			cache.recordHash(target, hash);
		} catch(IOException x)
		{
			IELogger.logger.error("Couldn't save data to {}", target, x);
		}
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "Block States";
	}

	public interface IVariantModelGenerator
	{
		ConfiguredModel getModel(BlockState state);
	}

	public static final class ConfiguredModel
	{
		public final ModelFile name;
		public final int rotationX;
		public final int rotationY;
		public final boolean uvLock;
		public final ImmutableMap<String, Object> additionalData;
		public final ImmutableMap<String, String> retexture;

		public ConfiguredModel(ModelFile name, int rotationX, int rotationY, boolean uvLock, ImmutableMap<String, Object> additionalData)
		{
			this(name, rotationX, rotationY, uvLock, additionalData, ImmutableMap.of());
		}

		public ConfiguredModel(ModelFile name, int rotationX, int rotationY, boolean uvLock, ImmutableMap<String, Object> additionalData,
							   ImmutableMap<String, String> textures)
		{
			Preconditions.checkNotNull(name);
			Preconditions.checkNotNull(name.getLocation());
			this.name = name;
			this.rotationX = rotationX;
			this.rotationY = rotationY;
			this.uvLock = uvLock;
			this.additionalData = additionalData;
			this.retexture = textures;
		}

		public ConfiguredModel(ModelFile name)
		{
			this(name, 0, 0, false, ImmutableMap.of());
		}

		public JsonObject toJSON()
		{
			JsonObject modelJson = new JsonObject();
			modelJson.addProperty("model", name.getLocation().toString());
			if(rotationX!=0)
				modelJson.addProperty("x", rotationX);
			if(rotationY!=0)
				modelJson.addProperty("y", rotationY);
			if(uvLock&&(rotationX!=0||rotationY!=0))
				modelJson.addProperty("uvlock", uvLock);
			if(!additionalData.isEmpty())
			{
				JsonObject custom = toJson(additionalData);
				modelJson.add("custom", custom);
			}
			if(!retexture.isEmpty())
			{
				JsonObject textures = new JsonObject();
				for(Entry<String, String> entry : retexture.entrySet())
					textures.addProperty(entry.getKey(), entry.getValue());
				modelJson.add("textures", textures);
			}
			return modelJson;
		}

		private JsonObject toJson(Map<String, Object> map)
		{
			JsonObject custom = new JsonObject();
			for(Entry<String, Object> e : map.entrySet())
			{
				if(e.getValue() instanceof Boolean)
					custom.addProperty(e.getKey(), (Boolean)e.getValue());
				else if(e.getValue() instanceof Number)
					custom.addProperty(e.getKey(), (Number)e.getValue());
				else if(e.getValue() instanceof Character)
					custom.addProperty(e.getKey(), (Character)e.getValue());
				else if(e.getValue() instanceof Map)
					custom.add(e.getKey(), toJson((Map<String, Object>)e.getValue()));
				else
					custom.addProperty(e.getKey(), e.getValue().toString());
			}
			return custom;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			ConfiguredModel that = (ConfiguredModel)o;
			return rotationX==that.rotationX&&
					rotationY==that.rotationY&&
					uvLock==that.uvLock&&
					name.equals(that.name)&&
					additionalData.equals(that.additionalData);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(name, rotationX, rotationY, uvLock, additionalData);
		}

		public ImmutableMap<String, String> getAddtionalDataAsStrings()
		{
			ImmutableMap.Builder<String, String> ret = ImmutableMap.builder();
			for(Entry<String, Object> entry : additionalData.entrySet())
			{
				if(entry.getValue() instanceof String)
					ret.put(entry.getKey(), "\""+entry.getValue()+"\n");
				else
					ret.put(entry.getKey(), entry.getValue().toString());
			}
			return ret.build();
		}
	}

	public static final class PropertyWithValues<T extends Comparable<T>>
	{
		public final IProperty<T> prop;
		public final List<T> values;

		public PropertyWithValues(IProperty<T> prop, T... values)
		{
			this.prop = prop;
			this.values = Arrays.asList(values);
		}
	}

	public static final class MultiPart
	{
		public final ConfiguredModel model;
		public final boolean useOr;
		public final List<PropertyWithValues> conditions;

		public MultiPart(ConfiguredModel model, boolean useOr, PropertyWithValues... conditionsArray)
		{
			conditions = Arrays.asList(conditionsArray);
			Preconditions.checkArgument(conditions.size()==conditions.stream()
					.map(pwv -> pwv.prop)
					.distinct()
					.count());
			Preconditions.checkArgument(conditions.stream().noneMatch(pwv -> pwv.values.isEmpty()));
			this.model = model;
			this.useOr = useOr;
		}

		public JsonObject toJson()
		{
			JsonObject out = new JsonObject();
			if(!conditions.isEmpty())
			{
				JsonObject when = new JsonObject();
				for(PropertyWithValues<?> prop : conditions)
				{
					StringBuilder activeString = new StringBuilder();
					for(Object val : prop.values)
					{
						if(activeString.length() > 0)
							activeString.append("|");
						activeString.append(val.toString());
					}
					when.addProperty(prop.prop.getName(), activeString.toString());
				}
				if(useOr)
				{
					JsonObject innerWhen = when;
					when = new JsonObject();
					when.add("OR", innerWhen);
				}
				out.add("when", when);
			}
			out.add("apply", model.toJSON());
			return out;
		}

		public boolean canApplyTo(Block b)
		{
			for(PropertyWithValues<?> p : conditions)
				if(!b.getStateContainer().getProperties().contains(p.prop))
					return false;
			return true;
		}
	}
}

/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface ClocheRenderFunction
{
	float getScale(ItemStack seed, float growth);

	Collection<Pair<BlockState, TRSRTransformation>> getBlocks(ItemStack stack, float growth);


	/**
	 * A map of factories for render functions, used to display blocks inside the cloche.
	 * By default, this map has 4 keys for other developers to use:
	 * "crop", builds a render function for any 1-block crops with an age property
	 * "stacking", builds a render function for stacking plants like sugarcane or cactus
	 * "stem", builds a render function for stem-grown plants like melon or pumpkin
	 * "generic", builds a render function for any block, making it grow in size, like mushrooms
	 */
	Map<String, ClocheRenderFunctionFactory> RENDER_FUNCTION_FACTORIES = new HashMap<>();

	interface ClocheRenderFunctionFactory extends Function<Block, ClocheRenderFunction>
	{
	}

	class ClocheRenderReference
	{
		private final String type;
		private final Block block;

		public ClocheRenderReference(String type, Block block)
		{
			this.type = type;
			this.block = block;
		}

		public String getType()
		{
			return type;
		}

		public Block getBlock()
		{
			return block;
		}

		public void write(PacketBuffer buffer)
		{
			buffer.writeString(getType());
			buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(getBlock()));
		}

		public static ClocheRenderReference read(PacketBuffer buffer)
		{
			String key = buffer.readString();
			ResourceLocation rl = buffer.readResourceLocation();
			return new ClocheRenderReference(key, ForgeRegistries.BLOCKS.getValue(rl));
		}

		public JsonObject serialize()
		{
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("type", getType());
			jsonObject.addProperty("block", ForgeRegistries.BLOCKS.getKey(getBlock()).toString());
			return jsonObject;
		}

		public static ClocheRenderReference deserialize(JsonObject jsonObject)
		{
			String key = JSONUtils.getString(jsonObject, "type");
			ResourceLocation rl = new ResourceLocation(JSONUtils.getString(jsonObject, "block"));
			return new ClocheRenderReference(key, ForgeRegistries.BLOCKS.getValue(rl));
		}
	}
}
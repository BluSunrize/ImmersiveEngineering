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
import com.mojang.math.Transformation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ClocheRenderFunction
{
	float getScale(ItemStack seed, float growth);

	Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth);

	/**
	 * Method to inject quads into the Cloche's plant rendering.
	 * Any quads passed to the consumer will be included in the plant rendering.
	 *
	 *
	 * Immersive Engineering will not cache any quads injected by this method,
	 * therefore it is up to the implementation to make sure quads are properly cached.
	 *
	 * Additionally, even though this method is only called client side, the containing class exists on both sides.
	 * As a result, it is imperative that implementations of this method do not contain direct references to client-only
	 * code.
	 *
	 * @param stack the stack containing the seed for which the plant is being rendered
	 * @param growth the current growth progress of the plant which is being rendered
	 * @param quadConsumer the consumer to pass quads to
	 */
	default void injectQuads(ItemStack stack, float growth, Consumer<?> quadConsumer)
	{

	}


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

		public void write(FriendlyByteBuf buffer)
		{
			buffer.writeUtf(getType());
			buffer.writeResourceLocation(ForgeRegistries.BLOCKS.getKey(getBlock()));
		}

		public static ClocheRenderReference read(FriendlyByteBuf buffer)
		{
			String key = buffer.readUtf();
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
			String key = GsonHelper.getAsString(jsonObject, "type");
			ResourceLocation rl = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
			return new ClocheRenderReference(key, ForgeRegistries.BLOCKS.getValue(rl));
		}
	}
}
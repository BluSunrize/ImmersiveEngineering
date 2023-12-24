/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.function.Consumer;

public interface ClocheRenderFunction
{
	float getScale(ItemStack seed, float growth);

	Collection<Pair<BlockState, Transformation>> getBlocks(ItemStack stack, float growth);

	/**
	 * Method to inject quads into the Cloche's plant rendering.
	 * Any quads passed to the consumer will be included in the plant rendering.<br>
	 * Immersive Engineering will not cache any quads injected by this method,
	 * therefore it is up to the implementation to make sure quads are properly cached.<br>
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

	Codec<? extends ClocheRenderFunction> codec();

	/**
	 * A map of factories for render functions, used to display blocks inside the cloche.
	 * By default, this map has 4 keys for other developers to use:
	 * "crop", builds a render function for any 1-block crops with an age property
	 * "stacking", builds a render function for stacking plants like sugarcane or cactus
	 * "stem", builds a render function for stem-grown plants like melon or pumpkin
	 * "generic", builds a render function for any block, making it grow in size, like mushrooms
	 */
	BiMap<ResourceLocation, Codec<? extends ClocheRenderFunction>> RENDER_FUNCTION_FACTORIES = HashBiMap.create();

	Codec<ClocheRenderFunction> CODEC = ResourceLocation.CODEC.dispatch(
			f -> RENDER_FUNCTION_FACTORIES.inverse().get(f.codec()), RENDER_FUNCTION_FACTORIES::get
	);

	static void write(FriendlyByteBuf buffer, ClocheRenderFunction f)
	{
		buffer.writeJsonWithCodec(CODEC, f);
	}

	static ClocheRenderFunction read(FriendlyByteBuf buffer)
	{
		return buffer.readJsonWithCodec(CODEC);
	}
}

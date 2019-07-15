/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameter;
import net.minecraft.world.storage.loot.functions.ILootFunction;

import java.util.Set;

import static net.minecraft.world.storage.loot.LootParameters.*;

public class TileDropLootFunction implements ILootFunction
{
	private static final TileDropLootFunction INSTANCE = new TileDropLootFunction();

	@Override
	public ItemStack apply(ItemStack itemStack, LootContext lootContext)
	{
		TileEntity tile = lootContext.get(BLOCK_ENTITY);
		if(tile instanceof ITileDrop)
			return ((ITileDrop)tile).getTileDrop(lootContext.get(THIS_ENTITY), lootContext.get(BLOCK_STATE));
		else
			return ItemStack.EMPTY;
	}

	@Override
	public Set<LootParameter<?>> getRequiredParameters()
	{
		return ImmutableSet.of(BLOCK_ENTITY, BLOCK_STATE);
	}

	public static class Serializer extends ILootFunction.Serializer<TileDropLootFunction>
	{

		public Serializer()
		{
			super(new ResourceLocation(ImmersiveEngineering.MODID, "tile_drop"), TileDropLootFunction.class);
		}

		@Override
		public void serialize(JsonObject object, TileDropLootFunction functionClazz, JsonSerializationContext serializationContext)
		{
		}

		@Override
		public TileDropLootFunction deserialize(JsonObject p_212870_1_, JsonDeserializationContext p_212870_2_)
		{
			return INSTANCE;
		}
	}
}

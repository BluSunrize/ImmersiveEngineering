/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillTileEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.Nonnull;

public class WindmillLootFunction extends LootItemConditionalFunction
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "windmill");

	protected WindmillLootFunction(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	protected ItemStack run(@Nonnull ItemStack stack, @Nonnull LootContext context)
	{
		if(stack.getItem()==WoodenDevices.windmill.asItem()&&context.hasParam(LootContextParams.BLOCK_ENTITY))
		{
			BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
			if(te instanceof WindmillTileEntity)
			{
				int sails = ((WindmillTileEntity)te).sails;
				if(sails > 0)
					ItemNBTHelper.putInt(stack, "sails", sails);
			}
		}
		return stack;
	}

	@Override
	public LootItemFunctionType getType()
	{
		return IELootFunctions.windmill;
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<WindmillLootFunction>
	{
		@Nonnull
		@Override
		public WindmillLootFunction deserialize(@Nonnull JsonObject object,
												@Nonnull JsonDeserializationContext deserializationContext,
												@Nonnull LootItemCondition[] conditionsIn)
		{
			return new WindmillLootFunction(conditionsIn);
		}
	}

	public static class Builder extends LootItemConditionalFunction.Builder<blusunrize.immersiveengineering.common.util.loot.WindmillLootFunction.Builder>
	{

		@Nonnull
		@Override
		protected blusunrize.immersiveengineering.common.util.loot.WindmillLootFunction.Builder getThis()
		{
			return this;
		}

		@Nonnull
		@Override
		public LootItemFunction build()
		{
			return new WindmillLootFunction(getConditions());
		}
	}
}

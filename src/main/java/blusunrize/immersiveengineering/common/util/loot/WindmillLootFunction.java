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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillTileEntity;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class WindmillLootFunction extends LootFunction
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "windmill");

	protected WindmillLootFunction(ILootCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	protected ItemStack doApply(@Nonnull ItemStack stack, @Nonnull LootContext context)
	{
		if(stack.getItem()==WoodenDevices.windmill.asItem()&&context.has(LootParameters.BLOCK_ENTITY))
		{
			TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
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
	public LootFunctionType getFunctionType()
	{
		return IELootFunctions.windmill;
	}

	public static class Serializer extends LootFunction.Serializer<WindmillLootFunction>
	{
		@Nonnull
		@Override
		public WindmillLootFunction deserialize(@Nonnull JsonObject object,
												@Nonnull JsonDeserializationContext deserializationContext,
												@Nonnull ILootCondition[] conditionsIn)
		{
			return new WindmillLootFunction(conditionsIn);
		}
	}

	public static class Builder extends LootFunction.Builder<Builder>
	{

		@Nonnull
		@Override
		protected Builder doCast()
		{
			return this;
		}

		@Nonnull
		@Override
		public ILootFunction build()
		{
			return new WindmillLootFunction(getConditions());
		}
	}
}

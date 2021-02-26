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
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;

public class BluprintzLootFunction extends LootFunction
{
	public static final ResourceLocation ID = new ResourceLocation(ImmersiveEngineering.MODID, "secret_bluprintz");

	protected BluprintzLootFunction(ILootCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Nonnull
	@Override
	public ItemStack doApply(ItemStack stack, @Nonnull LootContext context)
	{
		stack.setDisplayName(new StringTextComponent("Super Special BluPrintz"));
		ItemNBTHelper.setLore(stack, new StringTextComponent("Congratulations!"), new StringTextComponent("You have found an easter egg!"));
		return stack;
	}

	@Nonnull
	@Override
	public LootFunctionType getFunctionType()
	{
		return IELootFunctions.bluprintz;
	}

	public static class Serializer extends LootFunction.Serializer<BluprintzLootFunction>
	{
		@Override
		@Nonnull
		public BluprintzLootFunction deserialize(@Nonnull JsonObject object, @Nonnull JsonDeserializationContext deserializationContext, @Nonnull ILootCondition[] conditionsIn)
		{
			return new BluprintzLootFunction(conditionsIn);
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
			return new BluprintzLootFunction(getConditions());
		}
	}

	public static Builder builder()
	{
		return new Builder();
	}
}

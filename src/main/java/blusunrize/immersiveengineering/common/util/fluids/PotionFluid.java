/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PotionFluid extends IEFluid
{
	public PotionFluid(String fluidName, ResourceLocation still, ResourceLocation flowing)
	{
		super(fluidName, still, flowing, null);
	}

	public PotionFluid(String fluidName, ResourceLocation still, ResourceLocation flowing, boolean isSource)
	{
		super(fluidName, still, flowing, null, null, isSource);
	}

	@Override
	protected Fluid createSourceVariant()
	{
		PotionFluid ret = new PotionFluid(fluidName+"_source", stillTex, flowingTex, true);
		ret.flowing = this;
		return ret;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addTooltipInfo(FluidStack fluidStack, @Nullable PlayerEntity player, List<ITextComponent> tooltip)
	{
		if(fluidStack!=null&&fluidStack.hasTag())
		{
			List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluidStack.getOrCreateTag());
			if(effects.isEmpty())
				tooltip.add(new TranslationTextComponent("effect.none").setStyle(new Style().setColor(TextFormatting.GRAY)));
			else
			{
				for(EffectInstance potioneffect : effects)
				{
					ITextComponent s1 = new TranslationTextComponent(potioneffect.getEffectName());
					Effect potion = potioneffect.getPotion();

					if(potioneffect.getAmplifier() > 0)
						s1 = s1.appendText(" ").appendSibling(new TranslationTextComponent("potion.potency."+potioneffect.getAmplifier()));

					if(potioneffect.getDuration() > 20)
						s1 = s1.appendText(" ("+EffectUtils.getPotionDurationString(potioneffect, 1)+")");

					TextFormatting color;
					if(!potion.isBeneficial())
						color = TextFormatting.RED;
					else
						color = TextFormatting.BLUE;
					tooltip.add(s1.setStyle(new Style().setColor(color)));
				}
			}
			Potion potionType = PotionUtils.getPotionTypeFromNBT(fluidStack.getOrCreateTag());
			if(potionType!=Potions.EMPTY)
			{
				String modID = potionType.getRegistryName().getNamespace();
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"potionMod", Utils.getModName(modID))
						.setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
			}
		}
	}

	@Nonnull
	@Override
	protected FluidAttributes createAttributes(Fluid fluid)
	{
		return new PotionFluidAttributes(
				FluidAttributes.builder(fluidName, stillTex, flowingTex)
		);
	}

	private static class PotionFluidAttributes extends FluidAttributes
	{

		public PotionFluidAttributes(Builder builder)
		{
			super(builder);
		}

		@Override
		public int getColor(FluidStack stack)
		{
			if(stack.hasTag())
				return 0xff000000|PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(stack.getTag()));
			return 0xff0000ff;
		}

		@Override
		public ITextComponent getDisplayName(FluidStack stack)
		{
			if(stack.hasTag())
				return new TranslationTextComponent(PotionUtils.getPotionTypeFromNBT(stack.getOrCreateTag())
						.getNamePrefixed("potion.effect."));
			return super.getDisplayName(stack);
		}
	}

}

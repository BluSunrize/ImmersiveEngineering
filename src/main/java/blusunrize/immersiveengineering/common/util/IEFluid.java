/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.IDataSerializer;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * @author BluSunrize - 22.02.2017
 */
public class IEFluid extends Fluid
{
	public IEFluid(String fluidName, ResourceLocation still, ResourceLocation flowing)
	{
		super(fluidName, still, flowing);
	}

	@OnlyIn(Dist.CLIENT)
	public void addTooltipInfo(FluidStack fluidStack, @Nullable PlayerEntity player, List<ITextComponent> tooltip)
	{
	}

	public static class FluidPotion extends IEFluid
	{
		public FluidPotion(String fluidName, ResourceLocation still, ResourceLocation flowing)
		{
			super(fluidName, still, flowing);
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public void addTooltipInfo(FluidStack fluidStack, @Nullable PlayerEntity player, List<ITextComponent> tooltip)
		{
			if(fluidStack!=null&&fluidStack.tag!=null)
			{
				List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluidStack.tag);
				if(effects.isEmpty())
					tooltip.add(TextFormatting.GRAY+I18n.format("effect.none").trim());
				else
				{
					for(EffectInstance potioneffect : effects)
					{
						String s1 = I18n.format(potioneffect.getEffectName()).trim();
						Effect potion = potioneffect.getPotion();

						if(potioneffect.getAmplifier() > 0)
							s1 = s1+" "+I18n.format("potion.potency."+potioneffect.getAmplifier()).trim();

						if(potioneffect.getDuration() > 20)
							s1 = s1+" ("+EffectUtils.getPotionDurationString(potioneffect, 1)+")";

						if(potion.isBadEffect())
							tooltip.add(TextFormatting.RED+s1);
						else
							tooltip.add(TextFormatting.BLUE+s1);
					}
				}
				Potion potionType = PotionUtils.getPotionTypeFromNBT(fluidStack.tag);
				if(potionType!=Potions.EMPTY)
				{
					String modID = potionType.getRegistryName().getNamespace();
					tooltip.add(TextFormatting.DARK_GRAY+I18n.format(Lib.DESC_INFO+"potionMod", Utils.getModName(modID)));
				}
			}
		}

		@Override
		public String getLocalizedName(FluidStack stack)
		{
			if(stack==null||stack.tag==null)
				return super.getLocalizedName(stack);
			return I18n.format(PotionUtils.getPotionTypeFromNBT(stack.tag).getNamePrefixed("potion.effect."));
		}

		@Override
		public int getColor(FluidStack stack)
		{
			if(stack==null||stack.tag!=null)
				return 0xff000000|PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(stack.tag));
			return 0xff0000ff;
		}
	}

	public static final IDataSerializer<Optional<FluidStack>> OPTIONAL_FLUID_STACK = new IDataSerializer<Optional<FluidStack>>()
	{
		@Override
		public void write(PacketBuffer buf, Optional<FluidStack> value)
		{
			buf.writeBoolean(value.isPresent());
			value.ifPresent(fs -> buf.writeCompoundTag(fs.writeToNBT(new CompoundNBT())));
		}

		@Nonnull
		@Override
		public Optional<FluidStack> read(PacketBuffer buf)
		{
			FluidStack fs = !buf.readBoolean()?null: FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
			return Optional.ofNullable(fs);
		}

		@Override
		public DataParameter<Optional<FluidStack>> createKey(int id)
		{
			return new DataParameter<>(id, this);
		}

		@Override
		public Optional<FluidStack> copyValue(Optional<FluidStack> value)
		{
			return value.map(FluidStack::copy);
		}
	};
}

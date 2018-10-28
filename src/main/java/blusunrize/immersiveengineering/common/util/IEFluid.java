/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.PotionTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

/**
 * @author BluSunrize - 22.02.2017
 */
public class IEFluid extends Fluid
{
	public IEFluid(String fluidName, ResourceLocation still, ResourceLocation flowing)
	{
		super(fluidName, still, flowing);
	}

	@SideOnly(Side.CLIENT)
	public void addTooltipInfo(FluidStack fluidStack, @Nullable EntityPlayer player, List<String> tooltip)
	{
	}

	public static class FluidPotion extends IEFluid
	{
		public FluidPotion(String fluidName, ResourceLocation still, ResourceLocation flowing)
		{
			super(fluidName, still, flowing);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addTooltipInfo(FluidStack fluidStack, @Nullable EntityPlayer player, List<String> tooltip)
		{
			if(fluidStack!=null&&fluidStack.tag!=null)
			{
				List<PotionEffect> effects = PotionUtils.getEffectsFromTag(fluidStack.tag);
				if(effects.isEmpty())
					tooltip.add(TextFormatting.GRAY+I18n.translateToLocal("effect.none").trim());
				else
				{
					for(PotionEffect potioneffect : effects)
					{
						String s1 = I18n.translateToLocal(potioneffect.getEffectName()).trim();
						Potion potion = potioneffect.getPotion();

						if(potioneffect.getAmplifier() > 0)
							s1 = s1+" "+I18n.translateToLocal("potion.potency."+potioneffect.getAmplifier()).trim();

						if(potioneffect.getDuration() > 20)
							s1 = s1+" ("+Potion.getPotionDurationString(potioneffect, 1)+")";

						if(potion.isBadEffect())
							tooltip.add(TextFormatting.RED+s1);
						else
							tooltip.add(TextFormatting.BLUE+s1);
					}
				}
				PotionType potionType = PotionUtils.getPotionTypeFromNBT(fluidStack.tag);
				if(potionType!=PotionTypes.EMPTY)
				{
					String modID = potionType.getRegistryName().getNamespace();
					tooltip.add(TextFormatting.DARK_GRAY+I18n.translateToLocalFormatted(Lib.DESC_INFO+"potionMod", Utils.getModName(modID)));
				}
			}
		}

		@Override
		public String getLocalizedName(FluidStack stack)
		{
			if(stack==null||stack.tag==null)
				return super.getLocalizedName(stack);
			return I18n.translateToLocal(PotionUtils.getPotionTypeFromNBT(stack.tag).getNamePrefixed("potion.effect."));
		}

		@Override
		public int getColor(FluidStack stack)
		{
			if(stack==null||stack.tag!=null)
				return 0xff000000|PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(stack.tag));
			return 0xff0000ff;
		}
	}

	public static final DataSerializer<Optional<FluidStack>> OPTIONAL_FLUID_STACK = new DataSerializer<Optional<FluidStack>>()
	{
		@Override
		public void write(PacketBuffer buf, Optional<FluidStack> value)
		{
			buf.writeBoolean(value.isPresent());
			FluidStack fs = value.orNull();
			if(fs!=null)
				buf.writeCompoundTag(fs.writeToNBT(new NBTTagCompound()));
		}

		@Override
		public Optional<FluidStack> read(PacketBuffer buf) throws IOException
		{
			FluidStack fs = !buf.readBoolean()?null: FluidStack.loadFluidStackFromNBT(buf.readCompoundTag());
			return Optional.fromNullable(fs);
		}

		@Override
		public DataParameter<Optional<FluidStack>> createKey(int id)
		{
			return new DataParameter(id, this);
		}

		@Override
		public Optional<FluidStack> copyValue(Optional<FluidStack> value)
		{
			return value.isPresent()?Optional.of(value.get().copy()): Optional.absent();
		}
	};
}

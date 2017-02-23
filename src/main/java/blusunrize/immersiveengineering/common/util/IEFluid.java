package blusunrize.immersiveengineering.common.util;

import com.google.common.base.Optional;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
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
			if(fluidStack!=null && fluidStack.tag!=null)
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

						if(potioneffect.getAmplifier()>0)
							s1 = s1+" "+I18n.translateToLocal("potion.potency."+potioneffect.getAmplifier()).trim();

						if(potioneffect.getDuration()>20)
							s1 = s1+" ("+Potion.getPotionDurationString(potioneffect, 1)+")";

						if(potion.isBadEffect())
							tooltip.add(TextFormatting.RED+s1);
						else
							tooltip.add(TextFormatting.BLUE+s1);
					}
				}
			}
		}

		@Override
		public String getLocalizedName(FluidStack stack)
		{
			if(stack.tag==null)
				return super.getLocalizedName(stack);
			return net.minecraft.util.text.translation.I18n.translateToLocal(PotionUtils.getPotionTypeFromNBT(stack.tag).getNamePrefixed("potion.effect."));
		}

		@Override
		public int getColor(FluidStack stack)
		{
			if(stack.tag!=null)
				return PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(stack.tag));
			return 0x0000ff;
		}
	}

	public static final DataSerializer<Optional<FluidStack>> OPTIONAL_FLUID_STACK = new DataSerializer<Optional<FluidStack>>()
	{
		@Override
		public void write(PacketBuffer buf, Optional<FluidStack> value)
		{
			FluidStack fs = value.orNull();
			if(fs==null)
				buf.writeShort(-1);
			else
				buf.writeNBTTagCompoundToBuffer(fs.writeToNBT(new NBTTagCompound()));
		}
		@Override
		public Optional<FluidStack> read(PacketBuffer buf) throws java.io.IOException
		{
			FluidStack fs = buf.readShort()<0?null : FluidStack.loadFluidStackFromNBT(buf.readNBTTagCompoundFromBuffer());
			return Optional.fromNullable(fs);
		}
		@Override
		public DataParameter<Optional<FluidStack>> createKey(int id)
		{
			return new DataParameter(id, this);
		}
	};
	static
	{
		DataSerializers.registerSerializer(OPTIONAL_FLUID_STACK);
	}
}

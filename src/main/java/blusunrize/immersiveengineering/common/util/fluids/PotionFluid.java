/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.fluids;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.*;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidAttributes.Builder;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.List;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class PotionFluid extends Fluid
{
	public PotionFluid()
	{
		setRegistryName(ImmersiveEngineering.MODID, "potion");
		IEContent.registeredIEFluids.add(this);
	}

	@Nonnull
	@Override
	public Item getFilledBucket()
	{
		return Items.AIR;
	}

	@Override
	protected boolean canDisplace(@Nonnull FluidState p_215665_1_, @Nonnull IBlockReader p_215665_2_,
								  @Nonnull BlockPos p_215665_3_, @Nonnull Fluid p_215665_4_, @Nonnull Direction p_215665_5_)
	{
		return true;
	}

	@Nonnull
	@Override
	protected Vector3d getFlow(@Nonnull IBlockReader p_215663_1_, @Nonnull BlockPos p_215663_2_, @Nonnull FluidState p_215663_3_)
	{
		return Vector3d.ZERO;
	}

	@Override
	public int getTickRate(IWorldReader p_205569_1_)
	{
		return 0;
	}

	@Override
	protected float getExplosionResistance()
	{
		return 0;
	}

	@Override
	public float getActualHeight(@Nonnull FluidState p_215662_1_, @Nonnull IBlockReader p_215662_2_, @Nonnull BlockPos p_215662_3_)
	{
		return 0;
	}

	@Override
	public float getHeight(@Nonnull FluidState p_223407_1_)
	{
		return 0;
	}

	@Nonnull
	@Override
	protected BlockState getBlockState(@Nonnull FluidState state)
	{
		return Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isSource(@Nonnull FluidState state)
	{
		return true;
	}

	@Override
	public int getLevel(@Nonnull FluidState p_207192_1_)
	{
		return 0;
	}

	@Nonnull
	@Override
	public VoxelShape func_215664_b(@Nonnull FluidState p_215664_1_, @Nonnull IBlockReader p_215664_2_, @Nonnull BlockPos p_215664_3_)
	{
		return VoxelShapes.empty();
	}

	@Override
	protected FluidAttributes createAttributes()
	{
		Builder builder = FluidAttributes.builder(rl("block/fluid/potion_still"), rl("block/fluid/potion_flowing"));
		return new PotionFluidAttributes(builder, this);
	}

	public void addInformation(FluidStack fluidStack, List<ITextComponent> tooltip)
	{
		if(fluidStack!=null&&fluidStack.hasTag())
		{
			List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluidStack.getTag());
			if(effects.isEmpty())
				tooltip.add(new TranslationTextComponent("effect.none").applyTextStyle(TextFormatting.GRAY));
			else
			{
				for(EffectInstance instance : effects)
				{
					ITextComponent itextcomponent = new TranslationTextComponent(instance.getEffectName());
					Effect effect = instance.getPotion();
					if(instance.getAmplifier() > 0)
						itextcomponent.appendText(" ").appendSibling(new TranslationTextComponent("potion.potency."+instance.getAmplifier()));
					if(instance.getDuration() > 20)
						itextcomponent.appendText(" (").appendText(EffectUtils.getPotionDurationString(instance, 1)).appendText(")");

					tooltip.add(itextcomponent.applyTextStyle(effect.getEffectType().getColor()));
				}
			}
			Potion potionType = PotionUtils.getPotionTypeFromNBT(fluidStack.getTag());
			if(potionType!=Potions.EMPTY)
			{
				String modID = potionType.getRegistryName().getNamespace();
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"potionMod", Utils.getModName(modID)).applyTextStyle(TextFormatting.DARK_GRAY));
			}
		}
	}

	public static class PotionFluidAttributes extends FluidAttributes
	{
		protected PotionFluidAttributes(Builder builder, Fluid fluid)
		{
			super(builder, fluid);
		}

		@Override
		public ITextComponent getDisplayName(FluidStack stack)
		{
			if(stack==null||!stack.hasTag())
				return super.getDisplayName(stack);
			return new TranslationTextComponent(PotionUtils.getPotionTypeFromNBT(stack.getTag()).getNamePrefixed("item.minecraft.potion.effect."));
		}

		@Override
		public int getColor(FluidStack stack)
		{
			if(stack==null||!stack.hasTag())
				return 0xff0000ff;
			return 0xff000000|PotionUtils.getPotionColorFromEffectList(PotionUtils.getEffectsFromTag(stack.getTag()));
		}
	}
}

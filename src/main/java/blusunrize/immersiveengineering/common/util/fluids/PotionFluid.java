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
import blusunrize.immersiveengineering.common.items.PotionBucketItem;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidAttributes.Builder;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class PotionFluid extends Fluid
{
	public static final Item bucket = new PotionBucketItem();

	public PotionFluid()
	{
		setRegistryName(ImmersiveEngineering.MODID, "potion");
		IEContent.registeredIEFluids.add(this);
	}

	public static FluidStack getFluidStackForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidStack(Fluids.WATER, amount);
		FluidStack stack = new FluidStack(IEContent.fluidPotion, amount);
		stack.getOrCreateTag().putString("Potion", type.getRegistryName().toString());
		return stack;
	}

	public static Potion getType(FluidStack stack)
	{
		return fromTag(stack.getTag());
	}

	public static Potion fromTag(@Nullable CompoundNBT tag)
	{
		if(tag==null||!tag.contains("Potion", NBT.TAG_STRING))
			return Potions.WATER;
		ResourceLocation name = ResourceLocation.tryCreate(tag.getString("Potion"));
		if(name==null)
			return Potions.WATER;
		Potion result = ForgeRegistries.POTION_TYPES.getValue(name);
		return result==Potions.EMPTY?Potions.WATER: result;
	}

	@Nonnull
	@Override
	public Item getFilledBucket()
	{
		return Items.AIR;
	}

	@Override
	protected boolean canDisplace(@Nonnull FluidState fluidState, @Nonnull IBlockReader blockReader,
								  @Nonnull BlockPos pos, @Nonnull Fluid fluid, @Nonnull Direction direction)
	{
		return true;
	}

	@Nonnull
	@Override
	protected Vector3d getFlow(@Nonnull IBlockReader blockReader, @Nonnull BlockPos pos, @Nonnull FluidState fluidState)
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
	public int getLevel(@Nonnull FluidState state)
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
		Builder builder = FluidAttributes.builder(rl("block/fluid/potion_still"), rl("block/fluid/potion_flow"));
		return new PotionFluidAttributes(builder, this);
	}

	public void addInformation(FluidStack fluidStack, List<ITextComponent> tooltip)
	{
		if(fluidStack!=null&&fluidStack.hasTag())
		{
			List<EffectInstance> effects = PotionUtils.getEffectsFromTag(fluidStack.getTag());
			if(effects.isEmpty())
				tooltip.add(new TranslationTextComponent("effect.none").mergeStyle(TextFormatting.GRAY));
			else
			{
				for(EffectInstance instance : effects)
				{
					IFormattableTextComponent itextcomponent = new TranslationTextComponent(instance.getEffectName());
					Effect effect = instance.getPotion();
					if(instance.getAmplifier() > 0)
						itextcomponent.appendString(" ").appendSibling(new TranslationTextComponent("potion.potency."+instance.getAmplifier()));
					if(instance.getDuration() > 20)
						itextcomponent.appendString(" (").appendString(EffectUtils.getPotionDurationString(instance, 1)).appendString(")");

					tooltip.add(itextcomponent.mergeStyle(effect.getEffectType().getColor()));
				}
			}
			Potion potionType = PotionUtils.getPotionTypeFromNBT(fluidStack.getTag());
			if(potionType!=Potions.EMPTY)
			{
				String modID = potionType.getRegistryName().getNamespace();
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"potionMod", Utils.getModName(modID)).mergeStyle(TextFormatting.DARK_GRAY));
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

		@Override
		public ItemStack getBucket(FluidStack stack)
		{
			return PotionBucketItem.forPotion(getType(stack));
		}
	}
}

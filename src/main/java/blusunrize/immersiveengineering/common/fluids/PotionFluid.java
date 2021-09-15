/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.items.PotionBucketItem;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
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
	public static FluidStack getFluidStackForType(Potion type, int amount)
	{
		if(type==Potions.WATER||type==null)
			return new FluidStack(Fluids.WATER, amount);
		FluidStack stack = new FluidStack(IEFluids.POTION.get(), amount);
		stack.getOrCreateTag().putString("Potion", type.getRegistryName().toString());
		return stack;
	}

	public static Potion getType(FluidStack stack)
	{
		return fromTag(stack.getTag());
	}

	public static Potion fromTag(@Nullable CompoundTag tag)
	{
		if(tag==null||!tag.contains("Potion", NBT.TAG_STRING))
			return Potions.WATER;
		ResourceLocation name = ResourceLocation.tryParse(tag.getString("Potion"));
		if(name==null)
			return Potions.WATER;
		Potion result = ForgeRegistries.POTIONS.getValue(name);
		return result==Potions.EMPTY?Potions.WATER: result;
	}

	@Nonnull
	@Override
	public Item getBucket()
	{
		return Items.AIR;
	}

	@Override
	protected boolean canBeReplacedWith(@Nonnull FluidState fluidState, @Nonnull BlockGetter blockReader,
								  @Nonnull BlockPos pos, @Nonnull Fluid fluid, @Nonnull Direction direction)
	{
		return true;
	}

	@Nonnull
	@Override
	protected Vec3 getFlow(@Nonnull BlockGetter blockReader, @Nonnull BlockPos pos, @Nonnull FluidState fluidState)
	{
		return Vec3.ZERO;
	}

	@Override
	public int getTickDelay(LevelReader p_205569_1_)
	{
		return 0;
	}

	@Override
	protected float getExplosionResistance()
	{
		return 0;
	}

	@Override
	public float getHeight(@Nonnull FluidState p_215662_1_, @Nonnull BlockGetter p_215662_2_, @Nonnull BlockPos p_215662_3_)
	{
		return 0;
	}

	@Override
	public float getOwnHeight(@Nonnull FluidState p_223407_1_)
	{
		return 0;
	}

	@Nonnull
	@Override
	protected BlockState createLegacyBlock(@Nonnull FluidState state)
	{
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean isSource(@Nonnull FluidState state)
	{
		return true;
	}

	@Override
	public int getAmount(@Nonnull FluidState state)
	{
		return 0;
	}

	@Nonnull
	@Override
	public VoxelShape getShape(@Nonnull FluidState p_215664_1_, @Nonnull BlockGetter p_215664_2_, @Nonnull BlockPos p_215664_3_)
	{
		return Shapes.empty();
	}

	@Override
	protected FluidAttributes createAttributes()
	{
		Builder builder = FluidAttributes.builder(rl("block/fluid/potion_still"), rl("block/fluid/potion_flow"));
		return new PotionFluidAttributes(builder, this);
	}

	public void addInformation(FluidStack fluidStack, List<Component> tooltip)
	{
		if(fluidStack!=null&&fluidStack.hasTag())
		{
			List<MobEffectInstance> effects = PotionUtils.getAllEffects(fluidStack.getTag());
			if(effects.isEmpty())
				tooltip.add(new TranslatableComponent("effect.none").withStyle(ChatFormatting.GRAY));
			else
			{
				for(MobEffectInstance instance : effects)
				{
					MutableComponent itextcomponent = new TranslatableComponent(instance.getDescriptionId());
					MobEffect effect = instance.getEffect();
					if(instance.getAmplifier() > 0)
						itextcomponent.append(" ").append(new TranslatableComponent("potion.potency."+instance.getAmplifier()));
					if(instance.getDuration() > 20)
						itextcomponent.append(" (").append(MobEffectUtil.formatDuration(instance, 1)).append(")");

					tooltip.add(itextcomponent.withStyle(effect.getCategory().getTooltipFormatting()));
				}
			}
			Potion potionType = PotionUtils.getPotion(fluidStack.getTag());
			if(potionType!=Potions.EMPTY)
			{
				String modID = potionType.getRegistryName().getNamespace();
				tooltip.add(new TranslatableComponent(Lib.DESC_INFO+"potionMod", Utils.getModName(modID)).withStyle(ChatFormatting.DARK_GRAY));
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
		public Component getDisplayName(FluidStack stack)
		{
			if(stack==null||!stack.hasTag())
				return super.getDisplayName(stack);
			return new TranslatableComponent(PotionUtils.getPotion(stack.getTag()).getName("item.minecraft.potion.effect."));
		}

		@Override
		public int getColor(FluidStack stack)
		{
			if(stack==null||!stack.hasTag())
				return 0xff0000ff;
			return 0xff000000|PotionUtils.getColor(PotionUtils.getAllEffects(stack.getTag()));
		}

		@Override
		public ItemStack getBucket(FluidStack stack)
		{
			return PotionBucketItem.forPotion(getType(stack));
		}
	}
}

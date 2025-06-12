/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.fluids;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import blusunrize.immersiveengineering.common.items.PotionBucketItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
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
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class PotionFluid extends Fluid
{
	public static FluidStack getFluidStackForType(Optional<Holder<Potion>> type, int amount, PotionBottleType bottleType)
	{
		if(type.isEmpty()||type.get().is(Potions.WATER))
			return new FluidStack(Fluids.WATER, amount);
		FluidStack stack = new FluidStack(IEFluids.POTION.get(), amount);
		stack.set(DataComponents.POTION_CONTENTS, new PotionContents(type.get()));
		stack.set(IEDataComponents.POTION_BOTTLE_TYPE, bottleType);
		return stack;
	}

	public static Holder<Potion> getType(FluidStack stack)
	{
		return stack.getOrDefault(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER))
				.potion()
				.orElse(Potions.WATER);
	}

	@Nonnull
	@Override
	public Item getBucket()
	{
		return Misc.POTION_BUCKET.get();
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

	@Nonnull
	@Override
	public FluidType getFluidType()
	{
		return IEFluids.POTION_TYPE.value();
	}

	public void addInformation(FluidStack fluidStack, Consumer<Component> tooltip)
	{
		var potionData = fluidStack.get(DataComponents.POTION_CONTENTS);
		if(potionData==null)
			return;
		List<MobEffectInstance> effects = new ArrayList<>();
		potionData.forEachEffect(effects::add);
		if(effects.isEmpty())
			tooltip.accept(Component.translatable("effect.none").withStyle(ChatFormatting.GRAY));
		else
		{
			for(MobEffectInstance instance : effects)
			{
				MutableComponent itextcomponent = Component.translatable(instance.getDescriptionId());
				MobEffect effect = instance.getEffect().value();
				if(instance.getAmplifier() > 0)
					itextcomponent.append(" ").append(Component.translatable("potion.potency."+instance.getAmplifier()));
				if(instance.getDuration() > 20)
					itextcomponent.append(" (").append(MobEffectUtil.formatDuration(instance, 1, 20)).append(")");

				tooltip.accept(itextcomponent.withStyle(effect.getCategory().getTooltipFormatting()));
			}
		}
		if(potionData.potion().isPresent())
		{
			String modID = potionData.potion().get().unwrapKey().orElseThrow().location().getNamespace();
			tooltip.accept(Component.translatable(Lib.DESC_INFO+"potionMod", Utils.getModName(modID)).withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	public static class PotionFluidType extends FluidType
	{
		public static final ResourceLocation TEXTURE_STILL = rl("block/fluid/potion_still");
		public static final ResourceLocation TEXTURE_FLOW = rl("block/fluid/potion_flow");

		public PotionFluidType()
		{
			super(Properties.create()
					.sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
					.sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
					.density(1300));
		}

		@Override
		public Component getDescription(FluidStack stack)
		{
			var potionData = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
			PotionBottleType s = stack.getOrDefault(IEDataComponents.POTION_BOTTLE_TYPE, PotionBottleType.REGULAR);
			return Component.translatable(Potion.getName(potionData.potion(), s.getBottleItem().getDescriptionId()+".effect."));
		}

		@Override
		public ItemStack getBucket(FluidStack stack)
		{
			return PotionBucketItem.forPotion(getType(stack));
		}
	}

	public enum PotionBottleType implements StringRepresentable
	{
		REGULAR(Items.POTION), SPLASH(Items.SPLASH_POTION), LINGERING(Items.LINGERING_POTION);

		private final Item bottleItem;

		public static final DualCodec<ByteBuf, PotionBottleType> CODEC = IEDualCodecs.forEnum(values());

		PotionBottleType(Item bottleItem)
		{
			this.bottleItem = bottleItem;
		}

		@Override
		public @NotNull String getSerializedName()
		{
			return name().toLowerCase(Locale.ROOT);
		}

		public static PotionBottleType fromItem(Holder<Item> item)
		{
			if(item.value()==Items.LINGERING_POTION)
				return LINGERING;
			if(item.value()==Items.SPLASH_POTION)
				return SPLASH;
			return REGULAR;
		}

		public Item getBottleItem()
		{
			return bottleItem;
		}
	}

}

/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class SurveyToolsItem extends IEBaseItem
{
	private static final List<BiPredicate<Level, BlockPos>> CAN_USE_ON = new ArrayList<>();

	public SurveyToolsItem()
	{
		super(new Properties().stacksTo(1).durability(300));
		// earthen materials
		CAN_USE_ON.add((world, pos) -> world.getBlockState(pos).is(IETags.surveyToolTargets));
		// Stone, Diorite, Andesite, etc.
		CAN_USE_ON.add((world, pos) -> world.getBlockState(pos).is(Tags.Blocks.ORE_BEARING_GROUND_STONE));
		// Nether materials
		CAN_USE_ON.add((world, pos) -> {
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			return state.is(Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK)
					||block==Blocks.SOUL_SAND
					||block==Blocks.BLACKSTONE
					||block==Blocks.BASALT;
		});
		// soft rocks
		CAN_USE_ON.add((world, pos) -> {
			BlockState state = world.getBlockState(pos);
			return state.is(Tags.Blocks.STONES)&&state.getDestroySpeed(world, pos) < 0.5;
		});
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack p_41454_, LivingEntity p_344979_)
	{
		return 50;
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context)
	{
		Player player = context.getPlayer();
		if(player==null)
			return InteractionResult.PASS;
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if(!CAN_USE_ON.stream().anyMatch(predicate -> predicate.test(world, pos)))
		{
			player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"survey.wrong_block"), true);
			return InteractionResult.FAIL;
		}
		player.startUsingItem(context.getHand());
		return InteractionResult.SUCCESS;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving)
	{
		if(!(entityLiving instanceof ServerPlayer player))
			return stack;
		BlockHitResult rtr = getPlayerPOVHitResult(world, player, Fluid.NONE);
		BlockPos pos = rtr.getBlockPos();
		MineralVein vein = ExcavatorHandler.getRandomMineral(world, pos);
		if(vein==null||vein.getMineral(world)==null)
		{
			player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"survey.no_vein"), true);
			return stack;
		}

		List<HintedPosition> oldHints = getVeinData(stack, world.dimension(), vein.getPos());
		int dataCount = oldHints.size();
		/* I considered not giving any information after 3 surveys, but because the text is displayed above the action
		 * bar and can't be brought back after it fades, that could lead to frustration

		if(dataCount >= 3)
		{
			player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_INFO+"survey.finished"), true);
			return stack;
		}
		 */

		// Need at least 4 blocks between samples
		boolean tooClose = oldHints.stream().anyMatch(hint -> {
			int dX = hint.x-pos.getX();
			int dZ = hint.z-pos.getZ();
			return dX*dX+dZ*dZ < 16;
		});
		if(tooClose)
		{
			player.displayClientMessage(Component.translatable(Lib.CHAT_INFO+"survey.too_close"), true);
			return stack;
		}

		Component response;
		// Get angle between postion->center vector and standard (south facing) vector
		Vec2 vecToCenter = new Vec2(vein.getPos().x()-pos.getX(), vein.getPos().z()-pos.getZ());
		if(vecToCenter.x==0&&vecToCenter.y==0) // hit the vein center directly
			response = Component.translatable(Lib.CHAT_INFO+"survey.hint.center",
					Component.translatable(MineralMix.getTranslationKey(vein.getMineralName())));
		else
		{
			double angle = Math.toDegrees(Math.atan2(vecToCenter.y, vecToCenter.x));
			int segment = (int)((angle+270)%360/45);

			response = switch(dataCount)
			{
				case 0 -> // hint at the type of vein
						Component.translatable(Lib.CHAT_INFO+"survey.hint.1",
								Component.translatable(MineralMix.getTranslationKey(vein.getMineralName())));
				case 1 -> // hint at the direction
						Component.translatable(Lib.CHAT_INFO+"survey.hint.2",
								Component.translatable(MineralMix.getTranslationKey(vein.getMineralName())),
								Component.translatable(Lib.CHAT_INFO+"survey.direction."+segment)); // hint at distance
				default -> Component.translatable(Lib.CHAT_INFO+"survey.hint.3",
						Component.translatable(MineralMix.getTranslationKey(vein.getMineralName())),
						Math.round(Math.sqrt(vecToCenter.x*vecToCenter.x+vecToCenter.y*vecToCenter.y)),
						Component.translatable(Lib.CHAT_INFO+"survey.direction."+segment));
			};
		}
		// Send message to player
		player.displayClientMessage(response, false);
		addHintedPosition(stack, world.dimension(), vein.getPos(), new HintedPosition(pos.getX(), pos.getZ(), response));

		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 1.0F, 1.0F+(world.random.nextFloat()-world.random.nextFloat())*0.4F);
		stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

		return stack;
	}

	private static final String DATA_KEY = "veinData";

	public static List<HintedPosition> getVeinData(ItemStack surveyTools, ResourceKey<Level> dimension, ColumnPos veinPos)
	{
		List<VeinEntry> allVeins = surveyTools.getOrDefault(IEDataComponents.SURVERYTOOL_DATA, List.of());
		for(final var vein : allVeins)
			if(vein.x==veinPos.x()&&vein.z==veinPos.z()&&vein.level.equals(dimension))
				return vein.hinted;
		return List.of();
	}

	public static void addHintedPosition(ItemStack surveyTools, ResourceKey<Level> dimension, ColumnPos veinPos, HintedPosition toAdd)
	{
		List<VeinEntry> newVeins = new ArrayList<>(surveyTools.getOrDefault(IEDataComponents.SURVERYTOOL_DATA, List.of()));
		boolean found = false;
		for(int i = 0; i < newVeins.size(); ++i)
		{
			final var vein = newVeins.get(i);
			if(vein.x==veinPos.x()&&vein.z==veinPos.z()&&vein.level.equals(dimension))
			{
				found = true;
				final var newHints = new ArrayList<>(vein.hinted);
				newHints.add(toAdd);
				newVeins.set(i, new VeinEntry(vein.x, vein.z, vein.level, newHints));
				break;
			}
		}
		if(!found)
			newVeins.add(new VeinEntry(veinPos.x(), veinPos.z(), dimension, List.of(toAdd)));
		surveyTools.set(IEDataComponents.SURVERYTOOL_DATA, newVeins);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player)
	{
		return true;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack)
	{
		return false;
	}

	private record HintedPosition(int x, int z, Component hintText)
	{
		public static final DualCodec<RegistryFriendlyByteBuf, HintedPosition> CODECS = DualCodecs.composite(
				DualCodecs.INT.fieldOf("x"), HintedPosition::x,
				DualCodecs.INT.fieldOf("z"), HintedPosition::z,
				DualCodecs.CHAT_COMPONENT.fieldOf("hintText"), HintedPosition::hintText,
				HintedPosition::new
		);
	}

	public record VeinEntry(int x, int z, ResourceKey<Level> level, List<HintedPosition> hinted)
	{
		public static final DualCodec<RegistryFriendlyByteBuf, VeinEntry> CODECS = DualCodecs.composite(
				DualCodecs.INT.fieldOf("x"), VeinEntry::x,
				DualCodecs.INT.fieldOf("z"), VeinEntry::z,
				DualCodecs.resourceKey(Registries.DIMENSION).fieldOf("level"), VeinEntry::level,
				HintedPosition.CODECS.listOf().fieldOf("hinted"), VeinEntry::hinted,
				VeinEntry::new
		);
	}
}


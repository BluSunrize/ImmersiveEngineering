/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class SurveyToolsItem extends IEBaseItem
{
	private static final List<BiPredicate<Level, BlockPos>> CAN_USE_ON = new ArrayList<>();

	public SurveyToolsItem()
	{
		super(new Properties().stacksTo(1).defaultDurability(300));
		// earthen materials
		CAN_USE_ON.add((world, pos) -> {
			BlockState state = world.getBlockState(pos);
			return state.getMaterial()==Material.CLAY||state.getMaterial()==Material.DIRT
					||state.getMaterial()==Material.GRASS||state.getMaterial()==Material.SAND;
		});
		// Stone, Diorite, Andesite, etc.
		CAN_USE_ON.add((world, pos) -> Tags.Blocks.STONE.contains(world.getBlockState(pos).getBlock()));
		// Stone, Diorite, Andesite, etc.
		CAN_USE_ON.add((world, pos) -> {
			Block block = world.getBlockState(pos).getBlock();
			return block==Blocks.BLACKSTONE||block==Blocks.BASALT;
		});
		// soft rocks
		CAN_USE_ON.add((world, pos) -> {
			BlockState state = world.getBlockState(pos);
			return state.getMaterial()==Material.STONE&&state.getDestroySpeed(world, pos) < 0.5;
		});
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack)
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
			player.displayClientMessage(new TranslatableComponent(Lib.CHAT_INFO+"survey.wrong_block"), true);
			return InteractionResult.FAIL;
		}
		player.startUsingItem(context.getHand());
		return InteractionResult.SUCCESS;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving)
	{
		if(!(entityLiving instanceof ServerPlayer))
			return stack;
		ServerPlayer player = (ServerPlayer)entityLiving;
		HitResult rtr = getPlayerPOVHitResult(world, player, Fluid.NONE);
		if(!(rtr instanceof BlockHitResult))
			return stack;
		BlockPos pos = ((BlockHitResult)rtr).getBlockPos();
		MineralVein vein = ExcavatorHandler.getRandomMineral(world, pos);
		if(vein==null || vein.getMineral()==null)
		{
			player.displayClientMessage(new TranslatableComponent(Lib.CHAT_INFO+"survey.no_vein"), true);
			return stack;
		}

		ListTag data = getVeinData(stack, world.dimension(), vein.getPos());
		int dataCount = data.size();
		/* I considered not giving any information after 3 surveys, but because the text is displayed above the action
		 * bar and can't be brought back after it fades, that could lead to frustration

		if(dataCount >= 3)
		{
			player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_INFO+"survey.finished"), true);
			return stack;
		}
		 */

		// Need at least 4 blocks between samples
		boolean tooClose = data.stream().anyMatch(inbt -> {
			int dX = ((CompoundTag)inbt).getInt("x")-pos.getX();
			int dZ = ((CompoundTag)inbt).getInt("z")-pos.getZ();
			return dX*dX+dZ*dZ < 16;
		});
		if(tooClose)
		{
			player.displayClientMessage(new TranslatableComponent(Lib.CHAT_INFO+"survey.too_close"), true);
			return stack;
		}

		Component response;
		// Get angle between postion->center vector and standard (south facing) vector
		Vec2 vecToCenter = new Vec2(vein.getPos().x-pos.getX(), vein.getPos().z-pos.getZ());
		if(vecToCenter.x==0&&vecToCenter.y==0) // hit the vein center directly
			response = new TranslatableComponent(Lib.CHAT_INFO+"survey.hint.center",
					new TranslatableComponent(vein.getMineral().getTranslationKey()));
		else
		{
			double angle = Math.toDegrees(Math.atan2(vecToCenter.y, vecToCenter.x));
			int segment = (int)((angle+270)%360/45);

			switch(dataCount)
			{
				case 0: // hint at the type of vein
					response = new TranslatableComponent(Lib.CHAT_INFO+"survey.hint.1",
							new TranslatableComponent(vein.getMineral().getTranslationKey()));
					break;
				case 1: // hint at the direction
					response = new TranslatableComponent(Lib.CHAT_INFO+"survey.hint.2",
							new TranslatableComponent(vein.getMineral().getTranslationKey()),
							new TranslatableComponent(Lib.CHAT_INFO+"survey.direction."+segment));
					break;
				case 2: // hint at distance
				default:
					response = new TranslatableComponent(Lib.CHAT_INFO+"survey.hint.3",
							new TranslatableComponent(vein.getMineral().getTranslationKey()),
							Math.round(Math.sqrt(vecToCenter.x*vecToCenter.x+vecToCenter.y*vecToCenter.y)),
							new TranslatableComponent(Lib.CHAT_INFO+"survey.direction."+segment));
					break;
			}
		}
		// Send message to player
		player.displayClientMessage(response, true);

		// Add entry for current position
		CompoundTag tag = new CompoundTag();
		tag.putInt("x", pos.getX());
		tag.putInt("z", pos.getZ());
		// Lets save the hint, in case we ever want to give this a GUI
		tag.putString("hint", Component.Serializer.toJson(response));
		data.add(tag);

		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.NEUTRAL, 1.0F, 1.0F+(world.random.nextFloat()-world.random.nextFloat())*0.4F);
		stack.hurtAndBreak(1, player, (user) -> {
			user.broadcastBreakEvent(user.getUsedItemHand());
		});

		return stack;
	}

	private static final String DATA_KEY = "veinData";

	public static ListTag getVeinData(ItemStack surveyTools, ResourceKey<Level> dimension, ColumnPos veinPos)
	{
		ListTag list = surveyTools.getOrCreateTag().getList(DATA_KEY, NBT.TAG_COMPOUND);
		CompoundTag tag = null;
		String dimString = dimension.location().toString();
		for(Tag nbt : list)
		{
			CompoundTag tmp = (CompoundTag)nbt;
			if(dimString.equals(tmp.getString("dimension"))&&tmp.getInt("x")==veinPos.x&&tmp.getInt("z")==veinPos.z)
			{
				tag = tmp;
				break;
			}
		}
		if(tag==null)
		{
			tag = new CompoundTag();
			tag.putString("dimension", dimString);
			tag.putInt("x", veinPos.x);
			tag.putInt("z", veinPos.z);
			list.add(tag);
			surveyTools.getOrCreateTag().put(DATA_KEY, list);
		}
		if(tag.contains("data", NBT.TAG_LIST))
			return tag.getList("data", NBT.TAG_COMPOUND);
		ListTag data = new ListTag();
		tag.put("data", data);
		return data;
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
}


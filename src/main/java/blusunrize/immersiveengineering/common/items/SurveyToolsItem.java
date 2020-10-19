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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class SurveyToolsItem extends IEBaseItem
{
	private static final List<BiPredicate<World, BlockPos>> CAN_USE_ON = new ArrayList<>();

	public SurveyToolsItem()
	{
		super("survey_tools", new Properties().maxStackSize(1).defaultMaxDamage(300));
		// earthen materials
		CAN_USE_ON.add((world, pos) -> {
			BlockState state = world.getBlockState(pos);
			return state.getMaterial()==Material.CLAY||state.getMaterial()==Material.EARTH
					||state.getMaterial()==Material.ORGANIC||state.getMaterial()==Material.SAND;
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
			return state.getMaterial()==Material.ROCK&&state.getBlockHardness(world, pos) < 0.5;
		});
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		super.addInformation(stack, worldIn, tooltip, flagIn);
	}

	@Override
	public UseAction getUseAction(ItemStack stack)
	{
		return UseAction.BOW;
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return 50;
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		if(player==null)
			return ActionResultType.PASS;
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		if(!CAN_USE_ON.stream().anyMatch(predicate -> predicate.test(world, pos)))
		{
			player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_INFO+"survey.wrong_block"), true);
			return ActionResultType.FAIL;
		}
		player.setActiveHand(context.getHand());
		return ActionResultType.SUCCESS;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, LivingEntity entityLiving)
	{
		if(!(entityLiving instanceof ServerPlayerEntity))
			return stack;
		ServerPlayerEntity player = (ServerPlayerEntity)entityLiving;
		RayTraceResult rtr = rayTrace(world, player, FluidMode.NONE);
		if(!(rtr instanceof BlockRayTraceResult))
			return stack;
		BlockPos pos = ((BlockRayTraceResult)rtr).getPos();
		MineralVein vein = ExcavatorHandler.getRandomMineral(world, pos);
		if(vein==null)
		{
			player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_INFO+"survey.no_vein"), true);
			return stack;
		}

		ListNBT data = getVeinData(stack, world.getDimensionKey(), vein.getPos());
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
			int dX = ((CompoundNBT)inbt).getInt("x")-pos.getX();
			int dZ = ((CompoundNBT)inbt).getInt("z")-pos.getZ();
			return dX*dX+dZ*dZ < 16;
		});
		if(tooClose)
		{
			player.sendStatusMessage(new TranslationTextComponent(Lib.CHAT_INFO+"survey.too_close"), true);
			return stack;
		}

		ITextComponent response;
		// Get angle between postion->center vector and standard (south facing) vector
		Vector2f vecToCenter = new Vector2f(vein.getPos().x-pos.getX(), vein.getPos().z-pos.getZ());
		if(vecToCenter.x==0&&vecToCenter.y==0) // hit the vein center directly
			response = new TranslationTextComponent(Lib.CHAT_INFO+"survey.hint.center",
					new TranslationTextComponent(vein.getMineral().getTranslationKey()));
		else
		{
			double angle = Math.toDegrees(Math.atan2(vecToCenter.y, vecToCenter.x));
			int segment = (int)((angle+270)%360/45);

			switch(dataCount)
			{
				case 0: // hint at the type of vein
					response = new TranslationTextComponent(Lib.CHAT_INFO+"survey.hint.1",
							new TranslationTextComponent(vein.getMineral().getTranslationKey()));
					break;
				case 1: // hint at the direction
					response = new TranslationTextComponent(Lib.CHAT_INFO+"survey.hint.2",
							new TranslationTextComponent(vein.getMineral().getTranslationKey()),
							new TranslationTextComponent(Lib.CHAT_INFO+"survey.direction."+segment));
					break;
				case 2: // hint at distance
				default:
					response = new TranslationTextComponent(Lib.CHAT_INFO+"survey.hint.3",
							new TranslationTextComponent(vein.getMineral().getTranslationKey()),
							Math.round(Math.sqrt(vecToCenter.x*vecToCenter.x+vecToCenter.y*vecToCenter.y)),
							new TranslationTextComponent(Lib.CHAT_INFO+"survey.direction."+segment));
					break;
			}
		}
		// Send message to player
		player.sendStatusMessage(response, true);

		// Add entry for current position
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("x", pos.getX());
		tag.putInt("z", pos.getZ());
		// Lets save the hint, in case we ever want to give this a GUI
		tag.putString("hint", ITextComponent.Serializer.toJson(response));
		data.add(tag);

		world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.NEUTRAL, 1.0F, 1.0F+(world.rand.nextFloat()-world.rand.nextFloat())*0.4F);
		stack.damageItem(1, player, (user) -> {
			user.sendBreakAnimation(user.getActiveHand());
		});

		return stack;
	}

	private static final String DATA_KEY = "veinData";

	public static ListNBT getVeinData(ItemStack surveyTools, RegistryKey<World> dimension, ColumnPos veinPos)
	{
		ListNBT list = surveyTools.getOrCreateTag().getList(DATA_KEY, NBT.TAG_COMPOUND);
		CompoundNBT tag = null;
		String dimString = dimension.getLocation().toString();
		for(INBT nbt : list)
		{
			CompoundNBT tmp = (CompoundNBT)nbt;
			if(dimString.equals(tmp.getString("dimension"))&&tmp.getInt("x")==veinPos.x&&tmp.getInt("z")==veinPos.z)
			{
				tag = tmp;
				break;
			}
		}
		if(tag==null)
		{
			tag = new CompoundNBT();
			tag.putString("dimension", dimString);
			tag.putInt("x", veinPos.x);
			tag.putInt("z", veinPos.z);
			list.add(tag);
			surveyTools.getOrCreateTag().put(DATA_KEY, list);
		}
		if(tag.contains("data", NBT.TAG_LIST))
			return tag.getList("data", NBT.TAG_COMPOUND);
		ListNBT data = new ListNBT();
		tag.put("data", data);
		return data;
	}

	@Override

	public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player)
	{
		return true;
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack)
	{
		return false;
	}
}


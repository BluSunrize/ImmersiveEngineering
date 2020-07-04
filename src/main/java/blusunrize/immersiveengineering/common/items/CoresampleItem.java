/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler.MineralMix;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class CoresampleItem extends IEBaseItem
{
	public CoresampleItem()
	{
		super("coresample", new Properties().group(ImmersiveEngineering.itemGroup));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		DimensionChunkCoords coords = getCoords(stack);
		if(coords!=null)
		{
			MineralMix mineral = getMix(stack);
			if(mineral!=null)
			{
				String unloc = mineral.getTranslationKey();
				String loc = I18n.format(unloc);
				if(unloc.equals(loc))
					loc = mineral.getPlainName();
				list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.mineral", loc));
			}
			else
				list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.noMineral"));
			if(world==null||world.func_234923_W_()!=coords.dimension)
			{
				World clientWorld = Minecraft.getInstance().world;
				if(clientWorld!=null&&clientWorld.func_234923_W_()==coords.dimension)
					world = clientWorld;
				else
					world = null;
			}
			String s0 = (coords.x*16)+", "+(coords.z*16);
			String s1 = (coords.x*16+16)+", "+(coords.z*16+16);
			//TODO
			String s2 = coords.dimension.func_240901_a_().getPath();

			if(s2.toLowerCase(Locale.ENGLISH).startsWith("the_"))
				s2 = s2.substring(4);
			list.add(new StringTextComponent(Utils.toCamelCase(s2)));
			list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.pos", s0, s1, ""));

			if(ItemNBTHelper.hasKey(stack, "infinite"))
				list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.infinite"));
			else if(ItemNBTHelper.hasKey(stack, "depletion"))
				list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.yield", ExcavatorHandler.mineralVeinCapacity-ItemNBTHelper.getInt(stack, "depletion")));

			boolean hasStamp = ItemNBTHelper.hasKey(stack, "timestamp");
			if(hasStamp&&world!=null)
			{
				long timestamp = ItemNBTHelper.getLong(stack, "timestamp");
				long dist = world.getGameTime()-timestamp;
				if(dist < 0)
					list.add(new StringTextComponent("Somehow this sample is dated in the future...are you a time traveller?!"));
				else
					list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.timestamp", ClientUtils.fomatTimestamp(dist, ClientUtils.TimestampFormat.DHM)));
			}
			else if(hasStamp)
				list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.timezone"));
			else
				list.add(new TranslationTextComponent(Lib.CHAT_INFO+"coresample.noTimestamp"));
		}
	}


	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		PlayerEntity player = ctx.getPlayer();
		ItemStack stack = ctx.getItem();
		if(player!=null&&player.isSneaking())
		{
			World world = ctx.getWorld();
			BlockPos pos = ctx.getPos();
			Direction side = ctx.getFace();
			BlockState state = world.getBlockState(pos);
			BlockItemUseContext blockCtx = new BlockItemUseContext(ctx);
			if(!state.isReplaceable(blockCtx))
				pos = pos.offset(side);

			if(!stack.isEmpty()&&player.canPlayerEdit(pos, side, stack)&&world.getBlockState(pos).isReplaceable(blockCtx))
			{
				BlockState coresample = StoneDecoration.coresample.getDefaultState();
				if(world.setBlockState(pos, coresample, 3))
				{
					((IEBaseBlock)StoneDecoration.coresample).onIEBlockPlacedBy(blockCtx, coresample);
					SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
					stack.shrink(1);
				}
				return ActionResultType.SUCCESS;
			}
			else
				return ActionResultType.FAIL;
		}
		return super.onItemUse(ctx);
	}

	@Nullable
	public static MineralMix getMix(ItemStack coresample)
	{
		try
		{
			if(ItemNBTHelper.hasKey(coresample, "mineral", NBT.TAG_STRING))
			{
				ResourceLocation rl = new ResourceLocation(ItemNBTHelper.getString(coresample, "mineral"));
				return ExcavatorHandler.mineralList.get(rl);
			}
			else
				return null;
		} catch(ResourceLocationException x)
		{
			return null;
		}
	}

	@Nullable
	public static DimensionChunkCoords getCoords(@Nullable ItemStack stack)
	{
		if(stack!=null&&stack.hasTag())
			return DimensionChunkCoords.readFromNBT(stack.getOrCreateTag().getCompound("coords"));
		else
			return null;
	}

	public static void setCoords(ItemStack stack, DimensionChunkCoords coords)
	{
		stack.getOrCreateTag().put("coords", coords.writeToNBT());
	}
}
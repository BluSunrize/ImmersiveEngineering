/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.excavator.MineralWorldInfo;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.ClientUtils.TimestampFormat;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
		getCoresampleInfo(stack, list, TextFormatting.GRAY, world, true, true);
	}

	public static void getCoresampleInfo(ItemStack coresample, List<ITextComponent> list, TextFormatting baseColor, @Nullable World world, boolean showYield, boolean showTimestamp)
	{
		if(coresample.getOrCreateTag().contains("coords"))
			list.add(new TranslationTextComponent(Lib.DESC_INFO+"coresample.outdated"));

		ColumnPos coords = getCoords(coresample);
		if(coords!=null)
		{
			ListNBT nbtList = coresample.getOrCreateTag().getList("mineralInfo", NBT.TAG_COMPOUND);
			if(nbtList.size() > 0)
				nbtList.forEach(inbt -> {
					CompoundNBT tag = (CompoundNBT)inbt;
					MineralMix mineral = MineralMix.mineralList.get(new ResourceLocation(tag.getString("mineral")));
					if(mineral == null)
					{
						list.add(new StringTextComponent("INVALID MINERAL ID. NO MINERAL FOUND."));
						return;
					}

					IFormattableTextComponent component = new StringTextComponent(
							Utils.formatDouble(tag.getDouble("percentage")*100, "0.00")+"% "
					);
					component.appendSibling(new TranslationTextComponent(mineral.getTranslationKey()));
					list.add(component.mergeStyle(baseColor));
					if(showYield)
					{
						component = new StringTextComponent("  ");
						component.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"coresample.saturation",
								Utils.formatDouble(tag.getDouble("saturation")*100, "0.00")
						));
						list.add(component.mergeStyle(TextFormatting.DARK_GRAY));

						component = new StringTextComponent("  ");
						int yield = ExcavatorHandler.mineralVeinYield-tag.getInt("depletion");
						yield *= (1-mineral.failChance);
						if(ExcavatorHandler.mineralVeinYield==0)
							component.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"coresample.infinite"));
						else
							component.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"coresample.yield",
									yield));
						list.add(component.mergeStyle(TextFormatting.DARK_GRAY));
					}
				});
			else
				list.add(new TranslationTextComponent(Lib.DESC_INFO+"coresample.noMineral").mergeStyle(baseColor));

			RegistryKey<World> dimension = getDimension(coresample);
			if(dimension!=null)
			{
				String s2 = dimension.getLocation().getPath();
				if(s2.toLowerCase(Locale.ENGLISH).startsWith("the_"))
					s2 = s2.substring(4);
				list.add(new StringTextComponent(Utils.toCamelCase(s2)).mergeStyle(baseColor));
			}
			ColumnPos pos = getCoords(coresample);
			if(pos!=null)
				list.add(new TranslationTextComponent(Lib.DESC_INFO+"coresample.pos", pos.x, pos.z).mergeStyle(baseColor));

			if(showTimestamp)
			{
				boolean hasStamp = ItemNBTHelper.hasKey(coresample, "timestamp");
				if(hasStamp&&world!=null)
				{
					long timestamp = ItemNBTHelper.getLong(coresample, "timestamp");
					long dist = world.getGameTime()-timestamp;
					if(dist < 0)
						list.add(new StringTextComponent("Somehow this sample is dated in the future...are you a time traveller?!").mergeStyle(TextFormatting.RED));
					else
						list.add(new TranslationTextComponent(Lib.DESC_INFO+"coresample.timestamp", ClientUtils.fomatTimestamp(dist, TimestampFormat.DHM)).mergeStyle(baseColor));
				}
				else if(hasStamp)
					list.add(new TranslationTextComponent(Lib.DESC_INFO+"coresample.timezone").mergeStyle(baseColor));
				else
					list.add(new TranslationTextComponent(Lib.DESC_INFO+"coresample.noTimestamp").mergeStyle(baseColor));
			}
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

	public static MineralMix[] getMineralMixes(ItemStack coresample)
	{
		if(ItemNBTHelper.hasKey(coresample, "mineralInfo", NBT.TAG_LIST))
		{
			ListNBT nbtList = coresample.getOrCreateTag().getList("mineralInfo", NBT.TAG_COMPOUND);
			return nbtList.stream().map(inbt -> {
				CompoundNBT tag = (CompoundNBT)inbt;
				return MineralMix.mineralList.get(new ResourceLocation(tag.getString("mineral")));
			}).filter(Objects::nonNull).toArray(MineralMix[]::new);
		}
		return new MineralMix[0];
	}

	public static ListNBT getSimplifiedMineralList(ItemStack coresample)
	{
		ListNBT outList = new ListNBT();
		if(ItemNBTHelper.hasKey(coresample, "mineralInfo", NBT.TAG_LIST))
			coresample.getOrCreateTag().getList("mineralInfo", NBT.TAG_COMPOUND).
					forEach(inbt -> outList.add(StringNBT.valueOf(((CompoundNBT)inbt).getString("mineral"))));
		return outList;
	}

	public static void setMineralInfo(ItemStack stack, MineralWorldInfo info, BlockPos pos)
	{
		if(info==null)
			return;
		List<Pair<MineralVein, Integer>> veins = info.getAllVeins();
		ListNBT nbtList = new ListNBT();
		veins.forEach(pair -> {
			CompoundNBT tag = new CompoundNBT();
			tag.putDouble("percentage", pair.getRight()/(double)info.getTotalWeight());
			tag.putString("mineral", pair.getLeft().getMineral().getId().toString());
			tag.putInt("depletion", pair.getLeft().getDepletion());
			tag.putDouble("saturation", 1-pair.getLeft().getFailChance(pos));
			nbtList.add(tag);
		});
		stack.getOrCreateTag().put("mineralInfo", nbtList);
	}

	@Nullable
	public static ColumnPos getCoords(@Nullable ItemStack stack)
	{
		if(stack!=null&&stack.hasTag()&&stack.getOrCreateTag().contains("x"))
			return new ColumnPos(stack.getOrCreateTag().getInt("x"), stack.getOrCreateTag().getInt("z"));
		else
			return null;
	}

	public static void setCoords(ItemStack stack, BlockPos pos)
	{
		stack.getOrCreateTag().putInt("x", pos.getX());
		stack.getOrCreateTag().putInt("z", pos.getZ());
	}

	@Nullable
	public static RegistryKey<World> getDimension(ItemStack stack)
	{
		if(stack.hasTag()&&stack.getOrCreateTag().contains("dimension"))
		{
			ResourceLocation name = new ResourceLocation(stack.getOrCreateTag().getString("dimension"));
			return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, name);
		}
		return null;
	}


	public static void setDimension(ItemStack stack, RegistryKey<World> dimension)
	{
		stack.getOrCreateTag().putString("dimension", dimension.getLocation().toString());
	}
}

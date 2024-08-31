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
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import blusunrize.immersiveengineering.client.utils.TimestampFormat;
import blusunrize.immersiveengineering.common.register.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.Utils;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CoresampleItem extends IEBaseItem
{
	public CoresampleItem()
	{
		super(new Properties().component(IEDataComponents.CORESAMPLE, ItemData.EMPTY));
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		ItemData data = stack.getOrDefault(IEDataComponents.CORESAMPLE, ItemData.EMPTY);
		getCoresampleInfo(
				data, list, ChatFormatting.GRAY, ImmersiveEngineering.proxy.getClientWorld(), true, true
		);
	}

	public static void getCoresampleInfo(
			ItemData data, List<Component> list, ChatFormatting baseColor, @Nullable Level level, boolean showYield, boolean showTimestamp
	)
	{
		if(level==null)
			return;
		if(!data.veins.isEmpty())
			data.veins.forEach(veinData -> {
				MutableComponent component = Component.literal(
						Utils.formatDouble(veinData.percentageInTotalSample*100, "0.00")+"% "
				);
				component.append(Component.translatable(MineralMix.getTranslationKey(veinData.mineral)));
				list.add(component.withStyle(baseColor));
				if(showYield)
				{
					component = Component.literal("  ");
					component.append(Component.translatable(Lib.DESC_INFO+"coresample.saturation",
							Utils.formatDouble(veinData.saturation*100, "0.00")
					));
					list.add(component.withStyle(ChatFormatting.DARK_GRAY));

					component = Component.literal("  ");
					// Used to be called "yield", but apparently IntelliJ gets confused over whether we're in a
					// switch-case or nor
					int yield_ = ExcavatorHandler.mineralVeinYield-veinData.depletion;
					MineralMix mineral = MineralMix.RECIPES.getById(level, veinData.mineral);
					yield_ *= (1-mineral.failChance);
					if(ExcavatorHandler.mineralVeinYield==0)
						component.append(Component.translatable(Lib.DESC_INFO+"coresample.infinite"));
					else
						component.append(Component.translatable(Lib.DESC_INFO+"coresample.yield",
								yield_));
					list.add(component.withStyle(ChatFormatting.DARK_GRAY));
				}
			});
		else
			list.add(Component.translatable(Lib.DESC_INFO+"coresample.noMineral").withStyle(baseColor));

		ResourceKey<Level> dimension = data.position.dimension;
		if(dimension!=null)
		{
			String s2 = dimension.location().getPath();
			if(s2.toLowerCase(Locale.ENGLISH).startsWith("the_"))
				s2 = s2.substring(4);
			list.add(Component.literal(Utils.toCamelCase(s2)).withStyle(baseColor));
		}
		ColumnPos pos = data.position.position();
		if(pos!=null)
			list.add(Component.translatable(Lib.DESC_INFO+"coresample.pos", pos.x(), pos.z()).withStyle(baseColor));

		if(showTimestamp)
		{
			long dist = level.getGameTime()-data.timestamp;
			if(dist < 0)
				list.add(Component.literal("Somehow this sample is dated in the future...are you a time traveller?!").withStyle(ChatFormatting.RED));
			else
				list.add(Component.translatable(Lib.DESC_INFO+"coresample.timestamp", TimestampFormat.formatTimestamp(dist, TimestampFormat.DHM)).withStyle(baseColor));
		}
	}


	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Player player = ctx.getPlayer();
		ItemStack stack = ctx.getItemInHand();
		if(player!=null&&player.isShiftKeyDown())
		{
			Level world = ctx.getLevel();
			BlockPos pos = ctx.getClickedPos();
			Direction side = ctx.getClickedFace();
			BlockState state = world.getBlockState(pos);
			BlockPlaceContext blockCtx = new BlockPlaceContext(ctx);
			if(!state.canBeReplaced(blockCtx))
				pos = pos.relative(side);

			if(!stack.isEmpty()&&player.mayUseItemAt(pos, side, stack)&&world.getBlockState(pos).canBeReplaced(blockCtx))
			{
				BlockState coresample = StoneDecoration.CORESAMPLE.defaultBlockState();
				if(world.setBlock(pos, coresample, 3))
				{
					StoneDecoration.CORESAMPLE.get().onIEBlockPlacedBy(blockCtx, coresample);
					SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
					stack.shrink(1);
				}
				return InteractionResult.SUCCESS;
			}
			else
				return InteractionResult.FAIL;
		}
		return super.useOn(ctx);
	}

	public static List<RecipeHolder<MineralMix>> getMineralMixes(Level level, ItemStack coresample)
	{
		return coresample.get(IEDataComponents.CORESAMPLE).veins()
				.stream()
				.map(VeinSample::mineral)
				.map(rl -> MineralMix.RECIPES.holderById(level, rl))
				.toList();
	}

	public record ItemData(SamplePosition position, List<VeinSample> veins, long timestamp)
	{
		public static final DualCodec<ByteBuf, ItemData> CODECS = DualCompositeCodecs.composite(
				SamplePosition.CODECS.fieldOf("position"), ItemData::position,
				VeinSample.CODECS.listOf().fieldOf("veins"), ItemData::veins,
				DualCodecs.LONG.fieldOf("timestamp"), ItemData::timestamp,
				ItemData::new
		);
		public static final ItemData EMPTY = new ItemData(SamplePosition.NONE, List.of(), 0);
	}

	public record VeinSample(
			ResourceLocation mineral,
			int depletion,
			double saturation,
			double percentageInTotalSample
	)
	{
		public static final DualCodec<ByteBuf, VeinSample> CODECS = DualCompositeCodecs.composite(
				DualCodecs.RESOURCE_LOCATION.fieldOf("mineral"), VeinSample::mineral,
				DualCodecs.INT.fieldOf("depletion"), VeinSample::depletion,
				DualCodecs.DOUBLE.fieldOf("saturation"), VeinSample::saturation,
				DualCodecs.DOUBLE.fieldOf("percentage"), VeinSample::percentageInTotalSample,
				VeinSample::new
		);
	}

	public record SamplePosition(ResourceKey<Level> dimension, int x, int z)
	{
		public static final DualCodec<ByteBuf, SamplePosition> CODECS = DualCompositeCodecs.composite(
				DualCodecs.resourceKey(Registries.DIMENSION).fieldOf("dimension"), SamplePosition::dimension,
				DualCodecs.INT.fieldOf("x"), SamplePosition::x,
				DualCodecs.INT.fieldOf("z"), SamplePosition::z,
				SamplePosition::new
		);
		public static final SamplePosition NONE = new SamplePosition(
				ResourceKey.create(Registries.DIMENSION, ResourceLocation.withDefaultNamespace("overworld")), 0, 0
		);

		public ColumnPos position()
		{
			return new ColumnPos(x, z);
		}
	}

	public record CoresampleMapData(Map<String, List<ResourceLocation>> mapDataToMinerals)
	{
		public static final DualCodec<ByteBuf, CoresampleMapData> CODECS = IEDualCodecs.forMap(DualCodecs.STRING, DualCodecs.RESOURCE_LOCATION.listOf())
				.fieldOf("mapDataToMinerals")
				.codec()
				.map(CoresampleMapData::new, CoresampleMapData::mapDataToMinerals);
		public static final CoresampleMapData EMPTY = new CoresampleMapData(Map.of());

		public CoresampleMapData
		{
			mapDataToMinerals = Map.copyOf(mapDataToMinerals);
		}

		public CoresampleMapData remove(String key)
		{
			var newMap = new HashMap<>(mapDataToMinerals);
			newMap.remove(key);
			return new CoresampleMapData(newMap);
		}

		public CoresampleMapData with(String key, List<ResourceLocation> minerals)
		{
			var newMap = new HashMap<>(mapDataToMinerals);
			newMap.put(key, minerals);
			return new CoresampleMapData(newMap);
		}
	}
}

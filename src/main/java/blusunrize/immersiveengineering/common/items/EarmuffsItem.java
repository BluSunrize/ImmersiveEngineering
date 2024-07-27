/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.ItemGetterList;
import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static blusunrize.immersiveengineering.client.utils.FontUtils.withAppendColoredColour;

public class EarmuffsItem extends IEBaseItem implements IConfigurableTool, IColouredItem
{
	/**
	 * The minimum allowed volume multiplier, i.e. the strongest attenuation. Note that this has to be strictly
	 * positive: otherwise the vanilla sound system will not start any sounds while earmuffs are worn, so long-playing
	 * sounds will not return after the earmuffs are removed.
	 */
	public static final float MIN_MULTIPLIER = 0.05f;
	public static final float MAX_REDUCTION = 1-MIN_MULTIPLIER;

	public static ItemGetterList EARMUFF_GETTERS = new ItemGetterList(
			entity -> {
				ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
				if(head.getItem() instanceof EarmuffsItem)
					return head;
				final var contained = head.get(IEDataComponents.CONTAINED_EARMUFF);
				if(contained!=null)
					return contained.attached();
				else
					return ItemStack.EMPTY;
			}
	);

	public EarmuffsItem()
	{
		super(new Properties()
				.stacksTo(1)
				.component(IEDataComponents.EARMUFF_DATA, EarmuffData.DEFAULT)
				.component(IEDataComponents.COLOR, new Color4(0xff486c94))
		);
	}

	@Nullable
	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack)
	{
		return EquipmentSlot.HEAD;
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int renderPass)
	{
		if(renderPass==1)
			return 0xffffff;
		else
			return getColor(stack);
	}

	//TODO
	// @Override
	public boolean hasCustomColor(ItemStack stack)
	{
		return true;
	}

	//TODO
	// @Override
	public int getColor(ItemStack stack)
	{
		return stack.get(IEDataComponents.COLOR).toInt();
	}

	// TODO
	//@Override
	//public void clearColor(ItemStack stack)
	//{
	//	if(ItemNBTHelper.hasKey(stack, Lib.NBT_EarmuffColour))
	//		ItemNBTHelper.remove(stack, Lib.NBT_EarmuffColour);
	//}

	//@Override
	//public void setColor(ItemStack stack, int color)
	//{
	//	ItemNBTHelper.putInt(stack, Lib.NBT_EarmuffColour, color);
	//}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		int color = this.getColourForIEItem(stack, 0);
		MutableComponent mainComponent = Component.translatable(Lib.DESC_INFO+"colour").withStyle(ChatFormatting.GRAY);
		list.add(withAppendColoredColour(mainComponent, color));
	}

	public static LinkedHashSet<String> affectedSoundCategories = Sets.newLinkedHashSet();

	static
	{
		affectedSoundCategories.add("ambient");
		affectedSoundCategories.add("weather");
		affectedSoundCategories.add("record");
		affectedSoundCategories.add("block");
		affectedSoundCategories.add("neutral");
		affectedSoundCategories.add("hostile");
		affectedSoundCategories.add("player");
	}

	public static float getVolumeMod(ItemStack stack)
	{
		return stack.getOrDefault(IEDataComponents.EARMUFF_DATA, EarmuffData.DEFAULT).volumeMod;
	}

	@Override
	public boolean canConfigure(ItemStack stack)
	{
		return true;
	}

	@Override
	public ToolConfigBoolean[] getBooleanOptions(ItemStack stack)
	{
		ToolConfigBoolean[] array = new ToolConfigBoolean[affectedSoundCategories.size()];
		int i = -1;
		for(String cat : affectedSoundCategories)
			array[++i] = new ToolConfigBoolean(cat, 60+i/4*55, 32+10*(i%4), isMuted(stack, cat));
		return array;
	}

	@Override
	public ToolConfigFloat[] getFloatOptions(ItemStack stack)
	{
		return new ToolConfigFloat[]{
				new ToolConfigFloat("reductionValue", 60, 20, MAX_REDUCTION-getVolumeMod(stack), 0, MAX_REDUCTION)
		};
	}

	@Override
	public String fomatConfigName(ItemStack stack, ToolConfig config)
	{
		if(config instanceof ToolConfigFloat)
			return I18n.get(Lib.GUI_CONFIG+"earmuffs.noisegate");
		return I18n.get(Lib.GUI_CONFIG+"earmuffs.soundcategory."+config.name);
	}

	@Override
	public String fomatConfigDescription(ItemStack stack, ToolConfig config)
	{
		return null;
	}

	@Override
	public void applyConfigOption(ItemStack stack, String key, Object value)
	{
		if(value instanceof Boolean bool)
		{
			var oldData = stack.getOrDefault(IEDataComponents.EARMUFF_DATA, EarmuffData.DEFAULT);
			Set<String> newCategories = new HashSet<>(oldData.affectedCategories);
			if(bool)
				newCategories.add(key);
			else
				newCategories.remove(key);
			stack.set(IEDataComponents.EARMUFF_DATA, new EarmuffData(oldData.volumeMod, newCategories));
		}
		else if(value instanceof Float flt)
		{
			var oldData = stack.getOrDefault(IEDataComponents.EARMUFF_DATA, EarmuffData.DEFAULT);
			stack.set(IEDataComponents.EARMUFF_DATA, new EarmuffData(flt, oldData.affectedCategories));
		}
	}

	public boolean isMuted(ItemStack stack, String category)
	{
		return stack.getOrDefault(IEDataComponents.EARMUFF_DATA, EarmuffData.DEFAULT)
				.affectedCategories
				.contains(category);
	}

	public record EarmuffData(float volumeMod, Set<String> affectedCategories)
	{
		public static final DualCodec<ByteBuf, EarmuffData> CODECS = DualCodecs.composite(
				DualCodecs.FLOAT.fieldOf("volumeMod"), EarmuffData::volumeMod,
				DualCodecs.STRING.setOf().fieldOf("affectedCategories"), EarmuffData::affectedCategories,
				EarmuffData::new
		);
		public static final EarmuffData DEFAULT = new EarmuffData(
				0.1f, affectedSoundCategories
		);

		public EarmuffData
		{
			affectedCategories = Set.copyOf(affectedCategories);
		}
	}

}
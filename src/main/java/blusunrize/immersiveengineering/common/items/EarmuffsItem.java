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
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.ItemGetterList;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.Sets;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;

import static blusunrize.immersiveengineering.client.utils.FontUtils.withAppendColoredColour;

public class EarmuffsItem extends IEBaseItem implements DyeableLeatherItem, IConfigurableTool, IColouredItem
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
				else if(ItemNBTHelper.hasKey(head, Lib.NBT_Earmuffs))
					return ItemNBTHelper.getItemStack(head, Lib.NBT_Earmuffs);
				else
					return ItemStack.EMPTY;
			}
	);

	public EarmuffsItem()
	{
		super(new Properties().stacksTo(1));
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
		if(!ItemNBTHelper.hasKey(stack, Lib.NBT_EarmuffColour))
			return 0x486c94;
		return ItemNBTHelper.getInt(stack, Lib.NBT_EarmuffColour);
	}

	@Override
	public boolean hasCustomColor(ItemStack stack)
	{
		return true;
	}

	@Override
	public int getColor(ItemStack stack)
	{
		if(!ItemNBTHelper.hasKey(stack, Lib.NBT_EarmuffColour))
			return 0x486c94;
		return ItemNBTHelper.getInt(stack, Lib.NBT_EarmuffColour);
	}

	@Override
	public void clearColor(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, Lib.NBT_EarmuffColour))
			ItemNBTHelper.remove(stack, Lib.NBT_EarmuffColour);
	}

	@Override
	public void setColor(ItemStack stack, int color)
	{
		ItemNBTHelper.putInt(stack, Lib.NBT_EarmuffColour, color);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		int color = this.getColourForIEItem(stack, 0);
		MutableComponent mainComponent = Component.translatable(Lib.DESC_INFO+"colour");
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
		if(!ItemNBTHelper.hasKey(stack, "IE:Earmuffs:Volume"))
			return .1f;
		return ItemNBTHelper.getFloat(stack, "IE:Earmuffs:Volume");
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
			array[++i] = new ToolConfigBoolean(cat, 60+i/4*55, 32+10*(i%4), !ItemNBTHelper.getBoolean(stack, "IE:Earmuffs:Cat_"+cat));
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
			ItemNBTHelper.putBoolean(stack, "IE:Earmuffs:Cat_"+key, !bool);
		else if(value instanceof Float flt)
			ItemNBTHelper.putFloat(stack, "IE:Earmuffs:Volume", MAX_REDUCTION-flt);
	}
}
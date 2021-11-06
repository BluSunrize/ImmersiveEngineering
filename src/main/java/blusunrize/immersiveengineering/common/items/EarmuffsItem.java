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
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.models.ModelEarmuffs;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.ItemGetterList;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.Sets;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;

import static blusunrize.immersiveengineering.client.utils.FontUtils.withAppendColoredColour;

public class EarmuffsItem extends IEBaseItem implements DyeableLeatherItem, IConfigurableTool, ITool, IColouredItem
{
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
		super("earmuffs", new Properties().stacksTo(1));
	}

	@Override
	public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
	{
		return armorType==EquipmentSlot.HEAD;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
	{
		if("overlay".equals(type))
			return "immersiveengineering:textures/models/earmuffs_overlay.png";
		return "immersiveengineering:textures/models/earmuffs.png";
	}

	@OnlyIn(Dist.CLIENT)
	HumanoidModel armorModel;

	@Override
	@OnlyIn(Dist.CLIENT)
	public HumanoidModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel _default)
	{
		return ModelEarmuffs.getModel();
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		int color = this.getColourForIEItem(stack, 0);
		MutableComponent mainComponent = new TranslatableComponent(Lib.DESC_INFO+"colour");
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
		return new ToolConfigFloat[]{new ToolConfigFloat("reductionValue", 60, 20, 1-getVolumeMod(stack))};
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
		if(value instanceof Boolean)
			ItemNBTHelper.putBoolean(stack, "IE:Earmuffs:Cat_"+key, !(Boolean)value);
		else if(value instanceof Float)
			ItemNBTHelper.putFloat(stack, "IE:Earmuffs:Volume", 1-(Float)value);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}
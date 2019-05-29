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
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.models.ModelEarmuffs;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemArmorDyeable;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.LinkedHashSet;
import java.util.List;

public class ItemEarmuffs extends ItemArmorDyeable implements IConfigurableTool, ITool, IColouredItem
{
	public ItemEarmuffs()
	{
		super(ArmorMaterial.LEATHER, EntityEquipmentSlot.HEAD, new Properties().group(ImmersiveEngineering.itemGroup)
		.maxStackSize(1));
		IEContent.registeredIEItems.add(this);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type)
	{
		if("overlay".equals(type))
			return "immersiveengineering:textures/models/earmuffs_overlay.png";
		return "immersiveengineering:textures/models/earmuffs.png";
	}

	@OnlyIn(Dist.CLIENT)
	ModelBiped armorModel;

	@Override
	@OnlyIn(Dist.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default)
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
	public boolean hasColor(ItemStack stack)
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
	public void removeColor(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, Lib.NBT_EarmuffColour))
			ItemNBTHelper.remove(stack, Lib.NBT_EarmuffColour);
	}

	@Override
	public void setColor(ItemStack stack, int color)
	{
		ItemNBTHelper.setInt(stack, Lib.NBT_EarmuffColour, color);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		String hexCol = Integer.toHexString(this.getColourForIEItem(stack, 0));
		list.add(new TextComponentTranslation(Lib.DESC_INFO+"colour", "<hexcol="+hexCol+":#"+hexCol+">"));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@Override
	public IArmorMaterial getArmorMaterial()
	{
		return super.getArmorMaterial();//TODO
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
			return I18n.format(Lib.GUI_CONFIG+"earmuffs.noisegate");
		return I18n.format(Lib.GUI_CONFIG+"earmuffs.soundcategory."+config.name);
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
			ItemNBTHelper.setBoolean(stack, "IE:Earmuffs:Cat_"+key, !(Boolean)value);
		else if(value instanceof Float)
			ItemNBTHelper.setFloat(stack, "IE:Earmuffs:Volume", 1-(Float)value);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}
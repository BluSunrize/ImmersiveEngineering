package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.models.ModelSwingingArmor;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class LayerSwingingArmor extends LayerBipedArmor
{
	public LayerSwingingArmor(LayerBipedArmor layer)
	{
		super(layer.renderer);
	}

	@Nonnull
	@Override
	protected ModelBiped getArmorModelHook(@Nonnull EntityLivingBase entity, ItemStack itemStack, @Nonnull EntityEquipmentSlot slot, @Nonnull ModelBiped model)
	{
		ModelBiped newModel = super.getArmorModelHook(entity, itemStack, slot, model);
		if (newModel.getClass()==ModelBiped.class)
			return new ModelSwingingArmor(newModel);
		else
			return newModel;
	}
}

/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.compat.CuriosCompatModule;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IEBipedLayerRenderer<E extends LivingEntity, M extends HumanoidModel<E>> extends RenderLayer<E, M>
{
	public static boolean rendersAssigned = false;
	public static Map<UUID, Pair<ItemStack, Integer>> POWERPACK_PLAYERS = new HashMap<>();

	public IEBipedLayerRenderer(RenderLayerParent<E, M> entityRendererIn)
	{
		super(entityRendererIn);
	}

	@Override
	@ParametersAreNonnullByDefault
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, E living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch)
	{
		ItemStack head = living.getItemBySlot(EquipmentSlot.HEAD);
		ItemStack earmuffs = ItemStack.EMPTY;
		if(!head.isEmpty()&&(head.getItem()==Misc.earmuffs.get()||ItemNBTHelper.hasKey(head, Lib.NBT_Earmuffs)))
			earmuffs = head.getItem()==Misc.earmuffs.get()?head: ItemNBTHelper.getItemStack(head, Lib.NBT_Earmuffs);
		else if(ModList.get().isLoaded("curios"))
			earmuffs = CuriosCompatModule.getEarmuffs(living);

		if(!earmuffs.isEmpty())
		{
			HumanoidModel<E> model = Misc.earmuffs.get().getArmorModel(living, earmuffs, EquipmentSlot.HEAD, null);
			model.setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			RenderType type = model.renderType(new ResourceLocation(Misc.earmuffs.get().getArmorTexture(earmuffs, living, EquipmentSlot.HEAD, "overlay")));
			model.renderToBuffer(matrixStackIn, bufferIn.getBuffer(type), packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
			int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
			type = model.renderType(new ResourceLocation(Misc.earmuffs.get().getArmorTexture(earmuffs, living, EquipmentSlot.HEAD, null)));
			model.renderToBuffer(matrixStackIn, bufferIn.getBuffer(type), packedLightIn, OverlayTexture.NO_OVERLAY,
					(colour >> 16&255)/255f, (colour >> 8&255)/255f, (colour&255)/255f, 1F);
		}

		ItemStack chest = living.getItemBySlot(EquipmentSlot.CHEST);
		if(!chest.isEmpty()&&(chest.getItem()==Misc.powerpack.asItem()||ItemNBTHelper.hasKey(chest, Lib.NBT_Powerpack)))
		{
			ItemStack powerpack = chest.getItem()==Misc.powerpack.asItem()?chest: ItemNBTHelper.getItemStack(chest, Lib.NBT_Powerpack);
			addWornPowerpack(living, powerpack);
		}
		else if(ModList.get().isLoaded("curios"))
		{
			ItemStack powerpack = CuriosCompatModule.getPowerpack(living);
			if(!powerpack.isEmpty())
				addWornPowerpack(living, powerpack);
		}

		if(POWERPACK_PLAYERS.containsKey(living.getUUID()))
		{
			Pair<ItemStack, Integer> entry = POWERPACK_PLAYERS.get(living.getUUID());
			renderPowerpack(entry.getLeft(), matrixStackIn, bufferIn, packedLightIn, living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			int time = entry.getValue()-1;
			if(time <= 0)
				POWERPACK_PLAYERS.remove(living.getUUID());
			else
				POWERPACK_PLAYERS.put(living.getUUID(), Pair.of(entry.getLeft(), time));
		}
	}

	public static void addWornPowerpack(LivingEntity living, ItemStack powerpack)
	{
		POWERPACK_PLAYERS.put(living.getUUID(), Pair.of(powerpack, 5));
	}

	private void renderPowerpack(ItemStack powerpack, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, E living, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		if(!powerpack.isEmpty())
		{
			HumanoidModel<E> model = Misc.powerpack.asItem().getArmorModel(living, powerpack, EquipmentSlot.CHEST, null);
			model.setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			RenderType type = model.renderType(
					new ResourceLocation(Misc.powerpack.asItem().getArmorTexture(powerpack, living, EquipmentSlot.CHEST, null))
			);
			model.renderToBuffer(matrixStackIn, bufferIn.getBuffer(type), packedLightIn, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		}
	}
}